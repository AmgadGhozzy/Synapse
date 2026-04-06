package com.venom.synapse.features.session.presentation.screen

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.StatusColors
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.synapse.core.ui.components.StatusIconHeader
import com.venom.synapse.features.session.presentation.state.SessionSummaryUiState
import com.venom.synapse.features.session.presentation.viewmodel.SessionViewModel
import com.venom.ui.components.common.adp
import com.venom.ui.components.other.ConfettiAnimationType
import com.venom.ui.components.other.ConfettiView
import kotlinx.coroutines.delay

@Composable
fun SessionSummaryScreen(
    onBack   : () -> Unit,
    modifier : Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val summary by viewModel.summaryState.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)
    Scaffold(
        modifier            = modifier.fillMaxSize().safeDrawingPadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor       = Color.Transparent,
    ) { innerPadding ->
        SessionSummaryContent(
            summary  = summary,
            onBack   = onBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
internal fun SessionSummaryContent(
    summary : SessionSummaryUiState,
    onBack  : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accuracyPct = (summary.accuracy * 100).toInt()
    val incorrect   = summary.answeredCount - summary.correctCount
    val xp          = summary.correctCount * 5 + incorrect
    val isSuccess   = accuracyPct >= 70

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val heroVisible  by produceState(false) { if (visible) { delay(0);   value = true } }
    val statsVisible by produceState(false) { if (visible) { delay(120); value = true } }
    val leechVisible by produceState(false) { if (visible) { delay(240); value = true } }
    val ctaVisible   by produceState(false) { if (visible) { delay(320); value = true } }

    Box(modifier = modifier.fillMaxSize()) {
        if (isSuccess) {
            ConfettiView(
                animationType = ConfettiAnimationType.KONFETTI,
                modifier      = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = MaterialTheme.synapse.spacing.screen)
                .padding(top = MaterialTheme.synapse.spacing.screenContentTop, bottom = MaterialTheme.synapse.spacing.screenContentBottom),
        ) {
            AnimatedVisibility(
                visible = heroVisible,
                enter   = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow,
                    ),
                    initialOffsetY = { -it / 3 },
                ) + fadeIn(tween(400)),
            ) {
                SummaryHero(
                    accuracyPct = accuracyPct,
                    packTitle   = summary.packTitle,
                    duration    = summary.durationFormatted,
                )
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))
            AnimatedVisibility(
                visible = statsVisible,
                enter   = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness    = Spring.StiffnessMediumLow,
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
                    enter   = fadeIn(tween(240, 320)) + slideInVertically(tween(240, 320)) { it / 3 },
                ) {
                    SessionBreakdownRow(
                        newCount      = summary.newQuestionCount,
                        reviewedCount = summary.reviewedQuestionCount,
                    )
                }
            }

            if (summary.answeredCount > 0) {
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))
                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(280, 400)) + slideInVertically(tween(280, 400)) { it / 3 },
                ) {
                    PerformanceBar(
                        correctCount  = summary.correctCount,
                        answeredCount = summary.answeredCount,
                    )
                }
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(280, 500)) + slideInVertically(tween(280, 500)) { it / 3 },
            ) {
                SrsNoteCard()
            }

            if (summary.leechQuestionTexts.isNotEmpty()) {
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))
                AnimatedVisibility(
                    visible = leechVisible,
                    enter   = slideInVertically(
                        animationSpec = tween(380, easing = EaseOutCubic),
                        initialOffsetY = { it / 4 },
                    ) + fadeIn(tween(380)),
                ) {
                    LeechSummaryCard(
                        leechCount         = summary.leechCount,
                        leechQuestionTexts = summary.leechQuestionTexts,
                    )
                }
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))

            AnimatedVisibility(
                visible = ctaVisible,
                enter   = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium,
                    ),
                    initialScale = 0.82f,
                ) + fadeIn(tween(300)),
            ) {
                BackButton(onClick = onBack)
            }
        }
    }
}

