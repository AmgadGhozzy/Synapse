package com.venom.synapse.features.profile.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.ui.components.common.adp

@Composable
fun ProfileStatRow(
    packCount: Int,
    cardCount: Int,
    streakDays: Int,
    accentColor: Color,
    successColor: Color,
    goldColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.adp),
    ) {
        ProfileStatChip(
            value      = "$packCount",
            label      = stringResource(R.string.profile_stat_packs),
            valueColor = accentColor,
            modifier   = Modifier.weight(1f),
        )
        ProfileStatChip(
            value      = "$cardCount",
            label      = stringResource(R.string.profile_stat_cards),
            valueColor = successColor,
            modifier   = Modifier.weight(1f),
        )
        ProfileStatChip(
            value      = pluralStringResource(R.plurals.streak_days, streakDays, streakDays)   ,
            label      = stringResource(R.string.profile_stat_streak),
            valueColor = goldColor,
            modifier   = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileStatChip(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(16.adp),
        color    = MaterialTheme.colorScheme.surface,
        border   = BorderStroke(
            1.adp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 12.adp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color      = valueColor,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(2.adp))
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Profile · Stats Row · Light", showBackground = true)
@Preview(name = "Profile · Stats Row · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ProfileStatRowPreview() {
    SynapseTheme {
        val semantic = MaterialTheme.synapse.semantic
        ProfileStatRow(
            packCount    = 4,
            cardCount    = 470,
            streakDays   = 7,
            accentColor  = MaterialTheme.colorScheme.secondary,
            successColor = semantic.success,
            goldColor    = semantic.gold,
            modifier     = Modifier.padding(16.adp),
        )
    }
}
