package com.venom.synapse.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.view.WindowCompat
import com.venom.domain.model.AppTheme
import com.venom.domain.model.FontStyles
import com.venom.synapse.core.theme.tokens.DarkGradientTokens
import com.venom.synapse.core.theme.tokens.LightGradientTokens
import com.venom.synapse.core.theme.tokens.SynapseShapes
import com.venom.synapse.core.theme.tokens.buildAdaptiveRadius
import com.venom.synapse.core.theme.tokens.buildAdaptiveSpacing
import com.venom.synapse.core.theme.tokens.buildThemedTypography
import com.venom.ui.theme.Alexandria
import com.venom.ui.theme.Cairo
import com.venom.ui.theme.Caveat
import com.venom.ui.theme.Inter
import com.venom.ui.theme.InterBold
import com.venom.ui.theme.JosefinSans
import com.venom.ui.theme.PlaypenSans
import com.venom.ui.theme.Quicksand
import com.venom.ui.theme.Roboto
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
            FontStyles.Default -> BiScriptFonts(latin = InterBold, arabic = Cairo)
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
                } catch (_: Exception) { Inter }
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

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val w = with(density) { windowInfo.containerSize.width.toDp().value.toInt() }
    val h = with(density) { windowInfo.containerSize.height.toDp().value.toInt() }

    val adaptiveSpacing   = remember(w, h) { buildAdaptiveSpacing(w, h) }
    val adaptiveRadius    = remember(w, h) { buildAdaptiveRadius(w, h) }

    val fontFamily = if (arabicLocale) biScriptFonts.arabic else biScriptFonts.latin
    val themedTypography = remember(w, h, fontFamily, isDark) {
        buildThemedTypography(w, h, fontFamily, isDark)
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
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
        LocalTypographyTokens provides themedTypography.custom,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = themedTypography.material,
            shapes = SynapseShapes,
            content = content
        )
    }
}