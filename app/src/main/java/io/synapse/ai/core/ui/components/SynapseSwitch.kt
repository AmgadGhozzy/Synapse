package io.synapse.ai.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.synapse.ai.core.theme.tokens.adp

@Composable
fun SynapseSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedIconRes: Int? = null,
    uncheckedIconRes: Int? = null,
) {
    val iconRes = if (checked) checkedIconRes else uncheckedIconRes
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        thumbContent = if (iconRes != null) {
            {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(12.adp),
                )
            }
        } else null,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White.copy(alpha = 0.9f),
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkedBorderColor = Color.Transparent,

            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedBorderColor = Color.Transparent,
        ),
    )
}