package io.synapse.ai.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.synapse.ai.core.theme.tokens.BrandColors

@Immutable
data class SemanticColors(
    val success         : Color,
    val successContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val accent          : Color,
    val accentContainer : Color,
    val gold            : Color,
    val goldContainer   : Color,
)

val LightSynapseSemanticColors = SemanticColors(
    success          = BrandColors.BrandSuccessLight,
    successContainer = Emerald50,
    error            = BrandColors.BrandErrorLight,
    errorContainer   = Red50,
    accent           = BrandColors.BrandSecondaryLight,
    accentContainer  = Indigo50,
    gold             = BrandColors.BrandGoldLight,
    goldContainer    = Amber50,
)

val DarkSynapseSemanticColors = SemanticColors(
    success          = BrandColors.BrandSuccessDark,
    successContainer = Emerald950,
    error            = BrandColors.BrandErrorDark,
    errorContainer   = Red950,
    accent           = BrandColors.BrandSecondaryDark,
    accentContainer  = Indigo950,
    gold             = BrandColors.BrandGoldDark,
    goldContainer    = Amber950,
)

val LocalSemanticColors = staticCompositionLocalOf { LightSynapseSemanticColors }