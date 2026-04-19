package io.synapse.ai.core.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import io.synapse.ai.core.theme.Amber100
import io.synapse.ai.core.theme.Amber400
import io.synapse.ai.core.theme.Amber50
import io.synapse.ai.core.theme.Amber500
import io.synapse.ai.core.theme.Amber600
import io.synapse.ai.core.theme.Amber700
import io.synapse.ai.core.theme.Indigo50
import io.synapse.ai.core.theme.Indigo500
import io.synapse.ai.core.theme.Indigo600
import io.synapse.ai.core.theme.Indigo700
import io.synapse.ai.core.theme.Indigo950
import io.synapse.ai.core.theme.Orange400
import io.synapse.ai.core.theme.Purple600
import io.synapse.ai.core.theme.Purple700
import io.synapse.ai.core.theme.Slate50
import io.synapse.ai.core.theme.Slate950
import io.synapse.ai.core.theme.StreakBackgroundDeep
import io.synapse.ai.core.theme.StreakSurfaceDark
import io.synapse.ai.core.theme.Transparent
import io.synapse.ai.core.theme.Violet500
import io.synapse.ai.core.theme.Violet600
import io.synapse.ai.core.theme.Violet700
import io.synapse.ai.core.theme.White

@Immutable
data class SynapseGradients(
    val primary: Brush,
    val accent: Brush,
    val gold: Brush,
    val page: ShaderBrush,
    val streakHero: Brush,
    val shimmer: Brush,
)

// تم الحل هنا: استخدام List<Color> بدلاً من vararg
private fun smoothGradient(colors: List<Color>): Brush = Brush.linearGradient(
    colors = colors,
    start = Offset.Zero,
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)

private object LightGradients {
    val primary = smoothGradient(listOf(Violet500,Violet600, Indigo600))
    val accent = smoothGradient(listOf(Indigo500, Violet500, Purple600))
    val gold = smoothGradient(listOf(Orange400, Amber500, Amber400))
    val streakHero = smoothGradient(
        listOf(
            Amber100,
            Amber50,
            Amber100,
        )
    )

    val page: ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center = Offset(size.width * 0.5f, 0f),
            radius = size.width * 0.85f,
            colors = listOf(
                Indigo50.copy(alpha = 0.6f),
                Slate50
            ),
            colorStops = listOf(0f, 0.7f),
        )
    }

    val shimmer = Brush.linearGradient(
        colors = listOf(Transparent, White.copy(alpha = 0.25f), Transparent),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, 0f),
    )
}

private object DarkGradients {
    val primary = smoothGradient(listOf(Violet600, Violet700, Indigo700))
    val accent = smoothGradient(listOf(Indigo600, Violet600, Purple700))
    val gold = smoothGradient(listOf(Amber700, Amber600, Amber500))
    val streakHero =
        smoothGradient(listOf(StreakBackgroundDeep, StreakSurfaceDark, StreakBackgroundDeep))

    val page: ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center = Offset(size.width * 0.5f, 0f),
            radius = size.width * 0.85f,
            colors = listOf(
                Indigo950.copy(alpha = 0.4f),
                Slate950
            ),
            colorStops = listOf(0f, 0.7f),
        )
    }

    val shimmer = Brush.linearGradient(
        colors = listOf(Transparent, White.copy(alpha = 0.10f), Transparent),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, 0f),
    )
}

val LightSynapseGradients = SynapseGradients(
    primary = LightGradients.primary,
    accent = LightGradients.accent,
    gold = LightGradients.gold,
    page = LightGradients.page,
    streakHero = LightGradients.streakHero,
    shimmer = LightGradients.shimmer,
)

val DarkSynapseGradients = SynapseGradients(
    primary = DarkGradients.primary,
    accent = DarkGradients.accent,
    gold = DarkGradients.gold,
    page = DarkGradients.page,
    streakHero = DarkGradients.streakHero,
    shimmer = DarkGradients.shimmer,
)

val defaultGradientTokens = DarkSynapseGradients