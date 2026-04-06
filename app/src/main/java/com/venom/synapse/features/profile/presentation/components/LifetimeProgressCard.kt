package com.venom.synapse.features.profile.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.localized

@Composable
fun LifetimeProgressCard(
    cardsLearned: Int,
    studyTimeHours: Float,
    avgRetentionPct: Float,
    modifier: Modifier = Modifier,
) {
    val cs       = MaterialTheme.colorScheme
    val semantic = MaterialTheme.synapse.semantic
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.adp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.adp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.adp, vertical = 14.adp)) {

            // ── Section header ────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.adp),
            ) {
                Icon(
                    painter           = painterResource(R.drawable.ic_trending_up),
                    contentDescription = null,
                    tint              = cs.primary,
                    modifier          = Modifier.size(15.adp),
                )
                Text(
                    text       = stringResource(R.string.profile_lifetime_progress_title),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = cs.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.adp))

            // ── Three metric chips ────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.adp),
            ) {
                LifetimeMetricChip(
                    value      = cardsLearned.localized(),
                    label      = stringResource(R.string.profile_lifetime_cards_learned),
                    valueColor = cs.primary,
                    modifier   = Modifier.weight(1f),
                )
                LifetimeMetricChip(
                    value      = stringResource(R.string.profile_lifetime_hours_format, studyTimeHours),
                    label      = stringResource(R.string.profile_lifetime_study_time),
                    valueColor = semantic.success,
                    modifier   = Modifier.weight(1f),
                )
                LifetimeMetricChip(
                    value      = stringResource(
                        R.string.profile_lifetime_pct_format,
                        (avgRetentionPct * 100).toInt(),
                    ),
                    label      = stringResource(R.string.profile_lifetime_avg_retention),
                    valueColor = semantic.gold,
                    modifier   = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LifetimeMetricChip(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(14.adp),
        color    = cs.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 10.adp, horizontal = 6.adp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = valueColor,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(3.adp))
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines  = 2,
            )
        }
    }
}

@Preview(name = "LifetimeProgressCard · Light", showBackground = true)
@Preview(name = "LifetimeProgressCard · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LifetimeProgressCardPreview() {
    SynapseTheme {
        LifetimeProgressCard(
            cardsLearned    = 470,
            studyTimeHours  = 12.4f,
            avgRetentionPct = 0.77f,
            modifier        = Modifier.padding(16.adp),
        )
    }
}
