package com.venom.synapse.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
fun LimitedFontScale(
    maxScale: Float = 1.2f,
    minScale: Float = 0.9f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current

    val limitedScale = density.fontScale
        .coerceIn(minScale, maxScale)

    val newDensity = Density(
        density = density.density,
        fontScale = limitedScale
    )

    CompositionLocalProvider(
        LocalDensity provides newDensity
    ) {
        content()
    }
}