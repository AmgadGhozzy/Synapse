package com.venom.synapse.features.profile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.R
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.core.ui.state.UiText
import com.venom.synapse.data.sync.RemoteDataRepository
import com.venom.synapse.domain.repo.IAuthRepository
import com.venom.synapse.domain.repo.ILocalDataRepository
import com.venom.synapse.domain.repo.IPackRepository
import com.venom.synapse.domain.repo.IQuestionRepository
import com.venom.synapse.domain.repo.ISessionRepository
import com.venom.synapse.domain.stats.StreakCalculator
import com.venom.synapse.features.profile.presentation.state.ProfileUiState
import com.venom.synapse.navigation.SynapseScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    packRepo: IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val sessionRepo: ISessionRepository,
    private val authRepo: IAuthRepository,
    private val localDataRepo: ILocalDataRepository,
    private val remoteDataRepo: RemoteDataRepository,
    private val entitlementManager: com.venom.synapse.data.repo.EntitlementManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    // Separate loading flag for destructive async actions (sign-out, delete)
    // so the rest of the profile UI does not flash while the op is in flight.
    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading

    val uiState: StateFlow<ProfileUiState> =
        combine(
            packRepo.observeAllPacks(),
            sessionRepo.observeStudiedDayIndices(),
            combine(authRepo.userState, entitlementManager.entitlement, ::Pair)
        ) { packs, studiedDayIndices, (userState, entitlement) ->
            val todayIndex = System.currentTimeMillis() / 86_400_000L
            val streakDays = StreakCalculator.currentStreak(studiedDayIndices, todayIndex)

            val isPremium = entitlement.isAccessGranted

            val (totalCards, studyTimeHours, avgRetentionPct) = withContext(ioDispatcher) {
                val cards = packs.sumOf { pack -> questionRepo.countByPack(pack.id) }

                val allActivity = sessionRepo.getDailyActivity(0L, Long.MAX_VALUE)
                val durationMs  = allActivity.sumOf { it.totalDurationMs }
                val hours       = durationMs / 3_600_000f

                val nowMs = System.currentTimeMillis()
                val (mondayMs, nextMondayMs) = StreakCalculator.currentWeekBounds(nowMs)
                val weekActivity = sessionRepo.getDailyActivity(mondayMs, nextMondayMs)
                val retention = if (weekActivity.isEmpty()) 0f
                else weekActivity.map { it.accuracy }.average().toFloat()

                Triple(cards, hours, retention)
            }

            ProfileUiState(
                userName          = userState.displayName,
                userEmail         = userState.email,
                isPremium         = isPremium,
                isAnonymous       = userState.isAnonymous,
                planLabelRes      = if (isPremium) R.string.profile_plan_premium else R.string.profile_plan_free,
                avatarUrl         = userState.avatarUrl,
                avatarInitial     = userState.displayName?.firstOrNull() ?: 'A',
                packCount         = packs.size,
                cardCount         = totalCards,
                streakDays        = streakDays,
                isLoading         = false,
                totalCardsLearned = totalCards,
                studyTimeHours    = studyTimeHours,
                avgRetentionPct   = avgRetentionPct,
            )
        }
            .catch { e -> emit(ProfileUiState(isLoading = false, error = e.message)) }
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = ProfileUiState(),
            )

    // ── Navigation / simple actions ───────────────────────────────────────────
    fun onUpgradeTapped()    = _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Premium.route))

    fun onHelpTapped()       = _uiEffects.tryEmit(UiEffect.OpenExternal("https://help.synapse.app"))// TODO: implement privacy
    fun onPrivacyTapped()    = _uiEffects.tryEmit(UiEffect.OpenExternal("https://synapse.app/privacy"))
    fun onRateAppTapped()    = _uiEffects.tryEmit(UiEffect.OpenExternal("market://details?id=com.venom.synapse"))

    // ── Sign out (Authenticated only) ────────────────────────────────────────

    /**
     * Sign-out flow — wipe local cache FIRST, then revoke server session.
     *
     * Order rationale:
     *   Clearing Room first prevents the new anonymous session from
     *   inheriting old user data. If the signOut() network call fails,
     *   the user is still signed in and can retry — the data will be
     *   re-fetched from Supabase on next sync.
     *
     * Entitlement clearing is handled inside [AuthRepositoryImpl.signOut()].
     */
    fun onSignOut() {
        viewModelScope.launch {
            if (_isActionLoading.value) return@launch
            _isActionLoading.update { true }
            try {
                withContext(ioDispatcher) {
                    localDataRepo.clearAllLocalData()   // 1. wipe Room cache
                    authRepo.signOut()                  // 2. revoke JWT + new anon session
                }
                _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Dashboard.route))
            } catch (e: Exception) {
                val errorText = e.message?.takeIf { it.isNotBlank() }?.let {
                    UiText.Raw(R.string.profile_sign_out_failed_message, it)
                } ?: UiText.Raw(R.string.profile_sign_out_failed_message_generic)
                _uiEffects.tryEmit(UiEffect.ShowError(
                    title = UiText.Raw(R.string.profile_sign_out_failed_title),
                    text = errorText,
                ))
            } finally {
                _isActionLoading.update { false }
            }
        }
    }

    // ── Delete account (Authenticated only — NEVER anonymous) ────────────────

    /**
     * Delete-account flow — server delete FIRST, local wipe on success.
     *
     * Order rationale:
     *   If the remote delete fails (no network), we must NOT clear local data.
     *   That would leave the user with an intact remote account but empty local
     *   cache — they would be stuck. Only wipe after server confirms success.
     *
     * The Edge Function (`delete-account`) uses service_role server-side.
     * Entitlement clearing is handled inside [AuthRepositoryImpl.deleteAccount()].
     *
     * Anonymous users are blocked before reaching this method — the UI
     * hides "Delete Account" for anonymous sessions entirely.
     */
    fun onDeleteAccount() {
        // Double-check: never allow anonymous account deletion
        if (authRepo.userState.value.isAnonymous) return

        viewModelScope.launch {
            if (_isActionLoading.value) return@launch
            _isActionLoading.update { true }
            try {
                withContext(ioDispatcher) {
                    authRepo.deleteAccount().getOrThrow()   // 1. Edge Function delete
                    localDataRepo.clearAllLocalData()       // 2. wipe Room only on success
                }
                _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Dashboard.route))
            } catch (e: Exception) {
                val errorText = e.message?.takeIf { it.isNotBlank() }?.let {
                    UiText.Raw(R.string.profile_delete_account_failed_message, it)
                } ?: UiText.Raw(R.string.profile_delete_account_failed_message_generic)
                _uiEffects.tryEmit(UiEffect.ShowError(
                    title = UiText.Raw(R.string.profile_delete_account_failed_title),
                    text = errorText,
                ))
            } finally {
                _isActionLoading.update { false }
            }
        }
    }

    // ── Reset progress ───────────────────────────────────────────────────────

    /**
     * Resets all user progress.
     *
     * ANONYMOUS:
     *   Room-only wipe — works offline. The user's packs and progress are
     *   permanently deleted from the device.
     *
     * AUTHENTICATED:
     *   Requires internet. Calls the server to wipe remote progress FIRST,
     *   then clears the local Room cache. If the server call fails (offline),
     *   the action is blocked with an error message.
     *
     * TODO: When RemoteDataRepository is built, replace the local-only **/
    fun onClearAllData() {
        viewModelScope.launch {
            if (_isActionLoading.value) return@launch
            _isActionLoading.update { true }
            try {
                withContext(ioDispatcher) {
                    val user = authRepo.userState.value
                    if (!user.isAnonymous && user.userId != null) {
                        remoteDataRepo.deleteAllUserData(user.userId)
                    }
                    localDataRepo.clearAllLocalData()
                }
                _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.profile_all_data_cleared)))
            } catch (e: Exception) {
                val errorText = e.message?.takeIf { it.isNotBlank() }?.let {
                    UiText.Raw(R.string.profile_clear_data_failed_message, it)
                } ?: UiText.Raw(R.string.profile_clear_data_failed_message_generic)
                _uiEffects.tryEmit(UiEffect.ShowError(
                    title = UiText.Raw(R.string.profile_clear_data_failed_title),
                    text = errorText,
                ))
            } finally {
                _isActionLoading.update { false }
            }
        }
    }

    // ── Google sign-in ────────────────────────────────────────────────────────

    fun onGoogleSignIn(activityContext: Context) {
        viewModelScope.launch {
            val result = authRepo.linkGoogle(activityContext)
            result.onFailure {
                val errorText = it.message?.takeIf { msg -> msg.isNotBlank() }?.let { msg ->
                    UiText.Raw(R.string.auth_google_sign_in_failed_with_reason, msg)
                } ?: UiText.Raw(R.string.auth_google_sign_in_failed)
                _uiEffects.tryEmit(UiEffect.ShowToast(errorText))
            }
        }
    }
}
