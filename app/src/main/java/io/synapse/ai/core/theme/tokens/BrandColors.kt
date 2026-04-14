package io.synapse.ai.core.theme.tokens

import io.synapse.ai.core.theme.Amber600
import io.synapse.ai.core.theme.Amber700
import io.synapse.ai.core.theme.Emerald400
import io.synapse.ai.core.theme.Emerald600
import io.synapse.ai.core.theme.Indigo300
import io.synapse.ai.core.theme.Indigo400
import io.synapse.ai.core.theme.Indigo600
import io.synapse.ai.core.theme.Indigo700
import io.synapse.ai.core.theme.Indigo800
import io.synapse.ai.core.theme.Red400
import io.synapse.ai.core.theme.Red600
import io.synapse.ai.core.theme.Red800
import io.synapse.ai.core.theme.SynapseCardDark
import io.synapse.ai.core.theme.SynapseDeepDark
import io.synapse.ai.core.theme.SynapseElevatedDark
import io.synapse.ai.core.theme.SynapseGold
import io.synapse.ai.core.theme.SynapseGoldPale
import io.synapse.ai.core.theme.SynapseSurfaceDark
import io.synapse.ai.core.theme.SynapseVioletBright
import io.synapse.ai.core.theme.SynapseVioletDeep
import io.synapse.ai.core.theme.SynapseVioletMid
import io.synapse.ai.core.theme.Violet100
import io.synapse.ai.core.theme.Violet300
import io.synapse.ai.core.theme.Violet50

object BrandColors {

    // ── Primary — Synapse Violet ──────────────────────────────────────────────


    /** #4F46E5 — Indigo600. Used as primary on white (3.85:1 — passes AA at bold/large) */
    val BrandPrimaryLight  = Indigo600

    /**
     * #4C3EC7 — Dark-mode primary: 5.4:1 on #09090F.
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