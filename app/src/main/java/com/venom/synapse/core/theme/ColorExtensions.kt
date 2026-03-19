package com.venom.synapse.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.venom.synapse.core.theme.tokens.ComponentTokens
import com.venom.synapse.core.theme.tokens.ElevationTokens
import com.venom.synapse.core.theme.tokens.GradientTokens
import com.venom.synapse.core.theme.tokens.LevelColors
import com.venom.synapse.core.theme.tokens.LocalLevelColors
import com.venom.synapse.core.theme.tokens.RadiusTokens
import com.venom.synapse.core.theme.tokens.SpacingTokens
import com.venom.synapse.core.theme.tokens.TypographyTokens
import com.venom.synapse.core.theme.tokens.defaultComponentTokens
import com.venom.synapse.core.theme.tokens.defaultElevationTokens
import com.venom.synapse.core.theme.tokens.defaultRadiusTokens
import com.venom.synapse.core.theme.tokens.defaultSpacingTokens
import com.venom.synapse.core.theme.tokens.defaultTypographyTokens
import com.venom.ui.theme.tokens.GlassColors
import com.venom.ui.theme.tokens.LocalGlassColors

/**
 * Synthetic helper to read Synapse-specific theme tokens via MaterialTheme.
 */
data class SynapseColors(
    val semantic: SynapseSemanticColors,
    val levelColors: LevelColors,
    val glass: GlassColors,
    val gradients: GradientTokens,
    val spacing: SpacingTokens,
    val radius: RadiusTokens,
    val elevation: ElevationTokens,
    val typographyTokens: TypographyTokens,
    val components: ComponentTokens
)

val MaterialTheme.synapse: SynapseColors
    @Composable
    @ReadOnlyComposable
    get() = SynapseColors(
        semantic = LocalSemanticColors.current,
        levelColors = LocalLevelColors.current,
        glass = LocalGlassColors.current,
        gradients = LocalGradientTokens.current,
        spacing = LocalSpacingTokens.current,
        radius = LocalRadiusTokens.current,
        elevation = LocalElevationTokens.current,
        typographyTokens = LocalTypographyTokens.current,
        components = LocalComponentTokens.current
    )

val LocalGradientTokens = staticCompositionLocalOf<GradientTokens> {
    error("No GradientTokens provided")
}

val LocalSpacingTokens = staticCompositionLocalOf {
    defaultSpacingTokens
}

val LocalRadiusTokens = staticCompositionLocalOf {
    defaultRadiusTokens
}

val LocalElevationTokens = staticCompositionLocalOf {
    defaultElevationTokens
}

val LocalTypographyTokens = staticCompositionLocalOf {
    defaultTypographyTokens
}

val LocalComponentTokens = staticCompositionLocalOf {
    defaultComponentTokens
}
