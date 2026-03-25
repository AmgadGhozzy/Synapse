package com.venom.synapse.core.theme.tokens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SpacingTokens(
    val xs: Dp = Spacing.Spacing4,
    val sm: Dp = Spacing.Spacing8,
    val md: Dp = Spacing.Spacing16,
    val lg: Dp = Spacing.Spacing24,
    val xl: Dp = Spacing.Spacing32,
    val s2:  Dp = Spacing.Spacing2,
    val s3:  Dp = Spacing.Spacing3,
    val s4:  Dp = Spacing.Spacing4,
    val s6:  Dp = Spacing.Spacing6,
    val s8:  Dp = Spacing.Spacing8,
    val s10: Dp = Spacing.Spacing10,
    val s12: Dp = Spacing.Spacing12,
    val s14: Dp = Spacing.Spacing14,
    val s16: Dp = Spacing.Spacing16,
    val s18: Dp = Spacing.Spacing18,
    val s20: Dp = Spacing.Spacing20,
    val s24: Dp = Spacing.Spacing24,
    val s28: Dp = Spacing.Spacing28,
    val s32: Dp = Spacing.Spacing32,
    val s48: Dp = Spacing.Spacing48,
    val s68: Dp = Spacing.Spacing68,
    /** Universal horizontal screen margin — 20dp */
    val screen:       Dp = Spacing.ScreenHorizontalPadding,
    /** Standard card internal padding — 16dp */
    val cardInternal: Dp = Spacing.CardInternalPadding,
    /** Hero/goal card internal padding — 20dp */
    val cardLarge:    Dp = Spacing.CardInternalPaddingLarge,
    /** Vertical gap between section blocks — 20dp */
    val sectionGap:   Dp = Spacing.SectionVerticalGap,
    /** Vertical gap between list/card items — 12dp */
    val listItemGap:  Dp = Spacing.ListItemVerticalGap,
    /** Gap between icon and adjacent text — 8dp */
    val iconTextGap:  Dp = Spacing.InlineIconTextGap,
    /** Pill chip horizontal padding — 16dp */
    val chipH:        Dp = Spacing.ChipHorizontalPadding,
    /** Pill chip vertical padding — 6dp */
    val chipV:        Dp = Spacing.ChipVerticalPadding,
    /** FAB distance from screen bottom — 68dp */
    val fabBottom:    Dp = Spacing.FabBottomOffset,
    /** FAB distance from screen end — 20dp */
    val fabEnd:       Dp = Spacing.FabRightOffset,
)

val defaultSpacingTokens = SpacingTokens()

object Spacing {
    val Spacing2  = 2.dp
    val Spacing3  = 3.dp
    val Spacing4  = 4.dp
    val Spacing6  = 6.dp
    val Spacing8  = 8.dp
    val Spacing10 = 10.dp
    val Spacing12 = 12.dp
    val Spacing14 = 14.dp
    val Spacing16 = 16.dp
    val Spacing18 = 18.dp
    val Spacing20 = 20.dp
    val Spacing24 = 24.dp
    val Spacing28 = 28.dp
    val Spacing32 = 32.dp
    val Spacing48 = 48.dp
    val Spacing68 = 68.dp

    val ScreenHorizontalPadding  = Spacing20
    val CardInternalPadding      = Spacing16
    val CardInternalPaddingLarge = Spacing20
    val SectionVerticalGap       = Spacing20
    val ListItemVerticalGap      = Spacing12
    val InlineIconTextGap        = Spacing8
    val ChipHorizontalPadding    = Spacing16
    val ChipVerticalPadding      = Spacing6
    val FabBottomOffset          = Spacing68
    val FabRightOffset           = Spacing20

    val ScreenPaddingValues = PaddingValues(horizontal = Spacing20)
    val ScreenPaddingWithNav = PaddingValues(
        start  = Spacing20,
        end    = Spacing20,
        bottom = Spacing24,
    )
    val CardPaddingValues       = PaddingValues(all = Spacing16)
    val FeatureRowPaddingValues = PaddingValues(horizontal = Spacing16, vertical = Spacing12)
}