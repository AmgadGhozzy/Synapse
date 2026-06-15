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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.core.audio.SoundManager
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.audio.LocalSoundManager
import io.synapse.ai.core.ui.components.SynapseTopBar
import io.synapse.ai.features.premium.presentation.screen.SynapsePremiumScreen
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

@Composable
fun SynapseApp(
    soundManager: SoundManager,
    appState: AppState = rememberSynapseAppState(),
    rootViewModel: RootViewModel,
    profileViewModel: ProfileViewModel,
) {
    val onboardingDone by rootViewModel.onboardingDone.collectAsStateWithLifecycle()
    val subtitleOverride by rootViewModel.subtitleOverride.collectAsStateWithLifecycle()
    val sharedUri by rootViewModel.sharedUri.collectAsStateWithLifecycle()

    LaunchedEffect(sharedUri, onboardingDone) {
        if (onboardingDone && sharedUri != null) {
            val uri = sharedUri!!
            rootViewModel.consumeSharedUri()
            appState.navController.navigate(SynapseScreen.AddPdf.createRoute(SynapseScreen.AddPdf.SOURCE_FILE, uri))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SynapseShell(
    appState: AppState,
    onboardingDone: Boolean,
    subtitleOverride: SubtitleOverrideState,
    rootViewModel: RootViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val barConfig: BarConfig = appState.barConfig
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val paywallVisible by rootViewModel.paywallVisible.collectAsStateWithLifecycle()

    val routeReady = appState.currentRoute != null

    // Bottom-sheet state — skipPartiallyExpanded so it always opens fully
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier            = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(innerPadding)
                .background(MaterialTheme.synapse.gradients.page)
        ) {
            SynapseNavGraph(
                navController  = appState.navController,
                onboardingDone = onboardingDone,
                rootViewModel  = rootViewModel,
                modifier       = Modifier,
            )

            // Bottom nav
            AnimatedVisibility(
                visible  = routeReady && barConfig.showBottomBar,
                enter    = BarEnter,
                exit     = BarExit,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                BottomBar(
                    items         = SynapseNavigationItems.visibleItems,
                    navController = appState.navController,
                    currentRoute  = appState.currentRoute,
                )
            }

            // Top bar
            AnimatedVisibility(
                visible = routeReady && barConfig.showTopBar,
                enter   = BarEnter,
                exit    = BarExit,
            ) {
                var topBarHeightPx by remember { mutableIntStateOf(0) }
                val density = LocalDensity.current

                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(density) { topBarHeightPx.toDp() })
                            .matchParentSize()
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
                        title            = appState.currentScreen.titleRes
                            .takeIf { it != 0 }
                            ?.let { stringResource(it) }
                            ?: "",
                        subtitle         = when (subtitleOverride) {
                            is SubtitleOverrideState.Resource -> stringResource(subtitleOverride.resId)
                            SubtitleOverrideState.Default     -> appState.currentScreen.subtitleRes
                                .takeIf { it != 0 }
                                ?.let { stringResource(it) }
                                ?: ""
                        },
                        profileAvatarUrl = profileState.avatarUrl,
                        userInitial      = profileState.avatarInitial.toString(),
                        isPremium        = profileState.isPremium,
                        onProfileClick   = {
                            appState.navController.navigate(SynapseScreen.Profile.createRoute(reopenPaywall = false)) {
                                launchSingleTop = true
                            }
                        },
                        onPremiumClick   = { rootViewModel.showPaywall() },
                        modifier         = Modifier.onSizeChanged { topBarHeightPx = it.height }
                    )
                }
            }
            if (paywallVisible) {
                ModalBottomSheet(
                    onDismissRequest = rootViewModel::hidePaywall,
                    sheetState       = sheetState,
                    dragHandle       = null,
                    containerColor   = MaterialTheme.colorScheme.background,
                ) {
                    SynapsePremiumScreen(
                        onDismiss           = rootViewModel::hidePaywall,
                        onPurchaseSuccess   = { rootViewModel.hidePaywall() },
                        onNavigateToProfile = {
                            rootViewModel.hidePaywall()
                            appState.navController.navigate(SynapseScreen.Profile.createRoute(reopenPaywall = true)) {
                                launchSingleTop = true
                            }
                        },
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
    skuId: String? = null,
) {
    val baseUrl = "https://play.google.com/store/account/subscriptions"
    val url = buildString {
        append(baseUrl)
        append("?package=${context.packageName}")
        if (!skuId.isNullOrBlank()) append("&sku=$skuId")
    }
    context.startActivity(
        Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}
