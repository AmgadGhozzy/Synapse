package io.synapse.ai.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import io.synapse.ai.core.theme.tokens.adp

@Preview
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyProgressIndicator(
    progress: Float = 0.5f,
    modifier: Modifier = Modifier,
    height: Dp = 8.adp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    LinearWavyProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape),
        color = color,
        trackColor = trackColor
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyLoadingIndicator(
    modifier: Modifier = Modifier.padding(vertical = 3.adp).width(80.adp),
    color: Color = MaterialTheme.colorScheme.onPrimary,
    trackColor: Color = Color.Transparent,
    waveSpeed: Dp = 30.adp,
    amplitude: Float = 2f,
) {
    LinearWavyProgressIndicator(
        progress = { 1f },
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        waveSpeed = waveSpeed,
        amplitude = { amplitude }
    )
}