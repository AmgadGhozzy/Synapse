package com.venom.synapse.navigation

import androidx.compose.runtime.Immutable
import com.venom.synapse.R

sealed class SynapseScreen(
    val route: String,
    val titleRes: Int,
    val subtitleRes: Int = 0
) {
    data object Dashboard : SynapseScreen("synapse/dashboard", R.string.synapse_screen_dashboard)

    data object Library : SynapseScreen("synapse/library", R.string.synapse_screen_library, R.string.synapse_subtitle_library)

    data object Stats : SynapseScreen("synapse/stats", R.string.synapse_screen_stats, R.string.synapse_subtitle_stats)

    data object Profile : SynapseScreen("synapse/profile", R.string.synapse_screen_profile, R.string.synapse_subtitle_profile)

    data object AddPdf : SynapseScreen("synapse/add-pdf", R.string.synapse_screen_add_pdf, R.string.synapse_subtitle_add_pdf)

    data object Onboarding : SynapseScreen("synapse/onboarding", R.string.synapse_screen_onboarding, R.string.synapse_subtitle_onboarding)

    data object Premium : SynapseScreen("synapse/premium", R.string.synapse_screen_premium, R.string.synapse_subtitle_premium)

    data object Quiz : SynapseScreen("synapse/quiz/{packId}", R.string.synapse_screen_quiz, R.string.synapse_subtitle_quiz) {
        const val ARG_PACK_ID = "packId"
        fun createRoute(packId: Long) = "synapse/quiz/$packId"
    }
    object SessionSummary : SynapseScreen("synapse/session-summary", R.string.synapse_screen_session_summary, R.string.synapse_subtitle_session_summary)

    companion object {
        fun fromRoute(route: String?): SynapseScreen = when (route) {
            null -> Dashboard
            Dashboard.route -> Dashboard
            Library.route -> Library
            Stats.route -> Stats
            Profile.route -> Profile
            AddPdf.route -> AddPdf
            Onboarding.route -> Onboarding
            Premium.route -> Premium
            Quiz.route -> Quiz
            else -> Dashboard
        }
    }
}

@Immutable
data class BarConfig(
    val showTopBar: Boolean,
    val showBottomBar: Boolean
) {
    companion object {
        private val BOTH = BarConfig(showTopBar = true, showBottomBar = true)
        private val NONE = BarConfig(showTopBar = false, showBottomBar = false)
        private val TOP_ONLY = BarConfig(showTopBar = true, showBottomBar = false)

        fun forRoute(route: String?): BarConfig {
            if (route == null) return BOTH
            
            // Handle prefix-based matching for dynamic routes (e.g. Quiz)
            return when {
                route.startsWith(SynapseScreen.Onboarding.route) -> NONE
                route.startsWith(SynapseScreen.AddPdf.route) -> NONE
                route.startsWith(SynapseScreen.Premium.route) -> NONE
                route.startsWith("synapse/quiz") -> NONE
                route.startsWith(SynapseScreen.SessionSummary.route) -> NONE
                route == SynapseScreen.Profile.route -> TOP_ONLY
                else -> BOTH
            }
        }
    }
}