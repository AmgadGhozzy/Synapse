package io.synapse.ai.core.ui.utils


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private fun dashEffect(dashOnPx: Float, dashOffPx: Float, phase: Float = 0f) =
    PathEffect.dashPathEffect(floatArrayOf(dashOnPx, dashOffPx), phase)

fun Modifier.dashedBorder(
    color: Color,
    shape: Shape = RectangleShape,
    width: Dp = 1.dp,
    dashOn: Dp = 8.dp,
    dashOff: Dp = 6.dp,
): Modifier = drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(
        outline = outline,
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = dashEffect(dashOn.toPx(), dashOff.toPx()),
        ),
    )
}

fun Modifier.dashedBorder(
    brush: Brush,
    shape: Shape = RectangleShape,
    width: Dp = 1.dp,
    dashOn: Dp = 8.dp,
    dashOff: Dp = 6.dp,
): Modifier = drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(
        outline = outline,
        brush = brush,
        style = Stroke(
            width = width.toPx(),
            pathEffect = dashEffect(dashOn.toPx(), dashOff.toPx()),
        ),
    )
}

fun Modifier.animatedDashedBorder(
    color: Color,
    shape: Shape = RectangleShape,
    width: Dp = 1.dp,
    dashOn: Dp = 8.dp,
    dashOff: Dp = 6.dp,
    durationMs: Int = 1_500,
): Modifier = composed {
    val intervalDp = (dashOn + dashOff).value
    val phase by rememberInfiniteTransition(label = "marching_ants").animateFloat(
        initialValue = 0f,
        targetValue = intervalDp,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        )
    )
    drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(
            outline = outline,
            color = color,
            style = Stroke(
                width = width.toPx(),
                pathEffect = dashEffect(dashOn.toPx(), dashOff.toPx(), phase * density),
            ),
        )
    }
}

fun Modifier.animatedDashedBorder(
    brush: Brush,
    shape: Shape = RectangleShape,
    width: Dp = 1.dp,
    dashOn: Dp = 8.dp,
    dashOff: Dp = 6.dp,
    durationMs: Int = 1_500,
): Modifier = composed {
    val intervalDp = (dashOn + dashOff).value
    val phase by rememberInfiniteTransition(label = "marching_ants_brush").animateFloat(
        initialValue = 0f,
        targetValue = intervalDp,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        )
    )
    drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(
            outline = outline,
            brush = brush,
            style = Stroke(
                width = width.toPx(),
                pathEffect = dashEffect(dashOn.toPx(), dashOff.toPx(), phase * density),
            ),
        )
    }
}