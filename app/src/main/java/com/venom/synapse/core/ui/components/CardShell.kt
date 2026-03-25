package com.venom.synapse.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.HeroCardTokens
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.ui.components.common.adp

@Composable
fun CardShell(
    color: Color,
    bgGrad: Brush,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val cardStyle = MaterialTheme.synapse.components.heroCard
    Box(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = cardStyle.Shape,
                shadow = cardStyle.Shadow.toShadow(customColor = color)
            )
            .clip(cardStyle.Shape)
            .background(bgGrad)
            .border(0.5.adp, color.copy(alpha = 0.20f), HeroCardTokens.Shape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-56).dp)
                .size(172.dp)
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