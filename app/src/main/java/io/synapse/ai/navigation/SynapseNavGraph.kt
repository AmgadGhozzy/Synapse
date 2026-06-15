package io.synapse.ai.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import io.synapse.ai.features.analytics.viewmodel.ScreenTrackerViewModel
import io.synapse.ai.features.add_pdf.presentation.screen.AddPdfScreen
import io.synapse.ai.features.dashboard.presentation.screen.DashboardScreen
import io.synapse.ai.features.dashboard.presentation.viewmodel.DashboardViewModel
import io.synapse.ai.features.export.presentation.screen.ExportScreen
import io.synapse.ai.features.library.presentation.screen.LibraryScreen
import io.synapse.ai.features.marketplace.presentation.screen.MarketplaceScreen
import io.synapse.ai.features.onboarding.presentation.screen.OnboardingScreen
import io.synapse.ai.features.overview.presentation.screen.PackOverviewScreen
import io.synapse.ai.features.profile.presentation.components.AboutScreen
import io.synapse.ai.features.profile.presentation.screen.ProfileScreen
import io.synapse.ai.features.session.presentation.screen.QuizScreen
import io.synapse.ai.features.session.presentation.screen.SessionSummaryScreen
import io.synapse.ai.features.session.presentation.viewmodel.SessionViewModel
import io.synapse.ai.features.stats.presentation.screen.StatsScreen
import io.synapse.ai.features.summary.presentation.screen.SummaryGeneratorScreen
import io.synapse.ai.features.summary.presentation.screen.SummaryScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import io.synapse.ai.core.ui.components.LoadingIndicator
import io.synapse.ai.features.summary.presentation.state.SummaryConfig
import io.synapse.ai.features.summary.presentation.state.SummaryDepth
import io.synapse.ai.features.summary.presentation.state.SummaryEffect
import io.synapse.ai.features.summary.presentation.state.SummaryEvent
import io.synapse.ai.features.summary.presentation.viewmodel.SummaryViewModel
import io.synapse.ai.navigation.core.AnimatedNavHost
import io.synapse.ai.navigation.core.NavTransitions
import io.synapse.ai.navigation.core.navigateToStart
import io.synapse.ai.ui.viewmodel.RootViewModel

/**
 * Transition legend — only non-default motions are declared here.
 * AnimatedNavHost provides horizontal push/pop as the global default.
 *
 *   fadeThrough → bottom-nav tab switches
 *   vertical    → modal sheets (Quiz, AddPdf, Premium)
 */
