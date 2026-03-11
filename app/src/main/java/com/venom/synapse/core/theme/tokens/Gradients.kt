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

/**
 * Gradient Token System.
 */

// Token data class (consumed by composables via MaterialTheme.synapse.gradients)

data class GradientTokens(
    val primary:    Brush,
    val accent:     Brush,
    val gold:       Brush,
    val title:      Brush,
    val cta:        Brush,
    val annual:     Brush,
    val goPro:      Brush,
    /** StreakHeroCard + PremiumBanner background — resolves light/dark automatically. */
    val streakHero: Brush,
)

val LightGradientTokens = GradientTokens(
    primary    = Gradients.GradientPrimaryLight,
    accent     = Gradients.GradientAccentLight,
    gold       = Gradients.GradientGoldLight,
    title      = Gradients.GradientTitleLight,
    cta        = Gradients.GradientCtaLight,
    annual     = Gradients.GradientAnnualLight,
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
    goPro      = Gradients.GradientGoProDark,
    streakHero = Gradients.GradientStreakHeroDark,
)

// Static gradient constants + ShaderBrush factories

object Gradients {

    // Primary

    /**
     * GradientPrimaryLight — `linear-gradient(135deg, #4A3DD6, #7B6FFF)`
     * Usage: Daily goal hero card, FAB, avatar ring, circular progress track.
     */
    val GradientPrimaryLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandPrimaryDeep, BrandColors.BrandPrimaryBright),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    /**
     * GradientPrimaryDark — `linear-gradient(135deg, #7B6FFF, #BDB6FF)`
     * Usage: Same as GradientPrimaryLight on dark backgrounds.
     */
    val GradientPrimaryDark = Brush.linearGradient(
        colors = listOf(BrandColors.BrandPrimaryBright, BrandColors.BrandPrimaryDeep),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    // Accent

    /**
     * GradientAccentLight — `linear-gradient(135deg, #3730A3, #6366F1)`
     */
    val GradientAccentLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandSecondaryDeep, BrandColors.PackViolet),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    /**
     * GradientAccentDark — `linear-gradient(135deg, #6366F1, #A5B4FC)`
     */
    val GradientAccentDark = Brush.linearGradient(
        colors = listOf(BrandColors.PackViolet, BrandColors.BrandSecondaryPale),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    // Gold

    /**
     * GradientGoldLight — `linear-gradient(135deg, #B45309, #F59E0B)`
     * Usage: Streak flame avatar, "Go Pro" pill, premium badges.
     */
    val GradientGoldLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandGoldDeep, BrandColors.PackAmber),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    /**
     * GradientGoldDark — `linear-gradient(135deg, #92580A, #FBB830)`
     * Usage: Same as GradientGoldLight on dark screens.
     */
    val GradientGoldDark = Brush.linearGradient(
        colors = listOf(SynapseGoldDeep, SynapseGold),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    // Streak Hero Card

    /**
     * GradientStreakHeroLight
     * CSS: `linear-gradient(135deg, #FFF8E7, #FEF3C7, #FDE68A22)`
     *
     * Color mapping:
     *   SynapseStreakBgLight1 → #FFF8E7 (warm ivory — custom, warmer than Amber50)
     *   Amber100              → #FEF3C7 (exact palette match)
     *   Amber200@α=0.13       → #FDE68A22 (0x22 = 34/255 ≈ 0.133)
     */
    val GradientStreakHeroLight = Brush.linearGradient(
        colors = listOf(
            SynapseStreakBgLight1,
            Amber100,
            Amber200.copy(alpha = 0.13f),
        ),
        start = Offset(0f, 0f),
        end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    /**
     * GradientStreakHeroDark
     * CSS: `linear-gradient(135deg, #1C1200, #2D1F02, #1A1200)`
     *
     * Color mapping:
     *   SynapseStreakBgDark1 → #1C1200 (start + end — third stop is imperceptibly different)
     *   SynapseStreakBgDark2 → #2D1F02 (mid)
     */
    val GradientStreakHeroDark = Brush.linearGradient(
        colors = listOf(SynapseStreakBgDark1, SynapseStreakBgDark2, SynapseStreakBgDark1),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    // Title

    val GradientTitleLight = Brush.linearGradient(
        colors = listOf(SynapseVioletDarkest, SynapseVioletMid, BrandColors.PackViolet),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientTitleDark = Brush.linearGradient(
        colors = listOf(Violet300, BrandColors.BrandPrimaryDark, BrandColors.BrandPrimaryBright),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    // CTA

    val GradientCtaLight = Brush.linearGradient(
        colors = listOf(
            BrandColors.BrandSecondaryDeep,
            BrandColors.BrandPrimaryLight,
            BrandColors.BrandPrimaryBright,
        ),
        start = Offset(0f, 0f),
        end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientCtaDark = Brush.linearGradient(
        colors = listOf(
            SynapseVioletMid,
            BrandColors.BrandPrimaryBright,
            BrandColors.BrandPrimaryDark,
        ),
        start = Offset(0f, 0f),
        end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    // Annual Pricing

    val GradientAnnualLight = Brush.linearGradient(
        colors = listOf(
            BrandColors.BrandSecondaryDeep,
            BrandColors.BrandPrimaryLight,
            BrandColors.BrandPrimaryBright,
        ),
        start = Offset(0f, 0f),
        end   = Offset(0.85f * Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientAnnualDark = Brush.linearGradient(
        colors = listOf(
            SynapseVioletDarkAnnual,
            BrandColors.BrandPrimaryLight,
            BrandColors.BrandPrimaryBright,
        ),
        start = Offset(0f, 0f),
        end   = Offset(0.85f * Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    // Go Pro Pill

    val GradientGoProLight = Brush.linearGradient(
        colors = listOf(BrandColors.BrandGoldDeep, BrandColors.BrandGoldLight, BrandColors.PackAmber),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    val GradientGoProDark = Brush.linearGradient(
        colors = listOf(SynapseGoldDeep, BrandColors.BrandGoldLight, SynapseGold),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    // Page Ambient (ShaderBrush factories)
    // IMPORTANT: Do NOT cache these at top-level. The ShaderBrush must be
    // recreated per call site so createShader() receives the real pixel Size.
    // Always wrap the call in `remember { Gradients.gradientPageLight() }`.

    /** Ambient radial gradient for the app page background (light mode). */
    fun gradientPageLight(): ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center      = Offset(size.width * 0.5f, 0f),
            radius      = size.width * 0.85f,
            colors      = listOf(SynapseViolet600.copy(alpha = 0.12f), SynapsePageLight),
            colorStops  = listOf(0f, 0.6f),
        )
    }

    /** Ambient radial gradient for the app page background (dark mode). */
    fun gradientPageDark(): ShaderBrush = object : ShaderBrush() {
        override fun createShader(size: Size) = RadialGradientShader(
            center      = Offset(size.width * 0.5f, 0f),
            radius      = size.width * 0.85f,
            colors      = listOf(Violet800.copy(alpha = 0.16f), SynapsePageDark),
            colorStops  = listOf(0f, 0.6f),
        )
    }

    // Ambient Orb Helpers

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

    // Shimmer Overlay
    val GradientShimmer = Brush.linearGradient(
        colors = listOf(Transparent, White.copy(alpha = 0.16f), Transparent),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, 0f),
    )
}