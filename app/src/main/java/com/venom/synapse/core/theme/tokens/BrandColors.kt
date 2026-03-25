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

/** Brand identity color aliases — do not use Colors.kt constants directly in UI. */
object BrandColors {

    // Primary
    val BrandPrimaryLight  = SynapseViolet600   // #5B4EE8 — buttons, active tabs, FAB
    val BrandPrimaryDark   = SynapseViolet300   // #9D93FF — dark mode primary
    val BrandPrimaryDeep   = SynapseVioletDeep  // #4A3DD6 — gradient start
    val BrandPrimaryBright = SynapseVioletBright // #7B6FFF — gradient end, hover
    val BrandPrimaryPale   = SynapseVioletPale   // #BDB6FF — dark-mode end, onPrimaryContainer
    val BrandPrimaryAction = SynapseVioletMid    // #4C3EC7 — swipe actions, focus rings

    // Secondary
    val BrandSecondaryLight = Indigo700   // #4338CA
    val BrandSecondaryDark  = Indigo400   // #818CF8
    val BrandSecondaryDeep  = Indigo800   // #3730A3 — gradient start, share button
    val BrandSecondaryPale  = Indigo300   // #A5B4FC — dark onSecondaryContainer

    // Gold / Tertiary
    val BrandGoldLight = Amber600        // #D97706 — light mode gold
    val BrandGoldDark  = SynapseGold    // #FBB830 — dark mode gold
    val BrandGoldDeep  = Amber700       // #B45309 — gradient start, pressed
    val BrandGoldPale  = SynapseGoldPale // #FDCA6E — streak value, shimmer

    // Success
    val BrandSuccessLight = Emerald600  // #059669
    val BrandSuccessDark  = Emerald400  // #34D399

    // Error
    val BrandErrorLight         = Red600  // #DC2626
    val BrandErrorDark          = Red400  // #F87171
    val BrandErrorDestructiveBg = Red800  // #991B1B — destructive action bg (white icon safe)

    // Neutral surfaces
    val BrandNeutralDeepDark     = SynapseDeepDark    // #06051A — app bg dark
    val BrandNeutralSurfaceDark  = SynapseSurfaceDark // #0D0B25 — nav, sheets, card base
    val BrandNeutralCardDark     = SynapseCardDark    // #13112E — card elevation dark
    val BrandNeutralElevatedDark = SynapseElevatedDark // #1B183E — selected card, dialogs
    val BrandNeutralBgLight      = SynapseLightBg     // #F4F2FF — app bg light
    val BrandNeutralCardLight    = SynapseLavender    // #EDE8FF — elevated card light

    // Pack accent palette
    val PackViolet  = Indigo600   // ML / AI packs
    val PackEmerald = Emerald500  // Biology / Science packs
    val PackAmber   = Amber500    // History / Languages packs
    val PackPurple  = Violet500   // Philosophy / Arts packs
}