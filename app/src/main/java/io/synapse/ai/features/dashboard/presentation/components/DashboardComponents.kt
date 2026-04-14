package io.synapse.ai.features.dashboard.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.components.PrimaryGradientButton
import io.synapse.ai.core.ui.utils.localized

@Immutable
private data class StatChipData(
    val labelRes   : Int,
    val value      : String,
    val subLabel   : String,
    val accentColor: Color,
    val bgColor    : Color,
    val iconRes    : Int,
)

@Composable
fun StatsRow(
    streak          : Int,
    accuracyPercent : Int,
    accuracyDelta   : Int?,
    accuracyDeltaRes: Int?,
    timeMinutes     : Int,
    modifier        : Modifier = Modifier,
) {
    val tokens      = MaterialTheme.synapse
    val levelColors = tokens.levelColors

    val chips = listOf(
        StatChipData(
            labelRes    = R.string.stat_streak_label,
            value       = streak.localized(),
            subLabel    = pluralStringResource(R.plurals.streak_days_noun, streak),
            accentColor = levelColors.gold.accentColor,
            bgColor     = levelColors.gold.bgColor,
            iconRes     = R.drawable.ic_zap,
        ),
        StatChipData(
            labelRes    = R.string.stat_accuracy_label,
            value       = "${accuracyPercent.localized()}${stringResource(R.string.percent_mark)}",
            subLabel    = if (accuracyDelta != null) {
                stringResource(R.string.stats_card_retention_delta, accuracyDelta)
            } else {
                accuracyDeltaRes?.let { stringResource(it) } ?: ""
            },
            accentColor = levelColors.success.accentColor,
            bgColor     = levelColors.success.bgColor,
            iconRes     = R.drawable.ic_target,
        ),
        StatChipData(
            labelRes    = R.string.stat_time_label,
            value       = stringResource(R.string.stat_time_value, timeMinutes),
            subLabel    = stringResource(R.string.stat_time_sub),
            accentColor = levelColors.accent.accentColor,
            bgColor     = levelColors.accent.bgColor,
            iconRes     = R.drawable.ic_clock,
        ),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.spacing.s6)
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s8),
    ) {
        chips.forEach { chip ->
            StatChip(
                label       = stringResource(chip.labelRes),
                value       = chip.value,
                subLabel    = chip.subLabel,
                accentColor = chip.accentColor,
                bgColor     = chip.bgColor,
                iconRes     = chip.iconRes,
                modifier    = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun StatChip(
    label      : String,
    value      : String,
    subLabel   : String,
    accentColor: Color,
    bgColor    : Color,
    iconRes    : Int,
    modifier   : Modifier = Modifier,
) {
    val tokens = MaterialTheme.synapse
    val shape  = MaterialTheme.shapes.large

    Card(
        modifier = modifier.dropShadow(
            shape  = shape,
            shadow = tokens.shadows.subtle.toShadow(customColor = accentColor),
        ),
        shape    = shape,
        colors   = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top =  tokens.spacing.s2)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .background(bgColor),
        ) {

            // ── Content ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = tokens.spacing.s12,
                        vertical   = tokens.spacing.s12,
                    ),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.s4),
            ) {

                // Icon + label
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s6),
                ) {
                    Box(
                        modifier = Modifier
                            .size(MaterialTheme.synapse.spacing.icon_xl)
                            .clip(MaterialTheme.shapes.small)
                            .background(accentColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter           = painterResource(iconRes),
                            contentDescription = null,
                            tint              = accentColor,
                            modifier          = Modifier.size(MaterialTheme.synapse.spacing.icon_xs),
                        )
                    }
                    Text(
                        text     = label,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = accentColor,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Value
                Text(
                    text       = value,
                    style      = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1,
                )

                // Sub-label
                Text(
                    text     = subLabel,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SectionHeader
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title      : String,
    onSeeAll   : () -> Unit,
    modifier   : Modifier = Modifier,
    showSeeAll : Boolean = true,
) {
    val spacing = MaterialTheme.synapse.spacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = spacing.s6,
                end   = spacing.s4,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color      = MaterialTheme.colorScheme.onBackground
        )

        if (showSeeAll) {
            TextButton(onClick = onSeeAll) {
                Text(
                    text       = stringResource(R.string.action_see_all).uppercase(),
                    style      = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color      = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(MaterialTheme.synapse.spacing.icon_xs),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EmptyPacksState
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EmptyPacksState(
    onAddPack: () -> Unit,
    modifier : Modifier = Modifier,
) {
    val tokens = MaterialTheme.synapse

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                shape = MaterialTheme.shapes.large,
            ),
        shape    = MaterialTheme.shapes.large,
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(tokens.spacing.s20),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.s14),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s12),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.adp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_file_plus),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(MaterialTheme.synapse.spacing.icon_md),
                    )
                }

                Text(
                    text       = stringResource(R.string.dashboard_empty_title),
                    style      = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color      = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text  = stringResource(R.string.dashboard_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PrimaryGradientButton(
                text    = stringResource(R.string.dashboard_empty_cta),
                iconRes = R.drawable.ic_file_plus,
                enabled = true,
                onClick = onAddPack,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Stats Row · Light", showBackground = true)
@Preview(name = "Stats Row · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StatsRowPreview() {
    SynapseTheme {
        StatsRow(
            streak          = 7,
            accuracyPercent = 78,
            accuracyDelta   = 4,
            accuracyDeltaRes = R.string.stats_card_this_week,
            timeMinutes     = 18,
            modifier        = Modifier.padding(
                horizontal = MaterialTheme.synapse.spacing.screen,
                vertical   = MaterialTheme.synapse.spacing.s4,
            ),
        )
    }
}

@Preview(name = "Section Header · Light", showBackground = true)
@Preview(name = "Section Header · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    SynapseTheme {
        SectionHeader(
            title    = "Jump Back In",
            onSeeAll = {},
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.screen),
        )
    }
}

@Preview(name = "Empty Packs · Light", showBackground = true)
@Preview(name = "Empty Packs · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EmptyPacksPreview() {
    SynapseTheme {
        EmptyPacksState(
            onAddPack = {},
            modifier  = Modifier.padding(MaterialTheme.synapse.spacing.screen),
        )
    }
}
