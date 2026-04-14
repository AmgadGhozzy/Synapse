package io.synapse.ai.core.theme.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape

data class RadiusTokens(
    val xs: Shape,
    val sm: Shape,
    val md: Shape,
    val lg: Shape,
    val xl: Shape,
    val xxl: Shape,
    val xxxl: Shape,
    val pill: Shape,
)

fun buildAdaptiveRadius(scale: Float): RadiusTokens {
    fun dp(v: Float) = adaptDp(v, scale)
    return RadiusTokens(
        xs   = RoundedCornerShape(dp(4f)),
        sm   = RoundedCornerShape(dp(8f)),
        md   = RoundedCornerShape(dp(12f)),
        lg   = RoundedCornerShape(dp(16f)),
        xl   = RoundedCornerShape(dp(20f)),
        xxl  = RoundedCornerShape(dp(24f)),
        xxxl = RoundedCornerShape(dp(28f)),
        pill = RoundedCornerShape(50),
    )
}

val defaultRadiusTokens = buildAdaptiveRadius(1f)

fun buildAdaptiveMaterialShapes(scale: Float): Shapes {
    fun dp(v: Float) = adaptDp(v, scale)
    return Shapes(
        extraSmall = RoundedCornerShape(dp(4f)),
        small      = RoundedCornerShape(dp(8f)),
        medium     = RoundedCornerShape(dp(16f)),
        large      = RoundedCornerShape(dp(24f)),
        extraLarge = RoundedCornerShape(dp(28f)),
    )
}
