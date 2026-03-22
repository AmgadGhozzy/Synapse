package com.venom.synapse.core.theme.tokens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SpacingTokens(
    val xs: Dp = Spacing.Spacing4,
    val sm: Dp = Spacing.Spacing8,
    val md: Dp = Spacing.Spacing16,
    val lg: Dp = Spacing.Spacing24,
    val xl: Dp = Spacing.Spacing32
)

val defaultSpacingTokens = SpacingTokens()

/**
 * Spacing Token System.
 */
object Spacing {

    // Micro
    /**
     * Spacing2 — 2dp
     *
     * Usage: Micro gaps between tightly related elements.
     *        Inline icon+text spacing in streak chips.
     *        Online status dot inset offset.
     */
    val Spacing2 = 2.dp

    /**
     * Spacing3 — 3dp
     *
     * Usage: Accent stripe width on pack cards (swipe-reveal).
     *        Very tight icon padding.
     */
    val Spacing3 = 3.dp

    /**
     * Spacing4 — 4dp
     *
     * Usage: Gap between icon and label in small chips.
     *        Minimal vertical padding inside badge pills.
     *        Spacing between inline star icons.
     */
    val Spacing4 = 4.dp

    // Small
    /**
     * Spacing6 — 6dp
     *
     * Usage: Gap between avatar and title text in TopAppBar.
     *        Toggle switch thumb horizontal travel.
     *        Progress bar height.
     */
    val Spacing6 = 6.dp

    /**
     * Spacing8 — 8dp
     *
     * Usage: Small internal card padding.
     *        Gap between rows in feature lists.
     *        TopAppBar top padding (pt-2).
     *        Horizontal padding for badge pills.
     *        Status bar to content gap.
     */
    val Spacing8 = 8.dp

    /**
     * Spacing10 — 10dp
     *
     * Usage: Section header bottom margin (marginBottom: 10 on "Everything in Pro" label).
     *        Bottom padding of BottomNav item.
     *        Inset from card edge for floating check badge.
     */
    val Spacing10 = 10.dp

    // Medium
    /**
     * Spacing12 — 12dp
     *
     * Usage: Inner card padding (p-3 = 12dp).
     *        Vertical padding in TopAppBar (pb-3).
     *        Gap between stat cards.
     *        Bottom margin for "Daily Goal" section.
     *        Section sub-label margin.
     */
    val Spacing12 = 12.dp

    /**
     * Spacing14 — 14dp
     *
     * Usage: Pack card inner padding (p-3.5 = 14dp).
     *        Small icon container padding.
     *        Premium paywall top/bottom padding variations.
     */
    val Spacing14 = 14.dp

    /**
     * Spacing16 — 16dp
     *
     * Usage: Standard card internal padding (p-4).
     *        Bottom margin between major sections (mb-4).
     *        Pricing card internal padding.
     *        Feature list icon container: 38dp total → ~14dp inset.
     */
    val Spacing16 = 16.dp

    /**
     * Spacing18 — 18dp
     *
     * Usage: FAB horizontal padding (padding: "13px 18px").
     *        Toggle switch thumb travel distance (dark).
     */
    val Spacing18 = 18.dp

    // Standard Screen Edge
    /**
     * Spacing20 — 20dp  ★ PRIMARY SCREEN MARGIN
     *
     * Usage: Universal horizontal screen margin (px-5 = 20dp).
     *        Used on every screen: DashboardScreen, LibraryScreen,
     *        SynapsePremium, AddPdfScreen, ProfileScreen, QuizScreen.
     *
     *   Apply as: Modifier.padding(horizontal = Spacing.Spacing20)
     *   Or:       contentPadding = PaddingValues(horizontal = Spacing.Spacing20)
     */
    val Spacing20 = 20.dp

    // Large
    /**
     * Spacing24 — 24dp
     *
     * Usage: Screen bottom scroll padding (paddingBottom: 24).
     *        Large card internal padding (px-6 = 24dp → hero card).
     *        Border radius companion spacing.
     */
    val Spacing24 = 24.dp

    /**
     * Spacing28 — 28dp
     *
     * Usage: Paywall body bottom padding (paddingBottom: 28).
     *        Bottom sheet handle area height.
     *        Close button diagonal gutter.
     */
    val Spacing28 = 28.dp

    /**
     * Spacing32 — 32dp
     *
     * Usage: Large section separation.
     *        Outer page margin on tablets.
     *        Quiz result card padding.
     */
    val Spacing32 = 32.dp

    // Extra Large
    /**
     * Spacing48 — 48dp
     *
     * Usage: Full-screen section spacing.
     *        Large illustration margin.
     *        Wide-screen layout gutter.
     */
    val Spacing48 = 48.dp

    /**
     * Spacing82 — 82dp
     *
     * Usage: FAB bottom offset above BottomNav
     *        (bottom: 82dp in DashboardScreen — accounts for BottomNav height ~66dp + 16dp margin).
     */
    val Spacing72 = 72.dp

    // Semantic Spacing Aliases

    /** ScreenHorizontalPadding — 20dp — used on all scrollable screen content */
    val ScreenHorizontalPadding = Spacing20

    /** CardInternalPadding — 16dp — standard content padding inside cards */
    val CardInternalPadding = Spacing16

    /** CardInternalPaddingLarge — 20dp (p-5) — hero/goal card internal padding */
    val CardInternalPaddingLarge = Spacing20

    /** SectionVerticalGap — 20dp — vertical space between section blocks */
    val SectionVerticalGap = Spacing20

    /** ListItemVerticalGap — 12dp — vertical gap between list/card items */
    val ListItemVerticalGap = Spacing12

    /** InlineIconTextGap — 8dp — gap between icon and adjacent text */
    val InlineIconTextGap = Spacing8

    /** ChipHorizontalPadding — 16dp (px-4 = 16dp) — pill chip left/right padding */
    val ChipHorizontalPadding = Spacing16

    /** ChipVerticalPadding — 6dp (py-1.5 = 6dp) — pill chip top/bottom padding */
    val ChipVerticalPadding = Spacing6

    /** FabBottomOffset — 82dp — FAB distance from screen bottom */
    val FabBottomOffset = Spacing72

    /** FabRightOffset — 20dp — FAB distance from screen right */
    val FabRightOffset = Spacing20

    // PaddingValues Presets

    /** Standard screen content padding (horizontal 20dp) */
    val ScreenPaddingValues = PaddingValues(horizontal = Spacing20)

    /** Standard screen content padding with bottom safe-area clearance */
    val ScreenPaddingWithNav = PaddingValues(
        start = Spacing20,
        end = Spacing20,
        bottom = Spacing24,
    )

    /** Card internal padding preset */
    val CardPaddingValues = PaddingValues(all = Spacing16)

    /** Feature list row padding (horizontal 16dp, vertical 12dp) */
    val FeatureRowPaddingValues = PaddingValues(
        horizontal = Spacing16,
        vertical = Spacing12,
    )
}
