package com.venom.synapse.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens
 */
@Immutable
data class SynapseSemanticColors(
    // Gold — streak, premium, best-day highlights
    val gold: Color,
    val goldContainer: Color,

    // Success — accuracy, retention, verified badge
    val success: Color,
    val successContainer: Color,

    // Accent — indigo, time-studied, SRS settings
    val accent: Color,
    val accentContainer: Color,
)

val LightSynapseSemanticColors = SynapseSemanticColors(
    gold = Amber600,
    goldContainer = Amber50,
    success = Emerald600,
    successContainer = Emerald50,
    accent = Indigo600,
    accentContainer = Indigo50,
)

val DarkSynapseSemanticColors = SynapseSemanticColors(
    gold = Amber400,
    goldContainer = SynapseGoldContainerDk,       // #3D2000
    success = Emerald400,
    successContainer = SynapseSuccessContainerDk,    // #002818
    accent = Indigo400,
    accentContainer = SynapseAccentContainerDk,     // #1A1650
)

val LocalSynapseSemanticColors = staticCompositionLocalOf { LightSynapseSemanticColors }