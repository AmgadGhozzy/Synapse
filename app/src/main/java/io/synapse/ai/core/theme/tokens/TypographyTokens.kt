package io.synapse.ai.core.theme.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import io.synapse.ai.R

object TypeScale {

    // ── Display ───────────────────────────────────────────────────────────────

    val DisplayLarge = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.2).sp,
    )

    val DisplayMedium = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 45.sp,
        lineHeight    = 45.sp,
        letterSpacing = 0.sp,
    )

    val DisplaySmall = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = 0.sp,
    )

    // ── Headline ──────────────────────────────────────────────────────────────

    val HeadlineLarge = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = 0.sp,
    )

    val HeadlineMedium = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = 0.sp,
    )

    val HeadlineSmall = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 24.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp,
    )

    // ── Title ─────────────────────────────────────────────────────────────────

    val TitleLarge = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 22.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp,
    )

    val TitleMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 16.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.2.sp,
    )

    val TitleSmall = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    )

    // ── Body ──────────────────────────────────────────────────────────────────

    val BodyLarge = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp,
    )

    val BodyMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.2.sp,
    )

    val BodySmall = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp,
    )

    // ── Label ─────────────────────────────────────────────────────────────────

    val LabelLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    )

    val LabelMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    )

    val LabelSmall = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    )
}

enum class FontStyles(val title: String) {
    Default("Default"),
    INTER("Inter"),
    CAIRO("Cairo"),
}

private inline fun safeFontFamily(create: () -> FontFamily): FontFamily = try {
    create()
} catch (_: Throwable) {
    FontFamily.Default
}
val InterBold: FontFamily by lazy {
    safeFontFamily {
        FontFamily(
            Font(R.font.inter_bold, FontWeight.Bold),
            Font(R.font.inter_bold, FontWeight.Light),
            Font(R.font.inter_bold, FontWeight.Medium),
            Font(R.font.inter_bold, FontWeight.Normal)
        )
    }
}

val Cairo: FontFamily by lazy {
    safeFontFamily {
        FontFamily(
            Font(R.font.cairo_bold, FontWeight.Bold),
            Font(R.font.cairo_bold, FontWeight.Light),
            Font(R.font.cairo_bold, FontWeight.Medium),
            Font(R.font.cairo_bold, FontWeight.Normal)
        )
    }
}

fun buildThemedTypography(
    scale     : Float,
    fontFamily: FontFamily,
    isDark    : Boolean,
    isArabic  : Boolean,
): Typography {
    fun sp(spValue: Float) = adaptSp(spValue, scale)

    val shadow = Shadow(
        color      = Color.Black.copy(if (isDark) 0.5f else 0.15f),
        offset     = Offset(0f, if (isDark) 3f else 2f),
        blurRadius = if (isDark) 7f else 4f,
    )

    val platformStyle   = PlatformTextStyle(includeFontPadding = false)
    val lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim      = LineHeightStyle.Trim.Both,
    )

    fun TextStyle.themed(fontSize: Float, lineHeight: Float) = copy(
        fontFamily      = fontFamily,
        shadow          = shadow,
        fontSize        = sp(fontSize),
        lineHeight      = sp(lineHeight),
        platformStyle   = if (isArabic) platformStyle else null,
        lineHeightStyle = if (isArabic) lineHeightStyle else null,
    )

    return Typography(
        displayLarge   = TypeScale.DisplayLarge.themed(57f, 57f),
        displayMedium  = TypeScale.DisplayMedium.themed(45f, 45f),
        displaySmall   = TypeScale.DisplaySmall.themed(36f, 44f),
        headlineLarge  = TypeScale.HeadlineLarge.themed(32f, 40f),
        headlineMedium = TypeScale.HeadlineMedium.themed(28f, 36f),
        headlineSmall  = TypeScale.HeadlineSmall.themed(24f, 28f),
        titleLarge     = TypeScale.TitleLarge.themed(22f, 20f),
        titleMedium    = TypeScale.TitleMedium.themed(16f, 18f),
        titleSmall     = TypeScale.TitleSmall.themed(14f, 20f),
        bodyLarge      = TypeScale.BodyLarge.themed(16f, 24f),
        bodyMedium     = TypeScale.BodyMedium.themed(14f, 20f),
        bodySmall      = TypeScale.BodySmall.themed(12f, 16f),
        labelLarge     = TypeScale.LabelLarge.themed(14f, 20f),
        labelMedium    = TypeScale.LabelMedium.themed(12f, 16f),
        labelSmall     = TypeScale.LabelSmall.themed(11f, 16f),
    )
}