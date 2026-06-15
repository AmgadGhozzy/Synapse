package io.synapse.ai.features.onboarding.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.domains.config.data.AppConfigProvider
import io.synapse.ai.domains.config.data.AppSettingsRepository
import io.synapse.ai.core.analytics.TrackingManager
import io.synapse.ai.core.analytics.model.AnalyticsEvent
import io.synapse.ai.features.onboarding.presentation.state.OnboardingEvent
import io.synapse.ai.features.onboarding.presentation.state.OnboardingUiState
import io.synapse.ai.domains.auth.repository.IAuthRepository
import io.synapse.ai.domains.study.data.sync.SyncScheduler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: AppSettingsRepository,
    private val appConfigProvider: AppConfigProvider,
    private val trackingManager: TrackingManager,
    private val authRepo: IAuthRepository,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    init {
        trackingManager.logEvent(AnalyticsEvent.OnboardingStarted)
    }

    private val _currentStep = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<OnboardingUiState> = combine(
        _currentStep,
        _isLoading
    ) { step, loading ->
        OnboardingUiState(currentStep = step, isLoading = loading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OnboardingUiState()
    )

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    private val _uiEffects = MutableSharedFlow<UiEffect>()
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    fun onNext() {
        _currentStep.update { step ->
            (step + 1).coerceAtMost(uiState.value.steps.lastIndex)
        }
    }

    fun onSkip() = complete()

    fun onGetStarted() = complete()

    fun onTermsTapped() {
        viewModelScope.launch {
            _uiEffects.emit(UiEffect.OpenExternal(appConfigProvider.appTerms))
        }
    }

    fun onPrivacyTapped() {
        viewModelScope.launch {
            _uiEffects.emit(UiEffect.OpenExternal(appConfigProvider.appPrivacy))
        }
    }

    fun onGoogleSignIn(activityContext: Context) {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.update { true }
            val result = authRepo.linkGoogle(activityContext)
            _isLoading.update { false }
            result
                .onSuccess {
                    syncScheduler.schedule()
                    complete()
                }
                .onFailure {
                    val errorText = it.message?.takeIf { msg -> msg.isNotBlank() }?.let { msg ->
                        UiText.Raw(R.string.auth_google_sign_in_failed_with_reason, msg)
                    } ?: UiText.Raw(R.string.auth_google_sign_in_failed)
                    _events.emit(OnboardingEvent.ShowError(errorText))
                }
        }
    }

    private fun complete() {
        viewModelScope.launch {
            settingsRepository.setFirstLaunchComplete()
            trackingManager.logEvent(AnalyticsEvent.OnboardingCompleted)
            _events.emit(OnboardingEvent.Complete)
        }
    }
}



