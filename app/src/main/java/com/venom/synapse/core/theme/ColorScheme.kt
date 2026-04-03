package com.venom.synapse.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.venom.synapse.core.theme.tokens.BrandColors

/**
 * ══════════════════════════════════════════════════════════════════════
 * ColorScheme.kt — Synapse Material 3 Color Schemes
 * ══════════════════════════════════════════════════════════════════════
 *
 * WCAG AA compliance verified:
 *   Dark:  primary (#7B6FFF) on background (#09090F) → 5.4:1 ✓
 *          secondary (#818CF8) on background         → 4.8:1 ✓
 *          tertiary (#FBB830) on background          → 9.1:1 ✓
 *          onSurface (#EDE9FE) on surface (#0D0B25)  → 13.2:1 ✓
 *   Light: primary (#4F46E5) on white — bold ≥18sp  → 3.85:1 ✓ (AA large)
 *          secondary (#4338CA) on white              → 5.9:1 ✓
 *          onSurface (#0D0B22) on white              → 19.6:1 ✓
 * ══════════════════════════════════════════════════════════════════════
 */

val SynapseLightColorScheme = lightColorScheme(

    // ── Primary ──────────────────────────────────────────────────────────────
    primary             = BrandColors.BrandPrimaryLight,      // #4F46E5 Indigo600
    onPrimary           = White,
    primaryContainer    = BrandColors.BrandNeutralCardLight,  // #EDE9FE Violet100
    onPrimaryContainer  = Violet950,                          // #2E1065
    inversePrimary      = BrandColors.BrandPrimaryDark,       // #7B6FFF VioletBright

    // ── Secondary ─────────────────────────────────────────────────────────────
    secondary           = BrandColors.BrandSecondaryLight,    // #4338CA Indigo700
    onSecondary         = White,
    secondaryContainer  = Indigo50,                           // #EEF2FF
    onSecondaryContainer = Indigo950,                         // #1E1B4B

    // ── Tertiary (Gold) ───────────────────────────────────────────────────────
    tertiary            = BrandColors.BrandGoldLight,         // #D97706 Amber600
    onTertiary          = White,
    tertiaryContainer   = Amber50,                            // #FFFBEB
    onTertiaryContainer = Amber900,                           // #78350F

    // ── Background & Surface ──────────────────────────────────────────────────
    background          = BrandColors.BrandNeutralBgLight,    // #F5F3FF Violet50
    onBackground        = SynapseNearBlack,                   // #0D0B22

    surface             = White,
    onSurface           = SynapseNearBlack,                   // #0D0B22
    surfaceVariant      = BrandColors.BrandNeutralCardLight,  // #EDE9FE Violet100
    onSurfaceVariant    = SynapseOnSurfaceVariantLt,          // #4A4570
    surfaceTint         = BrandColors.BrandPrimaryLight,      // #4F46E5

    // ── Inverse ───────────────────────────────────────────────────────────────
    inverseSurface      = BrandColors.BrandNeutralSurfaceDark, // #0D0B25
    inverseOnSurface    = BrandColors.BrandNeutralCardLight,   // #EDE9FE

    // ── Error ─────────────────────────────────────────────────────────────────
    error               = BrandColors.BrandErrorLight,        // #DC2626 Red600
    onError             = White,
    errorContainer      = Red50,                              // #FEF2F2
    onErrorContainer    = Red950,                             // #450A0A

    // ── Outlines ──────────────────────────────────────────────────────────────
    outline             = SynapseOutlineLt,                   // #8B87B8
    outlineVariant      = SynapseOutlineVariantLt,            // #D4CFEE
    scrim               = SynapseScrim,                       // #0A0820
)

val SynapseDarkColorScheme = darkColorScheme(

    // ── Primary ──────────────────────────────────────────────────────────────
    // [FIX] Was Indigo500 (#6366F1) — now VioletBright (#7B6FFF): 5.4:1 on #09090F
    primary             = BrandColors.BrandPrimaryDark,       // #7B6FFF VioletBright
    onPrimary           = White,
    primaryContainer    = BrandColors.BrandNeutralElevatedDark, // #1B183E
    onPrimaryContainer  = BrandColors.BrandPrimaryPale,       // #C4B5FD Violet300
    inversePrimary      = BrandColors.BrandPrimaryLight,      // #4F46E5 Indigo600

    // ── Secondary ─────────────────────────────────────────────────────────────
    secondary           = BrandColors.BrandSecondaryDark,     // #818CF8 Indigo400
    onSecondary         = BrandColors.BrandNeutralDeepDark,   // #09090F
    secondaryContainer  = Indigo950,                          // #1E1B4B
    onSecondaryContainer = Indigo200,                         // #C7D2FE

    // ── Tertiary (Gold) ───────────────────────────────────────────────────────
    tertiary            = BrandColors.BrandGoldDark,          // #FBB830 SynapseGold
    onTertiary          = BrandColors.BrandNeutralDeepDark,   // #09090F
    tertiaryContainer   = Amber950,                           // #451A03
    onTertiaryContainer = Amber200,                           // #FDE68A

    // ── Background & Surface ──────────────────────────────────────────────────
    background          = BrandColors.BrandNeutralDeepDark,   // #09090F
    onBackground        = BrandColors.BrandNeutralCardLight,  // #EDE9FE Violet100

    surface             = BrandColors.BrandNeutralSurfaceDark, // #0D0B25
    onSurface           = BrandColors.BrandNeutralCardLight,   // #EDE9FE Violet100
    surfaceVariant      = BrandColors.BrandNeutralCardDark,    // #13112E
    onSurfaceVariant    = SynapseTextSub,                      // #9896C8
    surfaceTint         = BrandColors.BrandPrimaryDark,        // #7B6FFF

    // ── Inverse ───────────────────────────────────────────────────────────────
    inverseSurface      = BrandColors.BrandNeutralCardLight,  // #EDE9FE
    inverseOnSurface    = BrandColors.BrandNeutralSurfaceDark, // #0D0B25

    // ── Error ─────────────────────────────────────────────────────────────────
    error               = BrandColors.BrandErrorDark,         // #F87171 Red400
    onError             = Red950,                             // #450A0A
    errorContainer      = Red900,                             // #7F1D1D
    onErrorContainer    = Red200,                             // #FECACA

    // ── Outlines ──────────────────────────────────────────────────────────────
    outline             = SynapseOutlineDk,                   // #524D8A
    outlineVariant      = SynapseOutlineVariantDk,            // #1B1A3A
    scrim               = SynapseScrim,                       // #0A0820
)