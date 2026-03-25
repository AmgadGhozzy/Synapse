package com.venom.synapse.core.theme.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venom.synapse.core.theme.White

object TopAppBarTokens {
    val Height: Dp = 64.dp
    val HorizontalPadding: Dp = Spacing.Spacing20

    val AvatarSize: Dp = 48.dp
    val AvatarShape = Radius.ShapeCircle
    val AvatarBorderWidth: Dp = 1.5.dp

    val GoProHorizontalPadding: Dp = Spacing.Spacing12 + 2.dp
    val GoProVerticalPadding: Dp = Spacing.Spacing8
    val GoProShape = Radius.ShapePill
    val GoProIconSize: Dp = 14.dp
    val GoProFontSize: TextUnit = 12.sp
    val GoProFontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
    val GoProLetterSpacing: TextUnit = 0.04.sp
    val GoProShadow = ShadowTokens.ShadowGoPro

    val TitleFontStyle = TypeScale.TitleLarge
    val SubtitleFontStyle = TypeScale.BodySmallRegular
}

object BottomNavTokens {
    val BarHeight: Dp = 66.dp
    val HorizontalPadding: Dp = Spacing.Spacing8
    val VerticalPadding: Dp = Spacing.Spacing8

    val IndicatorWidth: Dp = 44.dp
    val IndicatorHeight: Dp = 28.dp
    val IndicatorShape = Radius.ShapeLarge

    val IconSizeActive: Dp = 19.dp
    val IconSizeInactive: Dp = 19.dp
    val IconStrokeWidthActive: Float = 2.5f
    val IconStrokeWidthInactive: Float = 1.8f

    val LabelFontStyle = TypeScale.LabelSmall
    val LabelFontStyleActive = TypeScale.LabelSmall.copy(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    )
    val IconLabelGap: Dp = Spacing.Spacing4
}

object PackCardTokens {
    val Shape = Radius.ShapeXL
    val InternalShape = Radius.ShapeLarge
    val Padding: Dp = Spacing.Spacing14

    val IconContainerSize: Dp = 46.dp
    val IconContainerShape = Radius.ShapeMedium
    val IconContainerBorderWidth: Dp = 1.dp

    val AccentStripeWidth: Dp = 3.dp
    val AccentStripeShape = Radius.ShapeAccentStripe

    val TitleFontStyle = TypeScale.BodyMedium
    val SubFontStyle = TypeScale.LabelMedium
    val StreakFontStyle = TypeScale.LabelMedium

    val CircularProgressSize: Dp = 46.dp
    val CircularProgressStrokeWidth: Dp = 5.dp

    val SwipeRevealWidth: Dp = 180.dp
    val SwipeThreshold: Dp = 55.dp
    val ActionButtonWidth: Dp = 56.dp
    val ActionIconSize: Dp = 16.dp
    val ProgressBarHeight: Dp = 3.dp
    val EmojiSize: TextUnit = 20.sp
    val MetaFontStyle = TypeScale.LabelMedium
    val MasteredFontStyle = TypeScale.LabelXSmall
    val CircularProgressFontSize: TextUnit = 12.sp

    val ActionButtonEditBg   = BrandColors.BrandPrimaryAction
    val ActionButtonShareBg  = BrandColors.BrandSecondaryDeep
    val ActionButtonDeleteBg = BrandColors.BrandErrorDestructiveBg
    val ActionButtonMoreBg   = BrandColors.BrandSuccessDark

    val ActionLabelFontStyle = TypeScale.LabelXSmall
    val Shadow = ShadowTokens.ShadowPack
}

object StatsCardTokens {
    val Shape = Radius.ShapeLarge
    val Padding: Dp = Spacing.Spacing14

    val LabelFontStyle = TypeScale.LabelXSmall
    val ValueFontStyle = TypeScale.HeadlineMedium
    val SubFontStyle = TypeScale.LabelSmall

    val IconSize: Dp = 13.dp
    val IconLabelGap: Dp = Spacing.Spacing4
    val BorderWidth: Dp = 1.dp
    val Shadow = ShadowTokens.ShadowStats
}

object HeroCardTokens {
    val Shape = Radius.ShapeXXL
    val Padding: Dp = Spacing.Spacing20

    val MetricFontStyle = TypeScale.DisplayHero
    val MetricDenomFontStyle = TypeScale.TitleMedium
    val MetricUnitFontStyle = TypeScale.BodySmallRegular
    val SupLabelFontStyle = TypeScale.LabelLarge

    val ProgressBarHeight: Dp = 6.dp
    val ProgressBarShape = Radius.ShapePill

    val InnerButtonShape = Radius.ShapeLarge
    val InnerButtonVerticalPadding: Dp = 12.dp
    /** Semi-transparent white overlay — pure tint, not a brand colour decision. */
    val InnerButtonBg = White.copy(alpha = 0.18f)
    val InnerButtonFontStyle = TypeScale.BodyMedium

    val CircularProgressSize: Dp = 72.dp
    val CircularProgressStrokeWidth: Dp = 6.dp
    val Shadow = ShadowTokens.ShadowHero
}

