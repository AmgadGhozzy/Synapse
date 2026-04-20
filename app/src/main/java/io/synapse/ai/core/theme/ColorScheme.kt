package io.synapse.ai.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import io.synapse.ai.core.theme.tokens.BrandColors

val SynapseLightColorScheme = lightColorScheme(

    primary = BrandColors.PrimaryLight,
    onPrimary = BrandColors.OnPrimaryLight,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo950,
    inversePrimary = BrandColors.PrimaryDark,

    secondary = BrandColors.SecondaryLight,
    onSecondary = BrandColors.OnSecondaryLight,
    secondaryContainer = Violet50,
    onSecondaryContainer = Violet900,

    tertiary = BrandColors.TertiaryLight,
    onTertiary = BrandColors.OnTertiaryLight,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Slate950,

    background = BrandColors.NeutralLightBg,
    onBackground = BrandColors.OnLight,

    surface = BrandColors.NeutralLightSurface,
    onSurface = BrandColors.OnLight,

    // used now
    surfaceVariant = BrandColors.NeutralLightSurfaceLow,
    onSurfaceVariant = BrandColors.OnLightMuted,

    inverseSurface = BrandColors.NeutralDarkSurface,
    inverseOnSurface = BrandColors.OnDark,

    surfaceTint = BrandColors.PrimaryLight,

    error = BrandColors.ErrorLight,
    onError = BrandColors.OnErrorLight,
    errorContainer = Red50,
    onErrorContainer = Red950,

    outline = BrandColors.OutlineLight,
    outlineVariant = BrandColors.OutlineVariantLight,

    scrim = Black
)

val SynapseDarkColorScheme = darkColorScheme(

    primary = BrandColors.PrimaryDark,
    onPrimary = BrandColors.OnPrimaryDark,
    primaryContainer = Indigo950,
    onPrimaryContainer = Indigo50,
    inversePrimary = BrandColors.PrimaryLight,

    secondary = BrandColors.SecondaryDark,
    onSecondary = BrandColors.OnSecondaryDark,
    secondaryContainer = Violet950,
    onSecondaryContainer = Violet50,

    tertiary = BrandColors.TertiaryDark,
    onTertiary = BrandColors.OnTertiaryDark,
    tertiaryContainer = Amber950,
    onTertiaryContainer = Amber50,

    background = BrandColors.NeutralDarkBg,
    onBackground = BrandColors.OnDark,

    surface = BrandColors.NeutralDarkSurface,
    onSurface = BrandColors.OnDark,

    // used now
    surfaceVariant = BrandColors.NeutralDarkSurfaceLow,
    onSurfaceVariant = BrandColors.OnDarkMuted,

    inverseSurface = White,
    inverseOnSurface = Slate900,

    surfaceTint = BrandColors.PrimaryDark,

    error = BrandColors.ErrorDark,
    onError = BrandColors.OnErrorDark,
    errorContainer = Red950,
    onErrorContainer = Red50,

    outline = BrandColors.OutlineDark,
    outlineVariant = BrandColors.OutlineVariantDark,

    scrim = Black
)