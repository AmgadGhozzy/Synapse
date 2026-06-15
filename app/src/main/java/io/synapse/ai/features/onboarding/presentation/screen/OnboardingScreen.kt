package io.synapse.ai.features.onboarding.presentation.screen

import android.content.Intent
import android.content.res.Configuration
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.components.PrimaryGradientButton
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.features.profile.presentation.components.GoogleSignInButton
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.features.onboarding.presentation.state.OnboardingEvent
import io.synapse.ai.features.onboarding.presentation.state.OnboardingStepData
import io.synapse.ai.features.onboarding.presentation.state.OnboardingUiState
import io.synapse.ai.features.onboarding.presentation.viewmodel.OnboardingViewModel

private val StepEasing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)


private const val TAG_TERMS   = "TERMS"
private const val TAG_PRIVACY = "PRIVACY"

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


    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.OpenExternal -> {
                    val intent = Intent(Intent.ACTION_VIEW, effect.url.toUri())
                    context.startActivity(intent)
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        snackbarHost = { snackbarController.SnackbarHost() },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        OnboardingContent(
            uiState         = uiState,
            onNext          = viewModel::onNext,
            onSkip          = viewModel::onSkip,
            onGetStarted    = viewModel::onGetStarted,
            onTermsTapped   = viewModel::onTermsTapped,
            onPrivacyTapped = viewModel::onPrivacyTapped,
            onGoogleSignIn  = { viewModel.onGoogleSignIn(context) },
            modifier        = Modifier.padding(innerPadding),
        )
    }
}


@Composable
internal fun OnboardingContent(
    uiState: OnboardingUiState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onGetStarted: () -> Unit,
    onTermsTapped: () -> Unit,
    onPrivacyTapped: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (uiState.steps.isEmpty()) return

    val accentColor = stepAccentColor(uiState.currentStep)

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

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
                    },
                    label = "Onboarding"
                ) { stepIndex ->
                    val step = uiState.steps.getOrNull(stepIndex) ?: return@AnimatedContent
                    StepContent(step = step, accentColor = accentColor)
                }
            }


            BottomControls(
                totalSteps      = uiState.steps.size,
                currentStep     = uiState.currentStep,
                isLastStep      = uiState.isLastStep,
                isLoading       = uiState.isLoading,
                onNext          = onNext,
                onGetStarted    = onGetStarted,
                onTermsTapped   = onTermsTapped,
                onPrivacyTapped = onPrivacyTapped,
                onGoogleSignIn  = onGoogleSignIn,
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.synapse.spacing.screen)
                    .padding(bottom = MaterialTheme.synapse.spacing.s32),
            )
        }
    }
}



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
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))

        Text(
            text      = stringResource(step.subtitleRes),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth(fraction = 0.85f),
            maxLines  = 3,
            overflow  = TextOverflow.Ellipsis,
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
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
        )
    }
}



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



@Composable
private fun BottomControls(
    totalSteps: Int,
    currentStep: Int,
    isLastStep: Boolean,
    isLoading: Boolean,
    onNext: () -> Unit,
    onGetStarted: () -> Unit,
    onTermsTapped: () -> Unit,
    onPrivacyTapped: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s24),
    ) {
        StepDots(totalSteps = totalSteps, currentStep = currentStep)

        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s16)) {

            PrimaryGradientButton(
                text      = stringResource(
                    if (isLastStep) R.string.onboarding_start_learning
                    else R.string.onboarding_next
                ),
                iconRes   = if (!isLastStep) R.drawable.ic_chevron_right else null,
                enabled   = !isLoading,
                isLoading = isLoading,
                onClick   = if (isLastStep) onGetStarted else onNext,
            )

            if (isLastStep) {
                GoogleSignInButton(
                    isLoading = isLoading,
                    onClick = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Legal consent text — only visible on the last step
            if (isLastStep) {
                LegalConsentText(
                    onTermsTapped   = onTermsTapped,
                    onPrivacyTapped = onPrivacyTapped,
                    modifier        = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}


@Composable
private fun LegalConsentText(
    onTermsTapped: () -> Unit,
    onPrivacyTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val beforeTerms = stringResource(R.string.legal_prefix)
    val partTerms   = stringResource(R.string.legal_terms)
    val and         = stringResource(R.string.legal_and)
    val partPrivacy = stringResource(R.string.legal_privacy)

    val linkColor    = MaterialTheme.colorScheme.primary
    val defaultStyle = MaterialTheme.typography.bodySmall

    val annotated = buildAnnotatedString {
        append("$beforeTerms ")

        // ── Terms of Service link ─────────────────────────────────────────────
        val termsLink = LinkAnnotation.Clickable(
            tag = TAG_TERMS,
            linkInteractionListener = { _ -> onTermsTapped() }
        )
        pushLink(termsLink)
        withStyle(
            SpanStyle(
                color          = linkColor,
                fontWeight     = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
            )
        ) { append(partTerms) }
        pop()

        append(" $and ")

        // ── Privacy Policy link ───────────────────────────────────────────────
        val privacyLink = LinkAnnotation.Clickable(
            tag = TAG_PRIVACY,
            linkInteractionListener = { _ -> onPrivacyTapped() }
        )
        pushLink(privacyLink)
        withStyle(
            SpanStyle(
                color          = linkColor,
                fontWeight     = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
            )
        ) { append(partPrivacy) }
        pop()

    }

    Text(
        text     = annotated,
        style    = defaultStyle.copy(
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        ),
        modifier = modifier,
    )
}


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
            uiState         = OnboardingUiState(currentStep = 0, steps = OnboardingStepData.steps),
            onNext          = {},
            onSkip          = {},
            onGetStarted    = {},
            onTermsTapped   = {},
            onPrivacyTapped = {},
        )
    }
}

@Preview(name = "Step 2 Generate — Light", showBackground = true)
@Preview(name = "Step 2 Generate — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OnboardingStep2Preview() {
    SynapseTheme {
        OnboardingContent(
            uiState         = OnboardingUiState(currentStep = 1, steps = OnboardingStepData.steps),
            onNext          = {},
            onSkip          = {},
            onGetStarted    = {},
            onTermsTapped   = {},
            onPrivacyTapped = {},
        )
    }
}

@Preview(name = "Step 3 Master — Light", showBackground = true)
@Preview(name = "Step 3 Master — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OnboardingStep3Preview() {
    SynapseTheme {
        OnboardingContent(
            uiState         = OnboardingUiState(currentStep = 2, steps = OnboardingStepData.steps),
            onNext          = {},
            onSkip          = {},
            onGetStarted    = {},
            onTermsTapped   = {},
            onPrivacyTapped = {},
        )
    }
}
