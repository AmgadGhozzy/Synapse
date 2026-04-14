package io.synapse.ai.core.theme.tokens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ln

private const val BASE_WIDTH        = 360f
private const val BASE_ASPECT_RATIO = 1.78f
private const val LOG_WEIGHT        = 0.6f
private const val AR_WEIGHT         = 0.00267f

fun computeAdaptiveScale(w: Int, h: Int): Float {
    val widthRatio = w / BASE_WIDTH
    val logScale   = ln(widthRatio + 1f) * LOG_WEIGHT + (1f - LOG_WEIGHT)
    val arAdjust   = 1f + AR_WEIGHT * ln((h.toFloat() / w) / BASE_ASPECT_RATIO)
    return logScale * arAdjust
}

val LocalAdaptiveScale = staticCompositionLocalOf { 1f }

val Int.adp: Dp
    @Composable get() = (this * LocalAdaptiveScale.current).dp

val Float.adp: Dp
    @Composable get() = (this * LocalAdaptiveScale.current).dp

val Double.adp: Dp
    @Composable get() = (toFloat() * LocalAdaptiveScale.current).dp

val Int.asp: TextUnit
    @Composable get() = (this * LocalAdaptiveScale.current).sp

val Float.asp: TextUnit
    @Composable get() = (this * LocalAdaptiveScale.current).sp

val Double.asp: TextUnit
    @Composable get() = (toFloat() * LocalAdaptiveScale.current).sp


fun adaptDp(value: Float, scale: Float): Dp    = (value * scale).dp
fun adaptDp(value: Dp,    scale: Float): Dp    = (value.value * scale).dp
fun adaptSp(value: Float, scale: Float): TextUnit = (value * scale).sp