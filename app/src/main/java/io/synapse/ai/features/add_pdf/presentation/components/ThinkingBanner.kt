package io.synapse.ai.features.add_pdf.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.components.SynapseSwitch

@Composable
fun ThinkingBanner(
    enabled: Boolean,
    isLocked: Boolean,
    onToggle: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = stringResource(
        when {
            isLocked -> R.string.thinking_subtitle_upgrade
            enabled -> R.string.thinking_subtitle_active
            else -> R.string.thinking_subtitle_off
        }
    )

    SectionCard(
        modifier = modifier.clickable {
            if (isLocked) onNavigateToPremium() else onToggle()
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            // ── Icon box ──────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.adp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.synapse.gradients.accent),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_brain),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.adp),
                )
            }

            // ── Title + subtitle row ──────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.thinking_title),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.adp),
                    modifier = Modifier.padding(top = 2.adp),
                ) {
                    // Crown when locked; check-mark when active and unlocked.
                    when {
                        isLocked -> Icon(
                            painter = painterResource(R.drawable.ic_crown),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(12.adp),
                        )

                        enabled -> Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.adp),
                        )

                        else -> Unit
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!isLocked && enabled)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                }
            }

            // ── Switch (disabled when locked) ─────────────────────────
            SynapseSwitch(
                checked = enabled,
                onCheckedChange = { if (isLocked) onNavigateToPremium() else onToggle() },
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────

@Preview(name = "ThinkingBanner — Locked", showBackground = true)
@Composable
private fun ThinkingBannerLockedPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ThinkingBanner(
                enabled = false,
                isLocked = true,
                onToggle = {},
                onNavigateToPremium = {},
                modifier = Modifier.padding(16.adp)
            )
        }
    }
}

@Preview(name = "ThinkingBanner — Active", showBackground = true)
@Composable
private fun ThinkingBannerActivePreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ThinkingBanner(
                enabled = true,
                isLocked = false,
                onToggle = {},
                onNavigateToPremium = {},
                modifier = Modifier.padding(16.adp)
            )
        }
    }
}

@Preview(name = "ThinkingBanner — Off", showBackground = true)
@Composable
private fun ThinkingBannerOffPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ThinkingBanner(
                enabled = false,
                isLocked = false,
                onToggle = {},
                onNavigateToPremium = {},
                modifier = Modifier.padding(16.adp)
            )
        }
    }
}