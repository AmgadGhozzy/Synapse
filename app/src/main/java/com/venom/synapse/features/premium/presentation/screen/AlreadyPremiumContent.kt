package com.venom.synapse.features.premium.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.venom.synapse.R
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.features.premium.presentation.components.AmbientOrb
import com.venom.synapse.features.premium.presentation.components.AppIconDisplay
import com.venom.ui.components.common.adp

@Composable
fun AlreadyPremiumContent(
    onDismiss: () -> Unit,
    openSubManage: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AmbientOrb(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            size = 280.adp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.adp, y = (-80).adp),
        )
        AmbientOrb(
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
            size = 240.adp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).adp, y = 120.adp),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.synapse.spacing.s32),
        ) {

            AppIconDisplay()

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s8))

            Text(
                text = stringResource(R.string.premium_already_active_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.premium_already_active_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s8))

            // Primary CTA — opens Play billing
            val gradients = MaterialTheme.synapse.gradients
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.synapse.radius.lg)
                    .background(gradients.gold)
                    .clickable { openSubManage() }
                    .padding(vertical = MaterialTheme.synapse.spacing.s16),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.premium_manage_subscription),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(0.9f),
                )
            }

            // Restore purchases button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.synapse.radius.lg)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onRestore() }
                    .padding(vertical = MaterialTheme.synapse.spacing.s16),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.premium_restore_purchases),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(R.string.premium_skip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(
                        vertical = MaterialTheme.synapse.spacing.s10,
                        horizontal = MaterialTheme.synapse.spacing.s16,
                    ),
            )
        }
    }
}
