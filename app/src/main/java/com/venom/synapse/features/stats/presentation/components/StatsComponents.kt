package com.venom.synapse.features.stats.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.HeroCardTokens
import com.venom.synapse.core.ui.components.CardShell
import com.venom.synapse.features.stats.presentation.state.DayActivity
import com.venom.synapse.features.stats.presentation.state.StatsUiState
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

@Composable
fun StreakHeroCard(
    currentStreak: Int,
    bestStreak: Int,
    streakActiveDays: List<Boolean>,
    todayDayIndex: Int,
    modifier: Modifier = Modifier,
) {
    val gold     = MaterialTheme.synapse.semantic.gold
    val goldGrad = MaterialTheme.synapse.gradients.gold
    val bgGrad   = MaterialTheme.synapse.gradients.streakHero

    CardShell(
        color     = gold,
        bgGrad   = bgGrad,
        modifier = modifier,
    ) {
        Column(Modifier.padding(HeroCardTokens.Padding)) {
            // ── Streak header row ─────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.adp),
            ) {
                // Flame icon box
                Box(
                    modifier = Modifier
                        .size(64.adp)
                        .clip(RoundedCornerShape(16.adp))
                        .background(goldGrad),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_flame),
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(30.adp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.adp)) {
                    Text(
                        text       = stringResource(R.string.stats_current_streak_label),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = gold.copy(alpha = 0.6f),
                    )
                    Row(
                        verticalAlignment     = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.adp),
                    ) {
                        Text(
                            text       = "$currentStreak",
                            fontSize   = 42.asp,
                            fontWeight = FontWeight.Black,
                            color      = gold,
                            lineHeight = 42.asp,
                        )
                        Text(
                            text       = stringResource(R.string.stats_days_unit),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = gold,
                            modifier   = Modifier.padding(bottom = 6.adp),
                        )
                    }
                    Text(
                        text  = stringResource(R.string.stats_streak_best_hint, bestStreak),
                        style = MaterialTheme.typography.labelSmall,
                        color = gold.copy(alpha = 0.5f),
                    )
                }
            }

            Spacer(Modifier.height(16.adp))

            // ── Day dot row ───────────────────────────────────────────────────
            val dayLetters = stringResource(R.string.stats_week_day_letters).split(",")
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.adp),
            ) {
                dayLetters.forEachIndexed { index, letter ->
                    StreakDayDot(
                        letter   = letter.trim(),
                        isActive = streakActiveDays.getOrElse(index) { false },
                        isToday  = index == todayDayIndex,
                        isFuture = index > todayDayIndex,
                        gold     = gold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakDayDot(
    letter: String,
    isActive: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    gold: Color,
    modifier: Modifier = Modifier,
) {
    val dotAlpha = when {
        isActive -> 0.80f
        isFuture -> 0.10f
        else     -> 0.25f
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.adp),
    ) {
        Box(
            modifier = Modifier
                .size(32.adp)
                .clip(CircleShape)
                .background(gold.copy(alpha = dotAlpha))
                .then(
                    if (isToday) Modifier.border(2.adp, gold, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isActive) {
                Icon(
                    painter = painterResource(R.drawable.ic_flame),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.adp),
                )
            } else {
                Text(
                    text = "—",
                    fontSize = 11.sp,
                    color = gold.copy(alpha = 0.4f),
                )
            }
        }
        Text(
            text = letter,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = gold.copy(alpha = 0.5f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stat Cards Grid
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatCardsGrid(
    state: StatsUiState,
    modifier: Modifier = Modifier,
) {
    val avgPct = (state.averageAccuracy * 100).toInt()
    val semanticColors = MaterialTheme.synapse.semantic
    
    val deltaColor = when {
        state.retentionDeltaPct > 0 -> semanticColors.success
        state.retentionDeltaPct < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.adp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.adp)) {
            StatCard(
                iconRes = R.drawable.ic_target,
                label = stringResource(R.string.stats_card_avg_retention),
                value = stringResource(R.string.stats_value_percent, avgPct),
                subtitle = stringResource(R.string.stats_card_retention_delta, state.retentionDeltaPct),
                iconTint = deltaColor,
                iconBg = if (state.retentionDeltaPct >= 0) semanticColors.successContainer else MaterialTheme.colorScheme.errorContainer,
                subtitleColor = deltaColor,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                iconRes = R.drawable.ic_book_open,
                label = stringResource(R.string.stats_card_cards_this_week),
                value = "${state.totalWeeklyCards}",
                subtitle = stringResource(R.string.stats_card_seven_day_total),
                iconTint = MaterialTheme.colorScheme.primary,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                subtitleColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.adp)) {
            StatCard(
                iconRes = R.drawable.ic_clock,
                label = stringResource(R.string.stats_card_time_studied),
                value = stringResource(R.string.stats_value_hours, state.timeStudiedHours),
                subtitle = stringResource(R.string.stats_card_this_week),
                iconTint = semanticColors.accent,
                iconBg = semanticColors.accentContainer,
                subtitleColor = semanticColors.accent,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                iconRes = R.drawable.ic_award,
                label = stringResource(R.string.stats_card_best_day),
                value = state.bestDayLabel,
                subtitle = stringResource(R.string.stats_card_cards_reviewed, state.bestDayCards),
                iconTint = semanticColors.gold,
                iconBg = semanticColors.goldContainer,
                subtitleColor = semanticColors.gold,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun StatCard(
    iconRes: Int,
    label: String,
    value: String,
    subtitle: String?,
    iconTint: Color,
    iconBg: Color,
    subtitleColor: Color,
    modifier: Modifier = Modifier,
) {
    // Animated value alpha — subtle entrance
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "stat_card_alpha",
    )
    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier = modifier.alpha(alpha),
        shape = RoundedCornerShape(20.adp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.adp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.adp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.adp),
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(36.adp)
                    .clip(RoundedCornerShape(10.adp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.adp),
                )
            }

            Spacer(Modifier.height(12.adp))

            // Value — HeadlineMedium (26sp Black) matching React's "22px, fontWeight 900"
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.adp),
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = subtitleColor,
                    modifier = Modifier.padding(top = 3.adp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chart Cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RetentionChartCard(
    weeklyActivity: List<DayActivity>,
    retentionDeltaPct: Int,
    modifier: Modifier = Modifier,
) {
    val successColor = MaterialTheme.synapse.semantic.success
    val deltaColor = when {
        retentionDeltaPct > 0 -> MaterialTheme.synapse.semantic.success
        retentionDeltaPct < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    ChartCard(
        title = stringResource(R.string.stats_chart_weekly_retention_title),
        subtitle = stringResource(R.string.stats_chart_weekly_retention_subtitle),
        badgeBg = if (retentionDeltaPct >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        badge = {
            Icon(
                painter = painterResource(
                    if (retentionDeltaPct >= 0) R.drawable.ic_trending_up else R.drawable.ic_trending_down
                ),
                contentDescription = null,
                tint = if (retentionDeltaPct >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(12.adp),
            )
            Text(
                text = stringResource(R.string.stats_chart_retention_delta, retentionDeltaPct),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = deltaColor,
            )
        },
        legendDotColor = MaterialTheme.synapse.semantic.success,
        legendLabel = stringResource(R.string.stats_chart_retention_legend),
        modifier = modifier,
    ) {
        SynapseAreaChart(
            values = weeklyActivity.map { it.accuracy * 100f },
            labels = weeklyActivity.map { it.dayLabel },
            lineColor = successColor,
            yAxisMinValue = 50.0,
            yAxisMaxValue = 100.0,
            chartHeight = 150.adp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ActivityChartCard(
    weeklyActivity: List<DayActivity>,
    totalWeeklyCards: Int,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    ChartCard(
        title = stringResource(R.string.stats_chart_daily_activity_title),
        subtitle = stringResource(R.string.stats_chart_daily_activity_subtitle),
        badgeBg = MaterialTheme.colorScheme.primaryContainer,
        badge = {
            Icon(
                painter = painterResource(R.drawable.ic_zap),
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(11.adp),
            )
            Text(
                text = stringResource(R.string.stats_chart_total_cards, totalWeeklyCards),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor,
            )
        },
        legendDotColor = null,
        legendLabel = null,
        modifier = modifier,
    ) {
        SynapseAreaChart(
            values = weeklyActivity.map { it.questionsStudied.toFloat() },
            labels = weeklyActivity.map { it.dayLabel },
            lineColor = primaryColor,
            chartHeight = 130.adp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ChartCard(
    title: String,
    subtitle: String,
    badgeBg: Color,
    badge: @Composable RowScope.() -> Unit,
    legendDotColor: Color?,
    legendLabel: String?,
    modifier: Modifier = Modifier,
    chart: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.adp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.adp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.adp),
    ) {
        Column(modifier = Modifier.padding(20.adp)) {
            // ── Header row ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                // Badge pill
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeBg)
                        .padding(horizontal = 10.adp, vertical = 4.adp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.adp),
                    content = badge,
                )
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.adp, bottom = 16.adp),
            )

            chart()

            // ── Legend ────────────────────────────────────────────────────────
            if (legendDotColor != null && !legendLabel.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 8.adp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.adp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.adp)
                            .clip(CircleShape)
                            .background(legendDotColor)
                    )
                    Text(
                        text = legendLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Synapse Area Chart — curved lines via cubic Bézier connector
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SynapseAreaChart(
    values: List<Float>,
    labels: List<String>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 150.adp,
    yAxisMinValue: Double? = null,
    yAxisMaxValue: Double? = null,
) {
    if (values.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(values) {
        modelProducer.runTransaction { lineSeries { series(values) } }
    }

    val areaFill = remember(lineColor) {
        Fill(Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.22f), Color.Transparent)))
    }
    val lineFill = remember(lineColor) { Fill(lineColor) }
    val dotFill  = remember(lineColor) { Fill(lineColor) }

    val rangeProvider = remember(yAxisMinValue, yAxisMaxValue) {
        if (yAxisMinValue != null && yAxisMaxValue != null)
            CartesianLayerRangeProvider.fixed(minY = yAxisMinValue, maxY = yAxisMaxValue)
        else
            CartesianLayerRangeProvider.auto()
    }

    val xFormatter = remember(labels) {
        CartesianValueFormatter { _, x, _ -> labels.getOrElse(x.toInt()) { "" } }
    }

    // Cubic Bézier point connector — smooth curves instead of sharp lines
    val cubicConnector = remember { LineCartesianLayer.PointConnector.cubic(curvature = 0.4f) }

    CartesianChartHost(
        modifier = modifier.height(chartHeight),
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(lineFill),
                        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.5.adp),
                        areaFill = LineCartesianLayer.AreaFill.single(areaFill),
                        pointProvider = LineCartesianLayer.PointProvider.single(
                            LineCartesianLayer.Point(
                                component = rememberShapeComponent(
                                    fill = dotFill,
                                    shape = CircleShape,
                                ),
                                size = 6.adp,
                            )
                        ),
                        // Smooth curve — cubic Bézier interpolation
                        pointConnector = cubicConnector,
                    )
                ),
                rangeProvider = rangeProvider,
            ),
            startAxis = VerticalAxis.rememberStart(
                line = null,
                tick = null,
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = null,
                tick = null,
                guideline = null,
                valueFormatter = xFormatter,
            ),
        ),
        modelProducer = modelProducer,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

private val previewState = StatsUiState(
    totalPacks = 5,
    totalQuestions = 120,
    currentStreak = 7,
    bestStreak = 21,
    streakActiveDays = listOf(true, true, true, true, true, false, false),
    todayDayIndex = 4,
    averageAccuracy = 0.77f,
    retentionDeltaPct = 4,
    totalWeeklyCards = 270,
    timeStudiedHours = 4.2f,
    bestDayLabel = "Thu",
    bestDayCards = 65,
    weeklyActivity = listOf(
        DayActivity("Mon", 32, 0.68f),
        DayActivity("Tue", 48, 0.74f),
        DayActivity("Wed", 21, 0.70f),
        DayActivity("Thu", 65, 0.82f),
        DayActivity("Fri", 54, 0.79f),
        DayActivity("Sat", 38, 0.85f),
        DayActivity("Sun", 12, 0.77f),
    ),
    isLoading = false,
)

@Preview(name = "StreakHeroCard · Light", showBackground = true)
@Preview(name = "StreakHeroCard · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StreakHeroCardPreview() {
    SynapseTheme {
        // SemanticColors would come from LocalSynapseSemanticColors in real usage
    }
}

@Preview(name = "StatCard · Light", showBackground = true)
@Preview(name = "StatCard · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StatCardPreview() {
    SynapseTheme {
        StatCard(
            iconRes = R.drawable.ic_target,
            label = stringResource(R.string.stats_card_avg_retention),
            value = "77%",
            subtitle = stringResource(R.string.stats_card_retention_delta, 4),
            iconTint = MaterialTheme.synapse.semantic.success,
            iconBg = MaterialTheme.synapse.semantic.successContainer,
            subtitleColor = MaterialTheme.synapse.semantic.success,
        )
    }
}
