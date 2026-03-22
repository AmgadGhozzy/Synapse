package com.venom.synapse.features.profile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.core.ui.state.UiEffect
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
            authRepo.userState,
        ) { packs, studiedDayIndices, userState ->
            val todayIndex = System.currentTimeMillis() / 86_400_000L
            val streakDays = StreakCalculator.currentStreak(studiedDayIndices, todayIndex)

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
                isPremium         = userState.isPremium,
                isAnonymous       = userState.isAnonymous,
                planLabel         = if (userState.isPremium) "Premium Plan" else "Free Plan",
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
    fun onHelpTapped()       = _uiEffects.tryEmit(UiEffect.OpenExternal("https://help.synapse.app"))
    fun onPrivacyTapped()    = _uiEffects.tryEmit(UiEffect.OpenExternal("https://synapse.app/privacy"))
    fun onRateAppTapped()    = _uiEffects.tryEmit(UiEffect.OpenExternal("market://details?id=com.venom.synapse"))
    fun onExportDataTapped() = {/*TODO("Implement export data feature")*/ }

    // ── Sign out ──────────────────────────────────────────────────────────────

    /**
     * Sign-out flow — local DB cleared FIRST, then server session revoked.
     *
     * Why clear first:
     *   Sign-out is a user-initiated exit. If network fails mid-flow, the
     *   user is simply still signed in and can retry. Clearing local data
     *   first avoids the new anonymous session inheriting old user data.
     *
     * Anonymous users:
     *   Their packs only exist in Room (no remote sync). Signing out
     *   permanently loses them. The UI confirmation dialog should warn
     *   the user of this before this method is called.
     */
    fun onSignOut() {
        viewModelScope.launch {
            if (_isActionLoading.value) return@launch
            _isActionLoading.update { true }
            try {
                withContext(ioDispatcher) {
                    localDataRepo.clearAllLocalData()   // 1. wipe Room
                    authRepo.signOut()                  // 2. revoke JWT + new anon session
                }
                _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Dashboard.route))
            } catch (e: Exception) {
                _uiEffects.tryEmit(UiEffect.ShowError(
                    title = "Sign out failed",
                    message = "Could not sign out: ${e.message}",
                ))
            } finally {
                _isActionLoading.update { false }
            }
        }
    }

    // ── Delete account ────────────────────────────────────────────────────────

    /**
     * Delete-account flow — server delete FIRST, local DB cleared on success.
     *
     * Why clear second:
     *   If the remote delete fails (no network), we must NOT clear local data.
     *   That would leave the user with an intact remote account but empty local
     *   storage — they would be stuck. By waiting for server confirmation,
     *   the user can safely retry on failure.
     *
     * On error: [UiEffect.ShowError] is emitted, navigation does NOT happen.
     *   The account still exists. The user can retry.
     */
    fun onDeleteAccount() {
        viewModelScope.launch {
            if (_isActionLoading.value) return@launch
            _isActionLoading.update { true }
            try {
                withContext(ioDispatcher) {
                    authRepo.deleteAccount().getOrThrow()   // 1. server delete
                    localDataRepo.clearAllLocalData()       // 2. wipe Room only on success
                }
                _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Dashboard.route))
            } catch (e: Exception) {
                _uiEffects.tryEmit(UiEffect.ShowError(
                    title = "Delete account failed",
                    message = "Could not delete account: ${e.message}",
                ))
            } finally {
                _isActionLoading.update { false }
            }
        }
    }

    // ── Clear data (settings row) ─────────────────────────────────────────────

    /** Wipes local Room data only — keeps the current session active. */
    fun onClearAllData() {
        viewModelScope.launch {
            if (_isActionLoading.value) return@launch
            _isActionLoading.update { true }
            try {
                withContext(ioDispatcher) { localDataRepo.clearAllLocalData() }
                _uiEffects.tryEmit(UiEffect.ShowToast("All data cleared"))
            } catch (e: Exception) {
                _uiEffects.tryEmit(UiEffect.ShowError(
                    title = "Could not clear data",
                    message = "An error occurred while clearing data: ${e.message}",
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
                _uiEffects.tryEmit(UiEffect.ShowToast("Google Sign-In failed: ${it.message}"))
            }
        }
    }
}