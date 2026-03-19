package com.venom.synapse.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.PrimaryButtonTokens
import com.venom.synapse.core.theme.tokens.ProBadgeTokens
import com.venom.synapse.core.theme.tokens.Spacing

/**
 * Full-width gradient CTA button.
 */
@Composable
fun PrimaryGradientButton(
    text: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val gradient: Brush = if (enabled) {
        MaterialTheme.synapse.gradients.cta
    } else {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant,
            )
        )
    }

    val contentColor = if (enabled) {
        Color.White.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(PrimaryButtonTokens.Height)
            .clip(PrimaryButtonTokens.Shape)
            .background(gradient)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            ),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PrimaryButtonTokens.IconTextGap),
        ) {
            Text(
                text  = text,
                style = PrimaryButtonTokens.FontStyle,
                color = contentColor,
            )
            when {
                icon != null -> {
                    Icon(
                        imageVector         = icon,
                        contentDescription  = null,
                        tint                = contentColor,
                        modifier            = Modifier.size(PrimaryButtonTokens.IconSize),
                    )
                }
                iconRes != null -> {
                    Icon(
                        painter            = painterResource(iconRes),
                        contentDescription = null,
                        tint               = contentColor,
                        modifier           = Modifier.size(PrimaryButtonTokens.IconSize),
                    )
                }
            }
        }
    }
}

/**
 * Clean outlined button for secondary / cancel actions.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(PrimaryButtonTokens.Height),
        shape   = PrimaryButtonTokens.Shape,
        border  = BorderStroke(
            width = Spacing.Spacing2 / 2,
            color = MaterialTheme.colorScheme.outline,
        ),
        colors  = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Inline PRO badge.
 */
@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    val gold = MaterialTheme.synapse.semantic.gold

    Surface(
        modifier = modifier,
        color    = gold.copy(alpha = 0.15f),
        shape    = ProBadgeTokens.Shape,
        border   = BorderStroke(
            width = ProBadgeTokens.BorderWidth,
            color = gold.copy(alpha = 0.50f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ProBadgeTokens.HorizontalPadding,
                vertical   = ProBadgeTokens.VerticalPadding,
            ),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ProBadgeTokens.IconTextGap),
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_crown),
                contentDescription = null,
                tint               = gold,
                modifier           = Modifier.size(ProBadgeTokens.IconSize),
            )
            Text(
                text  = stringResource(R.string.pro_badge_label),
                style = ProBadgeTokens.FontStyle,
                color = gold,
            )
        }
    }
}

@Preview(name = "Buttons — Light", showBackground = true)
@Preview(name = "Buttons — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ButtonsPreview() {
    SynapseTheme {
        Column(
            modifier  = Modifier.padding(Spacing.ScreenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
        ) {
            PrimaryGradientButton(
                text     = "Start Free Trial",
                icon     = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                enabled  = true,
                onClick  = {},
            )
            PrimaryGradientButton(
                text     = "Start Free Trial",
                icon     = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                enabled  = false,
                onClick  = {},
            )
            SecondaryButton(
                text    = "Maybe Later",
                onClick = {},
            )
            ProBadge()
        }
    }
}