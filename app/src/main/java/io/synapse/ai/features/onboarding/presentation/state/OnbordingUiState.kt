package io.synapse.ai.features.onboarding.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.UiText


@Immutable
data class OnboardingStepData(
    val index: Int,
    val labelRes: Int,
    val titleRes: Int,
    val subtitleRes: Int,
    val illustrationRes: Int,
) {
    companion object {
        val steps = listOf(
            OnboardingStepData(
                index = 0,
                labelRes = R.string.onboarding_step_input,
                titleRes = R.string.onboarding_title_input,
                subtitleRes = R.string.onboarding_subtitle_input,
                illustrationRes = R.drawable.ic_onboarding_upload,
            ),
            OnboardingStepData(
                index = 1,
                labelRes = R.string.onboarding_step_practice,
                titleRes = R.string.onboarding_title_practice,
                subtitleRes = R.string.onboarding_subtitle_practice,
                illustrationRes = R.drawable.ic_onboarding_generate,
            ),
            OnboardingStepData(
                index = 2,
                labelRes = R.string.onboarding_step_memory,
                titleRes = R.string.onboarding_title_memory,
                subtitleRes = R.string.onboarding_subtitle_memory,
                illustrationRes = R.drawable.ic_onboarding_master,
            ),
        )
    }
}

@Immutable
data class OnboardingUiState(
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val steps: List<OnboardingStepData> = OnboardingStepData.steps
) {
    val isLastStep: Boolean get() = steps.isNotEmpty() && currentStep == steps.lastIndex
    val currentStepData: OnboardingStepData? get() = steps.getOrNull(currentStep)
}

@Immutable
sealed interface OnboardingEvent {
    data object Complete : OnboardingEvent
    data class ShowError(val message: UiText) : OnboardingEvent
}
