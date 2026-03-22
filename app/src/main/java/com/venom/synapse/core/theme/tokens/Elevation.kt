package com.venom.synapse.core.theme.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.venom.synapse.core.theme.Amber600
import com.venom.synapse.core.theme.Black
import com.venom.synapse.core.theme.Emerald400
import com.venom.synapse.core.theme.Indigo800
import com.venom.synapse.core.theme.SynapseViolet300
import com.venom.synapse.core.theme.SynapseViolet600
import com.venom.synapse.core.theme.SynapseVioletMid

data class ElevationTokens(
    val xs: Dp = Elevation.Level1,
    val sm: Dp = Elevation.Level2,
    val md: Dp = Elevation.Level3,
    val lg: Dp = Elevation.Level4,
    val xl: Dp = Elevation.Level5,
)

val defaultElevationTokens = ElevationTokens()

/**
 * Elevation Token System.
 */
object Elevation {

    /**
     * Level0 — 0dp
     * Flat surfaces: BottomNav background (uses border instead), feature list rows,
     * disabled / inactive states.
     */
    val Level0: Dp = 0.dp

    /**
     * Level1 — 1dp
     * Default card resting state: pack card base, stats cards (light), input containers.
     */
    val Level1: Dp = 1.dp

    /**
     * Level2 — 3dp
     * Cards in default visible state: pack list, profile section cards, feature groups.
     */
    val Level2: Dp = 3.dp

    /**
     * Level3 — 6dp
     * Floating elements: FAB ("New Pack"), nav bar with blurred backdrop,
     * drag-held pack card, source tab selector.
     */
    val Level3: Dp = 6.dp

    /**
     * Level4 — 8dp
     * Elevated overlays: bottom sheets, modals, TopAppBar avatar pressed state,
     * selected pricing card.
     */
    val Level4: Dp = 8.dp

    /**
     * Level5 — 12dp
     * Full-screen surfaces: paywall overlay, premium CTA glow, decorative phone frame.
     */
    val Level5: Dp = 12.dp
}

/**
 * ShadowSpec — encapsulates a coloured shadow for brand-aware glow effects.
 *
 * @param elevation  M3 elevation dp (pass to Modifier.shadow elevation param).
 * @param blur       CSS-equivalent blur radius (for custom Canvas DrawScope use).
 * @param color      The tinted shadow / glow colour.
 * @param spread     CSS spread equivalent — informational only; not supported by
 *                   Modifier.shadow(). Implement via drawBehind canvas if needed.
 */
data class ShadowSpec(
    val elevation: Dp,
    val blur:      Dp,
    val color:     Color,
    val spread:    Dp = 0.dp,
)

object ShadowTokens {

    /**
     * ShadowAvatar
     * CSS: `box-shadow: 0 4px 14px rgba(91,78,232,0.20)` — TopAppBar avatar
     * Usage: Avatar circle, small icon-button active states.
     */
    val ShadowAvatar = ShadowSpec(
        elevation = Elevation.Level2,
        blur      = 14.dp,
        color     = SynapseViolet600.copy(alpha = 0.20f),
    )

    /**
     * ShadowCard
     * CSS: `box-shadow: 0 0 0 1.5px rgba(91,78,232,0.55)` — pack card ring on swipe
     * Usage: Elevated / active card border ring. Combine with Level2 elevation.
     */
    val ShadowCard = ShadowSpec(
        elevation = Elevation.Level2,
        blur      = 0.dp,
        color     = SynapseViolet600.copy(alpha = 0.55f),
        spread    = 1.5.dp,
    )

    /**
     * ShadowFab
     * CSS: `box-shadow: 0 8px 28px rgba(91,78,232,0.20), 0 0 0 1px rgba(91,78,232,0.12)`
     * Usage: FAB "New Pack" button, primary action buttons on hero cards.
     */
    val ShadowFab = ShadowSpec(
        elevation = Elevation.Level3,
        blur      = 28.dp,
        color     = SynapseViolet600.copy(alpha = 0.20f),
    )

