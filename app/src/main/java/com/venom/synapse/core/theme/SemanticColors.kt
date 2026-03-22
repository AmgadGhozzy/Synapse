package com.venom.synapse.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens.
 *
 * ── Light mode contrast audit (vs white surface #FFFFFF) ──────────────────────
 *
 *  gold    Amber700 #B45309  5.02:1  ✅ AA normal text
 *           was Amber600 #D97706 → 3.19:1 ❌ FAILED normal text
 *
 *  success  Emerald700 #047857  5.48:1  ✅ AA normal text
 *           was Emerald600 #059669 → 3.77:1 ❌ FAILED normal text
 *
 *  failure  Red600 #DC2626       4.83:1  ✅ AA normal text  — unchanged
 *  accent   Indigo600 #4F46E5    6.29:1  ✅ AA normal text  — unchanged
 *
 * ── Container colors ──────────────────────────────────────────────────────────
 * Container shades (50 / custom dark) are used as filled chip/badge backgrounds
 * and carry no text-contrast requirement themselves — the accentColor above is
 * placed on top of them and that pair is what must pass.
 */
@Immutable
data class SynapseSemanticColors(
    // Gold — streak, premium, best-day highlights
    val gold: Color,
    val goldContainer: Color,

    // Accent — indigo, time-studied, SRS settings
    val accent: Color,
    val accentContainer: Color,

    // Success — accuracy, retention, verified badge
    val success: Color,
    val successContainer: Color,

    // Failure — incorrect answers, errors, warnings
    val failure: Color,
    val failureContainer: Color,
)

val LightSynapseSemanticColors = SynapseSemanticColors(
    gold          = Amber700,
    goldContainer = Amber50,

    accent          = Indigo600,
    accentContainer = Indigo50,

    success          = Emerald700,
    successContainer = Emerald50,

    failure          = Red600,
    failureContainer = Red50,
)

val DarkSynapseSemanticColors = SynapseSemanticColors(
    // Dark mode unchanged — these lighter shades are correct on dark surfaces
    gold          = Amber400,
    goldContainer = SynapseGoldContainerDk,    // #3D2000

    accent          = Indigo400,
    accentContainer = SynapseAccentContainerDk, // #1A1650

    success          = Emerald400,
    successContainer = SynapseSuccessContainerDk, // #002818

    failure          = Red400,
    failureContainer = SynapseErrorContainerDk,   // #2A0E0E
)

val LocalSemanticColors = staticCompositionLocalOf { LightSynapseSemanticColors }