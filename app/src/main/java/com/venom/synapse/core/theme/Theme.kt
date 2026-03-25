package com.venom.synapse.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.venom.domain.model.AppTheme
import com.venom.domain.model.FontStyles
import com.venom.synapse.core.theme.tokens.DarkGradientTokens
import com.venom.synapse.core.theme.tokens.DarkLevelColors
import com.venom.synapse.core.theme.tokens.LightGradientTokens
import com.venom.synapse.core.theme.tokens.LightLevelColors
import com.venom.synapse.core.theme.tokens.LocalLevelColors
import com.venom.synapse.core.theme.tokens.SynapseShapes
import com.venom.synapse.core.theme.tokens.adp
import com.venom.synapse.core.theme.tokens.buildAdaptiveElevation
import com.venom.synapse.core.theme.tokens.buildAdaptiveRadius
import com.venom.synapse.core.theme.tokens.buildAdaptiveSpacing
import com.venom.synapse.core.theme.tokens.buildAdaptiveTypography
import com.venom.synapse.core.theme.tokens.defaultComponentTokens
import com.venom.ui.theme.Alexandria
import com.venom.ui.theme.BiScriptFonts
import com.venom.ui.theme.Cairo
import com.venom.ui.theme.Caveat
import com.venom.ui.theme.Inter
import com.venom.ui.theme.JosefinSans
import com.venom.ui.theme.LocalBiScriptFonts
import com.venom.ui.theme.PlaypenSans
import com.venom.ui.theme.Quicksand
import com.venom.ui.theme.Roboto
import com.venom.ui.theme.getTypographyForLocale
import com.venom.ui.theme.isArabicLocale
import com.venom.ui.theme.tokens.DarkGlassColors
import com.venom.ui.theme.tokens.LightGlassColors
import com.venom.ui.theme.tokens.LocalGlassColors

@Composable
fun SynapseTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    fontFamilyStyle: FontStyles = FontStyles.Default,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current

    val arabicLocale = isArabicLocale()

    val biScriptFonts = remember(fontFamilyStyle) {
        when (fontFamilyStyle) {
            FontStyles.Default -> BiScriptFonts(latin = Inter, arabic = Cairo)
            else -> {
                val chosen = try {
                    when (fontFamilyStyle) {
                        FontStyles.INTER -> Inter
                        FontStyles.CAIRO -> Cairo
                        FontStyles.ALEXANDRIA -> Alexandria
                        FontStyles.CAVEAT -> Caveat
                        FontStyles.ROBOTO -> Roboto
                        FontStyles.QUICKSAND -> Quicksand
                        FontStyles.JOSEFIN_SANS -> JosefinSans
                        FontStyles.PLAYPEN_SANS -> PlaypenSans
                        else -> Inter
                    }
                } catch (_: Exception) {
                    Inter
                }
                BiScriptFonts(latin = chosen, arabic = chosen)
            }
        }
    }

    val isDark = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) SynapseDarkColorScheme else SynapseLightColorScheme
    val navBarColor = colorScheme.surfaceColorAtElevation(3.adp)

    val config = LocalConfiguration.current
    val w = config.screenWidthDp
    val h = config.screenHeightDp

    val adaptiveSpacing    = remember(w, h) { buildAdaptiveSpacing(w, h) }
    val adaptiveRadius     = remember(w, h) { buildAdaptiveRadius(w, h) }
    val adaptiveTypography = remember(w, h) { buildAdaptiveTypography(w, h) }
    val adaptiveElevation  = remember(w, h) { buildAdaptiveElevation(w, h) }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            window.navigationBarColor = navBarColor.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalBiScriptFonts provides biScriptFonts,
        LocalSemanticColors provides if (isDark) DarkSynapseSemanticColors else LightSynapseSemanticColors,
        LocalLevelColors provides if (isDark) DarkLevelColors else LightLevelColors,
        LocalGlassColors provides if (isDark) DarkGlassColors else LightGlassColors,
        LocalGradientTokens provides if (isDark) DarkGradientTokens else LightGradientTokens,
        LocalSpacingTokens provides adaptiveSpacing,
        LocalRadiusTokens provides adaptiveRadius,
        LocalTypographyTokens provides adaptiveTypography,
        LocalElevationTokens provides adaptiveElevation,
        LocalComponentTokens provides defaultComponentTokens,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypographyForLocale(
                latinFamily = biScriptFonts.latin,
                arabicFamily = biScriptFonts.arabic,
                isArabic = arabicLocale,
            ),
            shapes = SynapseShapes,
            content = content,
        )
    }
}