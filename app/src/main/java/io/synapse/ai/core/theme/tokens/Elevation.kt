package io.synapse.ai.core.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Immutable
data class ShadowSpec(
    val elevation: Dp,
    val radius: Dp,
    val color: Color,
    val alpha: Float = 1f,
    val spread: Dp = 0.dp,
    val offset: DpOffset = DpOffset.Unspecified,
)

@Immutable
data class ShadowTokens(

    // structural
    val subtle: ShadowSpec,
    val medium: ShadowSpec,
    val strong: ShadowSpec,
    val overlay: ShadowSpec,

    // semantic
    val goldGlow: ShadowSpec,
    val goldBadge: ShadowSpec,
    val successGlow: ShadowSpec,
    val errorGlow: ShadowSpec,

    // CTA
    val cta: ShadowSpec,
)

fun buildAdaptiveShadowTokens(
    scale: Float,
    isDark: Boolean,
    customColor: Color? = null,
): ShadowTokens {

    fun dp(v: Float): Dp = (v * scale).dp

    val neutral = if (isDark) Color.Black else Color.Black

    val ctaColor = customColor ?: Color(0xFF6366F1)

    return if (isDark) {

        ShadowTokens(

            subtle = ShadowSpec(
                elevation = dp(0f),
                radius = dp(8f),
                color = Color.White,
                alpha = 0.03f,
                offset = DpOffset(0.dp, 0.dp)
            ),

            medium = ShadowSpec(
                elevation = dp(2f),
                radius = dp(14f),
                color = Color.Black,
                alpha = 0.18f,
                offset = DpOffset(0.dp, dp(2f))
            ),

            strong = ShadowSpec(
                elevation = dp(8f),
                radius = dp(26f),
                color = Color.Black,
                alpha = 0.28f,
                offset = DpOffset(0.dp, dp(8f))
            ),

            overlay = ShadowSpec(
                elevation = dp(18f),
                radius = dp(42f),
                color = Color.Black,
                alpha = 0.45f,
                offset = DpOffset(0.dp, dp(16f))
            ),

            goldGlow = ShadowSpec(
                elevation = dp(6f),
                radius = dp(24f),
                color = Color(0xFFD4A72C),
                alpha = 0.22f,
                offset = DpOffset(0.dp, dp(6f))
            ),

            goldBadge = ShadowSpec(
                elevation = dp(2f),
                radius = dp(12f),
                color = Color(0xFFD4A72C),
                alpha = 0.14f,
                offset = DpOffset(0.dp, dp(2f))
            ),

            successGlow = ShadowSpec(
                elevation = dp(2f),
                radius = dp(12f),
                color = Color(0xFF10B981),
                alpha = 0.18f,
                offset = DpOffset(0.dp, dp(2f))
            ),

            errorGlow = ShadowSpec(
                elevation = dp(2f),
                radius = dp(12f),
                color = Color(0xFFEF4444),
                alpha = 0.18f,
                offset = DpOffset(0.dp, dp(2f))
            ),

            cta = ShadowSpec(
                elevation = dp(8f),
                radius = dp(32f),
                color = ctaColor,
                alpha = 0.24f,
                offset = DpOffset(0.dp, dp(8f))
            )
        )
    } else {

        ShadowTokens(

            subtle = ShadowSpec(
                elevation = dp(2f),
                radius = dp(8f),
                color = neutral,
                alpha = 0.05f,
                offset = DpOffset(0.dp, dp(2f))
            ),

            medium = ShadowSpec(
                elevation = dp(6f),
                radius = dp(18f),
                color = neutral,
                alpha = 0.08f,
                offset = DpOffset(0.dp, dp(6f))
            ),

            strong = ShadowSpec(
                elevation = dp(12f),
                radius = dp(28f),
                color = neutral,
                alpha = 0.12f,
                offset = DpOffset(0.dp, dp(10f))
            ),

            overlay = ShadowSpec(
                elevation = dp(18f),
                radius = dp(40f),
                color = neutral,
                alpha = 0.18f,
                offset = DpOffset(0.dp, dp(14f))
            ),

            goldGlow = ShadowSpec(
                elevation = dp(6f),
                radius = dp(22f),
                color = Color(0xFFD4A72C),
                alpha = 0.18f,
                offset = DpOffset(0.dp, dp(6f))
            ),

            goldBadge = ShadowSpec(
                elevation = dp(2f),
                radius = dp(10f),
                color = Color(0xFFD4A72C),
                alpha = 0.12f,
                offset = DpOffset(0.dp, dp(2f))
            ),

            successGlow = ShadowSpec(
                elevation = dp(2f),
                radius = dp(10f),
                color = Color(0xFF10B981),
                alpha = 0.16f,
                offset = DpOffset(0.dp, dp(2f))
            ),

            errorGlow = ShadowSpec(
                elevation = dp(2f),
                radius = dp(10f),
                color = Color(0xFFEF4444),
                alpha = 0.16f,
                offset = DpOffset(0.dp, dp(2f))
            ),

            cta = ShadowSpec(
                elevation = dp(8f),
                radius = dp(28f),
                color = ctaColor,
                alpha = 0.20f,
                offset = DpOffset(0.dp, dp(8f))
            )
        )
    }
}

val defaultShadowTokens = buildAdaptiveShadowTokens(
    scale = 1f,
    isDark = true
)

fun ShadowSpec.toShadow(
    customColor: Color? = null,
    customAlpha: Float? = null,
    customOffset: DpOffset? = null,
): Shadow {
    return Shadow(
        radius = radius,
        spread = spread,
        color = customColor ?: color,
        alpha = customAlpha ?: alpha,
        offset = customOffset ?: if (offset == DpOffset.Unspecified) {
            DpOffset(0.dp, elevation)
        } else {
            offset
        }
    )
}