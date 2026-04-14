package io.synapse.ai.core.theme.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SpacingTokens(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    /** Spacing values */
    val s2:  Dp = 2.dp,
    val s3:  Dp = 3.dp,
    val s4:  Dp = 4.dp,
    val s6:  Dp = 6.dp,
    val s8:  Dp = 8.dp,
    val s10: Dp = 10.dp,
    val s12: Dp = 12.dp,
    val s14: Dp = 14.dp,
    val s16: Dp = 16.dp,
    val s18: Dp = 18.dp,
    val s20: Dp = 20.dp,
    val s24: Dp = 24.dp,
    val s28: Dp = 28.dp,
    val s32: Dp = 32.dp,
    val s48: Dp = 48.dp,
    val s56: Dp = 56.dp,
    val s74: Dp = 74.dp,
    val s80: Dp = 80.dp,
    /** Universal horizontal screen margin — 20dp */
    val screen:       Dp = s20,
    /** Hero/goal card internal padding — 20dp */
    val cardLarge:    Dp = s20,
    /** Vertical gap between section blocks — 20dp */
    val sectionGap:   Dp = s20,
    /** Vertical gap between list/card items — 16dp */
    val listItemGap:  Dp = s16,
    /** Gap between icon and adjacent text — 8dp */
    val iconTextGap:  Dp = s8,
    /** FAB distance from screen bottom — 68dp */
    val fabBottom:    Dp = s80,
    /** Standard bottom padding for screen*/
    val screenContentBottom: Dp = 172.dp,
    /** Standard top padding for screen*/
    val screenContentTop:    Dp = 128.dp,
    /** Icon sizes */
    val icon_xs: Dp = 18.dp,
    val icon_sm: Dp = 20.dp,
    val icon_md: Dp = 22.dp,
    val icon_lg: Dp = 24.dp,
    val icon_xl: Dp = 26.dp,
)

fun buildAdaptiveSpacing(scale: Float): SpacingTokens {
    fun dp(v: Float) = adaptDp(v, scale)
    return SpacingTokens(
        xs  = dp(4f),
        sm  = dp(8f),
        md  = dp(16f),
        lg  = dp(24f),
        xl  = dp(32f),
        s2  = dp(2f),
        s3  = dp(3f),
        s4  = dp(4f),
        s6  = dp(6f),
        s8  = dp(8f),
        s10 = dp(10f),
        s12 = dp(12f),
        s14 = dp(14f),
        s16 = dp(16f),
        s18 = dp(18f),
        s20 = dp(20f),
        s24 = dp(24f),
        s28 = dp(28f),
        s32 = dp(32f),
        s48 = dp(48f),
        s56 = dp(56f),
        s74 = dp(74f),
        s80 = dp(80f),
        screen              = dp(20f),
        screenContentTop    = dp(136f),
        screenContentBottom = dp(172f),
        cardLarge           = dp(20f),
        sectionGap          = dp(20f),
        listItemGap         = dp(16f),
        iconTextGap         = dp(8f),
        fabBottom           = dp(80f),
        icon_xs             = dp(18f),
        icon_sm             = dp(20f),
        icon_md             = dp(22f),
        icon_lg             = dp(24f),
        icon_xl             = dp(26f),
    )
}

val defaultSpacingTokens = SpacingTokens()
