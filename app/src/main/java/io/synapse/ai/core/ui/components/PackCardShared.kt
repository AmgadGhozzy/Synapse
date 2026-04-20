package io.synapse.ai.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import io.synapse.ai.R

data class SwipeAction(
    val labelRes: Int,
    val iconRes: Int,
    val color: Color,
    val onClick: () -> Unit,
)

@Composable
fun buildPackCardActions(
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
): List<SwipeAction> {

    val editColor = MaterialTheme.colorScheme.secondary
    val exportColor = MaterialTheme.colorScheme.primary
    val deleteColor = MaterialTheme.colorScheme.error

    return listOf(
        SwipeAction(
            labelRes = R.string.action_edit,
            iconRes = R.drawable.ic_edit,
            color = editColor,
            onClick = onEdit,
        ),
        SwipeAction(
            labelRes = R.string.action_export,
            iconRes = R.drawable.ic_share_2,
            color = exportColor,
            onClick = onExport,
        ),
        SwipeAction(
            labelRes = R.string.action_delete,
            iconRes = R.drawable.ic_trash_2,
            color = deleteColor,
            onClick = onDelete,
        ),
    )
}

enum class DragValue { Closed, Open }

@Immutable
data class CardColorSet(
    val accent: Color,
    val bg: Color,
    val border: Color,
)

fun parseColorSafe(hex: String, fallback: Color): Color =
    if (hex.isBlank()) fallback
    else try {
        Color(hex.toColorInt())
    } catch (_: IllegalArgumentException) {
        fallback
    }

@Composable
fun buildCardColorSet(hex: String): CardColorSet {
    val fallbackColor = MaterialTheme.colorScheme.primary
    val accent = parseColorSafe(hex, fallbackColor)

    return CardColorSet(
        accent = accent.copy(alpha = 0.85f),
        bg = accent.copy(alpha = 0.10f),
        border = accent.copy(alpha = 0.15f),
    )
}