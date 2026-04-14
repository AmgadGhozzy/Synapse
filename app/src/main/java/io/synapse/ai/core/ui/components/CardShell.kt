package io.synapse.ai.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.LocalAdaptiveScale
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CardShell(
    color: Color,
    bgGrad: Brush,
    shape: Shape = MaterialTheme.shapes.extraLargeIncreased,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scale = LocalAdaptiveScale.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = shape,
                shadow = MaterialTheme.synapse.shadows.medium.toShadow(customColor = color)
            )
            .border(
                width = Dp.Hairline,
                brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f))),
                shape = shape,
            )
            .clip(shape)
            .background(bgGrad)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 48.adp, y = (-56).adp)
                .size(172.adp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
                        radius = 172.adp.value * 1.64f,
                    ),
                    shape = CircleShape,
                )
        )
        content()
    }
}