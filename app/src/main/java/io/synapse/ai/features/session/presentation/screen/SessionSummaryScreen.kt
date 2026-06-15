package io.synapse.ai.features.session.presentation.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.features.profile.presentation.components.GoogleSignInButton
import io.synapse.ai.core.ui.components.GuidedPrimaryButton
import io.synapse.ai.core.ui.components.PrimaryGradientButton
import io.synapse.ai.core.ui.components.SecondaryButton
import io.synapse.ai.core.ui.components.StatusIconHeader
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.features.session.presentation.state.SessionSummaryUiState
import io.synapse.ai.features.session.presentation.viewmodel.SessionViewModel
import kotlinx.coroutines.delay

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun SessionSummaryScreen(
    onAddSource: () -> Unit,
    onReviewMistakes: (packId: Long) -> Unit,
    onContinuePack: (packId: Long) -> Unit,
    onGoToLibrary: () -> Unit,
    onNavigateToSession: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val summary by viewModel.summaryState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> viewModel.onPushConsentResult(isGranted) }

    // Collect one-shot navigation effects emitted by onReviewMistakes() / onContinuePack()
    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.NavigateToNewSession -> onNavigateToSession()
                else -> Unit
            }
        }
    }

    // Push-permission smart prompt: triggered only after first-ever session
    if (uiState.showPushRationale) {
        SmartNotificationSheet(
            onConfirm = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onPushConsentResult(true)
                }
            },
            onDismiss = { viewModel.dismissPushRationale() },
        )
    }

    BackHandler(onBack = onGoToLibrary)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
    ) { innerPadding ->
        SessionSummaryContent(
            summary = summary,
            onAddSource = onAddSource,
            onReviewMistakes = onReviewMistakes,
            onContinuePack = onContinuePack,
            onGoToLibrary = onGoToLibrary,
            onGoogleSignIn = { viewModel.onGoogleSignIn(context) },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
internal fun SessionSummaryContent(
    summary: SessionSummaryUiState,
    onAddSource: () -> Unit,
    onReviewMistakes: (packId: Long) -> Unit,
    onContinuePack: (packId: Long) -> Unit,
    onGoToLibrary: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val incorrectCount = summary.answeredCount - summary.correctCount
    val accuracyPct = (summary.accuracy * 100).toInt()
    // Use mistakeQuestionIds as the source of truth — keeps visibility in sync
    // with what the ViewModel can actually act on (demo sessions never populate this)
    val hasMistakes = summary.mistakeQuestionIds.isNotEmpty()

    // Staggered entrance animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val heroVisible by produceState(false) { delay(0); value = visible }
    val scoreVisible by produceState(false) { delay(160); value = visible }
    val barVisible by produceState(false) { delay(280); value = visible }
    val ctaVisible by produceState(false) { delay(400); value = visible }

    Box(modifier = modifier.fillMaxSize()) {

        // Scrollable body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.adp)
                .padding(top = 32.adp, bottom = 200.adp),
            verticalArrangement = Arrangement.spacedBy(20.adp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Layer 1: Celebration hero ─────────────────────────
            AnimatedVisibility(
                visible = heroVisible,
                enter = slideInVertically(
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                    initialOffsetY = { -it / 3 },
                ) + fadeIn(tween(400)),
            ) {
                CelebrationHero(accuracyPct = accuracyPct)
            }

            AnimatedVisibility(
                visible = scoreVisible,
                enter = fadeIn(tween(360)) +
                        slideInVertically(tween(360, easing = EaseOutCubic)) { it / 3 },
            ) {
                ScoreFractionCard(
                    correctCount = summary.correctCount,
                    answeredCount = summary.answeredCount,
                )
            }

            // ── Segmented performance bar ─────────────────────────
            if (summary.answeredCount > 0) {
                AnimatedVisibility(
                    visible = barVisible,
                    enter = fadeIn(tween(320, 80)) +
                            slideInVertically(tween(320, 80)) { it / 4 },
                ) {
                    SegmentedProgressBar(
                        correctCount = summary.correctCount,
                        answeredCount = summary.answeredCount,
                        modifier = Modifier.padding(horizontal = 12.adp),
                    )
                }
            }

            // ── SRS feedback chip ──────────────────────────────────
            AnimatedVisibility(
                visible = barVisible,
                enter = fadeIn(tween(300, 160)),
            ) {
                SrsChip()
            }
        }

        // ── Layer 3: Pinned CTA funnel (solid bg to prevent overlap) ──
        AnimatedVisibility(
            visible = ctaVisible,
            enter = scaleIn(
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                initialScale = 0.88f,
            ) + fadeIn(tween(280)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            PinnedCtaPanel(
                summary = summary,
                hasMistakes = hasMistakes,
                incorrectCount = incorrectCount,
                onAddSource = onAddSource,
                onReviewMistakes = onReviewMistakes,
                onContinuePack = onContinuePack,
                onGoToLibrary = onGoToLibrary,
                onGoogleSignIn = onGoogleSignIn,
            )
        }
    }
}

// ─── Layer 1: Celebration Hero ────────────────────────────────────────────────

@Composable
private fun CelebrationHero(
    accuracyPct: Int,
    modifier: Modifier = Modifier,
) {
    val (headline, iconRes) = when {
        accuracyPct == 100 -> R.string.summary_headline_mastermind to R.drawable.ic_trophy
        accuracyPct >= 70 -> R.string.summary_headline_great_work to R.drawable.ic_trophy
        else -> R.string.summary_headline_keep_going to R.drawable.ic_target
    }

    val accentColor = when {
        accuracyPct == 100 -> MaterialTheme.synapse.semantic.success
        accuracyPct >= 70 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.synapse.semantic.accent
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.adp),
    ) {
        StatusIconHeader(
            iconRes = iconRes,
            iconCd = headline,
            accentColor = accentColor,
        )

        Text(
            text = stringResource(headline),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Layer 2: Score Fraction Card ─────────────────────────────────────────────

@Composable
private fun ScoreFractionCard(
    correctCount: Int,
    answeredCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.adp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.adp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.adp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.adp),
        ) {
            // Large X / Y fraction
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "$correctCount",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.synapse.semantic.success,
                    lineHeight = 64.sp,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = " / $answeredCount",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    lineHeight = 64.sp,
                    modifier = Modifier.padding(bottom = 6.adp).alignByBaseline(),
                )
            }
            Text(
                text = stringResource(R.string.summary_label_correct),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
    }
}

// ─── Segmented Progress Bar ────────────────────────────────────────────────────
@Composable
private fun SegmentedProgressBar(
    correctCount: Int,
    answeredCount: Int,
    modifier: Modifier = Modifier,
) {
    if (answeredCount == 0) return

    val correctFraction = correctCount.toFloat() / answeredCount
    val animCorrect by animateFloatAsState(
        targetValue = correctFraction,
        animationSpec = tween(700, easing = EaseOutCubic)
    )

    // The bar
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(12.adp)
            .clip(RoundedCornerShape(6.adp))
            .background(MaterialTheme.synapse.semantic.error),
    ) {
        if (animCorrect > 0f) {
            Box(
                modifier = Modifier
                    .weight(animCorrect)
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.synapse.semantic.success,
                                MaterialTheme.synapse.semantic.success
                            )
                        ),
                        RoundedCornerShape(6.adp)
                    )
            )
        }
        if (animCorrect < 1f) {
            Box(modifier = Modifier
                .weight(1f - animCorrect)
                .fillMaxSize())
        }
    }
}

