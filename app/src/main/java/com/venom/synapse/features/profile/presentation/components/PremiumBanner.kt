package com.venom.synapse.features.profile.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.ui.components.CardShell
import com.venom.ui.components.common.adp

@Composable
fun PremiumBannerCard(
    gold: Color,
    goldGrad: Brush,
    bgGrad: Brush,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CardShell(
        color     = gold,
        bgGrad   = bgGrad,
        modifier = modifier,
    ) {
        Column(Modifier.padding(MaterialTheme.synapse.spacing.cardLarge)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.adp),
            ) {
                // Crown icon box
                Box(
                    modifier = Modifier
                        .size(56.adp)
                        .clip(RoundedCornerShape(16.adp))
                        .background(goldGrad),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_crown),
                        contentDescription = null,
                        tint               = Color.White.copy(0.9f),
                        modifier           = Modifier.size(24.adp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = stringResource(R.string.profile_premium_title),
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color      = gold,
                    )
                    Spacer(Modifier.height(2.adp))
                    Text(
                        text  = stringResource(R.string.profile_premium_subtitle),
                        style = MaterialTheme.typography.labelLarge,
                        color = gold.copy(alpha = 0.65f),
                    )
                }
            }

            Spacer(Modifier.height(20.adp))

            // ── CTA button ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.adp)
                    .clip(RoundedCornerShape(16.adp))
                    .background(goldGrad)
                    .semantics { role = Role.Button }
                    .clickable(onClick = onUpgrade),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.adp),
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_crown),
                        contentDescription = null,
                        tint               = Color.White.copy(0.9f),
                        modifier           = Modifier.size(16.adp),
                    )
                    Text(
                        text       = stringResource(R.string.profile_premium_cta),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White.copy(0.9f),
                    )
                }
            }
        }
    }
}

@Preview(name = "Profile · Premium Banner · Light", showBackground = true)
@Preview(name = "Profile · Premium Banner · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PremiumBannerPreview() {
    SynapseTheme {
        val synapse = MaterialTheme.synapse
        PremiumBannerCard(
            gold      = synapse.semantic.gold,
            goldGrad  = synapse.gradients.gold,
            bgGrad    = synapse.gradients.streakHero,
            onUpgrade = {},
            modifier  = Modifier.padding(16.adp),
        )
    }
}
