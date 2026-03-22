package com.venom.synapse.core.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.random.Random

fun Modifier.shake(
    offsetPx: Float = 6f,
    cycleDurationMs: Int = 80,
    cycles: Int = 3,
    intervalMs: Long = Random.nextLong(10000L, 20000L),
): Modifier = composed {
    val translationX = remember { Animatable(0f) }

    LaunchedEffect(offsetPx, cycleDurationMs, cycles, intervalMs) {

        val shakeSpec = tween<Float>(durationMillis = cycleDurationMs, easing = LinearEasing)
        val settleSpec = tween<Float>(durationMillis = cycleDurationMs / 2, easing = LinearEasing)

        while (true) {
            delay(intervalMs)

            repeat(cycles) {
                translationX.animateTo(offsetPx, shakeSpec)
                translationX.animateTo(-offsetPx, shakeSpec)
            }

            translationX.animateTo(0f, settleSpec)
        }
    }

    graphicsLayer { this.translationX = translationX.value }
}