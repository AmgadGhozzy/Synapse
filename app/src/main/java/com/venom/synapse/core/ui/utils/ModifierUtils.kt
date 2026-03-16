package com.venom.synapse.core.ui.utils

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private fun dashEffect(dashOnPx: Float, dashOffPx: Float, phase: Float = 0f) =
    PathEffect.dashPathEffect(floatArrayOf(dashOnPx, dashOffPx), phase)

// ── Static ────────────────────────────────────────────────────────────────────
fun Modifier.dashedBorder(
    color: Color,
    width: Dp   = 1.dp,
    radius: Dp  = 0.dp,
    dashOn: Dp  = 8.dp,
    dashOff: Dp = 6.dp,
): Modifier = drawBehind {

    drawRoundRect(
        color        = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style        = Stroke(
            width      = width.toPx(),
            pathEffect = dashEffect(dashOn.toPx(), dashOff.toPx()), // phase = 0 → clean corner
        ),
    )
}

fun Modifier.dashedBorder(
    brush: Brush,
    width: Dp   = 1.dp,
    radius: Dp  = 0.dp,
    dashOn: Dp  = 8.dp,
    dashOff: Dp = 6.dp,
): Modifier = drawBehind {
    drawRoundRect(
        brush        = brush,
        cornerRadius = CornerRadius(radius.toPx()),
        style        = Stroke(
            width      = width.toPx(),
            pathEffect = dashEffect(dashOn.toPx(), dashOff.toPx()),
        ),
    )
}

// ── Animated (marching ants) ──────────────────────────────────────────────────
fun Modifier.animatedDashedBorder(
    color: Color,
    width: Dp       = 1.dp,
    radius: Dp      = 0.dp,
    dashOn: Dp      = 8.dp,
    dashOff: Dp     = 6.dp,
    durationMs: Int = 1_500,
): Modifier = composed {
    val intervalDp = (dashOn + dashOff).value
    val phase by rememberInfiniteTransition(label = "marching_ants").animateFloat(
        initialValue  = 0f,
        targetValue   = intervalDp,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    drawBehind {
        drawRoundRect(
            color        = color,
            cornerRadius = CornerRadius(radius.toPx()),
            style        = Stroke(
                width      = width.toPx(),
                pathEffect = dashEffect(dashOn.toPx(), dashOff.toPx(), phase * density),
            ),
        )
    }
}

fun Modifier.animatedDashedBorder(
    brush: Brush,
    width: Dp       = 1.dp,
    radius: Dp      = 0.dp,
    dashOn: Dp      = 8.dp,
    dashOff: Dp     = 6.dp,
    durationMs: Int = 1_500,
): Modifier = composed {
    val intervalDp = (dashOn + dashOff).value
    val phase by rememberInfiniteTransition(label = "marching_ants_brush").animateFloat(
        initialValue  = 0f,
        targetValue   = intervalDp,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    drawBehind {
        drawRoundRect(
            brush        = brush,
            style        = Stroke(
                width      = width.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(8.dp.toPx(), 5.dp.toPx()),
                    phase * density,
                ),
            ),
        )
    }
}