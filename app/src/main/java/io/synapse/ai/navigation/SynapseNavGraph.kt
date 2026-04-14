package io.synapse.ai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import io.synapse.ai.features.add_pdf.presentation.screen.AddPdfScreen
import io.synapse.ai.features.dashboard.presentation.screen.DashboardScreen
import io.synapse.ai.features.dashboard.presentation.viewmodel.DashboardViewModel
import io.synapse.ai.features.library.presentation.screen.LibraryScreen
import io.synapse.ai.features.onboarding.presentation.screen.OnboardingScreen
import io.synapse.ai.features.premium.presentation.screen.SynapsePremiumScreen
import io.synapse.ai.features.profile.presentation.components.AboutScreen
import io.synapse.ai.features.profile.presentation.screen.ProfileScreen
import io.synapse.ai.features.session.presentation.screen.QuizScreen
import io.synapse.ai.features.session.presentation.screen.SessionSummaryScreen
import io.synapse.ai.features.session.presentation.viewmodel.SessionViewModel
import io.synapse.ai.features.stats.presentation.screen.StatsScreen
import io.synapse.ai.navigation.core.AnimatedNavHost
import io.synapse.ai.navigation.core.NavTransitions
import io.synapse.ai.ui.viewmodel.RootViewModel

@Composable
fun SynapseNavGraph(
    navController: NavHostController,
    onboardingDone: Boolean,
    rootViewModel: RootViewModel,
    modifier: Modifier = Modifier,
) {
    val startDestination = if (onboardingDone) "synapse/main" else SynapseScreen.Onboarding.route

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
                    navController.navigate(SynapseScreen.Quiz.createDemoRoute()) {
                        popUpTo(SynapseScreen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        // ── Main nested graph ───────────────────────────────────────────
        navigation(
            route = "synapse/main",
            startDestination = SynapseScreen.Dashboard.route,
        ) {
            tabComposable(SynapseScreen.Dashboard.route) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry("synapse/main")
                }
                val vm: DashboardViewModel = hiltViewModel(parentEntry)
                DashboardScreen(
                    viewModel = vm,
                    rootViewModel = rootViewModel,
                    onNavigate = { navController.navigate(it) },
                )
            }

            tabComposable(SynapseScreen.Library.route) {
                LibraryScreen(onNavigate = { navController.navigate(it) })
            }

            tabComposable(SynapseScreen.Stats.route) {
                StatsScreen()
            }

            composable(
                route = SynapseScreen.Profile.route,
                enterTransition = NavTransitions.horizontalEnter(),
                exitTransition = NavTransitions.horizontalExit(),
                popEnterTransition = NavTransitions.horizontalPopEnter(),
                popExitTransition = NavTransitions.horizontalPopExit(),
            ) {
                ProfileScreen(onNavigate = { navController.navigate(it) })
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
                val vm: SessionViewModel = hiltViewModel(parentEntry)

                SessionSummaryScreen(
                    viewModel = vm,
                    onAddPack = {
                        navController.navigate(
                            SynapseScreen.AddPdf.createRoute(SynapseScreen.AddPdf.SOURCE_FILE)
                        ) {
                            popUpTo("synapse/main") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onPasteText = {
                        navController.navigate(
                            SynapseScreen.AddPdf.createRoute(SynapseScreen.AddPdf.SOURCE_TEXT)
                        ) {
                            popUpTo("synapse/main") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGoToDashboard = {
                        navController.navigate(SynapseScreen.Dashboard.route) {
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
                onNavigateToPremium = {
                    navController.popBackStack()
                    navController.navigate(SynapseScreen.Premium.route)
                },
            )
        }

        // ── Premium — modal slide-up ────────────────────────────────────
        composable(
            route = SynapseScreen.Premium.route,
            enterTransition = NavTransitions.verticalEnter(),
            exitTransition = NavTransitions.verticalExit(),
            popEnterTransition = NavTransitions.verticalPopEnter(),
            popExitTransition = NavTransitions.verticalPopExit(),
        ) {
            SynapsePremiumScreen(
                onDismiss = { navController.popBackStack() },
                onPurchaseSuccess = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate(SynapseScreen.Profile.route) },
            )
        }

        // About Libraries
        composable(
            route = SynapseScreen.About.route,
            enterTransition = NavTransitions.horizontalEnter(),
            exitTransition = NavTransitions.horizontalExit(),
            popEnterTransition = NavTransitions.horizontalPopEnter(),
            popExitTransition = NavTransitions.horizontalPopExit(),
        ) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
            )
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