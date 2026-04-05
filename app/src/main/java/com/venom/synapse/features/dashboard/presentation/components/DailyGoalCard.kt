package com.venom.synapse.features.dashboard.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.Amber400
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.synapse.core.ui.components.CardShell
import com.venom.synapse.core.ui.utils.shake
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.localized
import com.venom.ui.components.other.CircularProgressRing

private const val SECONDS_PER_CARD = 8

@Composable
fun DailyGoalCard(
    todayStudied  : Int,
    dailyGoal     : Int,
    streakDays    : Int,
    totalDue      : Int,
    onStartStudying: () -> Unit,
    modifier      : Modifier = Modifier,
) {
    val progress = if (dailyGoal > 0) (todayStudied / dailyGoal.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1200)
    )
    val animatedStudied by animateIntAsState(
        targetValue   = todayStudied,
        animationSpec = tween(1000)
    )

    val isGoalComplete = dailyGoal in 1..todayStudied
    val ctaText = when {
        totalDue == 0   -> stringResource(R.string.goal_cta_all_caught_up)
        isGoalComplete  -> stringResource(R.string.goal_cta_extra_practice)
        else            -> stringResource(R.string.goal_cta_study_cards, totalDue)
    }

    val tokens = MaterialTheme.synapse

    CardShell(
        color  = MaterialTheme.colorScheme.primary,
        bgGrad = tokens.gradients.primary,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(top = tokens.spacing.s24, bottom = tokens.spacing.s20)
                .padding(horizontal = tokens.spacing.s24),
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    GoalLabel()
                    AnimatedCounterRow(
                        animatedStudied = animatedStudied.localized(),
                        dailyGoal       = dailyGoal,
                    )
                    GoalProgressBar(
                        progress = animatedProgress,
                        modifier = Modifier.padding(bottom = tokens.spacing.listItemGap),
                    )
                    GoalStatChips(
                        streakDays = streakDays,
                        totalDue   = totalDue,
                    )
                }

                CircularProgressRing(
                    progress      = progress,
                    progressColor = Color.White.copy(alpha = 0.9f),
                    trackColor    = Color.White.copy(alpha = 0.15f),
                    strokeWidthDp = 10.adp,
                    fontSize      = MaterialTheme.typography.headlineMedium.fontSize,
                    modifier      = Modifier.size(97.adp),
                )
            }

            Spacer(Modifier.height(tokens.spacing.s18))

            Surface(
                onClick      = onStartStudying,
                modifier     = Modifier
                    .fillMaxWidth()
                    .dropShadow(
                        shape  = MaterialTheme.shapes.medium,
                        shadow = tokens.shadows.medium.toShadow(
                            customColor = Color.White,
                            customAlpha = 0.1f,
                        ),
                    ),
                shape        = MaterialTheme.shapes.large,
                color        = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White.copy(0.9f),
            ) {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(vertical = tokens.spacing.s18),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s6),
                    ) {
                        Text(
                            text       = ctaText,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            modifier           = Modifier
                                .size(18.adp)
                                .shake(),
                        )
                    }

                    // ETA pill
                    if (totalDue > 0) {
                        CtaEtaBadge(
                            etaText  = etaLabel(totalDue),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = tokens.spacing.s14),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalLabel() {
    val tokens = MaterialTheme.synapse
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s4),
        modifier              = Modifier.padding(bottom = tokens.spacing.s6),
    ) {
        Icon(
            painter           = painterResource(R.drawable.ic_target),
            contentDescription = null,
            tint              = Color.White.copy(alpha = 0.9f),
            modifier          = Modifier.size(26.adp),
        )
        Text(
            text  = stringResource(R.string.daily_goal_label),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.9f),
        )
    }
}

@Composable
private fun AnimatedCounterRow(
    animatedStudied: String,
    dailyGoal      : Int,
) {
    val tokens = MaterialTheme.synapse
    Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s4)) {
        Text(
            text      = animatedStudied,
            style     = MaterialTheme.typography.displayLarge,
            color     = Color.White.copy(0.9f),
            modifier = Modifier.heightIn(max = 75.adp).alignByBaseline(),
        )
        Text(
            text       = stringResource(R.string.goal_denominator, dailyGoal),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = Color.White.copy(alpha = 0.8f),
            modifier   = Modifier.alignByBaseline(),
        )
        Text(
            text       = pluralStringResource(R.plurals.cards, dailyGoal),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = Color.White.copy(alpha = 0.8f),
            modifier   = Modifier.alignByBaseline(),
        )
    }
}

