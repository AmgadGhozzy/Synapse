package com.venom.synapse.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.venom.synapse.core.theme.tokens.BrandColors

@Immutable
data class StatusColors(
    val accentColor: Color,
    val bgColor    : Color,
    val borderColor: Color,
)

@Immutable
data class LevelColors(
    val success : StatusColors,
    val accent: StatusColors,
    val error: StatusColors,
    val primary : StatusColors,
    val gold  : StatusColors,
)

val DarkLevelColors = LevelColors(
    success = StatusColors(
        accentColor = BrandColors.BrandSuccessDark,              // Emerald400 #34D399
        bgColor     = BrandColors.BrandSuccessDark.copy(0.12f),
        borderColor = BrandColors.BrandSuccessDark.copy(0.28f),
    ),
    accent = StatusColors(
        accentColor = BrandColors.BrandSecondaryDark,            // Indigo400  #818CF8
        bgColor     = BrandColors.BrandSecondaryDark.copy(0.12f),
        borderColor = BrandColors.BrandSecondaryDark.copy(0.28f),
    ),
    error = StatusColors(
        accentColor = BrandColors.BrandErrorDark,                // Red400     #F87171
        bgColor     = BrandColors.BrandErrorDark.copy(0.12f),
        borderColor = BrandColors.BrandErrorDark.copy(0.28f),
    ),
    gold = StatusColors(
        accentColor = BrandColors.BrandGoldDark,                 // Amber      #FBB830
        bgColor     = BrandColors.BrandGoldDark.copy(0.12f),
        borderColor = BrandColors.BrandGoldDark.copy(0.28f),
    ),
    primary = StatusColors(
        accentColor = BrandColors.BrandPrimaryDark,              // Violet     #7B6FFF
        bgColor     = BrandColors.BrandPrimaryDark.copy(0.12f),
        borderColor = BrandColors.BrandPrimaryDark.copy(0.28f),
    ),
)

val LightLevelColors = LevelColors(
    success = StatusColors(
        accentColor = BrandColors.BrandSuccessLight,             // Emerald600 #059669
        bgColor     = Emerald50,
        borderColor = Emerald200,
    ),
    accent = StatusColors(
        accentColor = BrandColors.BrandSecondaryLight,           // Indigo700  #4338CA
        bgColor     = Indigo50,
        borderColor = Indigo200,
    ),
    error = StatusColors(
        accentColor = BrandColors.BrandErrorLight,               // Red600     #DC2626
        bgColor     = Red50,
        borderColor = Red200,
    ),
    gold = StatusColors(
        accentColor = BrandColors.BrandGoldLight,                // Amber600   #D97706
        bgColor     = Amber50,
        borderColor = Amber200,
    ),
    primary = StatusColors(
        accentColor = BrandColors.BrandPrimaryLight,             // Indigo600  #4F46E5
        bgColor     = Violet50,
        borderColor = Violet200,
    ),
)

val LocalLevelColors = staticCompositionLocalOf { LightLevelColors }