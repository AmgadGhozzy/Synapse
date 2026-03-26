package com.venom.synapse.core.theme.tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ln

private const val BASE_WIDTH = 360f
private const val BASE_ASPECT_RATIO = 1.78f
private const val LOG_WEIGHT = 0.6f
private const val AR_WEIGHT = 0.00267f

internal object AdaptiveCache {
    private val map = ConcurrentHashMap<Int, Float>()

    fun get(baseDp: Float, width: Int, height: Int): Float {
        val key = hash(baseDp, width, height)
        return map.getOrPut(key) { calculate(baseDp, width.toFloat(), height.toFloat()) }
    }

    private fun hash(baseDp: Float, width: Int, height: Int): Int {
        var result = baseDp.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }

    private fun calculate(base: Float, width: Float, height: Float): Float {
        val widthRatio = width / BASE_WIDTH
        val logScale = ln(widthRatio + 1) * LOG_WEIGHT + (1 - LOG_WEIGHT)
        val aspectRatio = height / width
        val arAdjust = 1f + AR_WEIGHT * ln(aspectRatio / BASE_ASPECT_RATIO)
        return base * logScale * arAdjust
    }
}

// ── Composable extensions ─────────────────────────────────────────────────────

val Int.adp: Dp
    @Composable get() {
        val c = LocalConfiguration.current
        return AdaptiveCache.get(toFloat(), c.screenWidthDp, c.screenHeightDp).dp
    }

val Float.adp: Dp
    @Composable get() {
        val c = LocalConfiguration.current
        return AdaptiveCache.get(this, c.screenWidthDp, c.screenHeightDp).dp
    }

val Double.adp: Dp
    @Composable get() {
        val c = LocalConfiguration.current
        return AdaptiveCache.get(toFloat(), c.screenWidthDp, c.screenHeightDp).dp
    }

val Int.asp: TextUnit
    @Composable get() {
        val c = LocalConfiguration.current
        return AdaptiveCache.get(toFloat(), c.screenWidthDp, c.screenHeightDp).sp
    }

val Float.asp: TextUnit
    @Composable get() {
        val c = LocalConfiguration.current
        return AdaptiveCache.get(this, c.screenWidthDp, c.screenHeightDp).sp
    }

val Double.asp: TextUnit
    @Composable get() {
        val c = LocalConfiguration.current
        return AdaptiveCache.get(toFloat(), c.screenWidthDp, c.screenHeightDp).sp
    }

// ── Non-composable helpers ─────────────
fun adaptDp(value: Float, screenWidthDp: Int, screenHeightDp: Int): Dp =
    AdaptiveCache.get(value, screenWidthDp, screenHeightDp).dp
fun adaptDp(value: Dp, screenWidthDp: Int, screenHeightDp: Int): Dp =
    AdaptiveCache.get(value.value, screenWidthDp, screenHeightDp).dp

fun adaptSp(value: Float, screenWidthDp: Int, screenHeightDp: Int): TextUnit =
    AdaptiveCache.get(value, screenWidthDp, screenHeightDp).sp