    /**
     * ShadowGoPro
     * CSS: `box-shadow: 0 4px 18px rgba(217,119,6,0.45), 0 0 0 1px rgba(251,184,48,0.22)`
     * Usage: "Go Pro" pill, gold / amber CTA buttons, premium action chips.
     *
     * Color: Amber600 (#D97706) — CSS rgba(217,119,6) is exact Amber600.
     */
    val ShadowGoPro = ShadowSpec(
        elevation = Elevation.Level2,
        blur      = 18.dp,
        color     = Amber600.copy(alpha = 0.45f),
    )

    /**
     * ShadowBadge
     * CSS: `box-shadow: 0 2px 8px rgba(217,119,6,0.40)` — "SAVE 50%" badge
     * Usage: Small badge pills, promotional labels.
     *
     * Color: Amber600 (#D97706) — same amber family as ShadowGoPro, lower blur.
     */
    val ShadowBadge = ShadowSpec(
        elevation = Elevation.Level1,
        blur      = 8.dp,
        color     = Amber600.copy(alpha = 0.40f),
    )

    /**
     * ShadowAnnualCard
     * CSS: `box-shadow: 0 8px 32px rgba(76,62,199,0.50)` — selected Annual pricing card
     * Usage: Primary selected state for gradient-filled cards.
     */
    val ShadowAnnualCard = ShadowSpec(
        elevation = Elevation.Level4,
        blur      = 32.dp,
        color     = SynapseVioletMid.copy(alpha = 0.50f),
    )

    /**
     * ShadowCtaDark / ShadowCtaLight
     * CSS dark:  `0 10px 40px rgba(76,62,199,0.55), 0 0 0 1px rgba(157,147,255,0.20)`
     * CSS light: `0 10px 32px rgba(55,48,163,0.32), 0 0 0 1px rgba(91,78,232,0.18)`
     * Usage: Full-width primary CTA button ("Start Free Trial", "Generate Quiz").
     *
     * ShadowCtaLight color: Indigo800 (#3730A3) — CSS rgba(55,48,163) is exact Indigo800.
     */
    val ShadowCtaDark = ShadowSpec(
        elevation = Elevation.Level4,
        blur      = 40.dp,
        color     = SynapseVioletMid.copy(alpha = 0.55f),
    )
    val ShadowCtaLight = ShadowSpec(
        elevation = Elevation.Level4,
        blur      = 32.dp,
        color     = Indigo800.copy(alpha = 0.32f),
    )

    /**
     * ShadowBottomNav
     * CSS: `border-top: 1px solid C.border` + `backdrop-filter: blur(20px)`
     * Usage: BottomNav bar — border + blur rather than a traditional shadow.
     * Implement via Modifier.border() + graphicsLayer { renderEffect = BlurEffect(...) } (API 31+).
     */
    val ShadowBottomNav = ShadowSpec(
        elevation = Elevation.Level3,
        blur      = 20.dp,   // backdrop blur
        color     = SynapseViolet300.copy(alpha = 0.06f),
    )

    /**
     * ShadowSheetDark / ShadowSheetLight
     * CSS dark:  `box-shadow: 0 48px 120px rgba(0,0,0,0.80)`
     * CSS light: `box-shadow: 0 48px 120px rgba(91,78,232,0.22)`
     * Usage: Bottom sheets, modal drawers, paywall overlay.
     */
    val ShadowSheetDark = ShadowSpec(
        elevation = Elevation.Level5,
        blur      = 120.dp,
        color     = Black.copy(alpha = 0.80f),
    )
    val ShadowSheetLight = ShadowSpec(
        elevation = Elevation.Level5,
        blur      = 120.dp,
        color     = SynapseViolet600.copy(alpha = 0.22f),
    )

    /**
     * ShadowNavDot
     * CSS: `box-shadow: 0 0 6px C.success` — online status dot on avatar
     * Usage: Status dot glow.
     *
     * Color: Emerald400 (#34D399) — the dark-mode success colour; renders well
     * on both light and dark avatar surfaces at this small dot size.
     */
    val ShadowNavDot = ShadowSpec(
        elevation = Elevation.Level0,
        blur      = 6.dp,
        color     = Emerald400,
    )
}