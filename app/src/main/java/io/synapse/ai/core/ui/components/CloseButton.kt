package io.synapse.ai.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp

@Composable
fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.adp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.cd_back),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_sm),
        )
    }
}
