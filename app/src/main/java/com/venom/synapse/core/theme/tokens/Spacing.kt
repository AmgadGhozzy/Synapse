package com.venom.synapse.core.theme.tokens

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
    val s72: Dp = 72.dp,
    /** Universal horizontal screen margin — 20dp */
    val screen:       Dp = s20,
    /** Hero/goal card internal padding — 20dp */
    val cardLarge:    Dp = s20,
    /** Vertical gap between section blocks — 20dp */
    val sectionGap:   Dp = s20,
    /** Vertical gap between list/card items — 12dp */
    val listItemGap:  Dp = s12,
    /** Gap between icon and adjacent text — 8dp */
    val iconTextGap:  Dp = s8,
    /** FAB distance from screen bottom — 68dp */
    val fabBottom:    Dp = s72,
    /** Standard bottom padding for screen*/
    val screenContentBottom: Dp = 172.dp,
    /** Standard top padding for screen*/
    val screenContentTop:    Dp = 128.dp,
)

val defaultSpacingTokens = SpacingTokens()