// ─── SRS Chip ─────────────────────────────────────────────────────────────────

@Composable
private fun SrsChip(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = primary.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.adp, vertical = 6.adp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.adp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_zap),
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(16.adp),
            )
            Text(
                text = stringResource(R.string.summary_srs_updated),
                style = MaterialTheme.typography.labelMedium,
                color = primary,
            )
        }
    }
}

@Composable
private fun PinnedCtaPanel(
    summary: SessionSummaryUiState,
    hasMistakes: Boolean,
    incorrectCount: Int,
    onAddSource: () -> Unit,
    onReviewMistakes: (packId: Long) -> Unit,
    onContinuePack: (packId: Long) -> Unit,
    onGoToLibrary: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = RoundedCornerShape(topStart = 32.adp, topEnd = 32.adp),
                shadow = MaterialTheme.synapse.shadows.medium.toShadow()
            ),
        shape = RoundedCornerShape(topStart = 32.adp, topEnd = 32.adp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.adp)
                .padding(20.adp),
            verticalArrangement = Arrangement.spacedBy(10.adp),
        ) {
            if (summary.isDemoSession) {
                DemoConversionPanel(
                    onAddSource = onAddSource,
                    onGoToLibrary = onGoToLibrary,
                    onGoogleSignIn = onGoogleSignIn,
                )
            } else {
                RegularCtaPanel(
                    summary = summary,
                    hasMistakes = hasMistakes,
                    incorrectCount = incorrectCount,
                    onReviewMistakes = onReviewMistakes,
                    onContinuePack = onContinuePack,
                    onGoToLibrary = onGoToLibrary,
                )
            }
        }
    }
}

