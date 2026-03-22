package com.venom.synapse.features.premium.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.LocalGradientTokens
import com.venom.synapse.core.theme.SynapseCardDark
import com.venom.synapse.core.theme.SynapseDeepDark
import com.venom.synapse.core.theme.SynapseLavender
import com.venom.synapse.core.theme.SynapseLightBg
import com.venom.synapse.core.theme.SynapseSurfaceDark
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.White
import com.venom.synapse.core.theme.tokens.Radius
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.theme.tokens.TypeScale
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.domain.model.FeatureColorRole
import com.venom.synapse.features.premium.presentation.state.PremiumEvent
import com.venom.synapse.features.premium.presentation.state.PremiumUiState
import com.venom.synapse.features.premium.presentation.state.ProFeatureUiModel
import com.venom.synapse.ui.viewmodel.PremiumViewModel
import com.venom.ui.components.common.adp

// ══════════════════════════════════════════════════════════════════
// CONNECTED ENTRY POINT (ViewModel-driven)
// ══════════════════════════════════════════════════════════════════

/**
 * ViewModel-connected bottom sheet. Use this at the call site:
 *
 * ```kotlin
 * if (showSheet) {
 *     SynapsePremiumBottomSheet(
 *         onDismiss = { showSheet = false },
 *         onPurchaseSuccess = { planId -> ... },
 *     )
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SynapsePremiumBottomSheet(
    onDismiss: () -> Unit,
    onPurchaseSuccess: (planId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PremiumEvent.PurchaseSuccess -> onPurchaseSuccess(event.planId)
                is PremiumEvent.Dismissed       -> onDismiss()
                is PremiumEvent.PurchaseFailed  -> snackbarController.error(event.reason)
                is PremiumEvent.ShowSnackbar    -> snackbarController.success(event.message)
            }
        }
    }

    when (val state = uiState) {
        is PremiumUiState.Ready -> {
            Box {
                PremiumBottomSheetContent(
                    features = state.features,
                    trialDays = state.trialDays,
                    isPurchasing = state.isPurchasing,
                    onStartTrial = viewModel::startPurchase,
                    onDismiss = viewModel::dismiss,
                    modifier = modifier,
                )
                snackbarController.SnackbarHost()
            }
        }
        else -> Unit // Sheet is not shown while loading or in error state
    }
}

// ══════════════════════════════════════════════════════════════════
// STATELESS BOTTOM SHEET — reusable without ViewModel
// ══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumBottomSheetContent(
    features: List<ProFeatureUiModel>,
    trialDays: Int,
    isPurchasing: Boolean,
    onStartTrial: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val sheetBrush = if (isDark) {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to SynapseCardDark,
                0.55f to SynapseDeepDark,
                1.0f to SynapseSurfaceDark,
            )
        )
    } else {
        Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to SynapseLightBg,
                0.55f to White,
                1.0f to SynapseLavender,
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = Radius.ShapeXXXL.let {
            RoundedCornerShape(
                topStart = 32.adp, topEnd = 32.adp,
            )
        },
        containerColor = Color.Transparent,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f),
            )
        },
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .background(sheetBrush),
        ) {
            // Top glow shimmer line — decorative
            TopGlowAccent()

            // Ambient radial blob at top
            AmbientOrb(
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.14f else 0.08f),
                size = 280.adp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = (-40).adp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Close button row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = Spacing.Spacing4,
                            end = Spacing.Spacing16,
                            bottom = Spacing.Spacing4,
                        ),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    PremiumCloseButton(onClick = onDismiss)
                }

                SheetBody(
                    features = features,
                    trialDays = trialDays,
                    isPurchasing = isPurchasing,
                    onStartTrial = onStartTrial,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// SHEET BODY
// ══════════════════════════════════════════════════════════════════

@Composable
private fun SheetBody(
    features: List<ProFeatureUiModel>,
    trialDays: Int,
    isPurchasing: Boolean,
    onStartTrial: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Spacing24)
            .navigationBarsPadding()
            .padding(bottom = Spacing.Spacing28),
    ) {
        // Animated Gem icon
        AnimatedGemIcon()

        Spacer(Modifier.height(Spacing.Spacing16))

        // Title
        Text(
            text = stringResource(R.string.premium_sheet_title),
            style = TypeScale.TitleXLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.Spacing4))

        // Subtitle
        Text(
            text = stringResource(R.string.premium_sheet_subtitle),
            style = TypeScale.BodySmallRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.Spacing20))

        // Features
        PremiumFeaturesCard(features = features, modifier = Modifier.padding(horizontal = 0.adp))

        Spacer(Modifier.height(Spacing.Spacing12))

        // Price note
        Text(
            text = stringResource(R.string.premium_price_note, trialDays),
            style = TypeScale.LabelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.Spacing12))

        // CTA
        PremiumCtaButton(
            trialDays = trialDays,
            isPurchasing = isPurchasing,
            onClick = onStartTrial,
        )

        Spacer(Modifier.height(Spacing.Spacing4))

        // Skip
        Text(
            text = stringResource(R.string.premium_cta_skip),
            style = TypeScale.BodySmallRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(vertical = Spacing.Spacing10, horizontal = Spacing.Spacing16),
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// ANIMATED GEM ICON
// ══════════════════════════════════════════════════════════════════

@Composable
private fun AnimatedGemIcon(modifier: Modifier = Modifier) {
    val gradients = LocalGradientTokens.current
    val iconTransition = rememberInfiniteTransition(label = "gemAnim")
    val scale by iconTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_750),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gemScale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(top = Spacing.Spacing16)
            .size(64.adp)
            .clip(Radius.ShapeXL)
            .background(gradients.primary)
            .graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_gem), // https://lucide.dev/icons/gem
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(28.adp),
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// DECORATIVE: TOP GLOW ACCENT LINE
// ══════════════════════════════════════════════════════════════════

@Composable
private fun TopGlowAccent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.adp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                        Color.Transparent,
                    )
                )
            ),
    )
}

// ══════════════════════════════════════════════════════════════════
// EXTENSION — luminance (shared with screen file)
// ══════════════════════════════════════════════════════════════════

private fun Color.luminance() = red * 0.2126f + green * 0.7152f + blue * 0.0722f

// ══════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════

@Preview(name = "Premium Sheet · Light", showBackground = true)
@Preview(name = "Premium Sheet · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PremiumSheetBodyPreview() {
    SynapseTheme {
        SheetBody(
            features = listOf(
                ProFeatureUiModel(R.drawable.ic_gem, "Unlimited Hints", "Never get stuck again", FeatureColorRole.TERTIARY),
                ProFeatureUiModel(R.drawable.ic_brain, "AI Step-by-Step Explanations", "Deep understanding, every card", FeatureColorRole.PRIMARY),
                ProFeatureUiModel(R.drawable.ic_layers, "Unlimited Packs", "No caps, create as many as you need", FeatureColorRole.SECONDARY),
                ProFeatureUiModel(R.drawable.ic_file_text, "Large PDF Processing", "Up to 500 pages per upload", FeatureColorRole.ERROR),
                ProFeatureUiModel(R.drawable.ic_globe, "Multi-Language Output", "Generate quizzes in 12 languages", FeatureColorRole.SUCCESS),
            ),
            trialDays = 7,
            isPurchasing = false,
            onStartTrial = {},
            onDismiss = {},
        )
    }
}
