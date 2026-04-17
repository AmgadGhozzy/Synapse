package io.synapse.ai.features.session.presentation.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.components.ConfettiAnimationType
import io.synapse.ai.core.ui.components.ConfettiView
import io.synapse.ai.core.ui.components.GuidedPrimaryButton
import io.synapse.ai.core.ui.components.PrimaryGradientButton
import io.synapse.ai.core.ui.components.SecondaryButton
import io.synapse.ai.core.ui.components.StatusIconHeader
import io.synapse.ai.features.session.presentation.state.SessionSummaryUiState
import io.synapse.ai.features.session.presentation.viewmodel.SessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SessionSummaryScreen(
    onAddPack: () -> Unit,
    onPasteText: () -> Unit,
    onGoToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val summary by viewModel.summaryState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPushConsentResult(isGranted)
    }

    if (uiState.showPushRationale) {
        NotificationRationaleDialog(
            onConfirm = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onPushConsentResult(true)
                }
            },
            onDismiss = {
                viewModel.dismissPushRationale()
            }
        )
    }

    BackHandler(onBack = onGoToDashboard)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
    ) { innerPadding ->
        SessionSummaryContent(
            summary = summary,
            onAddPack = onAddPack,
            onPasteText = onPasteText,
            onGoToDashboard = onGoToDashboard,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
internal fun SessionSummaryContent(
    summary: SessionSummaryUiState,
    onAddPack: () -> Unit,
    onPasteText: () -> Unit,
    onGoToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accuracyPct = (summary.accuracy * 100).toInt()
    val incorrect = summary.answeredCount - summary.correctCount
    val xp = summary.correctCount * 5 + incorrect
    val isSuccess = accuracyPct >= 70

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val heroVisible by produceState(false) {
        if (visible) {
            delay(0); value = true
        }
    }
    val statsVisible by produceState(false) {
        if (visible) {
            delay(120); value = true
        }
    }
    val leechVisible by produceState(false) {
        if (visible) {
            delay(240); value = true
        }
    }
    val ctaVisible by produceState(false) {
        if (visible) {
            delay(320); value = true
        }
    }

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        if (isSuccess) {
            ConfettiView(
                animationType = ConfettiAnimationType.KONFETTI,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaterialTheme.synapse.spacing.screen)
                .padding(
                    top = MaterialTheme.synapse.spacing.screen,
                    bottom = MaterialTheme.synapse.spacing.screenContentBottom
                ),
        ) {
            AnimatedVisibility(
                visible = heroVisible,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    initialOffsetY = { -it / 3 },
                ) + fadeIn(tween(400)),
            ) {
                SummaryHero(
                    accuracyPct = accuracyPct,
                    packTitle = summary.packTitle,
                    duration = summary.durationFormatted,
                )
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))
            AnimatedVisibility(
                visible = statsVisible,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialOffsetY = { it / 2 },
                ) + fadeIn(tween(350)),
            ) {
                StatsGrid(
                    correctCount = summary.correctCount,
                    answeredCount = summary.answeredCount,
                    accuracyPct = accuracyPct,
                    xp = xp,
                    duration = summary.durationFormatted,
                    visible = visible,
                )
            }
            if (summary.newQuestionCount > 0 || summary.reviewedQuestionCount > 0) {
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(240, 320)) + slideInVertically(tween(240, 320)) { it / 3 },
                ) {
                    SessionBreakdownRow(
                        newCount = summary.newQuestionCount,
                        reviewedCount = summary.reviewedQuestionCount,
                    )
                }
            }

            if (summary.answeredCount > 0) {
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(280, 400)) + slideInVertically(tween(280, 400)) { it / 3 },
                ) {
                    PerformanceBar(
                        correctCount = summary.correctCount,
                        answeredCount = summary.answeredCount,
                    )
                }
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(280, 500)) + slideInVertically(tween(280, 500)) { it / 3 },
            ) {
                LearningMeaningCard(summary = summary)
            }

            if (summary.leechQuestionTexts.isNotEmpty()) {
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))
                AnimatedVisibility(
                    visible = leechVisible,
                    enter = slideInVertically(
                        animationSpec = tween(380, easing = EaseOutCubic),
                        initialOffsetY = { it / 4 },
                    ) + fadeIn(tween(380)),
                ) {
                    LeechSummaryCard(
                        leechCount = summary.leechCount,
                        leechQuestionTexts = summary.leechQuestionTexts,
                    )
                }
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))

            AnimatedVisibility(
                visible = ctaVisible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    initialScale = 0.82f,
                ) + fadeIn(tween(300)),
            ) {
                ActionHookCard(
                    summary = summary,
                    onAddPack = onAddPack,
                    onPasteText = onPasteText,
                    onGoToDashboard = onGoToDashboard,
                )
            }
        }

        ScrollDownIndicator(
            scrollState = scrollState,
            ctaVisible = ctaVisible,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SummaryHero(
    accuracyPct: Int,
    packTitle: String,
    duration: String,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography
    val semantic = MaterialTheme.synapse.semantic

    val heroColor = when {
        accuracyPct >= 80 -> semantic.success
        accuracyPct >= 50 -> semantic.accent
        else -> semantic.error
    }
    val headlineRes = when {
        accuracyPct >= 80 -> R.string.summary_headline_outstanding
        accuracyPct >= 50 -> R.string.summary_headline_good_work
        else -> R.string.summary_headline_keep_practicing
    }
    val supportRes = when {
        accuracyPct >= 80 -> R.string.summary_support_outstanding
        accuracyPct >= 50 -> R.string.summary_support_good_work
        else -> R.string.summary_support_keep_practicing
    }
    val iconRes = if (accuracyPct >= 80) R.drawable.ic_trophy else R.drawable.ic_target
    val iconCd = if (accuracyPct >= 80) R.string.summary_cd_trophy else R.string.summary_cd_target

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StatusIconHeader(
            iconRes = iconRes,
            iconCd = iconCd,
            accentColor = heroColor,
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s20))

        Text(
            text = stringResource(headlineRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s8))

        Text(
            text = stringResource(supportRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (packTitle.isNotBlank()) {
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s10))
            Text(
                text = packTitle,
                style = typo.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(
                    horizontal = MaterialTheme.synapse.spacing.s12,
                    vertical = MaterialTheme.synapse.spacing.s6
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = stringResource(R.string.summary_cd_clock),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.adp),
            )
            Text(
                text = duration,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatsGrid(
    correctCount: Int,
    answeredCount: Int,
    accuracyPct: Int,
    xp: Int,
    duration: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic
    val primaryColor = MaterialTheme.colorScheme.primary

    data class StatDef(
        val labelRes: Int,
        val value: String,
        val accentColor: Color,
        val bgColor: Color,
        val borderColor: Color,
        val delay: Int
    )

    val stats = listOf(
        StatDef(
            R.string.summary_stat_score,
            stringResource(R.string.summary_score_format, correctCount, answeredCount),
            primaryColor,
            primaryColor.copy(alpha = 0.09f),
            primaryColor.copy(alpha = 0.20f),
            60
        ),
        StatDef(
            R.string.summary_stat_accuracy,
            stringResource(R.string.summary_accuracy_format, accuracyPct),
            when {
                accuracyPct >= 80 -> semantic.success
                accuracyPct >= 60 -> semantic.accent
                else -> semantic.error
            },
            when {
                accuracyPct >= 80 -> semantic.successBg
                accuracyPct >= 60 -> semantic.accentBg
                else -> semantic.errorBg
            },
            when {
                accuracyPct >= 80 -> semantic.successBorder
                accuracyPct >= 60 -> semantic.accentBorder
                else -> semantic.errorBorder
            },
            140
        ),
        StatDef(
            R.string.summary_stat_xp,
            stringResource(R.string.summary_xp_format, xp),
            semantic.accent,
            semantic.accentBg,
            semantic.accentBorder,
            220
        ),
        StatDef(
            R.string.summary_stat_duration,
            duration,
            semantic.error,
            semantic.errorBg,
            semantic.errorBorder,
            300
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {
        listOf(0, 2).forEach { rowStart ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            ) {
                stats.subList(rowStart, rowStart + 2).forEach { stat ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(240, stat.delay)) + slideInVertically(
                            tween(
                                240,
                                stat.delay
                            )
                        ) { it / 2 },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        StatCard(
                            label = stringResource(stat.labelRes),
                            value = stat.value,
                            accentColor = stat.accentColor,
                            bgColor = stat.bgColor,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.synapse.radius.xxl

    Box(
        modifier = modifier
            .dropShadow(
                shape = shape,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(customColor = accentColor),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .background(bgColor)
                .padding(MaterialTheme.synapse.spacing.s16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = accentColor,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s6))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SessionBreakdownRow(
    newCount: Int,
    reviewedCount: Int,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {
        BreakdownChip(
            iconRes = R.drawable.ic_zap,
            label = stringResource(R.string.summary_new_cards, newCount),
            accentColor = semantic.success,
            bgColor = semantic.successBg,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        BreakdownChip(
            iconRes = R.drawable.ic_clock,
            label = stringResource(R.string.summary_reviewed_cards, reviewedCount),
            accentColor = semantic.primary,
            bgColor = semantic.primaryBg,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun BreakdownChip(
    iconRes: Int,
    label: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography
    val shape = MaterialTheme.shapes.medium

    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .background(bgColor)
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s14,
                vertical = MaterialTheme.synapse.spacing.s12
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(14.adp),
        )
        Text(
            text = label,
            style = typo.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor,
        )
    }
}

@Composable
private fun PerformanceBar(
    correctCount: Int,
    answeredCount: Int,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic
    val incorrect = answeredCount - correctCount

    val correctColor = semantic.success
    val incorrectColor = semantic.error

    val animCorrect by animateFloatAsState(
        targetValue = correctCount.toFloat() / answeredCount,
        animationSpec = tween(700, 100, FastOutSlowInEasing),
    )
    val animIncorrect by animateFloatAsState(
        targetValue = incorrect.toFloat() / answeredCount,
        animationSpec = tween(700, 260, FastOutSlowInEasing),
    )
    val remainder = (1f - animCorrect - animIncorrect).coerceAtLeast(0f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.adp)
                .clip(MaterialTheme.synapse.radius.sm),
        ) {
            if (animCorrect > 0f) Box(
                Modifier
                    .weight(animCorrect)
                    .fillMaxSize()
                    .background(correctColor)
            )
            if (animIncorrect > 0f) Box(
                Modifier
                    .weight(animIncorrect)
                    .fillMaxSize()
                    .background(incorrectColor)
            )
            if (remainder > 0.001f) Box(
                Modifier
                    .weight(remainder)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s8))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = correctColor)) {
                    append(stringResource(R.string.summary_correct_count, correctCount))
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append(stringResource(R.string.summary_performance_separator))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = incorrectColor)) {
                    append(stringResource(R.string.summary_incorrect_count, incorrect))
                }
            },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LearningMeaningCard(
    summary: SessionSummaryUiState,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography
    val primary = MaterialTheme.colorScheme.primary
    val shape = MaterialTheme.synapse.radius.xxl

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = primary.copy(alpha = 0.07f),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.adp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_zap),
                        contentDescription = stringResource(R.string.summary_cd_zap),
                        tint = primary,
                        modifier = Modifier.size(16.adp),
                    )
                }
                if (summary.isDemoSession) {
                    Text(
                        text = stringResource(R.string.summary_learning_title),
                        style = typo.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primary
                                )
                            ) {
                                append(stringResource(R.string.summary_srs_label))
                            }
                            append(" ${stringResource(R.string.summary_srs_body)}")
                        },
                        style = typo.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (summary.isDemoSession) {
                Text(
                    text = stringResource(R.string.summary_learning_body),
                    style = typo.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = stringResource(
                        if (summary.reviewedQuestionCount > 0) R.string.summary_learning_detail_review
                        else R.string.summary_learning_detail_new,
                    ),
                    style = typo.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LeechSummaryCard(
    leechCount: Int,
    leechQuestionTexts: List<String>,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography
    val errorColor = MaterialTheme.colorScheme.error
    val shape = MaterialTheme.synapse.radius.xxl

    var isExpanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(240, easing = FastOutSlowInEasing)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = errorColor.copy(alpha = 0.07f),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(MaterialTheme.synapse.spacing.s16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.adp)
                        .clip(MaterialTheme.synapse.radius.sm)
                        .background(errorColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_alert_triangle),
                        contentDescription = stringResource(R.string.summary_leech_cd_alert),
                        tint = errorColor,
                        modifier = Modifier.size(17.adp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.summary_leech_title,
                            leechCount,
                            leechCount
                        ),
                        style = typo.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = errorColor,
                    )
                    Text(
                        text = stringResource(if (isExpanded) R.string.summary_leech_tap_hide else R.string.summary_leech_tap_view),
                        style = typo.labelMedium,
                        color = errorColor.copy(alpha = 0.58f),
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down),
                    contentDescription = stringResource(R.string.summary_leech_cd_chevron),
                    tint = errorColor.copy(alpha = 0.65f),
                    modifier = Modifier
                        .size(17.adp)
                        .graphicsLayer { rotationZ = chevronRotation },
                )
            }

            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s16),
                    color = errorColor.copy(alpha = 0.14f),
                    thickness = 1.adp,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.synapse.spacing.s12),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
                ) {
                    leechQuestionTexts.forEach { text ->
                        LeechRow(title = text, tint = errorColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeechRow(
    title: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(tint.copy(alpha = 0.07f))
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s12,
                vertical = MaterialTheme.synapse.spacing.s10
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
    ) {
        Box(
            modifier = Modifier
                .size(6.adp)
                .clip(CircleShape)
                .background(tint)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionHookCard(
    summary: SessionSummaryUiState,
    onAddPack: () -> Unit,
    onPasteText: () -> Unit,
    onGoToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    if (summary.isDemoSession) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s14),
            ) {
                Text(
                    text = stringResource(
                        if (summary.isDemoSession) R.string.summary_action_title_demo
                        else R.string.summary_action_title_default,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = stringResource(
                        if (summary.isDemoSession) R.string.summary_action_body_demo
                        else R.string.summary_action_body_default,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                GuidedPrimaryButton(
                    text = stringResource(R.string.summary_cta_add_pack),
                    iconRes = R.drawable.ic_file_plus,
                    enabled = true,
                    onClick = onAddPack,
                    showPulse = summary.isDemoSession,
                )

                PasteTextOption(onClick = onPasteText)

                SecondaryButton(
                    text = stringResource(R.string.summary_cta_dashboard),
                    onClick = onGoToDashboard,
                )
            }
        }
    } else {
        SecondaryButton(
            text = stringResource(R.string.summary_cta_dashboard),
            onClick = onGoToDashboard,
        )
    }
}

@Composable
private fun PasteTextOption(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.adp),
            )
            Text(
                text = stringResource(R.string.summary_cta_paste_text),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun ScrollDownIndicator(
    scrollState: ScrollState,
    ctaVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = scrollState.canScrollForward && ctaVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
        modifier = modifier
            .padding(bottom = MaterialTheme.synapse.spacing.s20)
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .graphicsLayer { translationY = offsetY }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
                .clickable {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
                .padding(MaterialTheme.synapse.spacing.s10),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_md)
            )
        }
    }
}

@Composable
private fun NotificationRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.push_rationale_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.push_rationale_body),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            PrimaryGradientButton(
                text = stringResource(R.string.push_rationale_confirm),
                onClick = onConfirm,
                enabled = true,
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s8)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.push_rationale_dismiss),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        shape = MaterialTheme.synapse.radius.xxl,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = MaterialTheme.synapse.spacing.s12
    )
}
