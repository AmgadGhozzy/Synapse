package io.synapse.ai.ui

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.core.framework.audio.SoundManager
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.audio.LocalSoundManager
import io.synapse.ai.core.ui.components.SynapseTopBar
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.features.profile.presentation.viewmodel.ProfileViewModel
import io.synapse.ai.navigation.AppState
import io.synapse.ai.navigation.BarConfig
import io.synapse.ai.navigation.SynapseNavGraph
import io.synapse.ai.navigation.SynapseNavigationItems
import io.synapse.ai.navigation.SynapseScreen
import io.synapse.ai.navigation.core.BottomBar
import io.synapse.ai.navigation.core.MotionTokens
import io.synapse.ai.navigation.rememberSynapseAppState
import io.synapse.ai.ui.viewmodel.RootViewModel
import io.synapse.ai.ui.viewmodel.SubtitleOverrideState

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun SynapseApp(
    soundManager: SoundManager,
    appState: AppState = rememberSynapseAppState(),
    rootViewModel: RootViewModel,
    profileViewModel: ProfileViewModel,
) {
    val onboardingDone by rootViewModel.onboardingDone.collectAsStateWithLifecycle()
    val subtitleOverride by rootViewModel.subtitleOverride.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        rootViewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate     -> appState.navigateTo(effect.route)
                is UiEffect.NavigateBack -> appState.navigateBack()
                else                     -> Unit
            }
        }
    }

    CompositionLocalProvider(LocalSoundManager provides soundManager) {
        SynapseShell(
            appState         = appState,
            onboardingDone   = onboardingDone,
            subtitleOverride = subtitleOverride,
            rootViewModel    = rootViewModel,
            profileViewModel = profileViewModel,
        )
    }
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
    appState        : AppState,
    onboardingDone  : Boolean,
    subtitleOverride: SubtitleOverrideState,
    rootViewModel   : RootViewModel   = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val barConfig: BarConfig = appState.barConfig
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier            = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.synapse.gradients.page)
                .padding(innerPadding)
                //.padding(bottom = 18.adp)
        ) {
            SynapseNavGraph(
                navController  = appState.navController,
                onboardingDone = onboardingDone,
                rootViewModel  = rootViewModel,
            )

            // Bottom nav
            AnimatedVisibility(
                visible  = barConfig.showBottomBar,
                enter    = BarEnter,
                exit     = BarExit,
                modifier = Modifier
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter),
            ) {
                BottomBar(
                    items        = SynapseNavigationItems.visibleItems,
                    navController = appState.navController,
                    currentRoute = appState.currentRoute,
                )
            }

            // Top bar overlaid on content with gradient backing
            AnimatedVisibility(
                visible = barConfig.showTopBar,
                enter   = BarEnter,
                exit    = BarExit,
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
                                        0.0f  to MaterialTheme.colorScheme.background,
                                        0.55f to MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                        1.0f  to Color.Transparent,
                                    )
                                )
                            ),
                    )

                    SynapseTopBar(
                        title    = stringResource(appState.currentScreen.titleRes),
                        subtitle = when (subtitleOverride) {
                            is SubtitleOverrideState.Resource -> stringResource(subtitleOverride.resId)
                            SubtitleOverrideState.Default -> appState.currentScreen.subtitleRes
                                .takeIf { it != 0 }
                                ?.let { stringResource(it) }
                                ?: ""
                        },
                        profileAvatarUrl          = profileState.avatarUrl,
                        userInitial               = profileState.avatarInitial.toString(),
                        isPremium                 = profileState.isPremium,
                        onProfileClick = {
                            appState.navController.navigate(SynapseScreen.Profile.route) {
                                launchSingleTop = true
                            }
                        },
                        onPremiumClick = {
                            appState.navController.navigate(SynapseScreen.Premium.route) {
                                launchSingleTop = true
                            }
                        },
                        modifier                  = Modifier.statusBarsPadding()//.padding(top = 48.adp),
                    )
                }
            }
        }
    }
}

/**
 * Deep-links the user to Google Play's subscription management page for this app.
 * @param skuId  Optional Play product ID. When provided, opens the specific
 *               plan page instead of the full subscriptions list.
 */
internal fun openSubscriptionManagement(
    context: Context,
    skuId  : String? = null,
) {
    val baseUrl = "https://play.google.com/store/account/subscriptions"
    val url = buildString {
        append(baseUrl)
        append("?package=${context.packageName}")
        if (!skuId.isNullOrBlank()) append("&sku=$skuId")
    }
    context.startActivity(
        Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            // Prefer the Play Store app; fall back to browser automatically.
            setPackage("com.android.vending")
            // If Play Store is not installed, Android strips setPackage and
            // the Intent resolves to a browser — no crash.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}
