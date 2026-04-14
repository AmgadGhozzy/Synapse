package io.synapse.ai.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class StatusColors(
    val accentColor : Color,
    val bgColor     : Color,
    val borderColor : Color,
)

@Immutable
data class LevelColors(
    val success : StatusColors,
    val accent  : StatusColors,
    val error   : StatusColors,
    val primary : StatusColors,
    val gold    : StatusColors,
)

private const val DarkAccent = 0.80f
private const val DarkBg     = 0.10f
private const val DarkBorder = 0.28f

val DarkLevelColors = LevelColors(

    success = StatusColors(
        accentColor = Emerald400.copy(alpha = DarkAccent),      // #34D399
        bgColor     = Emerald400.copy(alpha = DarkBg),
        borderColor = Emerald400.copy(alpha = DarkBorder),
    ),

    accent = StatusColors(
        accentColor = Indigo400.copy(alpha = DarkAccent),       // #818CF8
        bgColor     = Indigo400.copy(alpha = DarkBg),
        borderColor = Indigo400.copy(alpha = DarkBorder),
    ),

    error = StatusColors(
        accentColor = Red400.copy(alpha = DarkAccent),          // #F87171
        bgColor     = Red400.copy(alpha = DarkBg),
        borderColor = Red400.copy(alpha = DarkBorder),
    ),

    gold = StatusColors(
        accentColor = SynapseGold.copy(alpha = DarkAccent),     // #FBB830
        bgColor     = SynapseGold.copy(alpha = DarkBg),
        borderColor = SynapseGold.copy(alpha = DarkBorder),
    ),

    primary = StatusColors(
        accentColor = SynapseVioletBright,                      // #7B6FFF
        bgColor     = SynapseVioletBright.copy(alpha = DarkBg),
        borderColor = SynapseVioletBright.copy(alpha = DarkBorder),
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// Light token set — sources go straight to Colors.kt primitives
// ─────────────────────────────────────────────────────────────────────────────

val LightLevelColors = LevelColors(

    success = StatusColors(
        accentColor = Emerald600,                               // #059669
        bgColor     = Emerald50,
        borderColor = Emerald200,
    ),

    accent = StatusColors(
        accentColor = Indigo700,                                // #4338CA
        bgColor     = Indigo50,
        borderColor = Indigo200,
    ),

    error = StatusColors(
        accentColor = Red600,                                   // #DC2626
        bgColor     = Red50,
        borderColor = Red200,
    ),

    gold = StatusColors(
        accentColor = Amber600,                                 // #D97706
        bgColor     = Amber50,
        borderColor = Amber200,
    ),

    primary = StatusColors(
        accentColor = Indigo600,                                // #4F46E5
        bgColor     = Violet50,
        borderColor = Violet200,
    ),
)

// ─────────────────────────────────────────────────────────────────────────────

val LocalLevelColors = staticCompositionLocalOf { LightLevelColors }