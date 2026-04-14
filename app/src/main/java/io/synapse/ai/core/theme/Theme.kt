package io.synapse.ai.core.theme

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
import io.synapse.ai.R
import io.synapse.ai.core.theme.tokens.Cairo
import io.synapse.ai.core.theme.tokens.DarkGradientTokens
import io.synapse.ai.core.theme.tokens.FontStyles
import io.synapse.ai.core.theme.tokens.InterBold
import io.synapse.ai.core.theme.tokens.LightGradientTokens
import io.synapse.ai.core.theme.tokens.LocalAdaptiveScale
import io.synapse.ai.core.theme.tokens.buildAdaptiveMaterialShapes
import io.synapse.ai.core.theme.tokens.buildAdaptiveRadius
import io.synapse.ai.core.theme.tokens.buildAdaptiveShadowTokens
import io.synapse.ai.core.theme.tokens.buildAdaptiveSpacing
import io.synapse.ai.core.theme.tokens.buildThemedTypography
import io.synapse.ai.core.theme.tokens.computeAdaptiveScale

@Composable
fun SynapseTheme(
    appTheme        : AppTheme   = AppTheme.SYSTEM,
    fontFamilyStyle : FontStyles = FontStyles.Default,
    content         : @Composable () -> Unit,
) {
    val view         = LocalView.current
    val arabicLocale = true // isArabicLocale()

    val biScriptFonts = remember(fontFamilyStyle) {
        when (fontFamilyStyle) {
            FontStyles.Default -> BiScriptFonts(latin = InterBold, arabic = Cairo)
            else -> {
                val chosen = try {
                    when (fontFamilyStyle) {
                        FontStyles.INTER        -> InterBold
                        FontStyles.CAIRO        -> Cairo
                    }
                } catch (_: Exception) { InterBold }
                BiScriptFonts(latin = chosen, arabic = chosen)
            }
        }
    }

    val isDark = when (appTheme) {
        AppTheme.DARK   -> true
        AppTheme.LIGHT  -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (isDark) SynapseDarkColorScheme else SynapseLightColorScheme

    val windowInfo = LocalWindowInfo.current
    val density    = LocalDensity.current
    val w = with(density) { windowInfo.containerSize.width.toDp().value.toInt() }
    val h = with(density) { windowInfo.containerSize.height.toDp().value.toInt() }

    val scale = remember(w, h) { computeAdaptiveScale(w, h) }

    val adaptiveSpacing  = remember(scale) { buildAdaptiveSpacing(scale) }
    val adaptiveRadius   = remember(scale) { buildAdaptiveRadius(scale) }
    val adaptiveShapes   = remember(scale) { buildAdaptiveMaterialShapes(scale) }
    val adaptiveShadows  = remember(scale) { buildAdaptiveShadowTokens(scale) }

    val fontFamily       = if (arabicLocale) biScriptFonts.arabic else biScriptFonts.latin
    val themedTypography = remember(scale, fontFamily, isDark) {
        buildThemedTypography(scale, fontFamily, isDark, arabicLocale)
    }
    if (!view.isInEditMode) {
        SideEffect {
            val window     = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.isAppearanceLightStatusBars     = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalAdaptiveScale  provides scale,
        LocalBiScriptFonts  provides biScriptFonts,
        LocalSemanticColors provides if (isDark) DarkSynapseSemanticColors else LightSynapseSemanticColors,
        LocalLevelColors    provides if (isDark) DarkLevelColors           else LightLevelColors,
        LocalGradientTokens provides if (isDark) DarkGradientTokens        else LightGradientTokens,
        LocalSpacingTokens  provides adaptiveSpacing,
        LocalRadiusTokens   provides adaptiveRadius,
        LocalShadowTokens   provides adaptiveShadows,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = themedTypography,
            shapes      = adaptiveShapes,
            content     = content,
        )
    }
}

enum class AppTheme(val title: Int) {
    DARK(R.string.profile_dark_mode),
    LIGHT(R.string.profile_light_mode),
    SYSTEM(R.string.profile_follow_system)
}