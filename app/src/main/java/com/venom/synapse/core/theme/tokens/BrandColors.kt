package com.venom.synapse.core.theme.tokens

import com.venom.synapse.core.theme.Amber600
import com.venom.synapse.core.theme.Amber700
import com.venom.synapse.core.theme.Emerald400
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
import com.venom.synapse.core.theme.SynapseSurfaceDark
import com.venom.synapse.core.theme.SynapseVioletBright
import com.venom.synapse.core.theme.SynapseVioletDeep
import com.venom.synapse.core.theme.SynapseVioletMid
import com.venom.synapse.core.theme.Violet100
import com.venom.synapse.core.theme.Violet300
import com.venom.synapse.core.theme.Violet50

object BrandColors {

    // ── Primary — Synapse Violet ──────────────────────────────────────────────

    /** #4F46E5 — Indigo600. Used as primary on white (3.85:1 — passes AA at bold/large) */
    val BrandPrimaryLight  = Indigo600

    /**
     * #7B6FFF — SynapseVioletBright. Dark-mode primary: 5.4:1 on #09090F.
     * [FIX] Previously Indigo500 — identical to BrandPrimaryLight (collision).
     */
    val BrandPrimaryDark   = SynapseVioletBright

    /** #4A3DD6 — Deep violet: gradient start anchor */
    val BrandPrimaryDeep   = SynapseVioletDeep

    /** #7B6FFF — Hero accent and premium highlight (same as BrandPrimaryDark) */
    val BrandPrimaryBright = SynapseVioletBright

    /** #C4B5FD — Violet300: soft dark-mode accent for onPrimaryContainer text */
    val BrandPrimaryPale   = Violet300

    /** #4C3EC7 — Swipe action surfaces, focus rings */
    val BrandPrimaryAction = SynapseVioletMid

    // ── Secondary — Indigo ────────────────────────────────────────────────────

    val BrandSecondaryLight = Indigo700    // #4338CA
    val BrandSecondaryDark  = Indigo400    // #818CF8
    val BrandSecondaryDeep  = Indigo800    // #3730A3 — gradient anchor
    val BrandSecondaryPale  = Indigo300    // #A5B4FC — onSecondaryContainer (dark)

    // ── Tertiary — Premium Gold ───────────────────────────────────────────────

    val BrandGoldLight = Amber600          // #D97706 — light-mode gold
    val BrandGoldDark  = SynapseGold       // #FBB830 — dark-mode gold
    val BrandGoldDeep  = Amber700          // #B45309 — gradient start, pressed gold
    val BrandGoldPale  = SynapseGoldPale   // #FDCA6E — shimmer, streak chip highlight

    // ── Status — Success ──────────────────────────────────────────────────────

    val BrandSuccessLight = Emerald600     // #059669
    val BrandSuccessDark  = Emerald400     // #34D399

    // ── Status — Error ────────────────────────────────────────────────────────

    val BrandErrorLight         = Red600   // #DC2626
    val BrandErrorDark          = Red400   // #F87171
    val BrandErrorDestructiveBg = Red800   // #991B1B — for white icon safe bg

    // ── Neutral Surfaces — Dark Stack ─────────────────────────────────────────

    /** #09090F — App background: level 0 */
    val BrandNeutralDeepDark     = SynapseDeepDark

    /** #0D0B25 — Nav, sheets, card base: level 1 */
    val BrandNeutralSurfaceDark  = SynapseSurfaceDark

    /** #13112E — Card elevation: level 2 */
    val BrandNeutralCardDark     = SynapseCardDark

    /** #1B183E — Selected card, dialogs: level 3 */
    val BrandNeutralElevatedDark = SynapseElevatedDark

    // ── Neutral Surfaces — Light Stack ────────────────────────────────────────

    /** #F5F3FF — App background (light) — Violet50 */
    val BrandNeutralBgLight   = Violet50

    /** #EDE9FE — Elevated card (light) — Violet100 */
    val BrandNeutralCardLight = Violet100
}