@Composable
private fun SummaryHero(
    accuracyPct: Int,
    packTitle  : String,
    duration   : String,
    modifier   : Modifier = Modifier,
) {
    val typo        = MaterialTheme.typography
    val levelColors = MaterialTheme.synapse.levelColors

    val heroColors = when {
        accuracyPct >= 80 -> levelColors.success
        accuracyPct >= 50 -> levelColors.accent
        else              -> levelColors.error
    }
    val heroColor   = heroColors.accentColor
    val headlineRes = when {
        accuracyPct >= 80 -> R.string.summary_headline_outstanding
        accuracyPct >= 50 -> R.string.summary_headline_good_work
        else              -> R.string.summary_headline_keep_practicing
    }
    val iconRes = if (accuracyPct >= 80) R.drawable.ic_trophy else R.drawable.ic_target
    val iconCd  = if (accuracyPct >= 80) R.string.summary_cd_trophy else R.string.summary_cd_target

    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StatusIconHeader(
            iconRes     = iconRes,
            iconCd      = iconCd,
            accentColor = heroColor,
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s20))

        Text(
            text       = stringResource(headlineRes),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color      = MaterialTheme.colorScheme.onSurface,
            textAlign  = TextAlign.Center,
        )

        if (packTitle.isNotBlank()) {
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s4))
            Text(
                text      = packTitle,
                style     = typo.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = MaterialTheme.synapse.spacing.s12, vertical = MaterialTheme.synapse.spacing.s6),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_clock),
                contentDescription = stringResource(R.string.summary_cd_clock),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(12.adp),
            )
            Text(
                text       = duration,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatsGrid(
    correctCount : Int,
    answeredCount: Int,
    accuracyPct  : Int,
    xp           : Int,
    duration     : String,
    visible      : Boolean,
    modifier     : Modifier = Modifier,
) {
    val levelColors  = MaterialTheme.synapse.levelColors
    val primaryColor = MaterialTheme.colorScheme.primary

    val scoreColors = StatusColors(
        accentColor = primaryColor,
        bgColor     = primaryColor.copy(alpha = 0.09f),
        borderColor = primaryColor.copy(alpha = 0.20f),
    )
    val accuracyColors = when {
        accuracyPct >= 80 -> levelColors.success
        accuracyPct >= 60 -> levelColors.accent
        else              -> levelColors.error
    }

    data class StatDef(val labelRes: Int, val value: String, val statusColors: StatusColors, val delay: Int)

    val stats = listOf(
        StatDef(R.string.summary_stat_score,    stringResource(R.string.summary_score_format, correctCount, answeredCount), scoreColors,        60),
        StatDef(R.string.summary_stat_accuracy, stringResource(R.string.summary_accuracy_format, accuracyPct),              accuracyColors,    140),
        StatDef(R.string.summary_stat_xp,       stringResource(R.string.summary_xp_format, xp),                            levelColors.accent, 220),
        StatDef(R.string.summary_stat_duration, duration,                                                                   levelColors.error,  300),
    )

    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {
        listOf(0, 2).forEach { rowStart ->
            Row(
                modifier              = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            ) {
                stats.subList(rowStart, rowStart + 2).forEach { stat ->
                    AnimatedVisibility(
                        visible  = visible,
                        enter    = fadeIn(tween(240, stat.delay)) + slideInVertically(tween(240, stat.delay)) { it / 2 },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    ) {
                        StatCard(
                            label        = stringResource(stat.labelRes),
                            value        = stat.value,
                            statusColors = stat.statusColors,
                            modifier     = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label       : String,
    value       : String,
    statusColors: StatusColors,
    modifier    : Modifier = Modifier,
) {
    val typo  = MaterialTheme.typography
    val shape = MaterialTheme.synapse.radius.xxl

    Box(
        modifier = modifier
            .dropShadow(
                shape  = shape,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(customColor = statusColors.accentColor),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .background(statusColors.bgColor)
                .padding(MaterialTheme.synapse.spacing.s16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color      = statusColors.accentColor,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s6))
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SessionBreakdownRow(
    newCount     : Int,
    reviewedCount: Int,
    modifier     : Modifier = Modifier,
) {
    val levelColors = MaterialTheme.synapse.levelColors

    Row(
        modifier              = modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {
        BreakdownChip(
            iconRes      = R.drawable.ic_zap,
            label        = stringResource(R.string.summary_new_cards, newCount),
            statusColors = levelColors.success,
            modifier     = Modifier.weight(1f).fillMaxHeight(),
        )
        BreakdownChip(
            iconRes      = R.drawable.ic_clock,
            label        = stringResource(R.string.summary_reviewed_cards, reviewedCount),
            statusColors = levelColors.primary,
            modifier     = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun BreakdownChip(
    iconRes     : Int,
    label       : String,
    statusColors: StatusColors,
    modifier    : Modifier = Modifier,
) {
    val typo  = MaterialTheme.typography
    val shape = MaterialTheme.shapes.medium

        Row(
            modifier = modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .background(statusColors.bgColor)
                .padding(horizontal = MaterialTheme.synapse.spacing.s14, vertical = MaterialTheme.synapse.spacing.s12),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
        ) {
            Icon(
                painter            = painterResource(iconRes),
                contentDescription = null,
                tint               = statusColors.accentColor,
                modifier           = Modifier.size(14.adp),
            )
            Text(
                text       = label,
                style      = typo.labelMedium,
                fontWeight = FontWeight.Bold,
                color      = statusColors.accentColor,
            )
        }
}

@Composable
private fun PerformanceBar(
    correctCount : Int,
    answeredCount: Int,
    modifier     : Modifier = Modifier,
) {
    val levelColors = MaterialTheme.synapse.levelColors
    val incorrect   = answeredCount - correctCount

    val correctColor   = levelColors.success.accentColor
    val incorrectColor = levelColors.error.accentColor

    val animCorrect by animateFloatAsState(
        targetValue   = correctCount.toFloat() / answeredCount,
        animationSpec = tween(700, 100, FastOutSlowInEasing),
    )
    val animIncorrect by animateFloatAsState(
        targetValue   = incorrect.toFloat() / answeredCount,
        animationSpec = tween(700, 260, FastOutSlowInEasing),
    )
    val remainder = (1f - animCorrect - animIncorrect).coerceAtLeast(0f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(8.adp).clip(MaterialTheme.synapse.radius.sm),
        ) {
            if (animCorrect   > 0f)     Box(Modifier.weight(animCorrect).fillMaxSize().background(correctColor))
            if (animIncorrect > 0f)     Box(Modifier.weight(animIncorrect).fillMaxSize().background(incorrectColor))
            if (remainder     > 0.001f) Box(Modifier.weight(remainder).fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
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
            style     = MaterialTheme.typography.labelMedium,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SrsNoteCard(modifier: Modifier = Modifier) {
    val typo    = MaterialTheme.typography
    val primary = MaterialTheme.colorScheme.primary
    val shape   = MaterialTheme.synapse.radius.xxl

        Surface(
            modifier = modifier.fillMaxWidth(),
            shape    = shape,
            color    = primary.copy(alpha = 0.07f),
        ) {
            Row(
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_zap),
                    contentDescription = stringResource(R.string.summary_cd_zap),
                    tint               = primary,
                    modifier           = Modifier.size(16.adp).padding(top = 1.adp),
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = primary)) {
                            append(stringResource(R.string.summary_srs_label))
                        }
                        append(" ${stringResource(R.string.summary_srs_body)}")
                    },
                    style      = typo.bodySmall,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
}

@Composable
private fun LeechSummaryCard(
    leechCount        : Int,
    leechQuestionTexts: List<String>,
    modifier          : Modifier = Modifier,
) {
    val typo       = MaterialTheme.typography
    val errorColor = MaterialTheme.colorScheme.error
    val shape      = MaterialTheme.synapse.radius.xxl

    var isExpanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue   = if (isExpanded) 180f else 0f,
        animationSpec = tween(240, easing = FastOutSlowInEasing)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = shape,
        color    = errorColor.copy(alpha = 0.07f),
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
                    verticalAlignment     = Alignment.CenterVertically,
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
                            painter            = painterResource(R.drawable.ic_alert_triangle),
                            contentDescription = stringResource(R.string.summary_leech_cd_alert),
                            tint               = errorColor,
                            modifier           = Modifier.size(17.adp),
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = pluralStringResource(R.plurals.summary_leech_title, leechCount, leechCount),
                            style      = typo.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = errorColor,
                        )
                        Text(
                            text  = stringResource(if (isExpanded) R.string.summary_leech_tap_hide else R.string.summary_leech_tap_view),
                            style = typo.labelMedium,
                            color = errorColor.copy(alpha = 0.58f),
                        )
                    }

                    Icon(
                        painter            = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = stringResource(R.string.summary_leech_cd_chevron),
                        tint               = errorColor.copy(alpha = 0.65f),
                        modifier           = Modifier.size(17.adp).graphicsLayer { rotationZ = chevronRotation },
                    )
                }

                if (isExpanded) {
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s16),
                        color     = errorColor.copy(alpha = 0.14f),
                        thickness = 1.adp,
                    )
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(MaterialTheme.synapse.spacing.s12),
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
    title   : String,
    tint    : Color,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.synapse.radius.lg)
            .background(tint.copy(alpha = 0.07f))
            .padding(horizontal = MaterialTheme.synapse.spacing.s12, vertical = MaterialTheme.synapse.spacing.s10),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
    ) {
        Box(modifier = Modifier.size(6.adp).clip(CircleShape).background(tint))
        Text(
            text     = title,
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BackButton(
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape  = shape,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(customColor = MaterialTheme.colorScheme.outline),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(vertical = MaterialTheme.synapse.spacing.s16),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = stringResource(R.string.summary_cta_back),
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val previewOutstanding = SessionSummaryUiState(
    packTitle             = "Machine Learning Basics",
    totalQuestions        = 10,
    answeredCount         = 10,
    correctCount          = 9,
    accuracy              = 0.90f,
    durationFormatted     = "4m 32s",
    leechCount            = 0,
    leechQuestionTexts    = emptyList(),
    newQuestionCount      = 4,
    reviewedQuestionCount = 6,
)

private val previewAverage = SessionSummaryUiState(
    packTitle             = "Roman History",
    totalQuestions        = 10,
    answeredCount         = 10,
    correctCount          = 6,
    accuracy              = 0.60f,
    durationFormatted     = "6m 14s",
    leechCount            = 2,
    leechQuestionTexts    = listOf("Rubicon crossing year", "SM-2 ease factor formula"),
    newQuestionCount      = 3,
    reviewedQuestionCount = 7,
)

private val previewLow = SessionSummaryUiState(
    packTitle             = "Advanced Algorithms",
    totalQuestions        = 10,
    answeredCount         = 10,
    correctCount          = 2,
    accuracy              = 0.20f,
    durationFormatted     = "8m 02s",
    leechCount            = 4,
    leechQuestionTexts    = listOf("Big-O of quicksort", "AVL rotations", "Dijkstra complexity", "NP-hard"),
    newQuestionCount      = 5,
    reviewedQuestionCount = 5,
)

@Preview(name = "Outstanding ≥80% — Light", showBackground = true)
@Preview(name = "Outstanding ≥80% — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SummaryOutstandingPreview() {
    SynapseTheme { SessionSummaryContent(summary = previewOutstanding, onBack = {}) }
}

@Preview(name = "Average ≥50% + Leeches — Light", showBackground = true)
@Preview(name = "Average ≥50% + Leeches — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SummaryAveragePreview() {
    SynapseTheme { SessionSummaryContent(summary = previewAverage, onBack = {}) }
}

@Preview(name = "Low <50% + Many Leeches — Light", showBackground = true)
@Preview(name = "Low <50% + Many Leeches — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SummaryLowPreview() {
    SynapseTheme { SessionSummaryContent(summary = previewLow, onBack = {}) }
}