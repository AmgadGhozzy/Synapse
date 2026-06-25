package io.synapse.ai.features.profile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.R
import io.synapse.ai.core.analytics.TrackingManager
import io.synapse.ai.core.analytics.data.ConsentRepository
import io.synapse.ai.core.analytics.model.AnalyticsEvent
import io.synapse.ai.core.ui.state.ToastType
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.domains.config.data.AppConfigProvider
import io.synapse.ai.domains.config.data.ConsentManager
import io.synapse.ai.domains.premium.data.PremiumManager
import io.synapse.ai.domains.study.data.sync.RemoteDataRepository
import io.synapse.ai.domains.study.data.sync.SyncEngine
import io.synapse.ai.domains.study.data.sync.SyncStatus
import io.synapse.ai.domains.config.data.SyncConsent
import io.synapse.ai.domains.study.data.sync.SyncScheduler
import io.synapse.ai.domains.auth.repository.IAuthRepository
import io.synapse.ai.domains.study.repository.ILocalDataRepository
import io.synapse.ai.domains.study.repository.IPackRepository
import io.synapse.ai.domains.study.repository.IQuestionRepository
import io.synapse.ai.domains.study.repository.ISessionRepository
import io.synapse.ai.domains.study.usecase.StreakCalculator
import io.synapse.ai.features.profile.presentation.state.ProfileUiState
import io.synapse.ai.navigation.SynapseScreen
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
    private val premiumManager: PremiumManager,
    private val consentManager: ConsentManager,
    private val syncEngine: SyncEngine,
    private val syncScheduler: SyncScheduler,
    private val appConfigProvider: AppConfigProvider,
    private val analyticsConsentRepo: ConsentRepository,
    private val trackingManager: TrackingManager,
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
            combine(authRepo.userState, premiumManager.isPro, ::Pair),
            combine(
                consentManager.isSyncEnabled,
                consentManager.consentState,
                syncEngine.status,
                consentManager.lastSyncedTime
            ) { enabled, consent, status, lastSynced ->
                listOf(enabled, consent, status, lastSynced)
            },
            analyticsConsentRepo.consent,
        ) { packs, studiedDayIndices, (userState, entitlement), syncInfo, userConsent ->
            val isSyncEnabled = syncInfo[0] as Boolean
            val consentState = syncInfo[1] as SyncConsent
            val syncStatus = syncInfo[2] as SyncStatus
            val lastSyncedTime = syncInfo[3] as String?
            val todayIndex = System.currentTimeMillis() / 86_400_000L
            val streakDays = StreakCalculator.currentStreak(studiedDayIndices, todayIndex)

            val isPremium = entitlement

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
                isSyncEnabled     = isSyncEnabled,
                consentState      = consentState,
                syncStatus        = syncStatus,
                lastSyncedTime    = lastSyncedTime,
                analyticsEnabled = userConsent.analyticsEnabled,
                crashEnabled     = userConsent.crashEnabled,
                pushEnabled      = userConsent.pushEnabled,
            )
        }
            .catch { e -> emit(ProfileUiState(isLoading = false, error = e.message)) }
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = ProfileUiState(),
            )

    fun onHelpTapped()       = _uiEffects.tryEmit(UiEffect.OpenExternal(appConfigProvider.appHelpUrl))
    fun onPrivacyTapped()    = _uiEffects.tryEmit(UiEffect.OpenExternal(appConfigProvider.appPrivacy))
    fun onRateAppTapped()    = _uiEffects.tryEmit(UiEffect.OpenExternal(appConfigProvider.appRateAppUrl))
    fun onAboutTapped()      = _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.About.route))
    fun onContactUsTapped()  = _uiEffects.tryEmit(UiEffect.OpenExternal("mailto:${appConfigProvider.appEmail}"))
    fun onDeleteAccountViaTapped() = _uiEffects.tryEmit(UiEffect.OpenExternal(appConfigProvider.appDeleteAccountUrl))

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
                    syncEngine.onSignOut()              // 3. Clear sync logic / consent
                    syncScheduler.cancel()              // 4. Stop background sync worker
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
                _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.profile_all_data_cleared), ToastType.SUCCESS))
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
            _isActionLoading.value = true
            val result = authRepo.linkGoogle(activityContext)
            _isActionLoading.value = false
            result
                .onSuccess {
                    // Start background sync for newly authenticated user
                    syncScheduler.schedule()
                    // Navigate back to wherever launched ProfileScreen (e.g. the paywall)
                    _uiEffects.tryEmit(UiEffect.NavigateBack)
                }
                .onFailure {
                    val errorText = it.message?.takeIf { msg -> msg.isNotBlank() }?.let { msg ->
                        UiText.Raw(R.string.auth_google_sign_in_failed_with_reason, msg)
                    } ?: UiText.Raw(R.string.auth_google_sign_in_failed)
                    _uiEffects.tryEmit(UiEffect.ShowToast(errorText, ToastType.ERROR))
                }
        }
    }

    // ── Sync Actions ──────────────────────────────────────────────────────────

    fun onSyncToggleChanged(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                syncEngine.onSyncEnabled()
            } else {
                syncEngine.onSyncDisabled()
            }
        }
    }

    fun onConsentGranted() {
        viewModelScope.launch {
            consentManager.setConsent(SyncConsent.ACCEPTED)
            onSyncToggleChanged(true)
        }
    }

    fun onConsentDenied() {
        viewModelScope.launch {
            consentManager.setConsent(SyncConsent.REJECTED)
        }
    }

    fun onManualSyncRequested() {
        syncEngine.manualSync()
    }

    // ── Privacy Consent Actions ────────────────────────────────────────────────

    fun onAnalyticsConsentChanged(enabled: Boolean) {
        viewModelScope.launch {
            analyticsConsentRepo.updateAnalytics(enabled)
            // Fire the opt-in event BEFORE potentially disabling analytics,
            // so we capture the user's last preference change.
            trackingManager.logEvent(AnalyticsEvent.AnalyticsOptInChanged(enabled))
        }
    }

    fun onCrashConsentChanged(enabled: Boolean) {
        viewModelScope.launch {
            analyticsConsentRepo.updateCrash(enabled)
        }
    }

    fun onPushConsentChanged(enabled: Boolean) {
        viewModelScope.launch {
            analyticsConsentRepo.updatePush(enabled)
        }
    }
}
