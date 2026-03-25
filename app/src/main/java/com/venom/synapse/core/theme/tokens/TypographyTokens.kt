package com.venom.synapse.core.theme.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val InterFontFamily = FontFamily.Default

object TypeScale {

    val DisplayHero = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 40.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.5).sp,
    )

    val DisplayLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 34.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.6).sp,
    )

    val DisplayMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 32.sp,
        lineHeight    = 38.sp,
        letterSpacing = (-0.5).sp,
    )

    val HeadlineLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 28.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    )

    val HeadlineMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 26.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp,
    )

    val TitleXLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp,
    )

    val TitleLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 20.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    )

    val TitleMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    )

    val BodyXLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.01.sp,
    )

    val BodyLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp,
    )

    val BodyMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp,
    )

    val BodySmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    )

    val BodySmallRegular = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.02.sp,
    )

    val LabelXLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 13.5.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    )

    val LabelLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.03.sp,
    )

    val LabelMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.sp,
    )

    val LabelSmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.12.sp,
    )

    val LabelXSmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 9.sp,
        lineHeight    = 12.sp,
        letterSpacing = 0.07.sp,
    )

    val LabelMicro = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 8.sp,
        lineHeight    = 10.sp,
        letterSpacing = 0.05.sp,
    )
}

data class TypographyTokens(
    val displayHero:      TextStyle = TypeScale.DisplayHero,
    val displayLarge:     TextStyle = TypeScale.DisplayLarge,
    val displayMedium:    TextStyle = TypeScale.DisplayMedium,
    val headlineLarge:    TextStyle = TypeScale.HeadlineLarge,
    val headlineMedium:   TextStyle = TypeScale.HeadlineMedium,
    val titleXLarge:      TextStyle = TypeScale.TitleXLarge,
    val titleLarge:       TextStyle = TypeScale.TitleLarge,
    val titleMedium:      TextStyle = TypeScale.TitleMedium,
    val bodyXLarge:       TextStyle = TypeScale.BodyXLarge,
    val bodyLarge:        TextStyle = TypeScale.BodyLarge,
    val bodyMedium:       TextStyle = TypeScale.BodyMedium,
    val bodySmall:        TextStyle = TypeScale.BodySmall,
    val bodySmallRegular: TextStyle = TypeScale.BodySmallRegular,
    val labelXLarge:      TextStyle = TypeScale.LabelXLarge,
    val labelLarge:       TextStyle = TypeScale.LabelLarge,
    val labelMedium:      TextStyle = TypeScale.LabelMedium,
    val labelSmall:       TextStyle = TypeScale.LabelSmall,
    val labelXSmall:      TextStyle = TypeScale.LabelXSmall,
    val labelMicro:       TextStyle = TypeScale.LabelMicro,
)

val defaultTypographyTokens: TypographyTokens by lazy { TypographyTokens() }

val SynapseTypography = Typography(
    displayLarge  = TypeScale.DisplayLarge,
    displayMedium = TypeScale.DisplayMedium,
    displaySmall  = TypeScale.HeadlineLarge,
    headlineLarge  = TypeScale.HeadlineLarge,
    headlineMedium = TypeScale.HeadlineMedium,
    headlineSmall  = TypeScale.TitleXLarge,
    titleLarge  = TypeScale.TitleLarge,
    titleMedium = TypeScale.BodyLarge,
    titleSmall  = TypeScale.BodyMedium,
    bodyLarge  = TypeScale.BodyXLarge,
    bodyMedium = TypeScale.BodyMedium,
    bodySmall  = TypeScale.BodySmallRegular,
    labelLarge  = TypeScale.LabelLarge,
    labelMedium = TypeScale.LabelSmall,
    labelSmall  = TypeScale.LabelXSmall,
)