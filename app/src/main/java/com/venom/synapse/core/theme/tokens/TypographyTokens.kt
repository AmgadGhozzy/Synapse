package com.venom.synapse.core.theme.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * TypographyTokens.kt — Synapse Type System
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * Font: Inter (Google Fonts)
 * Source: All font-size values extracted from inline styles across:
 *   /src/app/components/DashboardScreen.tsx
 *   /src/app/components/SynapsePremium.tsx
 *   /src/app/components/QuizScreen.tsx
 *   /src/app/components/BottomNav.tsx
 *   /src/app/components/TopAppBar.tsx
 *   /src/styles/theme.css
 *
 * Scale note: 1rem = 16px; CSS px → sp at 1:1 for 16px base.
 *
 * Structure
 * ─────────
 * object TypeScale        — all 19 raw TextStyle constants (single source of truth).
 * data class TypographyTokens — mirrors TypeScale; consumed via LocalTypographyTokens.
 * val SynapseTypography   — M3 Typography mapping; consumed via MaterialTheme.typography.
 *
 * Always add new type styles to TypeScale FIRST, then add the corresponding
 * property to TypographyTokens and update SynapseTypography if a suitable M3
 * role exists.
 * ══════════════════════════════════════════════════════════════════════════════
 */

// Font Family setup — update once Inter font files are in /res/font/
val InterFontFamily = FontFamily.Default

// Raw Scale Tokens

object TypeScale {

    // Display ─────────────────────────────────────────────────────────────────

    /**
     * DisplayHero — 40sp / Black (900)
     * Source: Daily Goal card large number ("23"). lineHeight 1.0 — numeric display.
     * Usage: Hero metric numbers, large countdown displays, score screens.
     */
    val DisplayHero = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 40.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.5).sp,
    )

    /**
     * DisplayLarge — 34sp / Black (900)
     * Source: `fontSize: 34, fontWeight: 900, letterSpacing: -0.6px` — paywall hero title.
     * Usage: Screen hero titles (paywall, onboarding splash).
     */
    val DisplayLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 34.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.6).sp,
    )

    /**
     * DisplayMedium — 32sp / Black (900)
     * Source: `fontSize: 32, fontWeight: 900` — onboarding brand text.
     * Usage: Large decorative brand headings.
     */
    val DisplayMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 32.sp,
        lineHeight    = 38.sp,
        letterSpacing = (-0.5).sp,
    )

    // Headline ────────────────────────────────────────────────────────────────

    /**
     * HeadlineLarge — 28sp / Black (900)
     * Source: Pricing card price display.
     * Usage: Price displays, large numeric values in cards.
     */
    val HeadlineLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 28.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    )

    /**
     * HeadlineMedium — 26sp / Black (900)
     * Source: Stats card large values ("7", "78%", "18m").
     * Usage: Stats metric values, bold count displays.
     */
    val HeadlineMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 26.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp,
    )

    // Title ───────────────────────────────────────────────────────────────────

    /**
     * TitleXLarge — 22sp / ExtraBold (800)
     * Source: Quiz screen section title.
     * Usage: Major screen section titles.
     */
    val TitleXLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp,
    )

    /**
     * TitleLarge — 20sp / ExtraBold (800)
     * Source: TopAppBar title ("Dashboard").
     * Usage: TopAppBar screen title, primary section headings.
     */
    val TitleLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 20.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    )

    /**
     * TitleMedium — 18sp / Normal (400)
     * Source: Goal denominator ("/30").
     * Usage: Secondary metric denominators, supporting numbers.
     */
    val TitleMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    )

    // Body ────────────────────────────────────────────────────────────────────

    /**
     * BodyXLarge — 16sp / ExtraBold (800)
     * Source: CTA button labels ("Start Free Trial").
     * Usage: Primary action buttons, important CTAs.
     */
    val BodyXLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.01.sp,
    )

    /**
     * BodyLarge — 15sp / Bold (700)
     * Source: "Jump Back In" section header.
     * Usage: Section headers in scrollable content, list group titles.
     */
    val BodyLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 15.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp,
    )

    /**
     * BodyMedium — 14sp / SemiBold (600)
     * Source: Pack card title text.
     * Usage: Card titles, list item primary text, quiz answer options.
     */
    val BodyMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp,
    )

    /**
     * BodySmall — 13sp / Bold (700)
     * Source: "New Pack" FAB label, stat sub-labels.
     * Usage: FAB labels, small buttons, timestamp chips, "see all" links.
     */
    val BodySmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    )

    /**
     * BodySmallRegular — 13sp / Medium (500)
     * Source: TopAppBar subtitle ("Good morning"), paywall tagline.
     * Usage: Subtitles under screen titles, secondary body text.
     */
    val BodySmallRegular = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.02.sp,
    )

    // Label ───────────────────────────────────────────────────────────────────

    /**
     * LabelXLarge — 13.5sp / SemiBold (600)
     * Source: Feature list row primary label.
     * Usage: Feature list primary text, prominent list item labels.
     */
    val LabelXLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 13.5.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    )

    /**
     * LabelLarge — 12sp / SemiBold (600)
     * Source: Pack "7 due" indicator, "7-day streak" label.
     * Usage: Badge counts, chip text, secondary metric labels.
     */
    val LabelLarge = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.03.sp,
    )

    /**
     * LabelMedium — 11sp / SemiBold (600)
     * Source: Pack streak value, feature list sub-labels.
     * Usage: Supporting text in cards, SRS interval display, hint text.
     */
    val LabelMedium = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.sp,
    )

    /**
     * LabelSmall — 10sp / Bold (700) with 0.12sp caps tracking
     * Source: BottomNav labels (active), ALL-CAPS section headers.
     * Usage: Bottom navigation labels, section divider caps, small metadata.
     */
    val LabelSmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.12.sp,
    )

    /**
     * LabelXSmall — 9sp / ExtraBold (800)
     * Source: Stats STREAK caps label, swipe action button label.
     * Usage: Micro ALL-CAPS labels on chips, action button labels.
     */
    val LabelXSmall = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 9.sp,
        lineHeight    = 12.sp,
        letterSpacing = 0.07.sp,
    )

    /**
     * LabelMicro — 8sp / Black (900)
     * Source: "SAVE 50%" badge text.
     * Usage: Smallest promotional badges, pill label text.
     */
    val LabelMicro = TextStyle(
        fontFamily    = InterFontFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 8.sp,
        lineHeight    = 10.sp,
        letterSpacing = 0.05.sp,
    )
}

