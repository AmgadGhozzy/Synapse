package com.venom.synapse.data.repo

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.venom.synapse.BuildConfig
import com.venom.synapse.domain.model.UserState
import com.venom.synapse.domain.repo.IAuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : IAuthRepository {

    private val _userState = MutableStateFlow(UserState())
    override val userState: StateFlow<UserState> = _userState.asStateFlow()

    // ── Bootstrap ─────────────────────────────────────────────────

    override suspend fun ensureSignedIn(): Result<Unit> = runCatching {
        supabase.auth.awaitInitialization()

        if (supabase.auth.currentSessionOrNull() != null) {
            refreshUserState()
            return@runCatching
        }

        supabase.auth.signInAnonymously()
        refreshUserState()
    }

    // ── Google sign-in / link ─────────────────────────────────────

    /**
     * Connects a Google account to the current session.
     *
     * PATH A — Anonymous session (normal first-time case)
     * ─────────────────────────────────────────────────────────────
     * Calls [linkIdentityWithIdToken] to promote the anonymous row in-place
     * (same UUID, `is_anonymous` flips to `false`).
     *
     * IMPORTANT — metadata gap:
     * Unlike [signInWith], `linkIdentityWithIdToken` does NOT auto-populate
     * `raw_user_meta_data` with the Google profile (full_name, avatar_url).
     * We fix this by calling [pushGoogleMetadata] immediately after a
     * successful link. That triggers `on_auth_user_updated` →
     * `handle_user_updated()` which writes the values into `profiles`.
     *
     * Edge case — 422 (Google identity already belongs to another user):
     * Falls through to PATH B.
     *
     * PATH B — Already a full user, OR link returned 422
     * ─────────────────────────────────────────────────────────────
     * Standard [signInWith(IDToken)] — metadata is populated automatically.
     */
    override suspend fun linkGoogle(activityContext: Context): Result<Unit> = runCatching {
        val credential           = requestGoogleCredential(activityContext)
        val isCurrentlyAnonymous = supabase.auth.currentUserOrNull()?.isAnonymous == true

        if (isCurrentlyAnonymous) {
            val linked = tryLinkIdentityWithIdToken(credential.idToken)
            if (linked) {
                // linkIdentityWithIdToken does NOT auto-populate raw_user_meta_data.
                // Push name + avatar explicitly so handle_user_updated can save them.
                pushGoogleMetadata(credential)
            } else {
                // 422 — Google identity already owned by a different UUID
                signInWithGoogleToken(credential.idToken)
            }
        } else {
            if (!isAuthenticated()) {
                signInWithGoogleToken(credential.idToken)
            }
        }

        refreshUserState()
    }

    /**
     * Links a Google ID token to the current anonymous session using
     * [Auth.linkIdentityWithIdToken] — the correct API for ID-token linking.
     *
     * @return `true`  — link succeeded; anonymous user promoted in-place.
     *         `false` — HTTP 422: identity already belongs to another user.
     */
    private suspend fun tryLinkIdentityWithIdToken(idToken: String): Boolean {
        return try {
            supabase.auth.linkIdentityWithIdToken(
                provider = Google,
                idToken  = idToken,
            )
            true
        } catch (e: AuthRestException) {
            if (e.statusCode == 422 || e.message?.contains("already", ignoreCase = true) == true) {
                false
            } else {
                throw e
            }
        }
    }

    /**
     * Pushes Google profile data (full_name, avatar_url) into
     * `auth.users.raw_user_meta_data` via [Auth.updateUser].
     *
     * WHY this is needed:
     * When signing in normally with [signInWith(IDToken)], Supabase populates
     * `raw_user_meta_data` automatically from the Google token.
     * When using [linkIdentityWithIdToken], it does NOT — the metadata stays
     * null.  This call fills that gap, and the `handle_user_updated` DB trigger
     * then copies the values into `profiles.display_name` / `avatar_url`.
     *
     * Wrapped in [runCatching] so a metadata save failure doesn't block sign-in.
     */
    private suspend fun pushGoogleMetadata(credential: GoogleIdTokenCredential) {
        val name   = credential.displayName
        val avatar = credential.profilePictureUri?.toString()

        // Nothing to push — Google didn't return either field
        if (name == null && avatar == null) return

        runCatching {
            supabase.auth.updateUser {
                data = buildJsonObject {
                    name?.let   { put("full_name",   it) }
                    avatar?.let { put("avatar_url",  it) }
                }
            }
        }
        // Failure is non-fatal — user is linked; metadata can be fetched next session
    }

    private suspend fun signInWithGoogleToken(idToken: String) {
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider     = Google
        }
    }

    // ── Sign-out ──────────────────────────────────────────────────

    override suspend fun signOut() {
        supabase.auth.signOut()
        supabase.auth.signInAnonymously()
        refreshUserState()
    }

    // ── Account deletion ──────────────────────────────────────────

    /**
     * Permanently deletes the current Supabase user, then bootstraps a fresh
     * anonymous session so the app remains in a valid state.
     *
     * What happens server-side:
     *   • [Auth.admin.deleteUser] hard-deletes the `auth.users` row.
     *   • The `profiles` table has `ON DELETE CASCADE`, so the profile row
     *     is removed automatically.
     *   • All packs / cards owned by this user are removed via their own
     *     `ON DELETE CASCADE` FK constraints.
     *
     * If the deletion fails (e.g. network error) the existing session is
     * preserved and the error is propagated to the caller as a [Result.failure].
     */
    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("No active session — cannot delete account")

        // Hard-delete via Admin API (requires service_role key on the server,
        // or call your own Edge Function that performs the deletion server-side).
        supabase.auth.admin.deleteUser(userId)

        // Bootstrap a fresh anonymous session so the app stays usable
        supabase.auth.signInAnonymously()
        refreshUserState()
    }

    // ── Accessors ─────────────────────────────────────────────────

    override fun currentUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: error("Call ensureSignedIn() first")

    override fun isAuthenticated(): Boolean {
        val user = supabase.auth.currentUserOrNull() ?: return false
        return user.appMetadata?.get("providers")
            ?.toString()?.contains("google") == true
    }

    override fun userType(): String = when {
        _userState.value.isPremium -> "premium"
        isAuthenticated()          -> "registered"
        else                       -> "anonymous"
    }

    // ── Credential helpers ────────────────────────────────────────

    /**
     * Returns the full [GoogleIdTokenCredential] — not just the raw token —
     * so callers can access [GoogleIdTokenCredential.displayName] and
     * [GoogleIdTokenCredential.profilePictureUri] for metadata saving.
     */
    private suspend fun requestGoogleCredential(activityContext: Context): GoogleIdTokenCredential {
        val credentialManager = CredentialManager.create(activityContext)

        try {
            return fetchCredential(credentialManager, activityContext, silent = true)
        } catch (_: NoCredentialException) { /* fall through to picker */ }
        catch (_: GetCredentialCancellationException) { /* fall through to picker */ }

        return fetchCredential(credentialManager, activityContext, silent = false)
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

    // ── State refresh ─────────────────────────────────────────────

    /**
     * Builds [UserState] from the latest server-side session data.
     *
     * [retrieveUserForCurrentSession(updateSession = true)] is called first
     * to flush the SDK's stale cached [UserInfo] and get the server's current
     * view (`is_anonymous`, `providers`, `raw_user_meta_data`).
     * This is the fix for the "profile still shows anonymous" bug that occurs
     * because [linkIdentityWithIdToken] refreshes the JWT internally but does
     * not immediately update the cached [UserInfo] object.
     */
    private suspend fun refreshUserState() {
        // Force-refresh SDK cache from server before reading currentUserOrNull()
        val user = runCatching {
            supabase.auth.retrieveUserForCurrentSession(updateSession = true)
        }.getOrNull() ?: supabase.auth.currentUserOrNull()

        val meta      = user?.userMetadata
        val hasGoogle = user?.appMetadata
            ?.get("providers")?.toString()?.contains("google") == true

        val isPremium: Boolean = if (user != null) {
            runCatching {
                supabase.postgrest["profiles"]
                    .select(Columns.list("is_premium")) {
                        filter { eq("id", user.id) }
                        limit(1)
                    }
                    .decodeSingleOrNull<ProfilePremiumRow>()
                    ?.isPremium
                    ?: false
            }.getOrDefault(false)
        } else {
            false
        }

        _userState.value = UserState(
            userId      = user?.id,
            isAnonymous = !hasGoogle,
            displayName = meta?.get("full_name")?.jsonPrimitive?.contentOrNull,
            email       = user?.email,
            avatarUrl   = meta?.get("avatar_url")?.jsonPrimitive?.contentOrNull,
            isPremium   = isPremium,
        )
    }

    // ── DTOs ──────────────────────────────────────────────────────

    @Serializable
    private data class ProfilePremiumRow(
        @SerialName("is_premium") val isPremium: Boolean = false,
    )
}