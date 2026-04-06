package com.venom.synapse.features.onboarding.presentation.state

import androidx.compose.runtime.Immutable
import com.venom.synapse.R
import com.venom.synapse.core.ui.state.UiText


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
                labelRes = R.string.onboarding_label_upload,
                titleRes = R.string.onboarding_title_upload,
                subtitleRes = R.string.onboarding_subtitle_upload,
                illustrationRes = R.drawable.ic_onboarding_upload,
            ),
            OnboardingStepData(
                index = 1,
                labelRes = R.string.onboarding_label_generate,
                titleRes = R.string.onboarding_title_generate,
                subtitleRes = R.string.onboarding_subtitle_generate,
                illustrationRes = R.drawable.ic_onboarding_generate,
            ),
            OnboardingStepData(
                index = 2,
                labelRes = R.string.onboarding_label_master,
                titleRes = R.string.onboarding_title_master,
                subtitleRes = R.string.onboarding_subtitle_master,
                illustrationRes = R.drawable.ic_onboarding_master,
            ),
        )
    }
}

@Immutable
data class OnboardingUiState(
    val currentStep: Int = 0,
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

