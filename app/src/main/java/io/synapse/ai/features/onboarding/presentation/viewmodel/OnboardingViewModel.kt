package io.synapse.ai.features.onboarding.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.data.repo.AppConfigProvider
import io.synapse.ai.data.repo.AppSettingsRepository
import io.synapse.ai.domain.repo.IAuthRepository
import io.synapse.ai.features.onboarding.presentation.state.OnboardingEvent
import io.synapse.ai.features.onboarding.presentation.state.OnboardingUiState
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
    private val authRepo: IAuthRepository,
    private val appConfigProvider: AppConfigProvider,
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)

    val uiState: StateFlow<OnboardingUiState> = _currentStep
        .map { step -> OnboardingUiState(currentStep = step) }
        .stateIn(
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

    fun onGoogleSignIn(activityContext: Context) {
        viewModelScope.launch {
            val result = authRepo.linkGoogle(activityContext)
            result.onSuccess {
                complete()
            }
            result.onFailure {
                _events.emit(
                    OnboardingEvent.ShowError(
                        it.message?.takeIf { message -> message.isNotBlank() }?.let { message ->
                            UiText.Raw(R.string.auth_google_sign_in_failed_with_reason, message)
                        } ?: UiText.Raw(R.string.auth_google_sign_in_failed)
                    )
                )
            }
        }
    }

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

    private fun complete() {
        viewModelScope.launch {
            settingsRepository.setFirstLaunchComplete()
            _events.emit(OnboardingEvent.Complete)
        }
    }
}