package com.venom.synapse.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.core.ui.components.SynapseTopBar
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.features.profile.presentation.viewmodel.ProfileViewModel
import com.venom.synapse.navigation.AppState
import com.venom.synapse.navigation.BarConfig
import com.venom.synapse.navigation.SynapseNavGraph
import com.venom.synapse.navigation.SynapseNavigationItems
import com.venom.synapse.navigation.SynapseScreen
import com.venom.synapse.navigation.rememberSynapseAppState
import com.venom.synapse.ui.viewmodel.RootViewModel
import com.venom.ui.components.common.adp
import com.venom.ui.navigation.BottomBar
import com.venom.ui.navigation.MotionTokens

// ── Root composable ───────────────────────────────────────────────────────────
@Composable
fun SynapseApp(
    appState: AppState = rememberSynapseAppState(),
    rootViewModel: RootViewModel,
    profileViewModel: ProfileViewModel,
) {
    val onboardingDone by rootViewModel.onboardingDone.collectAsStateWithLifecycle()
    val subtitleOverride by rootViewModel.subtitleResOverride.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        rootViewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> appState.navigateTo(effect.route)
                is UiEffect.NavigateBack -> appState.navigateBack()
                else -> Unit
            }
        }
    }

    SynapseShell(
        appState = appState,
        onboardingDone = onboardingDone,
        subtitleOverride = subtitleOverride,
        rootViewModel = rootViewModel,
        profileViewModel = profileViewModel,
    )
}

// ── Shell ─────────────────────────────────────────────────────────────────────
private val BarEnter =
    slideInVertically(tween(280, easing = MotionTokens.EmphasizedDecelerate)) { it } +
            fadeIn(tween(230, easing = MotionTokens.StandardDecelerate))

private val BarExit =
    slideOutVertically(tween(220, easing = MotionTokens.EmphasizedAccelerate)) { it } +
            fadeOut(tween(180, easing = MotionTokens.StandardAccelerate))

@Composable
private fun SynapseShell(
    appState: AppState,
    onboardingDone: Boolean,
    subtitleOverride: Int?,
    rootViewModel: RootViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val barConfig: BarConfig = appState.barConfig
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SynapseNavGraph(
                navController = appState.navController,
                onboardingDone = onboardingDone,
                rootViewModel = rootViewModel,
            )

            // Bottom nav
            AnimatedVisibility(
                visible = barConfig.showBottomBar,
                enter = BarEnter,
                exit = BarExit,
                modifier = Modifier
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter),
            ) {
                BottomBar(
                    items = SynapseNavigationItems.visibleItems,
                    navController = appState.navController,
                    currentRoute = appState.currentRoute,
                )
            }

            // Top bar overlaid on content with gradient backing
            AnimatedVisibility(
                visible = barConfig.showTopBar,
                enter = BarEnter,
                exit = BarExit,
            ) {
                Box {
                    // Gradient scrim behind top bar fades
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.adp)
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to MaterialTheme.colorScheme.background,
                                        0.55f to MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                        1.0f to Color.Transparent,
                                    )
                                )
                            )
                    )

                    SynapseTopBar(
                        title = stringResource(appState.currentScreen.titleRes),
                        subtitle = subtitleOverride
                            ?.let { stringResource(it) }
                            ?: appState.currentScreen.subtitleRes
                                .takeIf { it != 0 }
                                ?.let { stringResource(it) }
                            ?: "",
                        profileAvatarUrl = profileState.avatarUrl,
                        userInitial = profileState.avatarInitial.toString(),
                        onProfileClick = { appState.navigateTo(SynapseScreen.Profile.route) },
                        onPremiumClick = { appState.navigateTo(SynapseScreen.Premium.route) },
                        isPremium = profileState.isPremium,
                        modifier = Modifier.statusBarsPadding(),
                    )
                }
            }
        }
    }
}