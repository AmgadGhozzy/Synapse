package com.venom.synapse.features.onboarding.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.data.repo.SettingsRepository
import com.venom.synapse.R
import com.venom.synapse.core.ui.state.UiText
import com.venom.synapse.domain.repo.IAuthRepository
import com.venom.synapse.features.onboarding.presentation.state.OnboardingEvent
import com.venom.synapse.features.onboarding.presentation.state.OnboardingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val settingsRepository: SettingsRepository,
    private val authRepo: IAuthRepository,
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

    private fun complete() {
        viewModelScope.launch {
            settingsRepository.markFirstLaunchComplete()
            _events.emit(OnboardingEvent.Complete)
        }
    }
}