object FabTokens {
    val Shape = Radius.ShapeLarge
    val HorizontalPadding: Dp = Spacing.Spacing18
    val VerticalPadding: Dp = 13.dp

    val IconSize: Dp = 20.dp
    val IconStrokeWidth: Float = 2.5f
    val LabelFontStyle = TypeScale.BodySmall

    val BottomOffset: Dp = Spacing.FabBottomOffset
    val RightOffset: Dp = Spacing.FabRightOffset

    val Shadow = ShadowTokens.ShadowFab
}

object PrimaryButtonTokens {
    val Shape = Radius.ShapeLarge
    val Height: Dp = 56.dp
    val HorizontalPadding: Dp = Spacing.Spacing20

    val FontStyle = TypeScale.BodyXLarge
    val IconSize: Dp = 18.dp
    val IconStrokeWidth: Float = 2.5f
    val IconTextGap: Dp = Spacing.Spacing8 + 2.dp

    val ShadowDark = ShadowTokens.ShadowCtaDark
    val ShadowLight = ShadowTokens.ShadowCtaLight
}

object FeatureRowTokens {
    val ContainerShape = Radius.ShapeXL
    val ContainerBorderWidth: Dp = 1.dp

    val HorizontalPadding: Dp = Spacing.Spacing16
    val VerticalPadding: Dp = Spacing.Spacing12
    val InternalGap: Dp = Spacing.Spacing12

    val IconContainerSize: Dp = 38.dp
    val IconContainerShape = Radius.ShapeMedium
    val IconSize: Dp = 17.dp
    val IconStrokeWidth: Float = 1.8f

    val PrimaryFontStyle = TypeScale.LabelXLarge
    val SecondaryFontStyle = TypeScale.LabelMedium

    val CheckBadgeSize: Dp = 22.dp
    val CheckBadgeShape = Radius.ShapeCircle
    val CheckBadgeBorderWidth: Dp = 1.dp
    val CheckIconSize: Dp = 11.dp
    val CheckIconStrokeWidth: Float = 3f
}

object PricingCardTokens {
    val Shape = Radius.ShapeXXL
    val Padding: Dp = Spacing.Spacing20
    val BorderWidth: Dp = 1.dp
    val BorderWidthSelected: Dp = 1.5.dp

    val LabelFontStyle = TypeScale.LabelXSmall
    val PriceFontStyle = TypeScale.HeadlineLarge
    val PeriodFontStyle = TypeScale.LabelLarge
    val NoteFontStyle = TypeScale.LabelMedium

    val BadgeShape = Radius.ShapePill
    val BadgeHorizontalPadding: Dp = Spacing.Spacing8
    val BadgeVerticalPadding: Dp = Spacing.Spacing2
    val BadgeFontStyle = TypeScale.LabelMicro

    val CheckBadgeSize: Dp = 18.dp
    val CheckBadgeShape = Radius.ShapeCircle
    val CheckBadgeInset: Dp = Spacing.Spacing16
}

object ChipTokens {
    val Shape = Radius.ShapePill
    val HorizontalPadding: Dp = Spacing.Spacing16
    val VerticalPadding: Dp = Spacing.Spacing8

    val SmallHorizontalPadding: Dp = Spacing.Spacing12
    val SmallVerticalPadding: Dp = Spacing.Spacing6

    val FontStyle = TypeScale.LabelLarge
    val SmallFontStyle = TypeScale.LabelSmall

    val IconSize: Dp = 13.dp
    val IconStrokeWidth: Float = 2f
    val IconTextGap: Dp = Spacing.Spacing6
    val BorderWidth: Dp = 1.dp
}

object ToggleSwitchTokens {
    val Width: Dp = 44.dp
    val Height: Dp = 24.dp
    val Shape = Radius.ShapePill

    val ThumbSize: Dp = 18.dp
    val ThumbShape = Radius.ShapeCircle
    val ThumbInset: Dp = 2.dp
    val ThumbTravelOn: Dp = 20.dp
    val ThumbTravelOff: Dp = 2.dp

    val BorderWidth: Dp = 1.5.dp
    val ThumbShadowOn = ShadowTokens.ShadowAvatar
}

object InputTokens {
    val Shape = Radius.ShapeLarge
    val HorizontalPadding: Dp = Spacing.Spacing16
    val VerticalPadding: Dp = Spacing.Spacing12
    val BorderWidth: Dp = 1.dp
    val FocusBorderWidth: Dp = 1.5.dp

    val FontStyle = TypeScale.BodySmallRegular
    val PlaceholderFontStyle = TypeScale.BodySmallRegular
    val LabelFontStyle = TypeScale.LabelSmall
}

object DropZoneTokens {
    val Shape = Radius.ShapeXXL
    val Padding: Dp = Spacing.Spacing24
    val BorderWidth: Dp = 1.5.dp
    val BorderStyle = "dashed"

    val IconSize: Dp = 32.dp
    val IconContainerSize: Dp = 64.dp
    val IconContainerShape = Radius.ShapeXL

