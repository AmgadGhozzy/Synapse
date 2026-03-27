package com.venom.synapse.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.venom.synapse.features.add_pdf.presentation.screen.AddPdfScreen
import com.venom.synapse.features.dashboard.presentation.screen.DashboardScreen
import com.venom.synapse.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.venom.synapse.features.library.presentation.screen.LibraryScreen
import com.venom.synapse.features.onboarding.presentation.screen.OnboardingScreen
import com.venom.synapse.features.premium.presentation.screen.SynapsePremiumScreen
import com.venom.synapse.features.premium.presentation.viewmodel.EntitlementViewModel
import com.venom.synapse.features.profile.presentation.screen.ProfileScreen
import com.venom.synapse.features.session.presentation.screen.QuizScreen
import com.venom.synapse.features.session.presentation.screen.SessionSummaryScreen
import com.venom.synapse.features.session.presentation.viewmodel.SessionViewModel
import com.venom.synapse.features.stats.presentation.screen.StatsScreen
import com.venom.synapse.ui.openSubscriptionManagement
import com.venom.synapse.ui.viewmodel.RootViewModel
import com.venom.ui.navigation.AnimatedNavHost
import com.venom.ui.navigation.NavTransitions

@Composable
fun SynapseNavGraph(
    navController  : NavHostController,
    onboardingDone : Boolean,
    rootViewModel  : RootViewModel,
    modifier       : Modifier = Modifier,
) {
    val startDestination = if (onboardingDone) "synapse/main" else SynapseScreen.Onboarding.route

    AnimatedNavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier,
    ) {

        // ── Onboarding ──────────────────────────────────────────────────
        composable(
            route              = SynapseScreen.Onboarding.route,
            enterTransition    = NavTransitions.fadeThroughEnter(),
            exitTransition     = NavTransitions.fadeThroughExit(),
            popEnterTransition = NavTransitions.fadeThroughEnter(),
            popExitTransition  = NavTransitions.fadeThroughExit(),
        ) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate("synapse/main") {
                        popUpTo(SynapseScreen.Onboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        // ── Main nested graph ───────────────────────────────────────────
        navigation(
            route            = "synapse/main",
            startDestination = SynapseScreen.Dashboard.route,
        ) {
            tabComposable(SynapseScreen.Dashboard.route) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry("synapse/main")
                }
                val vm: DashboardViewModel = hiltViewModel(parentEntry)
                DashboardScreen(
                    viewModel     = vm,
                    rootViewModel = rootViewModel,
                    onNavigate    = { navController.navigate(it) },
                )
            }

            tabComposable(SynapseScreen.Library.route) {
                LibraryScreen(onNavigate = { navController.navigate(it) })
            }

            tabComposable(SynapseScreen.Stats.route) {
                StatsScreen()
            }

            composable(
                route              = SynapseScreen.Profile.route,
                enterTransition    = NavTransitions.horizontalEnter(),
                exitTransition     = NavTransitions.horizontalExit(),
                popEnterTransition = NavTransitions.horizontalPopEnter(),
                popExitTransition  = NavTransitions.horizontalPopExit(),
            ) {
                ProfileScreen(onNavigate = { navController.navigate(it) })
            }
        }

        // ── Quiz Flow (Shared ViewModel) ──────────────────────────────
        navigation(
            route            = SynapseScreen.Quiz.route,
            startDestination = "synapse/quiz/content",
            arguments        = listOf(
                navArgument(SynapseScreen.Quiz.ARG_PACK_ID) { type = NavType.LongType },
            ),
        ) {
            composable(
                route              = "synapse/quiz/content",
                enterTransition    = NavTransitions.verticalEnter(),
                exitTransition     = NavTransitions.verticalExit(),
                popEnterTransition = NavTransitions.verticalPopEnter(),
                popExitTransition  = NavTransitions.verticalPopExit(),
            ) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(SynapseScreen.Quiz.route)
                }
                val vm: SessionViewModel = hiltViewModel(parentEntry)
                QuizScreen(
                    viewModel           = vm,
                    onBack              = { navController.popBackStack() },
                    onNavigateToSummary = { navController.navigate(SynapseScreen.SessionSummary.route) },
                )
            }

            composable(
                route              = SynapseScreen.SessionSummary.route,
                enterTransition    = NavTransitions.horizontalEnter(),
                exitTransition     = NavTransitions.horizontalExit(),
                popEnterTransition = NavTransitions.horizontalPopEnter(),
                popExitTransition  = NavTransitions.horizontalPopExit(),
            ) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(SynapseScreen.Quiz.route)
                }
                val vm: SessionViewModel = hiltViewModel(parentEntry)
                SessionSummaryScreen(
                    viewModel = vm,
                    onBack    = {
                        navController.navigate(SynapseScreen.Dashboard.route) {
                            popUpTo(SynapseScreen.Dashboard.route) { inclusive = false }
                        }
                    },
                )
            }
        }

        // ── AddPdf — modal slide-up ─────────────────────────────────────
        composable(
            route              = SynapseScreen.AddPdf.route,
            enterTransition    = NavTransitions.verticalEnter(),
            exitTransition     = NavTransitions.verticalExit(),
            popEnterTransition = NavTransitions.verticalPopEnter(),
            popExitTransition  = NavTransitions.verticalPopExit(),
        ) {
            AddPdfScreen(
                onNavigateBack      = { navController.popBackStack() },
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
            route              = SynapseScreen.Premium.route,
            enterTransition    = NavTransitions.verticalEnter(),
            exitTransition     = NavTransitions.verticalExit(),
            popEnterTransition = NavTransitions.verticalPopEnter(),
            popExitTransition  = NavTransitions.verticalPopExit(),
        ) {
            val entitlementViewModel: EntitlementViewModel = hiltViewModel()
            val entitlement by entitlementViewModel.entitlement.collectAsStateWithLifecycle()
            val context: Context = LocalContext.current

            LaunchedEffect(entitlement) {
                if (entitlement?.isAccessGranted == true) {
                    navController.popBackStack()
                    // TODO: send straight to Play billing
                    openSubscriptionManagement(context, skuId = null)
                }
            }

            SynapsePremiumScreen(
                onDismiss         = { navController.popBackStack() },
                onPurchaseSuccess = { navController.popBackStack() },
            )
        }
    }
}

private fun NavGraphBuilder.tabComposable(
    route  : String,
    content: @Composable (NavBackStackEntry) -> Unit,
) = composable(
    route              = route,
    enterTransition    = NavTransitions.fadeThroughEnter(),
    exitTransition     = NavTransitions.fadeThroughExit(),
    popEnterTransition = NavTransitions.fadeThroughEnter(),
    popExitTransition  = NavTransitions.fadeThroughExit(),
    content            = { content(it) },
)