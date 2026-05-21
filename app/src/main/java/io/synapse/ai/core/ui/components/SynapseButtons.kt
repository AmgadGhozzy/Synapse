package io.synapse.ai.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp

/**
 * Full-width gradient CTA button.
 */
@Composable
fun PrimaryGradientButton(
    text: String,
    iconRes: Int? = null,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val gradient: Brush = if (enabled) {
        MaterialTheme.synapse.gradients.primary
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
            .height(60.adp)
            .clip(MaterialTheme.shapes.medium)
            .background(gradient)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
            )
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_xl),
                )
            }
        }
    }
}

@Composable
fun GuidedPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    showPulse: Boolean = true,
) {
    if (!showPulse) {
        PrimaryGradientButton(
            text = text,
            iconRes = iconRes,
            enabled = enabled,
            onClick = onClick,
            modifier = modifier,
        )
        return
    }

    val pulse = rememberInfiniteTransition()
    val haloScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    PrimaryGradientButton(
        text = text,
        iconRes = iconRes,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = haloScale
                scaleY = haloScale
            }
    )
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
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(60.adp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = Dp.Hairline,
            color = MaterialTheme.colorScheme.outline.copy(0.9f),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
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
        color = gold.copy(alpha = 0.15f),
        shape = MaterialTheme.synapse.radius.pill,
        border = BorderStroke(
            width = 1.adp,
            color = gold.copy(alpha = 0.50f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.synapse.spacing.s8,
                vertical = MaterialTheme.synapse.spacing.s4,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s4),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_crown),
                contentDescription = null,
                tint = gold,
                modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_xs),
            )
            Text(
                text = stringResource(R.string.pro_badge_label),
                style = MaterialTheme.typography.labelSmall,
                color = gold,
            )
        }
    }
}

