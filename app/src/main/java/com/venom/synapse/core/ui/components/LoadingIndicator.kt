package com.venom.synapse.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.venom.synapse.core.theme.synapse
import com.venom.ui.components.common.adp

@Preview
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.adp,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    ContainedLoadingIndicator(
        modifier = modifier.size(size),
        indicatorColor = indicatorColor,
        containerColor = containerColor
    )
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
            )
    )
    {
        LoadingIndicator(
            modifier = Modifier.align(Alignment.Center),
            size = MaterialTheme.synapse.spacing.screenContentTop
        )
    }
}