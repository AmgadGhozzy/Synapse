package com.venom.synapse.core.theme.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
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

object Elevation {
    val Level0: Dp = 0.dp
    val Level1: Dp = 1.dp
    val Level2: Dp = 3.dp
    val Level3: Dp = 6.dp
    val Level4: Dp = 8.dp
    val Level5: Dp = 12.dp
}

/**
 * Design token for drop shadows.
 * [elevation] — Y-axis offset (key light distance from surface).
 * [blur] — blur radius (ambient softness).
 * [spread] — shadow growth beyond shape bounds.
 * [color] — shadow tint (pre-alpha).
 * [offset] — explicit DpOffset; defaults to (0, elevation) when not set.
 */
data class ShadowSpec(
    val elevation: Dp,
    val blur: Dp,
    val color: Color,
    val spread: Dp = 0.dp,
    val offset: DpOffset = DpOffset.Unspecified,
)

object ShadowTokens {

    // Avatar / thumb shadow
    val ShadowAvatar = ShadowSpec(
        elevation = Elevation.Level2,
        blur = 16.dp,
        spread = 0.5.dp,
        color = SynapseViolet600.copy(alpha = 0.22f),
    )

    // Active pack card ring (swipe reveal)
    val ShadowCard = ShadowSpec(
        elevation = Elevation.Level2,
        blur = 0.dp,
        spread = 1.5.dp,
        color = SynapseViolet600.copy(alpha = 0.55f),
    )

    // FAB
    val ShadowFab = ShadowSpec(
        elevation = Elevation.Level3,
        blur = 32.dp,
        spread = 2.dp,
        color = SynapseViolet600.copy(alpha = 0.18f),
    )

    // "Go Pro" pill
    val ShadowGoPro = ShadowSpec(
        elevation = Elevation.Level2,
        blur = 20.dp,
        spread = 1.dp,
        color = Amber600.copy(alpha = 0.45f),
    )

    // Promotional badges
    val ShadowBadge = ShadowSpec(
        elevation = Elevation.Level1,
        blur = 10.dp,
        color = Amber600.copy(alpha = 0.40f),
    )

    // Selected annual pricing card
    val ShadowAnnualCard = ShadowSpec(
        elevation = Elevation.Level4,
        blur = 36.dp,
        spread = 2.dp,
        color = SynapseVioletMid.copy(alpha = 0.48f),
    )

    // Full-width CTA button (dark theme)
    val ShadowCtaDark = ShadowSpec(
        elevation = Elevation.Level4,
        blur = 44.dp,
        spread = 2.dp,
        color = SynapseVioletMid.copy(alpha = 0.50f),
    )

    // Full-width CTA button (light theme)
    val ShadowCtaLight = ShadowSpec(
        elevation = Elevation.Level4,
        blur = 36.dp,
        spread = 1.dp,
        color = Indigo800.copy(alpha = 0.30f),
    )

    // Bottom nav top glow
    val ShadowBottomNav = ShadowSpec(
        elevation = Elevation.Level3,
        blur = 24.dp,
        color = SynapseViolet300.copy(alpha = 0.06f),
    )

    // Bottom sheets / modal drawers (dark)
    val ShadowSheetDark = ShadowSpec(
        elevation = Elevation.Level5,
        blur = 120.dp,
        color = Black.copy(alpha = 0.80f),
    )

    // Bottom sheets / modal drawers (light)
    val ShadowSheetLight = ShadowSpec(
        elevation = Elevation.Level5,
        blur = 120.dp,
        color = SynapseViolet600.copy(alpha = 0.22f),
    )

    // Online status dot glow
    val ShadowNavDot = ShadowSpec(
        elevation = Elevation.Level0,
        blur = 7.dp,
        color = Emerald400,
    )

    // Hero / daily goal card
    val ShadowHero = ShadowSpec(
        elevation = Elevation.Level3,
        blur = 36.dp,
        spread = 2.dp,
        color = SynapseViolet600.copy(alpha = 0.10f),
        offset = DpOffset(0.dp, 4.dp),
    )

    // Stats chip cards
    val ShadowStats = ShadowSpec(
        elevation = Elevation.Level2,
        blur = 20.dp,
        spread = 0.5.dp,
        color = Black.copy(alpha = 0.07f),
        offset = DpOffset(0.dp, 2.dp),
    )

    // Grid pack cards
    val ShadowPack = ShadowSpec(
        elevation = Elevation.Level1,
        blur = 16.dp,
        spread = 1.dp,
        color = Black.copy(alpha = 0.06f),
        offset = DpOffset(0.dp, 2.dp),
    )

    // Flashcard / flip card
    val ShadowFlashcard = ShadowSpec(
        elevation = Elevation.Level4,
        blur = 28.dp,
        spread = 1.dp,
        color = Black.copy(alpha = 0.08f),
        offset = DpOffset(0.dp, 4.dp),
    )

    // SRS action buttons / hint chips
    val ShadowQuizAction = ShadowSpec(
        elevation = Elevation.Level2,
        blur = 16.dp,
        spread = 0.5.dp,
        color = Black.copy(alpha = 0.08f),
        offset = DpOffset(0.dp, 2.dp),
    )

    // Action sheet top edge
    val ShadowQuizSheet = ShadowSpec(
        elevation = Elevation.Level3,
        blur = 24.dp,
        spread = 1.dp,
        color = Black.copy(alpha = 0.06f),
    )
}

/** Converts [ShadowSpec] to the Compose [Shadow] renderer. */
fun ShadowSpec.toShadow(
    customColor: Color? = null,
    offset: DpOffset = if (this.offset != DpOffset.Unspecified) this.offset
                       else DpOffset(0.dp, elevation),
): Shadow = Shadow(
    radius = blur,
    color  = customColor?.copy(alpha = color.alpha) ?: color,
    spread = spread,
    offset = offset,
)
