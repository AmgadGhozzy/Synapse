package com.venom.synapse.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.venom.synapse.core.theme.tokens.GradientTokens
import com.venom.synapse.core.theme.tokens.RadiusTokens
import com.venom.synapse.core.theme.tokens.ShadowTokens
import com.venom.synapse.core.theme.tokens.SpacingTokens
import com.venom.synapse.core.theme.tokens.defaultGradientTokens
import com.venom.synapse.core.theme.tokens.defaultRadiusTokens
import com.venom.synapse.core.theme.tokens.defaultShadowTokens
import com.venom.synapse.core.theme.tokens.defaultSpacingTokens
import com.venom.ui.theme.tokens.GlassColors
import com.venom.ui.theme.tokens.LocalGlassColors

/**
 * Synthetic helper to read Synapse-specific theme tokens via MaterialTheme.
 */
data class SynapseTokens(
    val semantic: SemanticColors,
    val levelColors: LevelColors,
    val glass: GlassColors,
    val gradients: GradientTokens,
    val spacing: SpacingTokens,
    val radius: RadiusTokens,
    val shadows: ShadowTokens,
)

val MaterialTheme.synapse: SynapseTokens
    @Composable
    @ReadOnlyComposable
    get() = SynapseTokens(
        semantic = LocalSemanticColors.current,
        levelColors = LocalLevelColors.current,
        glass = LocalGlassColors.current,
        gradients = LocalGradientTokens.current,
        spacing = LocalSpacingTokens.current,
        radius = LocalRadiusTokens.current,
        shadows = LocalShadowTokens.current,
    )

val LocalGradientTokens = staticCompositionLocalOf {
    defaultGradientTokens
}

val LocalSpacingTokens = staticCompositionLocalOf {
    defaultSpacingTokens
}

val LocalRadiusTokens = staticCompositionLocalOf {
    defaultRadiusTokens
}

val LocalShadowTokens = staticCompositionLocalOf {
    defaultShadowTokens
}