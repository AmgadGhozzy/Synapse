package com.venom.synapse.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.venom.domain.model.AppTheme
import com.venom.domain.model.FontStyles
import com.venom.synapse.core.theme.tokens.DarkGradientTokens
import com.venom.synapse.core.theme.tokens.LightGradientTokens
import com.venom.synapse.core.theme.tokens.SynapseShapes
import com.venom.synapse.core.theme.tokens.defaultComponentTokens
import com.venom.synapse.core.theme.tokens.defaultElevationTokens
import com.venom.synapse.core.theme.tokens.defaultRadiusTokens
import com.venom.synapse.core.theme.tokens.defaultSpacingTokens
import com.venom.synapse.core.theme.tokens.defaultTypographyTokens
import com.venom.ui.components.common.adp
import com.venom.ui.theme.getTypography
import com.venom.ui.theme.safeLoadFontFamily
import com.venom.ui.theme.tokens.DarkGlassColors
import com.venom.ui.theme.tokens.LightGlassColors
import com.venom.ui.theme.tokens.LocalGlassColors
import com.venom.ui.theme.tokens.LocalNavBarColors
import com.venom.ui.theme.tokens.NavBarAccent
import com.venom.ui.theme.tokens.resolveNavBarColors

@Composable
fun SynapseTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    fontFamilyStyle: FontStyles = FontStyles.Default,
    navBarAccent: NavBarAccent = NavBarAccent.Indigo,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val fontFamily = safeLoadFontFamily(fontFamilyStyle)

    val isDark = when (appTheme) {
        AppTheme.DARK   -> true
        AppTheme.LIGHT  -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) SynapseDarkColorScheme else SynapseLightColorScheme

    // Compute the nav-bar tint inline to prevent background flash on the first frame.
    val navBarColor = colorScheme.surfaceColorAtElevation(3.adp)

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            window.navigationBarColor = navBarColor.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.isAppearanceLightStatusBars    = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalSynapseSemanticColors provides if (isDark) DarkSynapseSemanticColors else LightSynapseSemanticColors,
        LocalGlassColors    provides if (isDark) DarkGlassColors else LightGlassColors,
        LocalNavBarColors   provides resolveNavBarColors(isDark, navBarAccent),
        LocalGradientTokens provides if (isDark) DarkGradientTokens else LightGradientTokens,
        LocalSpacingTokens  provides defaultSpacingTokens,
        LocalRadiusTokens   provides defaultRadiusTokens,
        LocalElevationTokens provides defaultElevationTokens,
        LocalTypographyTokens provides defaultTypographyTokens,
        LocalComponentTokens  provides defaultComponentTokens,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = getTypography(fontFamily),
            shapes      = SynapseShapes,
            content     = content,
        )
    }
}