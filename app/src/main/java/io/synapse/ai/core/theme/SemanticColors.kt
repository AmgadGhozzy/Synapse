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
    val primaryBg: Color
)

val LightSynapseSemanticColors = SemanticColors(
    // ── Success ──
    success = BrandColors.SuccessLight,
    onSuccess = BrandColors.OnSuccessLight,
    successBg = BrandColors.SuccessLight.copy(alpha = 0.10f),
    successContainer = BrandColors.SuccessLight.copy(alpha = 0.20f),
    successBorder = BrandColors.SuccessLight.copy(alpha = 0.30f),

    // ── Error ──
    error = BrandColors.ErrorLight,
    onError = BrandColors.OnErrorLight,
    errorBg = BrandColors.ErrorLight.copy(alpha = 0.10f),
    errorContainer = BrandColors.ErrorLight.copy(alpha = 0.20f),
    errorBorder = BrandColors.ErrorLight.copy(alpha = 0.30f),

    // ── Accent (Violet) ──
    accent = BrandColors.SecondaryLight,
    onAccent = BrandColors.OnSecondaryLight,
    accentBg = BrandColors.SecondaryLight.copy(alpha = 0.10f),
    accentContainer = BrandColors.SecondaryLight.copy(alpha = 0.20f),
    accentBorder = BrandColors.SecondaryLight.copy(alpha = 0.30f),

    gold = BrandColors.TertiaryLight,
    onGold = BrandColors.OnTertiaryLight,
    goldBg = BrandColors.TertiaryLight.copy(alpha = 0.10f),
    goldContainer = BrandColors.TertiaryLight.copy(alpha = 0.20f),
    goldBorder = BrandColors.TertiaryLight.copy(alpha = 0.30f),

    // ── Primary (Indigo) ──
    primary = BrandColors.PrimaryLight,
    onPrimary = BrandColors.OnPrimaryLight,
    primaryBg = BrandColors.PrimaryLight.copy(alpha = 0.10f),
    primaryContainer = BrandColors.PrimaryLight.copy(alpha = 0.20f),
    primaryBorder = BrandColors.PrimaryLight.copy(alpha = 0.30f),
)

val DarkSynapseSemanticColors = SemanticColors(
    // ── Success ──
    success = BrandColors.SuccessDark,
    onSuccess = BrandColors.OnSuccessDark,
    successBg = BrandColors.SuccessDark.copy(alpha = 0.10f),
    successContainer = BrandColors.SuccessDark.copy(alpha = 0.20f),
    successBorder = BrandColors.SuccessDark.copy(alpha = 0.30f),

    // ── Error ──
    error = BrandColors.ErrorDark,
    onError = BrandColors.OnErrorDark,
    errorBg = BrandColors.ErrorDark.copy(alpha = 0.10f),
    errorContainer = BrandColors.ErrorDark.copy(alpha = 0.20f),
    errorBorder = BrandColors.ErrorDark.copy(alpha = 0.30f),

    // ── Accent ──
    accent = BrandColors.SecondaryDark,
    onAccent = BrandColors.OnSecondaryDark,
    accentBg = BrandColors.SecondaryDark.copy(alpha = 0.10f),
    accentContainer = BrandColors.SecondaryDark.copy(alpha = 0.20f),
    accentBorder = BrandColors.SecondaryDark.copy(alpha = 0.30f),

    gold = BrandColors.TertiaryDark,
    onGold = BrandColors.OnTertiaryDark,
    goldBg = BrandColors.TertiaryDark.copy(alpha = 0.10f),
    goldContainer = BrandColors.TertiaryDark.copy(alpha = 0.20f),
    goldBorder = BrandColors.TertiaryDark.copy(alpha = 0.30f),

    // ── Primary ──
    primary = BrandColors.PrimaryDark,
    onPrimary = BrandColors.OnPrimaryDark,
    primaryBg = BrandColors.PrimaryDark.copy(alpha = 0.10f),
    primaryContainer = BrandColors.PrimaryDark.copy(alpha = 0.20f),
    primaryBorder = BrandColors.PrimaryDark.copy(alpha = 0.30f)
)

val defaultSemanticColors = DarkSynapseSemanticColors