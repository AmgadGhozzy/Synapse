package com.venom.synapse.features.onboarding.presentation.screen

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.Radius
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.ui.components.GoogleSignInButton
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.features.onboarding.presentation.viewmodel.OnboardingEvent
import com.venom.synapse.features.onboarding.presentation.viewmodel.OnboardingStepData
import com.venom.synapse.features.onboarding.presentation.viewmodel.OnboardingUiState
import com.venom.synapse.features.onboarding.presentation.viewmodel.OnboardingViewModel
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

// ─── Animation constants — match React: [0.25, 0.46, 0.45, 0.94] ─────────────

private val StepEasing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)

// ─── Entry point (stateful) ───────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.Complete -> onComplete()
                is OnboardingEvent.ShowError -> snackbarController.error(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0,0,0,0),
        snackbarHost = { snackbarController.SnackbarHost() },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        OnboardingContent(
            uiState        = uiState,
            onNext         = viewModel::onNext,
            onSkip         = viewModel::onSkip,
            onGetStarted   = viewModel::onGetStarted,
            onGoogleSignIn = viewModel::onGoogleSignIn,
            modifier       = Modifier.padding(innerPadding),
        )
    }
}

// ─── Stateless shell (previewable) ───────────────────────────────────────────

@Composable
internal fun OnboardingContent(
    uiState: OnboardingUiState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onGetStarted: () -> Unit,
    onGoogleSignIn: (android.content.Context) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.steps.isEmpty()) return

    val accentColor = stepAccentColor(uiState.currentStep)

    Surface(
        modifier = modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            // Skip ─────────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end    = Spacing.ScreenHorizontalPadding,
                        top    = Spacing.Spacing16,
                        bottom = Spacing.Spacing8,
                    ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                SkipButton(onSkip = onSkip)
            }

            // Illustration + text — cross-fades on each step ──────────────────
            Box(
                modifier         = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState    = uiState.currentStep,
                    transitionSpec = {
                        // React: initial={opacity:0, scale:0.88, y:20} animate={1,1,0}
                        val enter = fadeIn(tween(400, easing = StepEasing)) +
                                scaleIn(tween(400, easing = StepEasing), initialScale = 0.88f) +
                                slideInVertically(tween(400, easing = StepEasing)) { it / 5 }
                        // React: exit={opacity:0, scale:0.92, y:-20}
                        val exit  = fadeOut(tween(280, easing = StepEasing)) +
                                scaleOut(tween(280, easing = StepEasing), targetScale = 0.92f) +
                                slideOutVertically(tween(280, easing = StepEasing)) { -it / 5 }
                        enter togetherWith exit
                    },
                    label = "onboarding_step",
                ) { stepIndex ->
                    val step = uiState.steps.getOrNull(stepIndex) ?: return@AnimatedContent
                    StepContent(
                        step        = step,
                        accentColor = accentColor,
                    )
                }
            }

            // Bottom controls — static, outside animation ─────────────────────
            BottomControls(
                totalSteps     = uiState.steps.size,
                currentStep    = uiState.currentStep,
                isAnonymous    = uiState.isAnonymous,
                isLastStep     = uiState.isLastStep,
                onNext         = onNext,
                onGetStarted   = onGetStarted,
                onGoogleSignIn = onGoogleSignIn,
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.ScreenHorizontalPadding)
                    .padding(bottom = Spacing.Spacing32),
            )
        }
    }
}

// ─── Step content (per-slide) ────────────────────────────────────────────────

@Composable
private fun StepContent(
    step: OnboardingStepData,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.ScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 220dp glowing circle with inner radial glow + illustration ──────────
        IllustrationCircle(
            illustrationRes = step.illustrationRes,
            accentColor     = accentColor,
        )

        Spacer(Modifier.height(Spacing.Spacing24))

        // Colored step label pill ─────────────────────────────────────────────
        StepLabelPill(
            label       = step.label,
            accentColor = accentColor,
        )

        Spacer(Modifier.height(Spacing.Spacing16))

        // Title — 26sp ExtraBold, −0.02em letter-spacing ─────────────────────
        Text(
            text      = step.title,
            style     = MaterialTheme.typography.headlineMedium.copy(
                fontWeight    = FontWeight.ExtraBold,
                fontSize      = 26.asp,
                lineHeight    = 33.asp,
                letterSpacing = (-0.5).asp, // ≈ −0.02em at 26sp
            ),
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.Spacing12))

        // Subtitle — 14sp, 1.65 line-height, max 290dp ────────────────────────
        Text(
            text      = step.subtitle,
            style     = MaterialTheme.typography.bodyMedium.copy(
                fontSize   = 14.asp,
                lineHeight = 23.asp,
            ),
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.widthIn(max = 290.adp),
        )
    }
}