@Composable
private fun GoalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val radius = MaterialTheme.synapse.radius.pill
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.adp)
            .clip(radius)
            .background(Color.White.copy(alpha = 0.15f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(Color.White.copy(alpha = 0.9f)),
        )
    }
}

@Composable
private fun GoalStatChips(
    streakDays: Int,
    totalDue  : Int,
) {
    val tokens = MaterialTheme.synapse
    Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s6)) {
        HeroStatChip(
            label    = pluralStringResource(R.plurals.streak_days, streakDays, streakDays),
            emphasis = true,
            icon = {
                Icon(
                    painter           = painterResource(R.drawable.ic_zap),
                    contentDescription = null,
                    tint              = Amber400,
                    modifier          = Modifier
                        .size(16.adp)
                        .shake(),
                )
            },
        )
        HeroStatChip(
            label    = pluralStringResource(R.plurals.cards_due, totalDue, totalDue),
            emphasis = false,
            icon = {
                Icon(
                    painter           = painterResource(R.drawable.ic_clock),
                    contentDescription = null,
                    tint              = Color.White.copy(alpha = 0.55f),
                    modifier          = Modifier.size(16.adp),
                )
            },
        )
    }
}

@Composable
private fun CtaEtaBadge(
    etaText : String,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.synapse
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = tokens.spacing.s8, vertical = tokens.spacing.s3),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s3),
    ) {
        Icon(
            painter           = painterResource(R.drawable.ic_clock),
            contentDescription = null,
            tint              = Color.White.copy(alpha = 0.70f),
            modifier          = Modifier.size(12.adp),
        )
        Text(
            text  = etaText,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f),
        )
    }
}

@Composable
private fun HeroStatChip(
    label   : String,
    icon    : @Composable () -> Unit,
    emphasis: Boolean,
) {
    val tokens = MaterialTheme.synapse
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (emphasis) Color.White.copy(alpha = 0.13f)
                else          Color.White.copy(alpha = 0.10f)
            )
            .padding(horizontal = tokens.spacing.s10, vertical = tokens.spacing.s4),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s4),
    ) {
        icon()
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (emphasis) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color    = if (emphasis) Amber400 else Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun etaLabel(totalDue: Int): String {
    val totalMinutes = (totalDue * SECONDS_PER_CARD + 30) / 60
    return totalMinutes.toString()
}

@Composable
private fun etaLabel(totalDue: Int, unit: Nothing? = null): String {
    val totalMinutes = (totalDue * SECONDS_PER_CARD + 30) / 60
    return when {
        totalMinutes < 1  -> stringResource(R.string.goal_eta_less_than_min)
        totalMinutes < 60 -> pluralStringResource(R.plurals.goal_eta_minutes, totalMinutes, totalMinutes)
        else -> {
            val hours = totalMinutes / 60
            val mins  = totalMinutes % 60
            if (mins == 0) pluralStringResource(R.plurals.goal_eta_hours, hours, hours)
            else           stringResource(R.string.goal_eta_hours_minutes, hours, mins)
        }
    }
}


@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Arabic", showBackground = true, locale = "ar")
@Composable
private fun DailyGoalCardPreview() {
    SynapseTheme {
        DailyGoalCard(
            todayStudied    = 23,
            dailyGoal       = 30,
            streakDays      = 7,
            totalDue        = 87,
            onStartStudying = {},
            modifier        = Modifier.padding(MaterialTheme.synapse.spacing.screen),
        )
    }
}

@Preview(name = "All Caught Up", showBackground = true)
@Composable
private fun DailyGoalCardCaughtUpPreview() {
    SynapseTheme {
        DailyGoalCard(
            todayStudied    = 30,
            dailyGoal       = 30,
            streakDays      = 14,
            totalDue        = 0,
            onStartStudying = {},
            modifier        = Modifier.padding(MaterialTheme.synapse.spacing.screen),
        )
    }
}

@Preview(name = "Long Session", showBackground = true)
@Composable
private fun DailyGoalCardLongSessionPreview() {
    SynapseTheme {
        DailyGoalCard(
            todayStudied    = 5,
            dailyGoal       = 30,
            streakDays      = 3,
            totalDue        = 520,
            onStartStudying = {},
            modifier        = Modifier.padding(MaterialTheme.synapse.spacing.screen),
        )
    }
}