package com.venom.synapse.core.theme.tokens

import com.venom.synapse.core.theme.Amber500
import com.venom.synapse.core.theme.Amber600
import com.venom.synapse.core.theme.Amber700
import com.venom.synapse.core.theme.Emerald400
import com.venom.synapse.core.theme.Emerald500
import com.venom.synapse.core.theme.Emerald600
import com.venom.synapse.core.theme.Indigo300
import com.venom.synapse.core.theme.Indigo400
import com.venom.synapse.core.theme.Indigo600
import com.venom.synapse.core.theme.Indigo700
import com.venom.synapse.core.theme.Indigo800
import com.venom.synapse.core.theme.Red400
import com.venom.synapse.core.theme.Red600
import com.venom.synapse.core.theme.Red800
import com.venom.synapse.core.theme.SynapseCardDark
import com.venom.synapse.core.theme.SynapseDeepDark
import com.venom.synapse.core.theme.SynapseElevatedDark
import com.venom.synapse.core.theme.SynapseGold
import com.venom.synapse.core.theme.SynapseGoldPale
import com.venom.synapse.core.theme.SynapseLavender
import com.venom.synapse.core.theme.SynapseLightBg
import com.venom.synapse.core.theme.SynapseSurfaceDark
import com.venom.synapse.core.theme.SynapseViolet300
import com.venom.synapse.core.theme.SynapseViolet600
import com.venom.synapse.core.theme.SynapseVioletBright
import com.venom.synapse.core.theme.SynapseVioletDeep
import com.venom.synapse.core.theme.SynapseVioletMid
import com.venom.synapse.core.theme.SynapseVioletPale
import com.venom.synapse.core.theme.Violet500

/**
 * Core identity colors extracted from the brand identity.
 */
object BrandColors {

    /**
     * BrandPrimaryLight  |  #5B4EE8
     * The core brand colour — deep violet for intelligence, focus, premium.
     * Usage: Primary buttons, active tabs, FAB, progress fills, avatar rings.
     */
    val BrandPrimaryLight = SynapseViolet600

    /**
     * BrandPrimaryDark  |  #9D93FF
     * Dark-mode elevation of BrandPrimaryLight — lighter for WCAG AA on #0D0B25.
     */
    val BrandPrimaryDark = SynapseViolet300

    /**
     * BrandPrimaryDeep  |  #4A3DD6
     * Gradient start stop — adds depth to buttons and hero cards.
     */
    val BrandPrimaryDeep = SynapseVioletDeep

    /**
     * BrandPrimaryBright  |  #7B6FFF
     * Gradient end stop — lighter mid-violet for shimmer and hover states.
     */
    val BrandPrimaryBright = SynapseVioletBright

    /**
     * BrandPrimaryPale  |  #BDB6FF
     * Pale tint for dark-mode gradient ends and onPrimaryContainer in dark.
     */
    val BrandPrimaryPale = SynapseVioletPale

    /**
     * BrandPrimaryAction  |  #4C3EC7
     * Mid-deep violet for interactive action button backgrounds:
     * swipe-reveal Edit button, primary input focus rings, active chip borders.
     * Distinct from BrandPrimaryDeep (#4A3DD6) — intentionally deeper for
     * filled action surfaces that need higher contrast than a gradient stop.
     */
    val BrandPrimaryAction = SynapseVioletMid

    /**
     * BrandSecondaryLight  |  #4338CA
     * Indigo accent — visual hierarchy below primary actions.
     * Usage: Secondary buttons, toggle active states, share/edit chips.
     */
    val BrandSecondaryLight = Indigo700

    /**
     * BrandSecondaryDark  |  #818CF8
     * Dark-mode elevation of BrandSecondaryLight.
     */
    val BrandSecondaryDark = Indigo400

    /**
     * BrandSecondaryDeep  |  #3730A3
     * Darkest indigo — accent gradient starts, Share action button BG.
     */
    val BrandSecondaryDeep = Indigo800

    /**
     * BrandSecondaryPale  |  #A5B4FC
     * Pale indigo for dark-mode container content (onSecondaryContainer dark).
     */
    val BrandSecondaryPale = Indigo300

    /**
     * BrandGoldLight  |  #D97706
     * Warm amber — premium status, streak rewards, "Go Pro" pill.
     * Avoid for normal-size text (3.12:1 on white — large text / icons only).
     */
    val BrandGoldLight = Amber600

    /**
     * BrandGoldDark  |  #FBB830
     * Bright amber for dark surfaces — stays vivid on deep backgrounds.
     */
    val BrandGoldDark = SynapseGold

    /**
     * BrandGoldDeep  |  #B45309
     * Darkest amber — light-mode gradient starts, pressed amber states.
     */
    val BrandGoldDeep = Amber700

    /**
     * BrandGoldPale  |  #FDCA6E
     * Pale gold — streak value text on dark, badge shimmer highlights.
     */
    val BrandGoldPale = SynapseGoldPale

    /**
     * BrandSuccessLight  |  #059669
     * Accuracy stats, "Easy" SRS rating, online presence dot.
     */
    val BrandSuccessLight = Emerald600

    /**
     * BrandSuccessDark  |  #34D399
     * Brighter emerald for dark mode — maintains 7.6:1 contrast on #06051A.
     */
    val BrandSuccessDark = Emerald400

    // Semantic — Error (Red)

    /**
     * BrandErrorLight  |  #DC2626
     * M3 `error` role — incorrect answers, form validation, danger states.
     */
    val BrandErrorLight = Red600

    /**
     * BrandErrorDark  |  #F87171
     * Accessible error on dark surfaces — "Hard" SRS button, error text.
     */
    val BrandErrorDark = Red400

    /**
     * BrandErrorDestructiveBg  |  #991B1B
     * Intentionally darker than BrandErrorLight — used as a filled background
     * for irreversible destructive actions (e.g., swipe-reveal Delete button).
     * The extra darkness ensures white icon labels pass WCAG AA on this surface.
     */
    val BrandErrorDestructiveBg = Red800

    // Neutral — Surface Foundations

    /**
     * BrandNeutralDeepDark  |  #06051A
     * App background in dark mode — near-black indigo with subtle brand warmth.
     */
    val BrandNeutralDeepDark = SynapseDeepDark

    /**
     * BrandNeutralSurfaceDark  |  #0D0B25
     * Primary surface layer in dark — BottomNav, sheets, card base.
     */
    val BrandNeutralSurfaceDark = SynapseSurfaceDark

    /**
     * BrandNeutralCardDark  |  #13112E
     * Card-level surface elevation in dark — pack cards, stats cards, feature rows.
     */
    val BrandNeutralCardDark = SynapseCardDark

    /**
     * BrandNeutralElevatedDark  |  #1B183E
     * Highest card elevation in dark — selected pricing cards, elevated dialogs.
     */
    val BrandNeutralElevatedDark = SynapseElevatedDark

    /**
     * BrandNeutralBgLight  |  #F4F2FF
     * App background in light — barely-there violet tint for brand warmth.
     */
    val BrandNeutralBgLight = SynapseLightBg

    /**
     * BrandNeutralCardLight  |  #EDE8FF
     * Light-mode elevated card — secondaryContainer, chips, selected pricing.
     */
    val BrandNeutralCardLight = SynapseLavender

    // Pack Accent Palette

    /** PackViolet  — ML / AI packs */
    val PackViolet  = Indigo600

    /** PackEmerald — Biology / Science packs */
    val PackEmerald = Emerald500

    /** PackAmber   — History / Languages packs */
    val PackAmber   = Amber500

    /** PackPurple  — Philosophy / Arts packs */
    val PackPurple  = Violet500
}