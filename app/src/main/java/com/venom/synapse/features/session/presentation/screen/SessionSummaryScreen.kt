package com.venom.synapse.features.session.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.theme.tokens.StatusColors
import com.venom.synapse.features.session.presentation.state.SessionSummaryUiState
import com.venom.synapse.features.session.presentation.viewmodel.SessionViewModel
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp
import com.venom.ui.components.other.ConfettiAnimationType
import com.venom.ui.components.other.ConfettiView

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SessionSummaryScreen(
    onBack   : () -> Unit,
    modifier : Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val summary by viewModel.summaryState.collectAsStateWithLifecycle()

    Scaffold(
        modifier            = modifier.fillMaxSize().safeDrawingPadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor      = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        SessionSummaryContent(
            summary  = summary,
            onBack   = onBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CONTENT
// ─────────────────────────────────────────────────────────────────────────────

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

    Box(modifier = modifier.fillMaxSize()) {

        // FIX: confetti only on success (accuracy ≥ 70%)
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
                .padding(horizontal = Spacing.Spacing20)
                .padding(top = 132.adp, bottom = Spacing.Spacing32),
        ) {
            AnimatedVisibility(
                visible = visible,
                enter   = scaleIn(spring(Spring.StiffnessMediumLow, Spring.DampingRatioMediumBouncy)) + fadeIn(tween(300)),
            ) {
                SummaryHero(
                    accuracyPct = accuracyPct,
                    packTitle   = summary.packTitle,
                    duration    = summary.durationFormatted,
                )
            }

            Spacer(Modifier.height(Spacing.Spacing24))

            StatsGrid(
                correctCount  = summary.correctCount,
                answeredCount = summary.answeredCount,
                accuracyPct   = accuracyPct,
                xp            = xp,
                duration      = summary.durationFormatted,
                visible       = visible,
            )

            if (summary.newQuestionCount > 0 || summary.reviewedQuestionCount > 0) {
                Spacer(Modifier.height(Spacing.Spacing12))
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
                Spacer(Modifier.height(Spacing.Spacing16))
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

            Spacer(Modifier.height(Spacing.Spacing16))

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(280, 500)) + slideInVertically(tween(280, 500)) { it / 3 },
            ) {
                SrsNoteCard()
            }

            if (summary.leechCount > 0) {
                Spacer(Modifier.height(Spacing.Spacing12))
                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(280, 600)) + slideInVertically(tween(280, 600)) { it / 3 },
                ) {
                    LeechSummaryCard(
                        leechCount         = summary.leechCount,
                        leechQuestionTexts = summary.leechQuestionTexts,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.Spacing24))

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(280, 700)) + slideInVertically(tween(280, 700)) { it / 3 },
            ) {
                BackButton(onClick = onBack)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryHero(
    accuracyPct: Int,
    packTitle  : String,
    duration   : String,
    modifier   : Modifier = Modifier,
) {
    val typo        = MaterialTheme.synapse.typographyTokens
    val levelColors = MaterialTheme.synapse.levelColors

    val heroColors = when {
        accuracyPct >= 80 -> levelColors.basic
        accuracyPct >= 50 -> levelColors.normal
        else              -> levelColors.master
    }
    val heroColor   = heroColors.accentColor
    val headlineRes = when {
        accuracyPct >= 80 -> R.string.summary_headline_outstanding
        accuracyPct >= 50 -> R.string.summary_headline_good_work
        else              -> R.string.summary_headline_keep_practicing
    }
    // FIX: use correct icon per tier — trophy for outstanding, target otherwise
    val iconRes = if (accuracyPct >= 80) R.drawable.ic_trophy else R.drawable.ic_target
    val iconCd  = if (accuracyPct >= 80) R.string.summary_cd_trophy else R.string.summary_cd_target

    val pulse = rememberInfiniteTransition(label = "hero_pulse")
    val auraScale by pulse.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.55f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "aura_scale",
    )
    val auraAlpha by pulse.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "aura_alpha",
    )
    val iconScale by pulse.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.07f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "icon_scale",
    )

    Column(
        modifier            = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier         = Modifier.size(96.adp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleX = auraScale; scaleY = auraScale; alpha = auraAlpha }
                    .drawBehind { drawCircle(color = heroColor) },
            )
            Box(
                modifier = Modifier
                    .size(96.adp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(heroColor.copy(alpha = 0.78f), heroColor))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter            = painterResource(iconRes),
                    contentDescription = stringResource(iconCd),
                    // FIX: was Color.Transparent — icon was invisible
                    tint               = Color.White,
                    modifier           = Modifier.size(44.adp),
                )
            }
        }

        Spacer(Modifier.height(Spacing.Spacing20))

        Text(
            text       = stringResource(headlineRes),
            style      = typo.headlineMedium,
            fontWeight = FontWeight.Black,
            color      = MaterialTheme.colorScheme.onSurface,
            textAlign  = TextAlign.Center,
        )

        if (packTitle.isNotBlank()) {
            Spacer(Modifier.height(Spacing.Spacing4))
            Text(
                text      = packTitle,
                style     = typo.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(Spacing.Spacing12))

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.adp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .padding(horizontal = Spacing.Spacing12, vertical = Spacing.Spacing6),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing6),
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_clock),
                contentDescription = stringResource(R.string.summary_cd_clock),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(12.adp),
            )
            Text(
                text       = duration,
                style      = typo.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATS 2 × 2 GRID
// ─────────────────────────────────────────────────────────────────────────────

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
        accuracyPct >= 80 -> levelColors.basic
        accuracyPct >= 60 -> levelColors.normal
        else              -> levelColors.master
    }

    data class StatDef(val labelRes: Int, val value: String, val statusColors: StatusColors, val delay: Int)

    val stats = listOf(
        StatDef(R.string.summary_stat_score,    stringResource(R.string.summary_score_format, correctCount, answeredCount), scoreColors,            60),
        StatDef(R.string.summary_stat_accuracy, stringResource(R.string.summary_accuracy_format, accuracyPct),              accuracyColors,         140),
        StatDef(R.string.summary_stat_xp,       stringResource(R.string.summary_xp_format, xp),                            levelColors.normal,     220),
        StatDef(R.string.summary_stat_duration, duration,                                                                   levelColors.elite,      300),
    )

    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
    ) {
        listOf(0, 2).forEach { rowStart ->
            Row(
                modifier              = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
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
    val typo  = MaterialTheme.synapse.typographyTokens
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = modifier.shadow(4.adp, shape, clip = false,
            ambientColor = statusColors.accentColor.copy(alpha = 0.08f),
            spotColor    = statusColors.accentColor.copy(alpha = 0.14f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .background(statusColors.bgColor)
                .border(1.adp, statusColors.borderColor, shape)
                .padding(Spacing.Spacing16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text       = value,
                style      = typo.headlineMedium,
                fontWeight = FontWeight.Black,
                color      = statusColors.accentColor,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.Spacing6))
            Text(
                text      = label,
                style     = typo.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SESSION BREAKDOWN ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SessionBreakdownRow(
    newCount     : Int,
    reviewedCount: Int,
    modifier     : Modifier = Modifier,
) {
    val levelColors = MaterialTheme.synapse.levelColors

    Row(
        modifier              = modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
    ) {
        BreakdownChip(
            iconRes      = R.drawable.ic_zap,
            label        = stringResource(R.string.summary_new_cards, newCount),
            statusColors = levelColors.basic,
            modifier     = Modifier.weight(1f).fillMaxHeight(),
        )
        BreakdownChip(
            iconRes      = R.drawable.ic_clock,
            label        = stringResource(R.string.summary_reviewed_cards, reviewedCount),
            statusColors = levelColors.elite,
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
    val typo  = MaterialTheme.synapse.typographyTokens
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier.shadow(2.adp, shape, clip = false,
            ambientColor = statusColors.accentColor.copy(alpha = 0.06f),
            spotColor    = statusColors.accentColor.copy(alpha = 0.10f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .background(statusColors.bgColor)
                .border(1.adp, statusColors.borderColor, shape)
                .padding(horizontal = Spacing.Spacing14, vertical = Spacing.Spacing12),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
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
}

// ─────────────────────────────────────────────────────────────────────────────
// PERFORMANCE BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PerformanceBar(
    correctCount : Int,
    answeredCount: Int,
    modifier     : Modifier = Modifier,
) {
    val typo        = MaterialTheme.synapse.typographyTokens
    val levelColors = MaterialTheme.synapse.levelColors
    val incorrect   = answeredCount - correctCount

    val correctColor   = levelColors.basic.accentColor
    val incorrectColor = levelColors.master.accentColor

    val animCorrect by animateFloatAsState(
        targetValue   = correctCount.toFloat() / answeredCount,
        animationSpec = tween(700, 100, FastOutSlowInEasing),
        label         = "bar_correct",
    )
    val animIncorrect by animateFloatAsState(
        targetValue   = incorrect.toFloat() / answeredCount,
        animationSpec = tween(700, 260, FastOutSlowInEasing),
        label         = "bar_incorrect",
    )
    val remainder = (1f - animCorrect - animIncorrect).coerceAtLeast(0f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(8.adp).clip(MaterialTheme.shapes.small),
        ) {
            if (animCorrect   > 0f)     Box(Modifier.weight(animCorrect).fillMaxSize().background(correctColor))
            if (animIncorrect > 0f)     Box(Modifier.weight(animIncorrect).fillMaxSize().background(incorrectColor))
            if (remainder     > 0.001f) Box(Modifier.weight(remainder).fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        }

        Spacer(Modifier.height(Spacing.Spacing8))

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
            style    = typo.labelSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SRS NOTE CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SrsNoteCard(modifier: Modifier = Modifier) {
    val typo    = MaterialTheme.synapse.typographyTokens
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        color    = primary.copy(alpha = 0.07f),
    ) {
        Row(
            modifier = Modifier
                .border(1.adp, primary.copy(alpha = 0.18f), MaterialTheme.shapes.large)
                .padding(Spacing.Spacing16),
            verticalAlignment     = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
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
                    append(stringResource(R.string.summary_srs_body))
                },
                style      = typo.bodySmall,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.asp,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEECH SUMMARY CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LeechSummaryCard(
    leechCount        : Int,
    // FIX: leechQuestionTexts is always empty because SessionViewModel hardcodes
    // leechQuestionTexts = emptyList(). Fix in SessionViewModel.toUiSummary() by
    // wiring the actual leech question texts from the engine's SessionSummary.
    // When populated, real question texts appear; otherwise numbered placeholders.
    leechQuestionTexts: List<String>,
    modifier          : Modifier = Modifier,
) {
    val typo       = MaterialTheme.synapse.typographyTokens
    val errorColor = MaterialTheme.colorScheme.error

    var isExpanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue   = if (isExpanded) 180f else 0f,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label         = "chevron",
    )

    // Show real texts when available, numbered placeholders as fallback
    val displayTexts = leechQuestionTexts.ifEmpty {
        List(leechCount) { i -> stringResource(R.string.summary_leech_placeholder, i + 1) }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        color    = errorColor.copy(alpha = 0.07f),
    ) {
        Column(
            modifier = Modifier
                .border(1.5.adp, errorColor.copy(alpha = 0.25f), MaterialTheme.shapes.large)
                .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(Spacing.Spacing16),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.adp)
                        .clip(MaterialTheme.shapes.small)
                        .background(errorColor.copy(alpha = 0.12f))
                        .border(1.adp, errorColor.copy(alpha = 0.25f), MaterialTheme.shapes.small),
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
                        text = pluralStringResource(R.plurals.summary_leech_title, leechCount, leechCount),
                        style      = typo.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = errorColor,
                    )
                    Text(
                        text  = stringResource(if (isExpanded) R.string.summary_leech_tap_hide else R.string.summary_leech_tap_view),
                        style = typo.labelSmall,
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
                    modifier  = Modifier.padding(horizontal = Spacing.Spacing16),
                    color     = errorColor.copy(alpha = 0.14f),
                    thickness = 1.adp,
                )
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(Spacing.Spacing12),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
                ) {
                    displayTexts.forEach { text ->
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
    val typo = MaterialTheme.synapse.typographyTokens

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(tint.copy(alpha = 0.07f))
            .padding(horizontal = Spacing.Spacing12, vertical = Spacing.Spacing10),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing10),
    ) {
        Box(
            modifier = Modifier.size(6.adp).clip(CircleShape).background(tint),
        )
        Text(
            text     = title,
            style    = typo.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BACK BUTTON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BackButton(
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(1.5.adp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.Spacing16),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = stringResource(R.string.summary_cta_back),
            style      = MaterialTheme.synapse.typographyTokens.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────

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
    SynapseTheme {
        SessionSummaryContent(summary = previewOutstanding, onBack = {})
    }
}

@Preview(name = "Average ≥50% + Leeches — Light", showBackground = true)
@Preview(name = "Average ≥50% + Leeches — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SummaryAveragePreview() {
    SynapseTheme {
        SessionSummaryContent(summary = previewAverage, onBack = {})
    }
}

@Preview(name = "Low <50% + Many Leeches — Light", showBackground = true)
@Preview(name = "Low <50% + Many Leeches — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SummaryLowPreview() {
    SynapseTheme {
        SessionSummaryContent(summary = previewLow, onBack = {})
    }
}