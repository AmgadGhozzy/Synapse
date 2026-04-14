package io.synapse.ai.navigation

import androidx.compose.runtime.Immutable
import io.synapse.ai.R
import io.synapse.ai.data.demo.DemoStudyPack

sealed class SynapseScreen(
    val route      : String,
    val titleRes   : Int,
    val subtitleRes: Int = 0,
) {
    data object Dashboard : SynapseScreen(
        route      = "synapse/dashboard",
        titleRes   = R.string.synapse_screen_dashboard,
    )

    data object Library : SynapseScreen(
        route       = "synapse/library",
        titleRes    = R.string.synapse_screen_library,
        subtitleRes = R.string.synapse_subtitle_library,
    )

    data object Stats : SynapseScreen(
        route       = "synapse/stats",
        titleRes    = R.string.synapse_screen_stats,
        subtitleRes = R.string.synapse_subtitle_stats,
    )

    data object Profile : SynapseScreen(
        route       = "synapse/profile",
        titleRes    = R.string.synapse_screen_profile,
        subtitleRes = R.string.synapse_subtitle_profile,
    )

    data object AddPdf : SynapseScreen(
        route       = "synapse/add-pdf?source={source}",
        titleRes    = R.string.synapse_screen_add_pdf,
        subtitleRes = R.string.synapse_subtitle_add_pdf,
    ) {
        const val ARG_SOURCE  = "source"
        const val SOURCE_FILE = "file"
        const val SOURCE_TEXT = "text"
        val baseRoute: String = "synapse/add-pdf"

        fun createRoute(source: String? = null): String = when {
            source.isNullOrBlank() -> baseRoute
            else                   -> "$baseRoute?$ARG_SOURCE=$source"
        }
    }

    data object Onboarding : SynapseScreen(
        route       = "synapse/onboarding",
        titleRes    = R.string.synapse_screen_onboarding,
        subtitleRes = R.string.synapse_subtitle_onboarding,
    )

    data object Premium : SynapseScreen(
        route       = "synapse/premium",
        titleRes    = R.string.synapse_screen_premium,
        subtitleRes = R.string.synapse_subtitle_premium,
    )

    data object About : SynapseScreen(
        route       = "synapse/about",
        titleRes    = R.string.profile_help_faq,
    )


    data object Quiz : SynapseScreen(
        route       = "synapse/quiz/{packId}",
        titleRes    = R.string.synapse_screen_quiz,
        subtitleRes = R.string.synapse_subtitle_quiz,
    ) {
        const val ARG_PACK_ID  = "packId"
        const val DEMO_PACK_ID = DemoStudyPack.PACK_ID

        fun createRoute(packId: Long) = "synapse/quiz/$packId"
        fun createDemoRoute()        = createRoute(DEMO_PACK_ID)
    }

    data object QuizContent : SynapseScreen(
        route       = "synapse/quiz/content",
        titleRes    = R.string.synapse_screen_quiz,
        subtitleRes = R.string.synapse_subtitle_quiz,
    )

    data object SessionSummary : SynapseScreen(
        route       = "synapse/session-summary",
        titleRes    = R.string.synapse_screen_session_summary,
        subtitleRes = R.string.synapse_subtitle_session_summary,
    )

    companion object {
        fun fromRoute(route: String?): SynapseScreen = when {
            route == null                          -> Dashboard
            route == Dashboard.route               -> Dashboard
            route == Library.route                 -> Library
            route == Stats.route                   -> Stats
            route == Profile.route                 -> Profile
            route == Onboarding.route              -> Onboarding
            route == Premium.route                 -> Premium
            route == About.route                   -> About
            route == SessionSummary.route          -> SessionSummary
            route.startsWith(AddPdf.baseRoute)     -> AddPdf
            route.startsWith("synapse/quiz")       -> Quiz
            else                                   -> Dashboard
        }
    }
}

@Immutable
data class BarConfig(
    val showTopBar   : Boolean,
    val showBottomBar: Boolean,
) {
    companion object {
        private val BOTH     = BarConfig(showTopBar = true,  showBottomBar = true)
        private val NONE     = BarConfig(showTopBar = false, showBottomBar = false)
        private val TOP_ONLY = BarConfig(showTopBar = true,  showBottomBar = false)

        fun forRoute(route: String?): BarConfig {
            if (route == null) return BOTH
            return when {
                route.startsWith(SynapseScreen.Onboarding.route)     -> NONE
                route.startsWith(SynapseScreen.AddPdf.baseRoute)     -> NONE
                route.startsWith(SynapseScreen.Premium.route)        -> NONE
                route.startsWith(SynapseScreen.About.route)          -> NONE
                route.startsWith("synapse/quiz")                      -> NONE
                route.startsWith(SynapseScreen.SessionSummary.route) -> NONE
                route == SynapseScreen.Profile.route                  -> TOP_ONLY
                else                                                   -> BOTH
            }
        }
    }
}