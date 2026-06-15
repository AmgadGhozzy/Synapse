package io.synapse.ai.navigation

import android.net.Uri
import androidx.compose.runtime.Immutable
import io.synapse.ai.R
import io.synapse.ai.domains.study.demo.DemoStudyPack

sealed class SynapseScreen(
    val route: String,
    val titleRes: Int,
    val subtitleRes: Int = 0,
) {
    data object Dashboard : SynapseScreen(
        route = "synapse/dashboard",
        titleRes = R.string.synapse_screen_dashboard,
    )

    data object Library : SynapseScreen(
        route = "synapse/library",
        titleRes = R.string.synapse_screen_library,
        subtitleRes = R.string.synapse_subtitle_library,
    )

    data object Marketplace : SynapseScreen(
        route = "synapse/marketplace",
        titleRes = R.string.synapse_screen_marketplace,
        subtitleRes = R.string.synapse_subtitle_marketplace,
    )

    data object Stats : SynapseScreen(
        route = "synapse/stats",
        titleRes = R.string.synapse_screen_stats,
        subtitleRes = R.string.synapse_subtitle_stats,
    )

    data object Profile : SynapseScreen(
        route = "synapse/profile?reopenPaywall={reopenPaywall}",
        titleRes = R.string.synapse_screen_profile,
        subtitleRes = R.string.synapse_subtitle_profile,
    ) {
        const val ARG_REOPEN_PAYWALL = "reopenPaywall"
        val baseRoute: String = "synapse/profile"

        fun createRoute(reopenPaywall: Boolean = false): String {
            return if (reopenPaywall) "$baseRoute?$ARG_REOPEN_PAYWALL=true" else baseRoute
        }
    }

    data object AddPdf : SynapseScreen(
        route = "synapse/add-pdf?source={source}&uri={uri}",
        titleRes = R.string.synapse_screen_add_pdf,
        subtitleRes = R.string.synapse_subtitle_add_pdf,
    ) {
        const val ARG_SOURCE = "source"
        const val ARG_URI = "uri"
        const val SOURCE_FILE = "file"
        const val SOURCE_TEXT = "text"
        val baseRoute: String = "synapse/add-pdf"

        fun createRoute(source: String? = null, uri: String? = null): String {
            val encodedUri = uri?.let { Uri.encode(it) }
            return when {
                source == null && encodedUri == null -> baseRoute
                encodedUri != null -> "$baseRoute?$ARG_SOURCE=${source ?: SOURCE_FILE}&$ARG_URI=$encodedUri"
                else -> "$baseRoute?$ARG_SOURCE=$source"
            }
        }
    }

    data object Overview : SynapseScreen(
        route = "synapse/overview/{packId}",
        titleRes = R.string.synapse_screen_overview,
        subtitleRes = R.string.synapse_subtitle_overview,
    ) {
        const val ARG_PACK_ID = "packId"
        fun createRoute(packId: Long) = "synapse/overview/$packId"
    }

    data object Onboarding : SynapseScreen(
        route = "synapse/onboarding",
        titleRes = R.string.synapse_screen_onboarding,
        subtitleRes = R.string.synapse_subtitle_onboarding,
    )

    data object Export : SynapseScreen(
        route = "synapse/export/{packId}",
        titleRes = R.string.export_title,
    ) {
        const val ARG_PACK_ID = "packId"
        fun createRoute(packId: Long) = "synapse/export/$packId"
    }

    data object Premium : SynapseScreen(
        route = "synapse/gold",
        titleRes = R.string.synapse_screen_premium,
        subtitleRes = R.string.synapse_subtitle_premium,
    )

    data object About : SynapseScreen(
        route = "synapse/about",
        titleRes = R.string.profile_help_faq,
    )


    data object Quiz : SynapseScreen(
        route = "synapse/quiz/{packId}",
        titleRes = R.string.synapse_screen_quiz,
        subtitleRes = R.string.synapse_subtitle_quiz,
    ) {
        const val ARG_PACK_ID = "packId"
        const val DEMO_PACK_ID = DemoStudyPack.PACK_ID

        fun createRoute(packId: Long) = "synapse/quiz/$packId"
        fun createDemoRoute() = createRoute(DEMO_PACK_ID)
    }

    data object QuizContent : SynapseScreen(
        route = "synapse/quiz/content",
        titleRes = R.string.synapse_screen_quiz,
        subtitleRes = R.string.synapse_subtitle_quiz,
    )



    data object SessionSummary : SynapseScreen(
        route = "synapse/session-summary",
        titleRes = R.string.synapse_screen_session_summary,
        subtitleRes = R.string.synapse_subtitle_session_summary,
    )

    data object SummaryGenerator : SynapseScreen(
        route = "synapse/summary/generate?sourceType={sourceType}&sourceName={sourceName}&sourceContent={sourceContent}&depth={depth}&focus={focus}&language={language}&readAloud={readAloud}",
        titleRes = 0,
    ) {
        const val ARG_SOURCE_TYPE = "sourceType"
        const val ARG_SOURCE_NAME = "sourceName"
        const val ARG_SOURCE_CONTENT = "sourceContent"
        const val ARG_DEPTH = "depth"
        const val ARG_FOCUS = "focus"
        const val ARG_LANGUAGE = "language"
        const val ARG_READ_ALOUD = "readAloud"

        fun createRoute(
            type: String, 
            name: String, 
            content: String,
            depth: String,
            focus: String,
            language: String,
            readAloud: Boolean
        ): String {
            val encName = Uri.encode(name)
            val encContent = Uri.encode(content)
            val encFocus = Uri.encode(focus)
            return "synapse/summary/generate?$ARG_SOURCE_TYPE=$type&$ARG_SOURCE_NAME=$encName&$ARG_SOURCE_CONTENT=$encContent&$ARG_DEPTH=$depth&$ARG_FOCUS=$encFocus&$ARG_LANGUAGE=$language&$ARG_READ_ALOUD=$readAloud"
        }
    }

    data object SummaryViewer : SynapseScreen(
        route = "synapse/summary/viewer/{summaryId}",
        titleRes = 0,
    ) {
        const val ARG_SUMMARY_ID = "summaryId"

        fun createRoute(summaryId: Long): String {
            return "synapse/summary/viewer/$summaryId"
        }
    }

    companion object {
        fun fromRoute(route: String?): SynapseScreen = when {
            route == null -> Dashboard
            route == Dashboard.route -> Dashboard
            route == Library.route -> Library
            route == Stats.route -> Stats
            route == Profile.route -> Profile
            route == Onboarding.route -> Onboarding
            route == Premium.route -> Premium
            route == About.route -> About
            route == SessionSummary.route -> SessionSummary
            route.startsWith(AddPdf.baseRoute) -> AddPdf
            route.startsWith("synapse/overview") -> Overview
            route.startsWith("synapse/export") -> Export
            route.startsWith("synapse/quiz") -> Quiz
            route.startsWith("synapse/summary/generate") -> SummaryGenerator
            route.startsWith("synapse/summary/viewer") -> SummaryViewer
            route == Marketplace.route -> Marketplace
            else -> Dashboard
        }
    }
}

@Immutable
data class BarConfig(
    val showTopBar: Boolean,
    val showBottomBar: Boolean,
) {
    companion object {
        private val BOTH = BarConfig(showTopBar = true, showBottomBar = true)
        private val NONE = BarConfig(showTopBar = false, showBottomBar = false)
        private val TOP_ONLY = BarConfig(showTopBar = true, showBottomBar = false)

        fun forRoute(route: String?): BarConfig {
            if (route == null) return BOTH
            return when {
                route.startsWith(SynapseScreen.Onboarding.route) -> NONE
                route.startsWith(SynapseScreen.AddPdf.baseRoute) -> NONE

                route.startsWith(SynapseScreen.About.route) -> NONE
                route.startsWith("synapse/quiz") -> NONE
                route.startsWith("synapse/overview") -> NONE
                route.startsWith("synapse/export") -> NONE
                route.startsWith("synapse/summary") -> NONE
                route.startsWith(SynapseScreen.SessionSummary.route) -> NONE
                route.startsWith(SynapseScreen.Profile.baseRoute) -> NONE
                else -> BOTH
            }
        }
    }
}