package io.synapse.ai.core.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow

@Composable
fun StatusIconHeader(
    iconRes: Int,
    iconCd: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 104.adp,
    iconSize: Dp = 52.adp,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val enterScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
    )

    val pulse = rememberInfiniteTransition()

    val auraScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        )
    )
    val auraAlpha by pulse.animateFloat(
        initialValue = 0.28f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        )
    )

    val outerSize = size + 56.adp

    Box(
        modifier = modifier.size(outerSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(outerSize)
                .graphicsLayer { scaleX = enterScale; scaleY = enterScale },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = auraScale
                        scaleY = auraScale
                        alpha = auraAlpha
                    }
                    .drawBehind { drawCircle(color = accentColor) },
            )

            Box(
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = (auraScale - 0.20f).coerceAtLeast(1f)
                        scaleY = (auraScale - 0.20f).coerceAtLeast(1f)
                        alpha = (auraAlpha * 0.55f)
                    }
                    .drawBehind { drawCircle(color = accentColor) },
            )
            // Icon circle
            Box(
                modifier = Modifier
                    .size(size)
                    .dropShadow(
                        shape = CircleShape,
                        shadow = MaterialTheme.synapse.shadows.strong.toShadow(
                            customColor = accentColor,
                            customAlpha = 0.45f,
                        ),
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accentColor.copy(alpha = 0.70f),
                                accentColor,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = stringResource(iconCd),
                    tint = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}