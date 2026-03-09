package com.venom.synapse.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.venom.synapse.core.theme.tokens.BrandColors

/**
 * ColorScheme — Synapse Material 3 Color Schemes WCAG AA compliance
 */

// Light scheme
val SynapseLightColorScheme = lightColorScheme(
    primary = BrandColors.BrandPrimaryLight,
    onPrimary = White,
    primaryContainer = BrandColors.BrandNeutralCardLight,
    onPrimaryContainer = Violet900,
    inversePrimary = BrandColors.BrandPrimaryDark,

    secondary = BrandColors.BrandSecondaryLight,
    onSecondary = White,
    secondaryContainer = Indigo50,
    onSecondaryContainer = Indigo950,

    tertiary = BrandColors.BrandGoldLight,
    onTertiary = White,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Amber900,

    background = BrandColors.BrandNeutralBgLight,
    onBackground = SynapseNearBlack,

    surface = White,
    onSurface = SynapseNearBlack,
    surfaceVariant = BrandColors.BrandNeutralCardLight,
    onSurfaceVariant = SynapseOnSurfaceVariantLt,
    surfaceTint = BrandColors.BrandPrimaryLight,

    inverseSurface = BrandColors.BrandNeutralSurfaceDark,
    inverseOnSurface = SynapseGhostWhite,

    error = BrandColors.BrandErrorLight,
    onError = White,
    errorContainer = Rose50,
    onErrorContainer = Rose950,

    outline = SynapseOutlineLt,
    outlineVariant = SynapseOutlineVariantLt,

    scrim = SynapseDeepDark,
)

// Dark scheme
val SynapseDarkColorScheme = darkColorScheme(
    primary = BrandColors.BrandPrimaryDark,
    onPrimary = BrandColors.BrandNeutralDeepDark,
    primaryContainer = BrandColors.BrandNeutralCardDark,
    onPrimaryContainer = SynapseGhostWhite,
    inversePrimary = BrandColors.BrandPrimaryLight,

    secondary = BrandColors.BrandSecondaryDark,
    onSecondary = BrandColors.BrandNeutralDeepDark,
    secondaryContainer = BrandColors.BrandNeutralElevatedDark,
    onSecondaryContainer = Violet300,

    tertiary = BrandColors.BrandGoldDark,
    onTertiary = BrandColors.BrandNeutralDeepDark,
    tertiaryContainer = Amber950,
    onTertiaryContainer = Amber200,

    background = BrandColors.BrandNeutralDeepDark,
    onBackground = SynapseGhostWhite,

    surface = BrandColors.BrandNeutralSurfaceDark,
    onSurface = SynapseGhostWhite,
    surfaceVariant = BrandColors.BrandNeutralCardDark,
    onSurfaceVariant = SynapseTextSub,
    surfaceTint = BrandColors.BrandPrimaryDark,

    inverseSurface = BrandColors.BrandNeutralCardLight,
    inverseOnSurface = BrandColors.BrandNeutralSurfaceDark,

    error = BrandColors.BrandErrorDark,
    onError = Rose950,
    errorContainer = Red950,
    onErrorContainer = BrandColors.BrandErrorDark,

    outline = SynapseOutlineDk,
    outlineVariant = SynapseOutlineVariantDk,

    scrim = BrandColors.BrandNeutralDeepDark,
)
