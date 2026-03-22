package com.venom.synapse.features.onboarding.presentation.viewmodel

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.data.repo.SettingsRepository
import com.venom.synapse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI Models ─────────────────────────────────────────────────────────────────

@Immutable
data class OnboardingStepData(
    val index: Int,
    val label: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val illustrationRes: Int,
)

@Immutable
data class OnboardingUiState(
    val currentStep: Int = 0,
    val steps: List<OnboardingStepData> = emptyList(),
    val isAnonymous: Boolean = true,
) {
    // Derived — stable, based only on val properties
    val isLastStep: Boolean get() = steps.isNotEmpty() && currentStep == steps.lastIndex
    val currentStepData: OnboardingStepData? get() = steps.getOrNull(currentStep)
}

@Immutable
sealed interface OnboardingEvent {
    data object Complete : OnboardingEvent
    data class ShowError(val message: String) : OnboardingEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepo: com.venom.synapse.domain.repo.IAuthRepository,
) : ViewModel() {

    // Static step definitions — extract to a Repository if steps ever become remote/dynamic.
    // Drawable names: place ic_onboarding_upload, ic_onboarding_generate, ic_onboarding_master
    // in res/drawable/ as Vector XML files.
    private val steps = listOf(
        OnboardingStepData(
            index = 0,
            label = "Upload",
            title = "Turn any PDF into\na study engine",
            subtitle = "Import documents, textbooks, or lecture notes. Synapse reads them all.",
            illustrationRes = R.drawable.ic_onboarding_upload,
        ),
        OnboardingStepData(
            index = 1,
            label = "Generate",
            title = "AI crafts perfect\nquestions for you",
            subtitle = "Our engine extracts key concepts and generates MCQs, flashcards, and more.",
            illustrationRes = R.drawable.ic_onboarding_generate,
        ),
        OnboardingStepData(
            index = 2,
            label = "Master",
            title = "SRS keeps your\nknowledge sharp",
            subtitle = "Our spaced repetition algorithm schedules reviews right when you need them.",
            illustrationRes = R.drawable.ic_onboarding_master,
        ),
    )

    private val _currentStep = MutableStateFlow(0)

    val uiState: StateFlow<OnboardingUiState> = kotlinx.coroutines.flow.combine(
        _currentStep,
        authRepo.userState
    ) { step, userState ->
        OnboardingUiState(
            currentStep = step,
            steps = steps,
            isAnonymous = userState.isAnonymous
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = OnboardingUiState(steps = steps)
    )

    // One-shot events → SharedFlow (never StateFlow for events)
    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events: SharedFlow<OnboardingEvent> = _events.asSharedFlow()

    fun onNext() {
        _currentStep.update { step -> 
            (step + 1).coerceAtMost(steps.lastIndex)
        }
    }

    fun onSkip() = complete()

    fun onGetStarted() = complete()

    fun onGoogleSignIn(activityContext: android.content.Context) {
        viewModelScope.launch {
            val result = authRepo.linkGoogle(activityContext)
            result.onSuccess {
                complete()
            }
            result.onFailure {
                _events.emit(OnboardingEvent.ShowError(it.message ?: "Google Sign-In failed"))
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
