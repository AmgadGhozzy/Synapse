package io.synapse.ai.features.export.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow

@Composable
fun ExportActionButton(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes subtitleRes: Int? = null,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                MaterialTheme.shapes.medium,
                MaterialTheme.synapse.shadows.subtle.toShadow(),
            ),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = if (isPrimary) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.adp)
                    .clip(MaterialTheme.synapse.radius.md)
                    .background(
                        if (isPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primaryContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_md),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle ?: stringResource(subtitleRes!!),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPrimary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(end = MaterialTheme.synapse.spacing.s2)
                    .size(MaterialTheme.synapse.spacing.icon_md),
            )
        }
    }
}