    val TitleFontStyle = TypeScale.BodyMedium
    val SubFontStyle = TypeScale.BodySmallRegular
    val HintFontStyle = TypeScale.LabelSmall
}

object SrsButtonTokens {
    val Shape = Radius.ShapeLarge
    val VerticalPadding: Dp = 12.dp
    val HorizontalPadding: Dp = Spacing.Spacing8
    val BorderWidth: Dp = 1.dp

    val LabelFontStyle = TypeScale.BodySmall
    val IntervalFontStyle = TypeScale.LabelSmall

    val IconSize: Dp = 16.dp
    val IconStrokeWidth: Float = 2.2f
    val Shadow = ShadowTokens.ShadowQuizAction

    val HardColor  = BrandColors.BrandErrorDark
    val HardBg     = BrandColors.BrandErrorDark.copy(alpha = 0.10f)
    val HardBorder = BrandColors.BrandErrorDark.copy(alpha = 0.35f)

    val GoodColor  = BrandColors.BrandSecondaryDark
    val GoodBg     = BrandColors.BrandSecondaryDark.copy(alpha = 0.10f)
    val GoodBorder = BrandColors.BrandSecondaryDark.copy(alpha = 0.35f)

    val EasyColor  = BrandColors.BrandSuccessDark
    val EasyBg     = BrandColors.BrandSuccessDark.copy(alpha = 0.10f)
    val EasyBorder = BrandColors.BrandSuccessDark.copy(alpha = 0.35f)
}

object FlipCardTokens {
    val Shape = Radius.ShapeXXL
    val Padding: Dp = Spacing.Spacing24
    val MinHeight: Dp = 224.dp
    val FlipDurationMs: Int = 550

    val HintFontStyle = TypeScale.LabelSmall
    val FrontFontStyle = TypeScale.BodyMedium
    val BackFontStyle = TypeScale.BodySmall
    val TapHintFontStyle = TypeScale.LabelSmall
    val Shadow = ShadowTokens.ShadowFlashcard
}

object QuizCardTokens {
    val Shape = Radius.ShapeXXL
    val Shadow = ShadowTokens.ShadowQuizAction
}

object QuizSheetTokens {
    val Shape = Radius.ShapeXXXL
    val Shadow = ShadowTokens.ShadowQuizSheet
}

object ProBadgeTokens {
    val Shape = Radius.ShapePill
    val HorizontalPadding: Dp = Spacing.Spacing8
    val VerticalPadding: Dp = Spacing.Spacing4
    val BorderWidth: Dp = 1.dp

    val IconSize: Dp = 10.dp
    val IconTextGap: Dp = Spacing.Spacing4
    val FontStyle = TypeScale.LabelMicro
}

object AvatarStackTokens {
    val AvatarSize: Dp = 26.dp
    val AvatarShape = Radius.ShapeCircle
    val AvatarBorderWidth: Dp = 2.dp
    val Overlap: Dp = 8.dp

    val InitialFontSize: TextUnit = 9.sp
    val InitialFontWeight = androidx.compose.ui.text.font.FontWeight.Bold

    val StarSize: Dp = 10.dp
    val StarGap: Dp = Spacing.Spacing2
    val SubFontStyle = TypeScale.LabelSmall
}

object CircularProgressTokens {
    val DefaultSize: Dp = 46.dp
    val DefaultStrokeWidth: Dp = 3.5.dp

    val LargeSize: Dp = 72.dp
    val LargeStrokeWidth: Dp = 5.dp

    val TrackAlpha: Float = 0.06f
}

data class ComponentTokens(
    val topAppBar:        TopAppBarTokens       = TopAppBarTokens,
    val bottomNav:        BottomNavTokens        = BottomNavTokens,
    val packCard:         PackCardTokens         = PackCardTokens,
    val statsCard:        StatsCardTokens        = StatsCardTokens,
    val heroCard:         HeroCardTokens         = HeroCardTokens,
    val fab:              FabTokens              = FabTokens,
    val primaryButton:    PrimaryButtonTokens    = PrimaryButtonTokens,
    val featureRow:       FeatureRowTokens       = FeatureRowTokens,
    val pricingCard:      PricingCardTokens      = PricingCardTokens,
    val chip:             ChipTokens             = ChipTokens,
    val toggleSwitch:     ToggleSwitchTokens     = ToggleSwitchTokens,
    val input:            InputTokens            = InputTokens,
    val dropZone:         DropZoneTokens         = DropZoneTokens,
    val srsButton:        SrsButtonTokens        = SrsButtonTokens,
    val flipCard:         FlipCardTokens         = FlipCardTokens,
    val quizCard:         QuizCardTokens         = QuizCardTokens,
    val quizSheet:        QuizSheetTokens        = QuizSheetTokens,
    val proBadge:         ProBadgeTokens         = ProBadgeTokens,
    val avatarStack:      AvatarStackTokens      = AvatarStackTokens,
    val circularProgress: CircularProgressTokens = CircularProgressTokens,
)

val defaultComponentTokens = ComponentTokens()
