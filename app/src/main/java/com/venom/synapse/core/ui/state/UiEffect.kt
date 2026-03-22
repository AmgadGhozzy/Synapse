package com.venom.synapse.core.ui.state

/**
 * UI EFFECTS — One-time events consumed by Compose
 */
sealed class UiEffect {

    /** Navigate to a route. Handled by NavController in the host composable. */
    data class Navigate(val route: String) : UiEffect()

    /** Pop the current screen off the back stack. */
    data object NavigateBack : UiEffect()

    /** Show a transient toast/snackbar message. */
    data class ShowToast(val message: String) : UiEffect()

    /** Show an error dialog or styled error snackbar. */
    data class ShowError(
        val title: String = "Error",
        val message: String,
        val retryAction: String? = null
    ) : UiEffect()

    /** Trigger an upgrade/paywall prompt (e.g., Pro feature gating). */
    data class ShowUpgradePrompt(val feature: String) : UiEffect()

    /** Play a sound effect (correct/wrong answer feedback). */
    data class PlaySound(val soundId: SoundType) : UiEffect()

    /** Open the system share sheet. */
    data class OpenShareSheet(val text: String, val title: String = "") : UiEffect()

    /** Open an external URL in the browser. */
    data class OpenExternal(val url: String) : UiEffect()

    /** Scroll to a specific position (e.g., question requeue). */
    data class ScrollTo(val position: Int) : UiEffect()
}

/**
 * Sound effects used by the study session.
 * Mapped to actual resource IDs in the Compose layer.
 */
enum class SoundType {
    CORRECT,
    WRONG,
    SESSION_COMPLETE,
    LEECH_WARNING
}
