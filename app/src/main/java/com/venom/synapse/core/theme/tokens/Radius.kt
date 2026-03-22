package com.venom.synapse.core.theme.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class RadiusTokens(
    val sm: Shape = Radius.ShapeSmall,
    val md: Shape = Radius.ShapeMedium,
    val lg: Shape = Radius.ShapeLarge,
    val xl: Shape = Radius.ShapeXL,
    val pill: Shape = Radius.ShapePill,
    val circle: Shape = Radius.ShapeCircle
)

val defaultRadiusTokens = RadiusTokens()

/**
 * Border Radius Token System.
 */
object Radius {

    // Raw DP values

    val RadiusExtraSmall = 4.dp
    val RadiusSmall      = 8.dp
    val RadiusMedium     = 12.dp
    val RadiusLarge      = 16.dp
    val RadiusXL         = 20.dp
    val RadiusXXL        = 24.dp
    val RadiusXXXL       = 28.dp
    val RadiusFrame      = 44.dp  // Phone chrome frame

    // Shape Objects

    /**
     * ShapeExtraSmall — 4dp rounded
     *
     * Usage: Accent stripe left edge on pack cards (borderRadius: "12px 0 0 12px" per-corner).
     *        Tiny fill indicators.
     */
    val ShapeExtraSmall = RoundedCornerShape(RadiusExtraSmall)

    /**
     * ShapeSmall — 8dp rounded
     *
     * Usage: Swipe-reveal action buttons (Edit, Share, Delete).
     *        Toggle switch pill.
     *        Progress bar track.
     */
    val ShapeSmall = RoundedCornerShape(RadiusSmall)

    /**
     * ShapeMedium — 12dp rounded  (= Tailwind rounded-xl)
     *
     * Usage: Icon container squares inside feature list rows and pack cards.
     *        Bottom navigation active indicator pill (44×28dp pill).
     *        Small stat card inner elements.
     */
    val ShapeMedium = RoundedCornerShape(RadiusMedium)

    /**
     * ShapeLarge — 16dp rounded  (= Tailwind rounded-2xl)
     *
     * Usage: Pack cards (draggable surface).
     *        Stat cards (STREAK, ACCURACY, TIME).
     *        "Start Studying" button inside goal hero card.
     *        Quiz mode selector tabs.
     *        Action chips with icons.
     *        Daily goal progress bar button (mt-4 rounded-2xl).
     */
    val ShapeLarge = RoundedCornerShape(RadiusLarge)

    /**
     * ShapeXL — 20dp rounded
     *
     * Usage: Feature list container (borderRadius: 20).
     *        SwipePackCard outer wrapper (borderRadius: 20).
     *        Premium screen section containers.
     */
    val ShapeXL = RoundedCornerShape(RadiusXL)

    /**
     * ShapeXXL — 24dp rounded  (= Tailwind rounded-3xl)
     *
     * Usage: Daily Goal hero card (rounded-3xl p-5).
     *        Premium paywall CTA button (rounded-2xl h-56 → 56dp height, 24dp radius).
     *        FAB (New Pack button) — rounded-2xl with larger padding.
     *        Add-PDF "Done" primary button.
     *        Bottom sheet containers.
     */
    val ShapeXXL = RoundedCornerShape(RadiusXXL)

    /**
     * ShapeXXXL — 28dp rounded
     *
     * Usage: Pricing plan cards in SynapsePremium paywall.
     *        Social proof / trust bar container.
     *        Large modal dialog containers.
     */
    val ShapeXXXL = RoundedCornerShape(RadiusXXXL)

    /**
     * ShapePill — 9999dp (fully rounded pill)
     *
     * Usage: "Go Pro" pill in TopAppBar.
     *        "7-DAY FREE TRIAL" badge pill.
     *        PRO inline chip next to Synapse title.
     *        "SAVE 50%" badge on pricing card.
     *        Language bottom sheet option chips.
     *        All chip filters (question type pills in AddPdf).
     *        BottomNav tab label + icon stack.
     */
    val ShapePill = RoundedCornerShape(50)

    /**
     * ShapeCircle — CircleShape
     *
     * Usage: Avatar button in TopAppBar (40×40dp).
     *        Online status dot (9×9dp).
     *        Circular progress indicator.
     *        Neural network node circles in paywall illustration.
     *        Check badge circles in feature rows.
     *        Close button (34×34dp).
     *        Animated pulse ring overlays.
     */
    val ShapeCircle = CircleShape

    // Per-side Radius for Special Cases

    /**
     * ShapeAccentStripe
     *
     * The 3dp accent stripe on the left edge of pack cards.
     * Only the left corners are rounded to align flush with the card's left edge.
     *
     * Source: `borderRadius: "12px 0 0 12px"` — DashboardScreen SwipePackCard
     */
    val ShapeAccentStripe = RoundedCornerShape(
        topStart    = RadiusMedium,
        topEnd      = 0.dp,
        bottomEnd   = 0.dp,
        bottomStart = RadiusMedium,
    )

    // Material 3 Shape Scale Mapping
    /**
     * Synapse Material 3 Shapes
     *
     * Usage:
     *   MaterialTheme(shapes = SynapseShapes) { ... }
     */
}

val SynapseShapes = Shapes(
    extraSmall = Radius.ShapeExtraSmall,   // 4dp  — tiny elements
    small      = Radius.ShapeSmall,        // 8dp  — action buttons, toggles
    medium     = Radius.ShapeLarge,        // 16dp — cards, chips (M3 "medium")
    large      = Radius.ShapeXXL,          // 24dp — hero cards, modals
    extraLarge = Radius.ShapeXXXL,         // 28dp — pricing cards, dialogs
)
