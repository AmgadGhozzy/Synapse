package io.synapse.ai.features.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.utils.animatedDashedBorder
import io.synapse.ai.core.ui.utils.localized

@Composable
fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    showSeeAll: Boolean = true,
) {

    Row(
        modifier = modifier
            .semantics { heading() }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        )

        if (showSeeAll) {
            TextButton(onClick = onSeeAll) {
                Text(
                    text = stringResource(R.string.action_see_all).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(MaterialTheme.synapse.spacing.s2))
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_lg),
                )
            }
        }
    }
}

@Composable
fun EmptyPacksState(
    onAddPack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.synapse
    val shape = MaterialTheme.shapes.large

    val addPackDescription = stringResource(R.string.fab_new_pack_description)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = addPackDescription }
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onAddPack() }
            .clip(shape)
            .animatedDashedBorder(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                shape = shape
            )
            .padding(horizontal = tokens.spacing.s20, vertical = 24.adp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Box(
            modifier = Modifier
                .size(56.adp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file_plus),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.adp)
            )
        }

        Spacer(modifier = Modifier.height(tokens.spacing.s16))

        Text(
            text = stringResource(R.string.dashboard_empty_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.25).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(tokens.spacing.s4))

        Text(
            text = stringResource(R.string.dashboard_empty_body),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = tokens.spacing.s8)
        )

    }
}

@Composable
fun DashboardKpiCard(
    streakDays: Int,
    accuracyPercent: Int,
    masteredCards: Int,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val tokens = MaterialTheme.synapse
    val semantic = tokens.semantic
    val shape = MaterialTheme.shapes.large

    Card(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(shape = shape, shadow = tokens.shadows.subtle.toShadow()),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = cs.surface)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = tokens.spacing.s16,
                vertical = tokens.spacing.s14
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.s8),
            ) {
                KpiChip(
                    value = streakDays.localized(),
                    label = stringResource(R.string.dashboard_kpi_streak),
                    valueColor = semantic.gold,
                    bgColor = semantic.goldBg,
                    modifier = Modifier.weight(1f),
                )
                KpiChip(
                    value = stringResource(R.string.profile_lifetime_pct_format, accuracyPercent),
                    label = stringResource(R.string.dashboard_kpi_accuracy),
                    valueColor = semantic.success,
                    bgColor = semantic.successBg,
                    modifier = Modifier.weight(1f),
                )
                KpiChip(
                    value = masteredCards.localized(),
                    label = stringResource(R.string.dashboard_kpi_mastered),
                    valueColor = semantic.primary,
                    bgColor = semantic.primaryBg,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun KpiChip(
    value: String,
    label: String,
    valueColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.adp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.adp, horizontal = 6.adp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(3.adp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