@Composable
fun SynapseNavGraph(
    navController: NavHostController,
    onboardingDone: Boolean,
    rootViewModel: RootViewModel,
    modifier: Modifier = Modifier,
    screenTracker: ScreenTrackerViewModel = hiltViewModel(),
) {
    val startDestination = remember {
        if (onboardingDone) "synapse/main" else SynapseScreen.Onboarding.route
    }

    // Automatic screen tracking — fires on every navigation destination change
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            entry.destination.route?.let { route ->
                screenTracker.onScreenChanged(route)
            }
        }
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {

        // ── Onboarding ──────────────────────────────────────────────────
        composable(
            route = SynapseScreen.Onboarding.route,
            enterTransition = NavTransitions.fadeThroughEnter(),
            exitTransition = NavTransitions.fadeThroughExit(),
            popEnterTransition = NavTransitions.fadeThroughEnter(),
            popExitTransition = NavTransitions.fadeThroughExit(),
        ) {
            OnboardingScreen(
                onComplete = {
                    rootViewModel.completeOnboarding()
                    navController.navigate("synapse/main") {
                        popUpTo(SynapseScreen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    navController.navigate(SynapseScreen.Quiz.createDemoRoute())
                },
            )
        }

        // ── Main nested graph ───────────────────────────────────────────
        navigation(
            route = "synapse/main",
            startDestination = SynapseScreen.Dashboard.route,
        ) {
            // SynapseNavGraph.kt
            tabComposable(SynapseScreen.Dashboard.route) { entry ->
                val uri by rootViewModel.sharedUri.collectAsStateWithLifecycle()
                if (uri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                } else {
                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry("synapse/main")
                    }
                    val vm: DashboardViewModel = hiltViewModel(parentEntry)
                    DashboardScreen(
                        viewModel = vm,
                        rootViewModel = rootViewModel,
                        onNavigate = { route ->
                            if (route == SynapseScreen.Library.route) {
                                navController.navigateToStart(route)
                            } else {
                                navController.navigate(route)
                            }
                        },
                    )
                }
            }

            tabComposable(SynapseScreen.Library.route) {
                LibraryScreen(onNavigate = { navController.navigate(it) })
            }

            tabComposable(SynapseScreen.Marketplace.route) {
                MarketplaceScreen(
                    onNavigateToPack = { remotePackId ->
                        // Switch to the Library tab properly using MD3 semantics
                        navController.navigateToStart(SynapseScreen.Library.route)
                    },
                    onNavigateToPremium = {
                        rootViewModel.showPaywall()
                    },
                )
            }

            tabComposable(SynapseScreen.Stats.route) {
                StatsScreen()
            }

            // Profile: inherits horizontal push/pop from NavHost default ✅
            composable(
                route = SynapseScreen.Profile.route,
                arguments = listOf(
                    navArgument(SynapseScreen.Profile.ARG_REOPEN_PAYWALL) {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { entry ->
                val reopenPaywall =
                    entry.arguments?.getBoolean(SynapseScreen.Profile.ARG_REOPEN_PAYWALL) == true
                ProfileScreen(
                    onNavigate = { navController.navigate(it) },
                    onNavigateBack = {
                        navController.popBackStack()
                        if (reopenPaywall) {
                            rootViewModel.showPaywall()
                        }
                    },
                )
            }
        }

        // ── Quiz Flow (Shared ViewModel) ──────────────────────────────
        navigation(
            route = SynapseScreen.Quiz.route,
            startDestination = SynapseScreen.QuizContent.route,
            arguments = listOf(
                navArgument(SynapseScreen.Quiz.ARG_PACK_ID) { type = NavType.LongType },
            ),
        ) {
            composable(
                route = SynapseScreen.QuizContent.route,
                enterTransition = NavTransitions.verticalEnter(),
                exitTransition = NavTransitions.verticalExit(),
                popEnterTransition = NavTransitions.verticalPopEnter(),
                popExitTransition = NavTransitions.verticalPopExit(),
            ) { entry ->
                val isDemoSession = entry.arguments
                    ?.getLong(SynapseScreen.Quiz.ARG_PACK_ID) == SynapseScreen.Quiz.DEMO_PACK_ID

                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(SynapseScreen.Quiz.route)
                }
                val parentLifecycle by parentEntry.lifecycle.currentStateFlow
                    .collectAsStateWithLifecycle()
                if (parentLifecycle == Lifecycle.State.DESTROYED) return@composable

                val vm: SessionViewModel = hiltViewModel(parentEntry)

                QuizScreen(
                    viewModel = vm,
                    onBack = {
                        if (isDemoSession) {
                            navController.navigate(SynapseScreen.Dashboard.route) {
                                popUpTo(SynapseScreen.Quiz.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onNavigateToSummary = {
                        navController.navigate(SynapseScreen.SessionSummary.route)
                    },
                )
            }

            composable(
                route = SynapseScreen.SessionSummary.route,
                enterTransition = NavTransitions.verticalEnter(),
                exitTransition = NavTransitions.verticalExit(),
                popEnterTransition = NavTransitions.verticalPopEnter(),
                popExitTransition = NavTransitions.verticalPopExit(),
            ) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(SynapseScreen.Quiz.route)
                }
                val parentLifecycle by parentEntry.lifecycle.currentStateFlow
                    .collectAsStateWithLifecycle()
                if (parentLifecycle == Lifecycle.State.DESTROYED) return@composable

                val vm: SessionViewModel = hiltViewModel(parentEntry)

                SessionSummaryScreen(
                    viewModel = vm,
                    onAddSource = {
                        navController.navigate(
                            SynapseScreen.AddPdf.createRoute(SynapseScreen.AddPdf.SOURCE_FILE)
                        ) {
                            popUpTo(SynapseScreen.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onReviewMistakes = { _ ->
                        vm.onReviewMistakes()
                    },
                    onContinuePack = { _ ->
                        vm.onContinuePack()
                    },
                    onNavigateToSession = {
                        // Pop the summary screen; QuizContent is already on the back-stack
                        // and the shared ViewModel (parentEntry scope) already holds the
                        // new session plan — so it will render the first question immediately.
                        navController.popBackStack(
                            route = SynapseScreen.QuizContent.route,
                            inclusive = false,
                        )
                    },
                    onGoToLibrary = {
                        navController.navigate(SynapseScreen.Library.route) {
                            popUpTo("synapse/main") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        // ── AddPdf — modal slide-up ─────────────────────────────────────
        composable(
            route = SynapseScreen.AddPdf.route,
            arguments = listOf(
                navArgument(SynapseScreen.AddPdf.ARG_SOURCE) {
                    type = NavType.StringType
                    defaultValue = SynapseScreen.AddPdf.SOURCE_FILE
                },
                navArgument(SynapseScreen.AddPdf.ARG_URI) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = NavTransitions.verticalEnter(),
            exitTransition = NavTransitions.verticalExit(),
            popEnterTransition = NavTransitions.verticalPopEnter(),
            popExitTransition = NavTransitions.verticalPopExit(),
        ) {
            AddPdfScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSession = { packId ->
                    navController.navigate(SynapseScreen.Quiz.createRoute(packId))
                },
                onNavigateToExport = { packId ->
                    navController.navigate(SynapseScreen.Export.createRoute(packId))
                },
                onNavigateToSummaryConfig = { type, name, content, config ->
                    navController.navigate(
                        SynapseScreen.SummaryGenerator.createRoute(
                            type = type,
                            name = name,
                            content = content,
                            depth = config.depth.name,
                            focus = config.focus.joinToString(","),
                            language = config.language,
                            readAloud = config.readAloud
                        )
                    )
                },
                onNavigateToPremium = {
                    rootViewModel.showPaywall()
                },
                onNavigateToDashboard = {
                    navController.popBackStack()
                    navController.navigate(SynapseScreen.Dashboard.route)
                },
            )
        }


        composable(route = SynapseScreen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ── Overview — pack overview with vertical motion ────────────────────────
        composable(
            route = SynapseScreen.Overview.route,
            arguments = listOf(
                navArgument(SynapseScreen.Overview.ARG_PACK_ID) { type = NavType.LongType },
            ),
            enterTransition = NavTransitions.verticalEnter(),
            exitTransition = NavTransitions.verticalExit(),
            popEnterTransition = NavTransitions.verticalPopEnter(),
            popExitTransition = NavTransitions.verticalPopExit(),
        ) {
            PackOverviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToExport = { packId ->
                    navController.navigate(SynapseScreen.Export.createRoute(packId))
                },
            )
        }

        // ── Export — PDF builder wizard ──────────────────────────────────────────
        composable(
            route = SynapseScreen.Export.route,
            arguments = listOf(
                navArgument(SynapseScreen.Export.ARG_PACK_ID) { type = NavType.LongType },
            ),
            enterTransition = NavTransitions.verticalEnter(),
            exitTransition = NavTransitions.verticalExit(),
            popEnterTransition = NavTransitions.verticalPopEnter(),
            popExitTransition = NavTransitions.verticalPopExit(),
        ) {
            ExportScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { rootViewModel.showPaywall() },
            )
        }
        // ── Summary Flow ────────────────────────────────────────────────────────
        composable(
            route = SynapseScreen.SummaryGenerator.route,
            arguments = listOf(
                navArgument(SynapseScreen.SummaryGenerator.ARG_SOURCE_TYPE) {
                    type = NavType.StringType
                },
                navArgument(SynapseScreen.SummaryGenerator.ARG_SOURCE_NAME) {
                    type = NavType.StringType
                },
                navArgument(SynapseScreen.SummaryGenerator.ARG_SOURCE_CONTENT) {
                    type = NavType.StringType
                },
                navArgument(SynapseScreen.SummaryGenerator.ARG_DEPTH) { type = NavType.StringType },
                navArgument(SynapseScreen.SummaryGenerator.ARG_FOCUS) { type = NavType.StringType },
                navArgument(SynapseScreen.SummaryGenerator.ARG_LANGUAGE) {
                    type = NavType.StringType
                },
                navArgument(SynapseScreen.SummaryGenerator.ARG_READ_ALOUD) {
                    type = NavType.BoolType
                },
            ),
            enterTransition = NavTransitions.verticalEnter(),
            exitTransition = NavTransitions.verticalExit(),
            popEnterTransition = NavTransitions.verticalPopEnter(),
            popExitTransition = NavTransitions.verticalPopExit(),
        ) { entry ->
            val args = entry.arguments
            val sourceType = args?.getString(SynapseScreen.SummaryGenerator.ARG_SOURCE_TYPE) ?: ""
            val sourceName = args?.getString(SynapseScreen.SummaryGenerator.ARG_SOURCE_NAME) ?: ""
            val sourceContent =
                args?.getString(SynapseScreen.SummaryGenerator.ARG_SOURCE_CONTENT) ?: ""
            val depthStr = args?.getString(SynapseScreen.SummaryGenerator.ARG_DEPTH) ?: "STANDARD"
            val focusStr = args?.getString(SynapseScreen.SummaryGenerator.ARG_FOCUS) ?: ""
            val language = args?.getString(SynapseScreen.SummaryGenerator.ARG_LANGUAGE) ?: "auto"
            val readAloud = args?.getBoolean(SynapseScreen.SummaryGenerator.ARG_READ_ALOUD) ?: true

            val vm: SummaryViewModel = hiltViewModel()

            // 1. Initialize config once — separate from effect collection
            LaunchedEffect(Unit) {
                val config = SummaryConfig(
                    depth = SummaryDepth.valueOf(
                        depthStr
                    ),
                    focus = focusStr.split(",").filter { it.isNotBlank() }.toSet(),
                    language = language,
                    readAloud = readAloud
                )
                vm.onEvent(
                    SummaryEvent.ConfigChanged(
                        config
                    )
                )
            }

            // 2. Collect effects in a separate coroutine so it never blocks setup
            LaunchedEffect(vm) {
                vm.effect.collect { effect ->
                    when (effect) {
                        is SummaryEffect.NavigateToSummary -> {
                            navController.navigate(SynapseScreen.SummaryViewer.createRoute(effect.id)) {
                                popUpTo(SynapseScreen.SummaryGenerator.route) { inclusive = true }
                            }
                        }

                        is SummaryEffect.NavigateBack -> {
                            navController.popBackStack()
                        }
                        else -> {}
                    }
                }
            }

            SummaryGeneratorScreen(
                sourceType = sourceType,
                sourceContent = sourceContent,
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = SynapseScreen.SummaryViewer.route,
            arguments = listOf(
                navArgument(SynapseScreen.SummaryViewer.ARG_SUMMARY_ID) {
                    type = NavType.LongType
                },
            ),
            enterTransition = NavTransitions.verticalEnter(),
            exitTransition = NavTransitions.verticalExit(),
            popEnterTransition = NavTransitions.verticalPopEnter(),
            popExitTransition = NavTransitions.verticalPopExit(),
        ) { entry ->
            val summaryId = entry.arguments?.getLong(SynapseScreen.SummaryViewer.ARG_SUMMARY_ID)
                ?: return@composable
            val vm: SummaryViewModel = hiltViewModel()

            LaunchedEffect(summaryId) {
                vm.onEvent(
                    SummaryEvent.LoadSummary(
                        summaryId
                    )
                )
            }

            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(vm) {
                vm.effect.collect { effect ->
                    when (effect) {
                        is SummaryEffect.NavigateBack -> {
                            navController.popBackStack()
                        }

                        else -> {}
                    }
                }
            }

            if (state.summary != null) {
                SummaryScreen(
                    summary = state.summary!!,
                    audioState = state.audioState,
                    readAloudState = state.readAloudState,
                    activeSentenceIdProvider = { state.activeSentenceId },
                    resumeFromMs = state.resumeFromMs,
                    onBack = { vm.onEvent(SummaryEvent.BackClicked) },
                    onShare = { vm.onEvent(SummaryEvent.ShareClicked) },
                    onInitiateReadAloud = { vm.onEvent(SummaryEvent.InitiateReadAloud) },
                    onPlayPause = { vm.onEvent(SummaryEvent.PlayPauseAudio) },
                    onSeekTo = { pos -> vm.onEvent(SummaryEvent.SeekAudio(pos)) },
                    onSeekBack = { vm.onEvent(SummaryEvent.SeekAudioBack) },
                    onSeekForward = { vm.onEvent(SummaryEvent.SeekAudioForward) },
                    onSpeedChange = { speed ->
                        vm.onEvent(
                            SummaryEvent.ChangeAudioSpeed(
                                speed
                            )
                        )
                    },
                    onRestart = { vm.onEvent(SummaryEvent.RestartAudio) },
                    onAcceptResume = { vm.onEvent(SummaryEvent.AcceptResumeAudio) },
                    onDismissResume = { vm.onEvent(SummaryEvent.DismissResumeAudio) },
                )
            } else {
                // Summary is loading from Room — show spinner
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
        }
    }
}

private fun NavGraphBuilder.tabComposable(
    route: String,
    content: @Composable (NavBackStackEntry) -> Unit,
) = composable(
    route = route,
    enterTransition = NavTransitions.fadeThroughEnter(),
    exitTransition = NavTransitions.fadeThroughExit(),
    popEnterTransition = NavTransitions.fadeThroughEnter(),
    popExitTransition = NavTransitions.fadeThroughExit(),
    content = { content(it) },
)