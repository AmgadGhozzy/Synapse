package com.venom.synapse.features.dashboard.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.Radius
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.theme.tokens.StatsCardTokens
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp
import com.venom.ui.components.common.localized

@Immutable
private data class StatChipData(
    val labelRes: Int,
    val value: String,
    val subLabel: String,
    val accentColor: Color,
    val bgColor: Color,
    val borderColor: Color,
    val iconRes: Int,
)

@Composable
fun StatsRow(
    streak: Int,
    accuracyPercent: Int,
    accuracyDelta: Int?,
    accuracyDeltaRes: Int?,
    timeMinutes: Int,
    modifier: Modifier = Modifier,
) {
    val levelColors = MaterialTheme.synapse.levelColors

    val chips = listOf(
        StatChipData(
            labelRes = R.string.stat_streak_label,
            value = streak.localized(),
            subLabel = pluralStringResource(R.plurals.streak_days_noun, streak),
            accentColor = levelColors.normal.accentColor,
            bgColor = levelColors.normal.bgColor,
            borderColor = levelColors.normal.borderColor,
            iconRes = R.drawable.ic_zap,
        ),
        StatChipData(
            labelRes = R.string.stat_accuracy_label,
            value = "${accuracyPercent.localized()}${stringResource(R.string.percent_mark)}",
            subLabel = if (accuracyDelta != null) {
                stringResource(R.string.stats_card_retention_delta, accuracyDelta)
            } else {
                accuracyDeltaRes?.let { stringResource(it) } ?: ""
            },
            accentColor = levelColors.basic.accentColor,
            bgColor = levelColors.basic.bgColor,
            borderColor = levelColors.basic.borderColor,
            iconRes = R.drawable.ic_target,
        ),
        StatChipData(
            labelRes = R.string.stat_time_label,
            value = stringResource(R.string.stat_time_value, timeMinutes),
            subLabel = stringResource(R.string.stat_time_sub),
            accentColor = levelColors.elite.accentColor,
            bgColor = levelColors.elite.bgColor,
            borderColor = levelColors.elite.borderColor,
            iconRes = R.drawable.ic_clock,
        ),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing10),
    ) {
        chips.forEach { chip ->
            StatChip(
                label = stringResource(chip.labelRes),
                value = chip.value,
                subLabel = chip.subLabel,
                accentColor = chip.accentColor,
                bgColor = chip.bgColor,
                borderColor = chip.borderColor,
                iconRes = chip.iconRes,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    subLabel: String,
    accentColor: Color,
    bgColor: Color,
    borderColor: Color,
    iconRes: Int,
    modifier: Modifier = Modifier,
) {

    Box(
        modifier = modifier.dropShadow(
            shape = StatsCardTokens.Shape,
            shadow = StatsCardTokens.Shadow.toShadow(customColor = accentColor)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(StatsCardTokens.Shape)
                .background(MaterialTheme.colorScheme.surface)
                .background(bgColor)
                .padding(StatsCardTokens.Padding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StatsCardTokens.IconLabelGap),
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(StatsCardTokens.IconSize),
                )
                Text(
                    text = label,
                    style = StatsCardTokens.LabelFontStyle,
                    color = accentColor,
                    letterSpacing = 0.07.asp,
                )
            }

            Text(
                text = value,
                style = StatsCardTokens.ValueFontStyle,
                color = accentColor,
            )

            Text(
                text = subLabel,
                style = StatsCardTokens.SubFontStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.Spacing4),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        TextButton(onClick = onSeeAll) {
            Text(
                text = stringResource(R.string.action_see_all),
                fontSize = 12.asp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.adp),
            )
        }
    }
}

@Composable
fun EmptyPacksState(
    onAddPack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onAddPack,
        modifier = modifier
            .fillMaxWidth()
            .height(140.adp),
        shape = Radius.ShapeXXL,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            Spacing.Spacing2 / 2,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file_plus),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(32.adp),
            )
            Spacer(Modifier.height(Spacing.Spacing12))
            Text(
                text = stringResource(R.string.empty_packs_hint),
                fontSize = 13.asp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Preview(name = "Stats Row · Light", showBackground = true)
@Preview(name = "Stats Row · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StatsRowPreview() {
    SynapseTheme {
        StatsRow(
            streak = 7,
            accuracyPercent = 78,
            accuracyDelta = 4,
            accuracyDeltaRes = R.string.stats_card_this_week,
            timeMinutes = 18,
            modifier = Modifier.padding(
                horizontal = Spacing.ScreenHorizontalPadding,
                vertical = Spacing.Spacing4,
            ),
        )
    }
}

@Preview(name = "Section Header · Light", showBackground = true)
@Preview(
    name = "Section Header · Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun SectionHeaderPreview() {
    SynapseTheme {
        SectionHeader(
            title = "Jump Back In",
            onSeeAll = {},
            modifier = Modifier.padding(Spacing.ScreenHorizontalPadding),
        )
    }
}

@Preview(name = "Empty Packs · Light", showBackground = true)
@Preview(
    name = "Empty Packs · Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun EmptyPacksPreview() {
    SynapseTheme {
        EmptyPacksState(
            onAddPack = {},
            modifier = Modifier.padding(Spacing.ScreenHorizontalPadding),
        )
    }
}