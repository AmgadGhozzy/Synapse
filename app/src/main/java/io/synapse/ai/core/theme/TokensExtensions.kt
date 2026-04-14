package io.synapse.ai.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import io.synapse.ai.core.theme.tokens.GradientTokens
import io.synapse.ai.core.theme.tokens.RadiusTokens
import io.synapse.ai.core.theme.tokens.ShadowTokens
import io.synapse.ai.core.theme.tokens.SpacingTokens
import io.synapse.ai.core.theme.tokens.defaultGradientTokens
import io.synapse.ai.core.theme.tokens.defaultRadiusTokens
import io.synapse.ai.core.theme.tokens.defaultShadowTokens
import io.synapse.ai.core.theme.tokens.defaultSpacingTokens

/**
 * Synthetic helper to read Synapse-specific theme tokens via MaterialTheme.
 */
data class SynapseTokens(
    val semantic: SemanticColors,
    val levelColors: LevelColors,
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