// ─── Illustration circle ──────────────────────────────────────────────────────

/**
 * 220dp glowing circle card — exact match of React reference:
 *
 * ```js
 * boxShadow: `0 20px 60px ${colors.primaryLight}, 0 0 0 1px ${colors.border}`
 * border: `1.5px solid ${colors.border}`
 * background: radial-gradient(circle, ${current.color}20 0%, transparent 70%)
 * ```
 *
 * Compose equivalent:
 *   • `Modifier.shadow()` with accent-tinted `ambientColor` + `spotColor`
 *   • `Modifier.border()` with `outline.copy(alpha = 0.18f)`
 *   • Inner glow `Box` at 180dp with `accentColor.copy(alpha = 0.14f)`
 *   • Illustration `Image` above the glow
 *
 * Accent color crossfades smoothly via [animateColorAsState] as the step changes.
 */
@Composable
private fun IllustrationCircle(
    @DrawableRes illustrationRes: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val animatedAccent by animateColorAsState(
        targetValue   = accentColor,
        animationSpec = tween(durationMillis = 500),
        label         = "illus_accent",
    )

    Box(
        modifier = modifier
            // Brand-colored ambient glow — replaces CSS box-shadow
            .shadow(
                elevation    = 20.adp,
                shape        = Radius.ShapeCircle,
                ambientColor = animatedAccent.copy(alpha = 0.25f),
                spotColor    = animatedAccent.copy(alpha = 0.20f),
            )
            .size(260.adp)
            .clip(Radius.ShapeCircle)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        // Inner radial glow disc — ${current.color}20 → alpha ≈ 0.13f
        Box(
            modifier = Modifier
                .size(250.adp)
                .background(
                    color  = animatedAccent.copy(alpha = 0.13f),
                    shape  = Radius.ShapeCircle,
                ),
        )

        // Illustration rendered above the glow
        Image(
            painter            = painterResource(illustrationRes),
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier.size(240.adp),
        )
    }
}

// ─── Step label pill ──────────────────────────────────────────────────────────
@Composable
private fun StepLabelPill(
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val animatedAccent by animateColorAsState(
        targetValue   = accentColor,
        animationSpec = tween(durationMillis = 400),
        label         = "label_accent",
    )

    Row(
        modifier = modifier
            .clip(Radius.ShapePill)
            .background(animatedAccent.copy(alpha = 0.10f))
            .border(
                width = 1.adp,
                color = animatedAccent.copy(alpha = 0.22f),
                shape = Radius.ShapePill,
            )
            .padding(horizontal = Spacing.Spacing12, vertical = Spacing.Spacing4),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing6),
    ) {
        Box(
            modifier = Modifier
                .size(6.adp)
                .background(color = animatedAccent, shape = Radius.ShapeCircle),
        )
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.ExtraBold,
                fontSize      = 11.asp,
                letterSpacing = 0.7.asp,
            ),
            color = animatedAccent,
        )
    }
}

// ─── Skip button ──────────────────────────────────────────────────────────────

@Composable
private fun SkipButton(
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text     = stringResource(R.string.onboarding_skip),
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication        = null,
            onClick           = onSkip,
        ),
    )
}

// ─── Bottom controls ──────────────────────────────────────────────────────────

@Composable
private fun BottomControls(
    totalSteps: Int,
    currentStep: Int,
    isAnonymous: Boolean,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onGetStarted: () -> Unit,
    onGoogleSignIn: (android.content.Context) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(Spacing.Spacing24),
    ) {
        StepDots(totalSteps = totalSteps, currentStep = currentStep)

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Spacing16)) {
            val context = LocalContext.current

            if (isLastStep && isAnonymous) {
                GoogleSignInButton(
                    onClick = { onGoogleSignIn(context) },
                    modifier = Modifier.fillMaxWidth().height(56.adp)
                )
            }

            CtaButton(
                labelRes         = if (isLastStep) R.string.onboarding_get_started
                else R.string.onboarding_next,
                showTrailingIcon = !isLastStep,
                onClick          = if (isLastStep) onGetStarted else onNext,
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(56.adp),
            )
        }
    }
}

