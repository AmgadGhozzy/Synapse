package com.venom.synapse.core.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.venom.synapse.R
import com.venom.synapse.core.theme.tokens.PackCardTokens


// ── SwipeAction model ─────────────────────────────────────────────────────────

/**
 * Describes a single action revealed when the card is swiped.
 *
 * @param labelRes  String resource for the icon label (also used as contentDescription).
 * @param iconRes   Drawable resource for the action icon.
 * @param color     Background colour of the action button.
 * @param onClick   Callback invoked when the action is tapped.
 */
data class SwipeAction(
    val labelRes: Int,
    val iconRes:  Int,
    val color:    Color,
    val onClick:  () -> Unit,
)

fun buildPackCardActions(
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
): List<SwipeAction> = listOf(
    SwipeAction(
        labelRes = R.string.action_edit,
        iconRes  = R.drawable.ic_edit,
        color    = PackCardTokens.ActionButtonEditBg,
        onClick  = onEdit,
    ),
    SwipeAction(
        labelRes = R.string.action_export,
        iconRes  = R.drawable.ic_share_2,
        color    = PackCardTokens.ActionButtonShareBg,
        onClick  = onExport,
    ),
    SwipeAction(
        labelRes = R.string.action_delete,
        iconRes  = R.drawable.ic_trash_2,
        color    = PackCardTokens.ActionButtonDeleteBg,
        onClick  = onDelete,
    ),
)

enum class DragValue { Closed, Open }

/**
 * Shared color logic for pack cards.
 */
@Immutable
data class CardColorSet(
    /** Primary accent — progress bars, badge bg, icon color. */
    val accent: Color,
    /** Translucent tinted background — icon container, category pill bg. */
    val bg: Color,
    /** Translucent tinted border — icon container outline. */
    val border: Color,
)

/**
 * Safely parse a CSS hex string (e.g. `"#4F46E5"`) into a Compose [Color].
 * Returns [fallback] on blank or malformed input instead of throwing.
 */
fun parseColorSafe(hex: String, fallback: Color = Color(0xFF4F46E5)): Color =
    if (hex.isBlank()) fallback
    else try {
        Color(hex.toColorInt())
    } catch (_: IllegalArgumentException) {
        fallback
    }

/**
 * Build a [CardColorSet] from a hex string.
 */
fun buildCardColorSet(hex: String): CardColorSet {
    val accent = parseColorSafe(hex)
    return CardColorSet(
        accent = accent,
        bg     = accent.copy(alpha = 0.10f),
        border = accent.copy(alpha = 0.15f),
    )
}
