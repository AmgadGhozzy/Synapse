package io.synapse.ai.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.synapse.ai.core.theme.tokens.BrandColors

@Immutable
data class SemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val successBorder: Color,
    val successBg: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val errorBorder: Color,
    val errorBg: Color,
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val accentBorder: Color,
    val accentBg: Color,
    val gold: Color,
    val onGold: Color,
    val goldContainer: Color,
    val goldBorder: Color,
    val goldBg: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val primaryBorder: Color,
    val primaryBg: Color,
    val disabledBg: Color,
    val disabledBorder: Color,
    val onDisabled: Color
)

val LightSynapseSemanticColors = SemanticColors(
    // ── Success ──
    success = BrandColors.SuccessLight,
    onSuccess = BrandColors.OnSuccessLight,
    successBg = Emerald50,
    successContainer = Emerald100,
    successBorder = Emerald200,

    // ── Error ──
    error = BrandColors.ErrorLight,
    onError = BrandColors.OnErrorLight,
    errorBg = Red50,
    errorContainer = Red100,
    errorBorder = Red200,

    // ── Accent (Violet) ──
    accent = BrandColors.SecondaryLight,
    onAccent = BrandColors.OnSecondaryLight,
    accentBg = Violet50,
    accentContainer = Violet100,
    accentBorder = Violet200,
    gold = BrandColors.TertiaryLight,
    onGold = BrandColors.OnTertiaryLight,
    goldBg = Amber50,
    goldContainer = Amber100,
    goldBorder = Amber200,

    // ── Primary (Indigo) ──
    primary = BrandColors.PrimaryLight,
    onPrimary = BrandColors.OnPrimaryLight,
    primaryBg = Indigo50,
    primaryContainer = Indigo100,
    primaryBorder = Indigo200,

    // ── Disabled ──
    disabledBg = Slate100.copy(alpha = 0.5f),
    disabledBorder = Slate200,
    onDisabled = Slate400
)

val DarkSynapseSemanticColors = SemanticColors(
    // ── Success ──
    success = BrandColors.SuccessDark,
    onSuccess = BrandColors.OnSuccessDark,
    successBg = Emerald400.copy(alpha = 0.10f),
    successContainer = Emerald400.copy(alpha = 0.20f),
    successBorder = Emerald400.copy(alpha = 0.30f),

    // ── Error ──
    error = BrandColors.ErrorDark,
    onError = BrandColors.OnErrorDark,
    errorBg = Red400.copy(alpha = 0.10f),
    errorContainer = Red400.copy(alpha = 0.20f),
    errorBorder = Red400.copy(alpha = 0.30f),

    // ── Accent ──
    accent = BrandColors.SecondaryDark,
    onAccent = BrandColors.OnSecondaryDark,
    accentBg = Violet400.copy(alpha = 0.10f),
    accentContainer = Violet400.copy(alpha = 0.20f),
    accentBorder = Violet400.copy(alpha = 0.30f),

    gold = BrandColors.TertiaryDark,
    onGold = BrandColors.OnTertiaryDark,
    goldBg = Amber400.copy(alpha = 0.10f),
    goldContainer = Amber400.copy(alpha = 0.20f),
    goldBorder = Amber400.copy(alpha = 0.30f),

    // ── Primary ──
    primary = BrandColors.PrimaryDark,
    onPrimary = BrandColors.OnPrimaryDark,
    primaryBg = Indigo400.copy(alpha = 0.10f),
    primaryContainer = Indigo400.copy(alpha = 0.20f),
    primaryBorder = Indigo400.copy(alpha = 0.30f),

    // ── Disabled ──
    disabledBg = Color.White.copy(alpha = 0.05f),
    disabledBorder = Color.White.copy(alpha = 0.08f),
    onDisabled = Color.White.copy(alpha = 0.30f)
)

val defaultSemanticColors = DarkSynapseSemanticColors