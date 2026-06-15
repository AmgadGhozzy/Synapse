package io.synapse.ai.core.ui.state

import io.synapse.ai.R
import io.synapse.ai.core.audio.model.SoundType

enum class ToastType {
    SUCCESS, ERROR, INFO, WARNING
}

sealed class UiEffect {

    data class Navigate(val route: String) : UiEffect()

    data object NavigateBack : UiEffect()

    /**
     * Emitted by [SessionViewModel.onReviewMistakes] and [SessionViewModel.onContinuePack]
     * after the new session plan is fully loaded and [StudyEngine.startSession] has been called.
     *
     * Using a dedicated subclass (instead of [Navigate]) avoids a SharedFlow race condition:
     * both QuizScreen (in the back-stack) and SessionSummaryScreen (currently visible) subscribe
     * to [SessionViewModel.uiEffects] simultaneously. A generic [Navigate] event would be
     * consumed by whichever collector runs first. This effect is intentionally ignored by
     * QuizScreen's collector, so it is always handled by SessionSummaryScreen.
     */
    data object NavigateToNewSession : UiEffect()

    data class ShowToast(
        val text: UiText,
        val type: ToastType = ToastType.SUCCESS,
    ) : UiEffect()

    data class ShowError(
        val text       : UiText,
        val title      : UiText  = UiText.Raw(R.string.error_title),
        val retryAction: UiText? = null,
    ) : UiEffect()

    data class ShowPaywall(val feature: UiText) : UiEffect()

    data class PlaySound(val soundId: SoundType) : UiEffect()

    data class OpenShareSheet(val text: String, val title: String = "") : UiEffect()

    data class OpenExternal(val url: String) : UiEffect()

    data class ScrollTo(val position: Int) : UiEffect()
}