@Composable
private fun DemoConversionPanel(
    onAddSource: () -> Unit,
    onGoToLibrary: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.adp),
    ) {
        Text(
            text = stringResource(R.string.summary_demo_headline),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.summary_demo_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(4.adp))

        // Primary — pulsing gradient button
        GuidedPrimaryButton(
            text = stringResource(R.string.summary_demo_cta),
            iconRes = R.drawable.ic_file_plus,
            enabled = true,
            onClick = onAddSource,
            showPulse = true,
        )

        GoogleSignInButton(
            isLoading = false,
            subtitle = stringResource(R.string.google_sign_in_save_progress),
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.adp),
        )

        // Tertiary
        TextButton(
            onClick = onGoToLibrary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.summary_back_to_library),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun RegularCtaPanel(
    summary: SessionSummaryUiState,
    hasMistakes: Boolean,
    incorrectCount: Int,
    onReviewMistakes: (packId: Long) -> Unit,
    onContinuePack: (packId: Long) -> Unit,
    onGoToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.adp),
    ) {

        // ── Primary CTA ─────────────────────────────────────────
        if (hasMistakes) {
            Button(
                onClick = { onReviewMistakes(summary.sessionId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.adp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.synapse.semantic.error,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = if (incorrectCount == 1) stringResource(
                        R.string.summary_review_mistakes_one,
                        incorrectCount
                    ) else stringResource(R.string.summary_review_mistakes_plural, incorrectCount),
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            // Perfect score — still offer continuation (SRS due cards)
            PrimaryGradientButton(
                text = stringResource(R.string.summary_continue_all_correct),
                onClick = { onContinuePack(summary.packId ?: 0L) },
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.adp),
            )
        }

        // ── Secondary CTA ────────────────────────────────────────
        SecondaryButton(
            onClick = { onContinuePack(summary.packId ?: 0L) },
            text = stringResource(R.string.summary_continue_same_pack),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.adp),
        )

        // ── Tertiary ─────────────────────────────────────────
        TextButton(
            onClick = onGoToLibrary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.summary_back_to_library),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun SmartNotificationSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(28.adp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.adp)) {
                Text(
                    text = stringResource(R.string.summary_smart_reminder_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Text(
                text = stringResource(R.string.summary_smart_reminder_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            )
        },
        confirmButton = {
            PrimaryGradientButton(
                text = stringResource(R.string.summary_smart_reminder_enable),
                onClick = onConfirm,
                enabled = true,
                modifier = Modifier.padding(bottom = 4.adp),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.summary_smart_reminder_later),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                )
            }
        },
        tonalElevation = 0.adp,
    )
}
