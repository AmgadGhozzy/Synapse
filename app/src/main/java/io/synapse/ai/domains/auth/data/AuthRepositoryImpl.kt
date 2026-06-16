package io.synapse.ai.domains.auth.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.Lazy
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.synapse.ai.BuildConfig
import io.synapse.ai.domains.auth.model.UserState
import io.synapse.ai.domains.auth.repository.IAuthRepository
import io.synapse.ai.domains.premium.data.PremiumManager
import io.synapse.ai.domains.study.data.sync.SyncMediator
import io.synapse.ai.domains.study.data.sync.SyncUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val premiumManager: Lazy<PremiumManager>,
    private val syncMediator: Lazy<SyncMediator>,
    private val syncUploader: Lazy<SyncUploader>,
) : IAuthRepository {

    private val _userState = MutableStateFlow(UserState())
    override val userState: StateFlow<UserState> = _userState.asStateFlow()

    // ── Bootstrap ──────────────────────────────────────────────────

    override suspend fun ensureSignedIn(): Result<Unit> = runCatching {
        supabase.auth.awaitInitialization()

        if (supabase.auth.currentSessionOrNull() != null) {
            refreshUserState()
            syncUploader.get().pushDirtyChanges()
            return@runCatching
        }

        supabase.auth.signInAnonymously()
        refreshUserState()
    }

    // ── Google sign-in / link ──────────────────────────────────────

    /**
     * Link Google account.
     * PATH A: anon → Google (same UUID): upload local data, then pull.
     * PATH B: Google belongs to different UUID: delete orphan, pull server state.
     */
    override suspend fun linkGoogle(activityContext: Context): Result<Unit> = runCatching {
        val credential           = requestGoogleCredential(activityContext)
        val isCurrentlyAnonymous = !isAuthenticated()

        // Track which path was taken so we call the right sync function below.
        var tookPathB = false

        if (isCurrentlyAnonymous) {
            val linked = tryLinkIdentityWithIdToken(credential.idToken)
            if (linked) {
                // PATH A: same UUID, promoted in-place.
                // linkIdentityWithIdToken does NOT auto-populate raw_user_meta_data.
                pushGoogleMetadata(credential)
            } else {
                // PATH B: identity belongs to a different UUID.
                // Refresh session before deleting so the JWT is current.
                runCatching { supabase.auth.refreshCurrentSession() }
                    .onFailure { Log.w(TAG, "Session refresh before orphan delete failed", it) }

                runCatching { deleteServerAccount() }
                    .onFailure { Log.e(TAG, "Failed to delete orphaned anonymous account", it) }

                signInWithGoogleToken(credential.idToken)
                tookPathB = true
            }
        } else {
            signInWithGoogleToken(credential.idToken)
        }

        refreshUserState()

        val userId = currentUserId()

        when {
            // PATH B or already-authenticated sign-in: just pull server state.
            // The anon account (and its Supabase data) was deleted — nothing to migrate.
            tookPathB || !isCurrentlyAnonymous -> {
                Log.d(TAG, "linkGoogle PATH B or re-auth: pulling server state for $userId")
                syncMediator.get().pullFromServer(userId).getOrThrow()
            }

            // PATH A: anonymous → Google link (same UUID).
            // Upload local progress first, then pull the canonical server state.
            else -> {
                Log.d(TAG, "linkGoogle PATH A: migrating anonymous data for $userId")
                syncMediator.get().migrateAnonymousData(userId).getOrThrow()
            }
        }

        premiumManager.get().verifyWithServer(force = true)
    }

    private suspend fun tryLinkIdentityWithIdToken(idToken: String): Boolean {
        return try {
            supabase.auth.linkIdentityWithIdToken(provider = Google, idToken = idToken)
            true
        } catch (e: AuthRestException) {
            if (e.statusCode == 422 || e.message?.contains("already", ignoreCase = true) == true) {
                false
            } else {
                throw e
            }
        }
    }

    private suspend fun pushGoogleMetadata(credential: GoogleIdTokenCredential) {
        val name   = credential.displayName
        val avatar = credential.profilePictureUri?.toString()
        if (name == null && avatar == null) return
        runCatching {
            supabase.auth.updateUser {
                data = buildJsonObject {
                    name?.let   { put("full_name",  it) }
                    avatar?.let { put("avatar_url", it) }
                }
            }
        }
    }

    private suspend fun signInWithGoogleToken(idToken: String) {
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider     = Google
        }
    }

    // ── Sign-out ───────────────────────────────────────────────────

    override suspend fun signOut() {
        val isAnonymous = !isAuthenticated()

        if (isAnonymous) {
            runCatching { deleteServerAccount() }
                .onFailure { Log.w(TAG, "Failed to delete orphan anon account on sign-out", it) }
        }

        premiumManager.get().clearOnSignOut()
        syncMediator.get().clearLocalData()

        val scope = if (isAnonymous) SignOutScope.LOCAL else SignOutScope.GLOBAL
        runCatching { supabase.auth.signOut(scope) }

        supabase.auth.signInAnonymously()
        refreshUserState()
    }

    // ── Account deletion ───────────────────────────────────────────

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        supabase.auth.currentUserOrNull()?.id
            ?: error("No active session — cannot delete account")

        deleteServerAccount()

        premiumManager.get().clearOnSignOut()
        syncMediator.get().clearLocalData()

        supabase.auth.signInAnonymously()
        refreshUserState()
    }

    private suspend fun deleteServerAccount() {
        val response = supabase.functions.invoke("delete-account")
        if (response.status.value !in 200..299) {
            val body = runCatching {
                response.body<ByteArray>().toString(Charsets.UTF_8)
            }.getOrDefault("Unknown error")
            throw IllegalStateException("Failed to delete account (HTTP ${response.status.value}): $body")
        }
    }

    // ── Accessors ──────────────────────────────────────────────────

    override fun currentUserId(): String =
        supabase.auth.currentUserOrNull()?.id ?: error("Call ensureSignedIn() first")

    override fun isAuthenticated(): Boolean {
        val user = supabase.auth.currentUserOrNull() ?: return false
        return user.appMetadata?.get("providers")
            ?.jsonArray?.any { it.jsonPrimitive.contentOrNull == "google" } == true
    }

    override fun userType(): String = if (isAuthenticated()) "registered" else "anonymous"

    // ── Credential helpers ─────────────────────────────────────────

    private suspend fun requestGoogleCredential(activityContext: Context): GoogleIdTokenCredential {
        val manager = CredentialManager.create(activityContext)
        try {
            return fetchCredential(manager, activityContext, silent = true)
        } catch (_: NoCredentialException) {}
        catch (_: GetCredentialCancellationException) {}
        return fetchCredential(manager, activityContext, silent = false)
    }

    private suspend fun fetchCredential(
        manager        : CredentialManager,
        activityContext: Context,
        silent         : Boolean,
    ): GoogleIdTokenCredential {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(silent)
            .setAutoSelectEnabled(silent)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result = manager.getCredential(activityContext, request)
        return GoogleIdTokenCredential.createFrom(result.credential.data)
    }

    // ── State refresh ──────────────────────────────────────────────

    private suspend fun refreshUserState() {
        val user = runCatching {
            supabase.auth.retrieveUserForCurrentSession(updateSession = true)
        }.getOrNull() ?: supabase.auth.currentUserOrNull()

        val meta      = user?.userMetadata
        val hasGoogle = user?.appMetadata
            ?.get("providers")?.jsonArray?.any {
                it.jsonPrimitive.contentOrNull == "google"
            } == true

        _userState.value = UserState(
            userId      = user?.id,
            isAnonymous = !hasGoogle,
            displayName = meta?.get("full_name")?.jsonPrimitive?.contentOrNull,
            email       = user?.email,
            avatarUrl   = meta?.get("avatar_url")?.jsonPrimitive?.contentOrNull,
        )
    }

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }
}