// ─── Step dots ───────────────────────────────────────────────────────────────
@Composable
private fun StepDots(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    val activeDotWidth: Dp   = 24.adp
    val inactiveDotWidth: Dp = 8.adp
    val dotHeight: Dp        = 8.adp

    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep

            val dotWidth: Dp by animateDpAsState(
                targetValue   = if (isActive) activeDotWidth else inactiveDotWidth,
                animationSpec = spring(
                    stiffness    = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                ),
                label = "dot_w_$index",
            )

            val dotAlpha by animateFloatAsState(
                targetValue   = if (isActive) 1f else 0.30f,
                animationSpec = tween(durationMillis = 300),
                label         = "dot_a_$index",
            )

            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(dotHeight)
                    .graphicsLayer { alpha = dotAlpha }
                    .clip(Radius.ShapePill)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

// ─── CTA button ───────────────────────────────────────────────────────────────
@Composable
private fun CtaButton(
    @StringRes labelRes: Int,
    showTrailingIcon: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shadowToken = if (isDark) MaterialTheme.synapse.components.primaryButton.ShadowDark else MaterialTheme.synapse.components.primaryButton.ShadowLight
    Box(
        modifier = modifier
            .shadow(
                elevation    = shadowToken.elevation,
                shape        = Radius.ShapeXXL,
                ambientColor = shadowToken.color,
                spotColor    = shadowToken.color,
            )
            .clip(Radius.ShapeXXL)
            .background(MaterialTheme.synapse.gradients.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing6),
        ) {
            Text(
                text  = stringResource(labelRes),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 16.asp,
                ),
                color = Color.White,
            )
            if (showTrailingIcon) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(18.adp),
                )
            }
        }
    }
}


@Composable
private fun stepAccentColor(stepIndex: Int): Color = when (stepIndex) {
    0    -> MaterialTheme.colorScheme.primary
    1    -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.tertiary
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewSteps = listOf(
    OnboardingStepData(
        index              = 0,
        label              = "Upload",
        title              = "Turn any PDF into\na study engine",
        subtitle           = "Import documents, textbooks, or lecture notes. Synapse reads them all.",
        illustrationRes    = R.drawable.ic_onboarding_upload,
    ),
    OnboardingStepData(
        index              = 1,
        label              = "Generate",
        title              = "AI crafts perfect\nquestions for you",
        subtitle            = "Our engine extracts key concepts and generates MCQs, flashcards, and more.",
        illustrationRes = R.drawable.ic_onboarding_generate,
    ),
    OnboardingStepData(
        index                 = 2,
        label           = "Master",
        title           = "SRS keeps your\nknowledge sharp",
        subtitle        = "Our spaced repetition algorithm schedules reviews right when you need them.",
        illustrationRes = R.drawable.ic_onboarding_master,
    ),
)

@Preview(name = "Step 1 Upload — Light", showBackground = true)
@Preview(name = "Step 1 Upload — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OnboardingStep1Preview() {
    SynapseTheme {
        OnboardingContent(
            uiState        = OnboardingUiState(currentStep = 0, steps = previewSteps),
            onNext         = {},
            onSkip         = {},
            onGetStarted   = {},
            onGoogleSignIn = {},
        )
    }
}

@Preview(name = "Step 2 Generate — Light", showBackground = true)
@Preview(name = "Step 2 Generate — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OnboardingStep2Preview() {
    SynapseTheme {
        OnboardingContent(
            uiState        = OnboardingUiState(currentStep = 1, steps = previewSteps),
            onNext         = {},
            onSkip         = {},
            onGetStarted   = {},
            onGoogleSignIn = {},
        )
    }
}

@Preview(name = "Step 3 Master — Light", showBackground = true)
@Preview(name = "Step 3 Master — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OnboardingStep3Preview() {
    SynapseTheme {
        OnboardingContent(
            uiState        = OnboardingUiState(currentStep = 2, steps = previewSteps),
            onNext         = {},
            onSkip         = {},
            onGetStarted   = {},
            onGoogleSignIn = {},
        )
    }
}