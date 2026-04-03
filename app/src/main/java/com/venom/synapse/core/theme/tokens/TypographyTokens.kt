package com.venom.synapse.core.theme.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object TypeScale {

    val DisplayLarge = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 50.sp,
        lineHeight    = 60.sp,
        letterSpacing = (-0.6).sp,
    )

    val DisplayMedium = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 32.sp,
        lineHeight    = 38.sp,
        letterSpacing = (-0.5).sp,
    )
    val HeadlineLarge = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = 0.sp,
    )

    val HeadlineMedium = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 26.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    )

    val HeadlineSmall = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp,
    )

    val TitleLarge = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 20.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    )

    val TitleMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 16.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp,
    )

    val TitleSmall = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp,
    )
    val BodyLarge = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.01.sp,
    )

    val BodySmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.02.sp,
    )

    val LabelLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.03.sp,
    )

    val LabelMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.12.sp,
    )

    val LabelSmall = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 9.sp,
        lineHeight    = 12.sp,
        letterSpacing = 0.07.sp,
    )

    // Custom
    val DisplayHero = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 40.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.5).sp,
    )

    val TitleNormal = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    )

    val BodySmallBold = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    )

    val LabelXLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    )

    val LabelBase = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.sp,
    )

    val LabelMicro = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 8.sp,
        lineHeight    = 10.sp,
        letterSpacing = 0.05.sp,
    )
}

data class TypographyTokens(
    val displayHero:   TextStyle = TypeScale.DisplayHero,
    val titleNormal:   TextStyle = TypeScale.TitleNormal,
    val bodySmallBold: TextStyle = TypeScale.BodySmallBold,
    val labelXLarge:   TextStyle = TypeScale.LabelXLarge,
    val labelBase:     TextStyle = TypeScale.LabelBase,
    val labelMicro:    TextStyle = TypeScale.LabelMicro,
)

val defaultTypographyTokens: TypographyTokens by lazy { TypographyTokens() }