// ── TypographyTokens data class ───────────────────────────────────────────────
// Mirrors TypeScale 1:1 so every style is accessible via
// MaterialTheme.synapse.typographyTokens.*  with full IDE completion.

data class TypographyTokens(
    // Display
    val displayHero:      TextStyle = TypeScale.DisplayHero,
    val displayLarge:     TextStyle = TypeScale.DisplayLarge,
    val displayMedium:    TextStyle = TypeScale.DisplayMedium,
    // Headline
    val headlineLarge:    TextStyle = TypeScale.HeadlineLarge,
    val headlineMedium:   TextStyle = TypeScale.HeadlineMedium,
    // Title
    val titleXLarge:      TextStyle = TypeScale.TitleXLarge,
    val titleLarge:       TextStyle = TypeScale.TitleLarge,
    val titleMedium:      TextStyle = TypeScale.TitleMedium,
    // Body
    val bodyXLarge:       TextStyle = TypeScale.BodyXLarge,
    val bodyLarge:        TextStyle = TypeScale.BodyLarge,
    val bodyMedium:       TextStyle = TypeScale.BodyMedium,
    val bodySmall:        TextStyle = TypeScale.BodySmall,
    val bodySmallRegular: TextStyle = TypeScale.BodySmallRegular,
    // Label
    val labelXLarge:      TextStyle = TypeScale.LabelXLarge,
    val labelLarge:       TextStyle = TypeScale.LabelLarge,
    val labelMedium:      TextStyle = TypeScale.LabelMedium,
    val labelSmall:       TextStyle = TypeScale.LabelSmall,
    val labelXSmall:      TextStyle = TypeScale.LabelXSmall,
    val labelMicro:       TextStyle = TypeScale.LabelMicro,
)

val defaultTypographyTokens = TypographyTokens()

// ── Material 3 Typography Mapping ─────────────────────────────────────────────
/**
 * SynapseTypography — static M3 mapping with InterFontFamily.
 *
 * Passed to MaterialTheme via getTypography(fontFamily) in Theme.kt, which
 * overrides fontFamily to support user-selected font preferences while keeping
 * all size/weight values from TypeScale.
 */
val SynapseTypography = Typography(
    // Display
    displayLarge  = TypeScale.DisplayLarge,
    displayMedium = TypeScale.DisplayMedium,
    displaySmall  = TypeScale.HeadlineLarge,   // largest headline maps to M3 displaySmall

    // Headline
    headlineLarge  = TypeScale.HeadlineLarge,
    headlineMedium = TypeScale.HeadlineMedium,
    headlineSmall  = TypeScale.TitleXLarge,

    // Title
    titleLarge  = TypeScale.TitleLarge,
    titleMedium = TypeScale.BodyLarge,
    titleSmall  = TypeScale.BodyMedium,

    // Body
    bodyLarge  = TypeScale.BodyXLarge,
    bodyMedium = TypeScale.BodyMedium,
    bodySmall  = TypeScale.BodySmallRegular,

    // Label
    labelLarge  = TypeScale.LabelLarge,
    labelMedium = TypeScale.LabelSmall,
    labelSmall  = TypeScale.LabelXSmall,
)