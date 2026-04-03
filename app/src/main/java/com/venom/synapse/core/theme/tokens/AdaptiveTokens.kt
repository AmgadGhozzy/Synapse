package com.venom.synapse.core.theme.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

fun buildAdaptiveSpacing(w: Int, h: Int): SpacingTokens {
    fun dp(v: Float) = adaptDp(v, w, h)
    return SpacingTokens(
        xs = dp(4f),
        sm = dp(8f),
        md = dp(16f),
        lg = dp(24f),
        xl = dp(32f),
        s2  = dp(2f),
        s3  = dp(3f),
        s4  = dp(4f),
        s6  = dp(6f),
        s8  = dp(8f),
        s10 = dp(10f),
        s12 = dp(12f),
        s14 = dp(14f),
        s16 = dp(16f),
        s18 = dp(18f),
        s20 = dp(20f),
        s24 = dp(24f),
        s28 = dp(28f),
        s32 = dp(32f),
        s48 = dp(48f),
        s56 = dp(56f),
        s72 = dp(72f),
        screen       = dp(20f),
        screenContentTop = dp(128f),
        screenContentBottom = dp(172f),
        cardInternal = dp(16f),
        cardLarge    = dp(20f),
        sectionGap   = dp(20f),
        listItemGap  = dp(12f),
        iconTextGap  = dp(8f),
        chipH        = dp(16f),
        chipV        = dp(6f),
        fabBottom    = dp(72f),
        fabEnd       = dp(20f),
    )
}

fun buildAdaptiveRadius(w: Int, h: Int): RadiusTokens {
    fun dp(v: Float) = adaptDp(v, w, h)
    return RadiusTokens(
        xs     = RoundedCornerShape(dp(4f)),
        sm     = RoundedCornerShape(dp(8f)),
        md     = RoundedCornerShape(dp(12f)),
        lg     = RoundedCornerShape(dp(16f)),
        xl     = RoundedCornerShape(dp(20f)),
        xxl    = RoundedCornerShape(dp(24f)),
        xxxl   = RoundedCornerShape(dp(28f)),
        pill   = RoundedCornerShape(50),
        circle = CircleShape,
        stripe = RoundedCornerShape(
            topStart    = dp(12f),
            topEnd      = 0.dp,
            bottomEnd   = 0.dp,
            bottomStart = dp(12f),
        ),
    )
}

data class ThemedTypography(
    val material: Typography,
    val custom: TypographyTokens,
)

fun buildThemedTypography(
    w: Int,
    h: Int,
    fontFamily: FontFamily,
    isDark: Boolean,
): ThemedTypography {
    fun sp(v: Float) = adaptSp(v, w, h)

    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.12f)
    val shadow = Shadow(color = shadowColor, offset = Offset(0f, 2f), blurRadius = 4f)

    fun TextStyle.themed(size: Float, leading: Float) = copy(
        fontFamily = fontFamily,
        shadow     = shadow,
        fontSize   = sp(size),
        lineHeight = sp(leading),
    )

    val material = Typography(
        displayLarge   = TypeScale.DisplayLarge.themed(50f, 60f),
        displayMedium  = TypeScale.DisplayMedium.themed(32f, 38f),
        displaySmall   = TypeScale.HeadlineLarge.themed(28f, 36f),
        headlineLarge  = TypeScale.HeadlineLarge.themed(28f, 36f),
        headlineMedium = TypeScale.HeadlineMedium.themed(26f, 32f),
        headlineSmall  = TypeScale.HeadlineSmall.themed(22f, 28f),
        titleLarge     = TypeScale.TitleLarge.themed(20f, 24f),
        titleMedium    = TypeScale.TitleMedium.themed(16f, 26f),
        titleSmall     = TypeScale.TitleSmall.themed(15f, 22f),
        bodyLarge      = TypeScale.BodyLarge.themed(16f, 24f),
        bodyMedium     = TypeScale.TitleSmall.themed(16f, 26f),
        bodySmall      = TypeScale.BodySmall.themed(13f, 18f),
        labelLarge     = TypeScale.LabelLarge.themed(12f, 16f),
        labelMedium    = TypeScale.LabelMedium.themed(10f, 14f),
        labelSmall     = TypeScale.LabelSmall.themed(9f, 12f),
    )

    val custom = TypographyTokens(
        displayHero   = TypeScale.DisplayHero.themed(40f, 40f),
        titleNormal   = TypeScale.TitleNormal.themed(18f, 24f),
        bodySmallBold = TypeScale.BodySmallBold.themed(13f, 18f),
        labelXLarge   = TypeScale.LabelXLarge.themed(14f, 18f),
        labelBase     = TypeScale.LabelBase.themed(11f, 14f),
        labelMicro    = TypeScale.LabelMicro.themed(8f, 10f),
    )

    return ThemedTypography(material, custom)
}
