package com.venom.synapse.core.ui.state

import com.venom.synapse.R

sealed class UiEffect {

    data class Navigate(val route: String) : UiEffect()

    data object NavigateBack : UiEffect()

    data class ShowToast(val text: UiText) : UiEffect()

    data class ShowError(
        val text       : UiText,
        val title      : UiText  = UiText.Raw(R.string.error_title),
        val retryAction: UiText? = null,
    ) : UiEffect()

    data class ShowUpgradePrompt(val feature: UiText) : UiEffect()

    data class PlaySound(val soundId: SoundType) : UiEffect()

    data class OpenShareSheet(val text: String, val title: String = "") : UiEffect()

    data class OpenExternal(val url: String) : UiEffect()

    data class ScrollTo(val position: Int) : UiEffect()
}

enum class SoundType {
    CORRECT,
    WRONG,
    SESSION_COMPLETE,
    LEECH_WARNING,
}
