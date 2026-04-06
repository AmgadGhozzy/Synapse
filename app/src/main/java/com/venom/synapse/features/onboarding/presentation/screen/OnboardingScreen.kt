package com.venom.synapse.features.onboarding.presentation.screen

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.synapse.core.ui.components.GoogleSignInButton
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.features.onboarding.presentation.state.OnboardingEvent
import com.venom.synapse.features.onboarding.presentation.state.OnboardingStepData
import com.venom.synapse.features.onboarding.presentation.state.OnboardingUiState
import com.venom.synapse.features.onboarding.presentation.viewmodel.OnboardingViewModel
import com.venom.ui.components.common.adp

private val StepEasing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.Complete ->
                    onComplete()
                is OnboardingEvent.ShowError ->
                    snackbarController.error(event.message.asString(context))
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { snackbarController.SnackbarHost() },
        containerColor       = Color.Transparent,
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
    onGoogleSignIn: (Context) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.steps.isEmpty()) return

    val accentColor = stepAccentColor(uiState.currentStep)

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
            // Skip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end    = MaterialTheme.synapse.spacing.screen,
                        top    = MaterialTheme.synapse.spacing.s16,
                        bottom = MaterialTheme.synapse.spacing.s8,
                    ),
                contentAlignment = Alignment.CenterEnd,
            ) {
                SkipButton(onSkip = onSkip)
            }

            // Animated step content
            Box(
                modifier         = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState    = uiState.currentStep,
                    transitionSpec = {
                        val enter = fadeIn(tween(400, easing = StepEasing)) +
                                scaleIn(tween(400, easing = StepEasing), initialScale = 0.88f) +
                                slideInVertically(tween(400, easing = StepEasing)) { it / 5 }
                        val exit  = fadeOut(tween(280, easing = StepEasing)) +
                                scaleOut(tween(280, easing = StepEasing), targetScale = 0.92f) +
                                slideOutVertically(tween(280, easing = StepEasing)) { -it / 5 }
                        enter togetherWith exit
                    }
                ) { stepIndex ->
                    val step = uiState.steps.getOrNull(stepIndex) ?: return@AnimatedContent
                    StepContent(step = step, accentColor = accentColor)
                }
            }

            // Bottom controls — static, outside animation ─────────────────────
            BottomControls(
                totalSteps     = uiState.steps.size,
                currentStep    = uiState.currentStep,
                isLastStep     = uiState.isLastStep,
                onNext         = onNext,
                onGetStarted   = onGetStarted,
                onGoogleSignIn = onGoogleSignIn,
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.synapse.spacing.screen)
                    .padding(bottom = MaterialTheme.synapse.spacing.s32),
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
            .padding(horizontal = MaterialTheme.synapse.spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IllustrationCircle(
            illustrationRes = step.illustrationRes,
            accentColor     = accentColor,
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))

        StepLabelPill(
            label       = stringResource(step.labelRes),
            accentColor = accentColor,
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))

        Text(
            text       = stringResource(step.titleRes),
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color      = MaterialTheme.colorScheme.onBackground,
            textAlign  = TextAlign.Center,
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))

        Text(
            text      = stringResource(step.subtitleRes),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.widthIn(max = 290.adp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IllustrationCircle(
    @DrawableRes illustrationRes: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val animatedAccent by animateColorAsState(
        targetValue   = accentColor,
        animationSpec = tween(500)
    )

    val infiniteTransition = rememberInfiniteTransition()

    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.22f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        )
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.40f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        )
    )

    val floatPx by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -10f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        )
    )

    val shape = MaterialShapes.Pentagon.toShape()

    Box(
        modifier         = modifier.size(260.adp),
        contentAlignment = Alignment.Center,
    ) {
        // pulsing outer ring
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha  = pulseAlpha
                }
                .border(
                    width = 1.5.adp,
                    color = animatedAccent,
                    shape = shape,
                ),
        )

        // backdrop circle with colored shadow + inner glow
        Box(
            modifier = Modifier
                .dropShadow(
                    shape = shape,
                    shadow = MaterialTheme.synapse.shadows.medium.toShadow(animatedAccent)
                )
                .size(240.adp)
                .clip(MaterialShapes.Pentagon.toShape())
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(210.adp)
                    .background(
                        color = animatedAccent.copy(alpha = 0.13f),
                        shape = shape,
                    ),
            )
        }

        // illustration with float
        Image(
            painter            = painterResource(illustrationRes),
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .size(200.adp)
                .graphicsLayer { translationY = floatPx },
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
        animationSpec = tween(400)
    )

    Row(
        modifier = modifier
            .clip(MaterialTheme.synapse.radius.pill)
            .background(animatedAccent.copy(alpha = 0.10f))
            .border(
                width = 1.adp,
                color = animatedAccent.copy(alpha = 0.22f),
                shape = MaterialTheme.synapse.radius.pill,
            )
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s12,
                vertical   = MaterialTheme.synapse.spacing.s4,
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {
        Box(
            modifier = Modifier
                .size(6.adp)
                .background(color = animatedAccent, shape = CircleShape),
        )
        Text(
            text       = label.uppercase(),
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color      = animatedAccent,
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
        style    = MaterialTheme.typography.titleSmall,
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
    isLastStep: Boolean,
    onNext: () -> Unit,
    onGetStarted: () -> Unit,
    onGoogleSignIn: (Context) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s24),
    ) {
        StepDots(totalSteps = totalSteps, currentStep = currentStep)

        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s16)) {
            val context = LocalContext.current

            if (isLastStep) {
                GoogleSignInButton(
                    onClick  = { onGoogleSignIn(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.adp),
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
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep

            val dotWidth by animateDpAsState(
                targetValue   = if (isActive) activeDotWidth else inactiveDotWidth,
                animationSpec = spring(
                    stiffness    = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                )
            )
            val dotAlpha by animateFloatAsState(
                targetValue   = if (isActive) 1f else 0.30f,
                animationSpec = tween(300)
            )

            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(dotHeight)
                    .graphicsLayer { alpha = dotAlpha }
                    .clip(MaterialTheme.synapse.radius.pill)
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
    )

    val shadowToken   = MaterialTheme.synapse.shadows.strong

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .dropShadow(
                shape = MaterialTheme.synapse.radius.lg,
                shadow = shadowToken.toShadow()
            )
            .clip(MaterialTheme.synapse.radius.lg)
            .background(MaterialTheme.synapse.gradients.primary)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
        ) {
            Text(
                text       = stringResource(labelRes),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White.copy(alpha = 0.95f),
            )
            if (showTrailingIcon) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint               = Color.White.copy(alpha = 0.95f),
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

@Preview(name = "Step 1 Upload — Light", showBackground = true)
@Preview(name = "Step 1 Upload — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OnboardingStep1Preview() {
    SynapseTheme {
        OnboardingContent(
            uiState        = OnboardingUiState(currentStep = 0, steps = OnboardingStepData.steps),
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
            uiState        = OnboardingUiState(currentStep = 1, steps = OnboardingStepData.steps),
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
            uiState        = OnboardingUiState(currentStep = 2, steps = OnboardingStepData.steps),
            onNext         = {},
            onSkip         = {},
            onGetStarted   = {},
            onGoogleSignIn = {},
        )
    }
}