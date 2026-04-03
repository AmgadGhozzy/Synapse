package com.venom.synapse.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.venom.synapse.core.theme.tokens.ShadowTokens
import com.venom.synapse.core.theme.tokens.adp
import com.venom.synapse.core.theme.tokens.toShadow

@Composable
fun CardShell(
    color: Color,
    bgGrad: Brush,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = MaterialTheme.shapes.large,
                shadow = ShadowTokens.Strong.toShadow(customColor = color)
            )
            .clip(MaterialTheme.shapes.large)
            .background(bgGrad)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 48.adp, y = (-56).adp)
                .size(172.adp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.15f), Color.Transparent),
                        radius = 220f,
                    ),
                    shape = CircleShape
                )
        )
        content()
    }
}