package io.synapse.ai.core.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import io.synapse.ai.core.theme.Amber400
import io.synapse.ai.core.theme.Amber800
import io.synapse.ai.core.theme.Emerald500
import io.synapse.ai.core.theme.Emerald600
import io.synapse.ai.core.theme.Indigo50
import io.synapse.ai.core.theme.Indigo500
import io.synapse.ai.core.theme.Indigo600
import io.synapse.ai.core.theme.Indigo950
import io.synapse.ai.core.theme.Orange400
import io.synapse.ai.core.theme.Orange50
import io.synapse.ai.core.theme.Orange900
import io.synapse.ai.core.theme.Slate200
import io.synapse.ai.core.theme.Slate50
import io.synapse.ai.core.theme.StreakBackgroundDeep
import io.synapse.ai.core.theme.StreakSurfaceDark
import io.synapse.ai.core.theme.Transparent
import io.synapse.ai.core.theme.Violet400
import io.synapse.ai.core.theme.Violet500
import io.synapse.ai.core.theme.Violet600
import io.synapse.ai.core.theme.White
import io.synapse.ai.core.theme.Yellow100

@Immutable
data class SynapseGradients(
    val primary: Brush,
    val accent: Brush,
    val premium: Brush,
    val page: ShaderBrush,
    val streakHero: Brush,
    val shimmer: Brush,
    val success: Brush,
)

private object LightGradients {

    val primary = Brush.linearGradient(
        colors = listOf(
            Indigo600,
            Indigo500
        )
    )

    val accent = Brush.linearGradient(
        colors = listOf(
            Violet600,
            Indigo600
        )
    )

    val premium = Brush.linearGradient(
        colors = listOf(Amber400, Orange400)
    )

    val streakHero = Brush.linearGradient(
        colors = listOf(
            White,
            Orange50,
            Yellow100
        )
    )

    val success = Brush.linearGradient(
        colors = listOf(
            Emerald500,
            Emerald600
        )
    )

    val shimmer = Brush.linearGradient(
        colors = listOf(
            Transparent,
            Slate200.copy(alpha = 0.5f),
            Transparent
        ),
        start = Offset.Zero,
        end = Offset(800f, 0f)
    )

    val page = object : ShaderBrush() {
        override fun createShader(size: Size) =
            RadialGradientShader(
                center = Offset(size.width * 0.5f, 0f),
                radius = size.width * 0.85f,
                colors = listOf(
                    Indigo50.copy(alpha = 0.5f),
                    Slate50
                ),
                colorStops = listOf(0f, 0.7f),
            )
    }
}

private object DarkGradients {

    val primary = Brush.linearGradient(
        colors = listOf(
            Indigo600,
            Indigo500
        )
    )

    val accent = Brush.linearGradient(
        colors = listOf(
            Violet500,
            Violet400
        )
    )

    val premium = Brush.linearGradient(
        colors = listOf(Amber800, Orange900)
    )
    val streakHero = Brush.linearGradient(
        colors = listOf(StreakBackgroundDeep, StreakSurfaceDark, StreakBackgroundDeep)
    )

    val success = Brush.linearGradient(
        colors = listOf(
            Emerald600,
            Emerald500
        )
    )

    val shimmer = Brush.linearGradient(
        colors = listOf(
            Transparent,
            Color(0x22FFFFFF),
            Transparent
        ),
        start = Offset.Zero,
        end = Offset(800f, 0f)
    )

    val page = object : ShaderBrush() {
        override fun createShader(size: Size) =
            RadialGradientShader(
                center = Offset(size.width * 0.5f, 0f),
                radius = size.width * 0.85f,
                colors = listOf(
                    Indigo950.copy(alpha = 0.3f),
                    BrandColors.NeutralDarkBg
                ),
                colorStops = listOf(0f, 0.7f),
            )
    }
}

val LightSynapseGradients = SynapseGradients(
    primary = LightGradients.primary,
    accent = LightGradients.accent,
    premium = LightGradients.premium,
    streakHero = LightGradients.streakHero,
    page = LightGradients.page,
    shimmer = LightGradients.shimmer,
    success = LightGradients.success
)

val DarkSynapseGradients = SynapseGradients(
    primary = DarkGradients.primary,
    accent = DarkGradients.accent,
    premium = DarkGradients.premium,
    streakHero = DarkGradients.streakHero,
    page = DarkGradients.page,
    shimmer = DarkGradients.shimmer,
    success = DarkGradients.success,
)

val defaultGradientTokens = DarkSynapseGradients