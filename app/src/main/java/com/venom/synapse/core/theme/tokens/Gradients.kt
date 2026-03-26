package com.venom.synapse.core.theme.tokens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import com.venom.synapse.core.theme.Amber100
import com.venom.synapse.core.theme.Amber200
import com.venom.synapse.core.theme.SynapseGold
import com.venom.synapse.core.theme.SynapseGoldDeep
import com.venom.synapse.core.theme.SynapsePageDark
import com.venom.synapse.core.theme.SynapsePageLight
import com.venom.synapse.core.theme.SynapseStreakBgDark1
import com.venom.synapse.core.theme.SynapseStreakBgDark2
import com.venom.synapse.core.theme.SynapseStreakBgLight1
import com.venom.synapse.core.theme.SynapseViolet600
import com.venom.synapse.core.theme.SynapseVioletDarkAnnual
import com.venom.synapse.core.theme.SynapseVioletMid
import com.venom.synapse.core.theme.Transparent
import com.venom.synapse.core.theme.Violet800
import com.venom.synapse.core.theme.White

data class GradientTokens(
    val primary: Brush,
    val accent: Brush,
    val gold: Brush,
    val button: Brush,
    val page: ShaderBrush,
    val streakHero: Brush,
    val shimmer: Brush,
)

val LightGradientTokens = GradientTokens(
    primary = Gradients.GradientPrimaryLight,
    accent = Gradients.GradientAccentLight,
    gold = Gradients.GradientGoldLight,
    button = Gradients.GradientButtonLight,
    page = Gradients.GradientPageLight,
    streakHero = Gradients.GradientStreakHeroLight,
    shimmer = Gradients.GradientShimmer,
)

val DarkGradientTokens = GradientTokens(
    primary = Gradients.GradientPrimaryDark,
    accent = Gradients.GradientAccentDark,
    gold = Gradients.GradientGoldDark,
    button = Gradients.GradientButtonDark,
    page = Gradients.GradientPageDark,
    streakHero = Gradients.GradientStreakHeroDark,
    shimmer = Gradients.GradientShimmer,
)

object Gradients {

    val GradientPrimaryLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandPrimaryDeep, BrandColors.BrandPrimaryBright),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientPrimaryDark = Brush.linearGradient(
        colors = listOf(BrandColors.BrandPrimaryBright, BrandColors.BrandPrimaryDeep),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientAccentLight = Brush.linearGradient(
        colors = listOf(
            BrandColors.BrandSecondaryDeep,
            BrandColors.BrandPrimaryLight,
            BrandColors.BrandPrimaryBright,
        ),
        start = Offset.Zero,
        end = Offset(0.85f * Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientAccentDark = Brush.linearGradient(
        colors = listOf(
            SynapseVioletDarkAnnual,
            BrandColors.BrandPrimaryLight,
            BrandColors.BrandPrimaryBright,
        ),
        start = Offset.Zero,
        end = Offset(0.85f * Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientGoldLight = Brush.linearGradient(
        colors = listOf(
            BrandColors.BrandGoldDeep,
            BrandColors.BrandGoldLight,
            BrandColors.PackAmber
        ),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientGoldDark = Brush.linearGradient(
        colors = listOf(SynapseGoldDeep, BrandColors.BrandGoldLight, SynapseGold),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientStreakHeroLight = Brush.linearGradient(
        colors = listOf(
            SynapseStreakBgLight1,
            Amber100,
            Amber200.copy(alpha = 0.13f),
        ),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
    val GradientStreakHeroDark = Brush.linearGradient(
        colors = listOf(SynapseStreakBgDark1, SynapseStreakBgDark2, SynapseStreakBgDark1),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientButtonLight = Brush.linearGradient(
        colors = listOf(
            BrandColors.BrandSecondaryDeep,
            BrandColors.BrandPrimaryLight,
            BrandColors.BrandPrimaryBright,
        ),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientButtonDark = Brush.linearGradient(
        colors = listOf(
            SynapseVioletMid,
            BrandColors.BrandPrimaryBright
        ),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientPageLight: ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center = Offset(size.width * 0.5f, 0f),
            radius = size.width * 0.85f,
            colors = listOf(SynapseViolet600.copy(alpha = 0.12f), SynapsePageLight),
            colorStops = listOf(0f, 0.6f),
        )
    }

    val GradientPageDark: ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center = Offset(size.width * 0.5f, 0f),
            radius = size.width * 0.85f,
            colors = listOf(Violet800.copy(alpha = 0.16f), SynapsePageDark),
            colorStops = listOf(0f, 0.6f),
        )
    }

    val GradientShimmer = Brush.linearGradient(
        colors = listOf(Transparent, White.copy(alpha = 0.16f), Transparent),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, 0f),
    )
}