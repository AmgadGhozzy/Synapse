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
import com.venom.synapse.core.theme.SynapseVioletDarkest
import com.venom.synapse.core.theme.SynapseVioletMid
import com.venom.synapse.core.theme.Transparent
import com.venom.synapse.core.theme.Violet300
import com.venom.synapse.core.theme.Violet800
import com.venom.synapse.core.theme.White

data class GradientTokens(
    val primary:    Brush,
    val accent:     Brush,
    val gold:       Brush,
    val title:      Brush,
    val cta:        Brush,
    val annual:     Brush,
    val page:       ShaderBrush,
    val goPro:      Brush,
    val streakHero: Brush,
)

val LightGradientTokens = GradientTokens(
    primary    = Gradients.GradientPrimaryLight,
    accent     = Gradients.GradientAccentLight,
    gold       = Gradients.GradientGoldLight,
    title      = Gradients.GradientTitleLight,
    cta        = Gradients.GradientCtaLight,
    annual     = Gradients.GradientAnnualLight,
    page       = Gradients.GradientPageLight,
    goPro      = Gradients.GradientGoProLight,
    streakHero = Gradients.GradientStreakHeroLight,
)

val DarkGradientTokens = GradientTokens(
    primary    = Gradients.GradientPrimaryDark,
    accent     = Gradients.GradientAccentDark,
    gold       = Gradients.GradientGoldDark,
    title      = Gradients.GradientTitleDark,
    cta        = Gradients.GradientCtaDark,
    annual     = Gradients.GradientAnnualDark,
    page       = Gradients.GradientPageDark,
    goPro      = Gradients.GradientGoProDark,
    streakHero = Gradients.GradientStreakHeroDark,
)

private val diag = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)

object Gradients {

    val GradientPrimaryLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandPrimaryDeep, SynapseViolet600),
        start = Offset.Zero, end = diag,
    )
    val GradientPrimaryDark = Brush.linearGradient(
        colors = listOf( BrandColors.BrandSecondaryDeep, BrandColors.BrandPrimaryLight),
        start = Offset.Zero, end = diag,
    )

    val GradientAccentLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandSecondaryDeep, BrandColors.PackViolet),
        start = Offset.Zero, end = diag,
    )
    val GradientAccentDark = Brush.linearGradient(
        colors = listOf(BrandColors.PackViolet, BrandColors.BrandSecondaryPale),
        start = Offset.Zero, end = diag,
    )

    /** Streak flame avatar, "Go Pro" pill, premium badges (light). */
    val GradientGoldLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandGoldDeep, BrandColors.PackAmber),
        start = Offset.Zero, end = diag,
    )
    val GradientGoldDark = Brush.linearGradient(
        colors = listOf(SynapseGoldDeep, SynapseGold),
        start = Offset.Zero, end = diag,
    )

    /** Warm ivory → Amber100 → Amber200@13% — streak hero card light bg. */
    val GradientStreakHeroLight = Brush.linearGradient(
        colors = listOf(SynapseStreakBgLight1, Amber100, Amber200.copy(alpha = 0.13f)),
        start = Offset.Zero, end = diag,
    )
    val GradientStreakHeroDark = Brush.linearGradient(
        colors = listOf(SynapseStreakBgDark1, SynapseStreakBgDark2, SynapseStreakBgDark1),
        start = Offset.Zero, end = diag,
    )

    val GradientTitleLight = Brush.linearGradient(
        colors = listOf(SynapseVioletDarkest, SynapseVioletMid, BrandColors.PackViolet),
        start = Offset.Zero, end = diag,
    )
    val GradientTitleDark = Brush.linearGradient(
        colors = listOf(Violet300, BrandColors.BrandPrimaryDark, BrandColors.BrandPrimaryBright),
        start = Offset.Zero, end = diag,
    )

    val GradientCtaLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandSecondaryDeep, BrandColors.BrandPrimaryLight),
        start = Offset.Zero, end = diag,
    )
    val GradientCtaDark = Brush.linearGradient(
        colors = listOf(SynapseVioletMid, SynapseViolet600),
        start = Offset.Zero, end = diag,
    )

    val GradientAnnualLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandSecondaryDeep, BrandColors.BrandPrimaryLight, BrandColors.BrandPrimaryBright),
        start = Offset.Zero, end = Offset(0.85f * Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
    val GradientAnnualDark = Brush.linearGradient(
        colors = listOf(SynapseVioletDarkAnnual, BrandColors.BrandPrimaryLight, BrandColors.BrandPrimaryBright),
        start = Offset.Zero, end = Offset(0.85f * Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientGoProLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandGoldDeep, BrandColors.BrandGoldLight, BrandColors.PackAmber),
        start = Offset.Zero, end = diag,
    )
    val GradientGoProDark = Brush.linearGradient(
        colors = listOf(SynapseGoldDeep, BrandColors.BrandGoldLight, SynapseGold),
        start = Offset.Zero, end = diag,
    )

    // IMPORTANT: Do NOT cache at top-level — ShaderBrush must be recreated per call site.
    // Always wrap in `remember { Gradients.GradientPageLight }`.

    val GradientPageLight: ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center     = Offset(size.width * 0.5f, 0f),
            radius     = size.width * 0.85f,
            colors     = listOf(SynapseViolet600.copy(alpha = 0.12f), SynapsePageLight),
            colorStops = listOf(0f, 0.6f),
        )
    }

    val GradientPageDark: ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center     = Offset(size.width * 0.5f, 0f),
            radius     = size.width * 0.85f,
            colors     = listOf(Violet800.copy(alpha = 0.16f), SynapsePageDark),
            colorStops = listOf(0f, 0.6f),
        )
    }

    fun gradientOrbPrimaryDark(): ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center     = Offset(size.width * 0.5f, size.height * 0.5f),
            radius     = size.width * 0.5f,
            colors     = listOf(SynapseViolet600.copy(alpha = 0.20f), SynapseViolet600.copy(alpha = 0f)),
            colorStops = listOf(0f, 0.7f),
        )
    }

    fun gradientOrbGoldDark(): ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center     = Offset(size.width * 0.5f, size.height * 0.5f),
            radius     = size.width * 0.5f,
            colors     = listOf(SynapseGold.copy(alpha = 0.09f), SynapseGold.copy(alpha = 0f)),
            colorStops = listOf(0f, 0.7f),
        )
    }

    val GradientShimmer = Brush.linearGradient(
        colors = listOf(Transparent, White.copy(alpha = 0.16f), Transparent),
        start  = Offset.Zero,
        end    = Offset(Float.POSITIVE_INFINITY, 0f),
    )
}