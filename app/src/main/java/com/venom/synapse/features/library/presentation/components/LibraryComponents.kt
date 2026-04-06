package com.venom.synapse.features.library.presentation.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.ui.utils.animatedDashedBorder
import com.venom.synapse.features.library.presentation.state.LibrarySortOption
import com.venom.ui.components.common.adp

// ── Filter tab definition (UI-layer only) ─────────────────────────────────────
enum class LibraryFilter(
    val labelRes: Int,
    val sort: LibrarySortOption,
    val onlyDue: Boolean = false,
) {
    ALL(R.string.library_filter_all, LibrarySortOption.RECENT),
    RECENT(R.string.library_filter_recent, LibrarySortOption.RECENT),
    DUE(R.string.library_filter_due, LibrarySortOption.MOST_DUE, onlyDue = true),
}

// ── Add-Pack Cell ─────────────────────────────────────────────────────────────
@Composable
fun AddPackCell(
    isLocked: Boolean,
    packCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (isLocked) MaterialTheme.colorScheme.tertiary
    else MaterialTheme.colorScheme.primary

    val bgColor = accentColor.copy(alpha = 0.06f)
    val borderColor = accentColor.copy(alpha = 0.35f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.synapse.radius.xl)
            .background(bgColor)
            .animatedDashedBorder(color = borderColor, shape = MaterialTheme.synapse.radius.xl)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        val isWide = maxWidth > 280.adp

        val titleRes = when {
            isLocked -> R.string.add_pack_cell_locked_title
            isWide   -> R.string.library_add_pack_title
            else     -> R.string.library_add_pack_short_title
        }
        val subtitleRes = if (isLocked) R.string.add_pack_cell_locked_subtitle
        else R.string.library_add_pack_subtitle

        val cellHeight = if (isWide) 92.adp else 148.adp
        val contentPadding = if (isWide)
            PaddingValues(horizontal = MaterialTheme.synapse.spacing.s16, vertical = MaterialTheme.synapse.spacing.s12)
        else
            PaddingValues(MaterialTheme.synapse.spacing.s16)

        // ── Icon ──────────────────────────────────────────────────────────────
        @Composable
        fun PackIcon() {
            Box(
                modifier = Modifier
                    .size(36.adp)
                    .clip(MaterialTheme.synapse.radius.md)
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        if (isLocked) R.drawable.ic_lock else R.drawable.ic_plus
                    ),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.adp),
                )
            }
        }

        // ── Label ─────────────────────────────────────────────────────────────
        @Composable
        fun PackLabel() {
            Column(
                horizontalAlignment = if (isWide) Alignment.Start else Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                )
                Spacer(Modifier.height(2.adp))
                Text(
                    text = if (isLocked) stringResource(subtitleRes, packCount)
                    else stringResource(subtitleRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Layout ────────────────────────────────────────────────────────────
        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight)
                    .padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            ) {
                PackIcon()
                PackLabel()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight)
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PackIcon()
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s8))
                PackLabel()
            }
        }
    }
}

// ── Search Bar ────────────────────────────────────────────────────────────────
@Composable
fun LibrarySearchBar(
    query: String,
    onChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(R.string.library_search_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.icon_search),
                contentDescription = stringResource(R.string.library_search_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.adp),
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = stringResource(R.string.library_search_clear),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.adp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onChanged("") },
                )
            }
        } else null,
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}

// ── Filter Tab Row ────────────────────────────────────────────────────────────
@Composable
fun FilterTabRow(
    activeFilter: LibraryFilter,
    onSelect: (LibraryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
        contentPadding = PaddingValues(end = MaterialTheme.synapse.spacing.s4),
    ) {
        items(LibraryFilter.entries.size) { i ->
            val tab = LibraryFilter.entries[i]
            FilterTab(
                labelRes = tab.labelRes,
                active = tab == activeFilter,
                onClick = { onSelect(tab) },
            )
        }
    }
}

@Composable
fun FilterTab(
    @StringRes labelRes: Int,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200)
    )
    val labelColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "tab_label",
    )

    Row(
        modifier = modifier
            .clip(MaterialTheme.synapse.radius.pill)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s14,
                vertical = MaterialTheme.synapse.spacing.s6
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
        )
    }
}

// ── Pack Count Row ────────────────────────────────────────────────────────────
@Composable
fun PackCountRow(
    packCount: Int,
    totalDue: Int,
    showDueSum: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.library_pack_count, packCount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showDueSum) {
            Text(
                text = stringResource(R.string.library_cards_due, totalDue),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────
@Composable
fun PackEmptyState(
    filter: LibraryFilter,
    modifier: Modifier = Modifier,
) {
    data class EmptyMsg(val iconRes: Int, val titleRes: Int, val subRes: Int)

    val msg = when (filter) {
        LibraryFilter.ALL,
        LibraryFilter.RECENT -> EmptyMsg(
            R.drawable.ic_book_open,
            R.string.library_empty_title_all,
            R.string.library_empty_sub_all
        )

        LibraryFilter.DUE -> EmptyMsg(
            R.drawable.ic_check_circle_2,
            R.string.library_empty_title_due,
            R.string.library_empty_sub_due
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = MaterialTheme.synapse.spacing.s32,
                horizontal = MaterialTheme.synapse.spacing.s24
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(msg.iconRes),
            contentDescription = null,
            modifier = Modifier.size(64.adp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))
        Text(
            text = stringResource(msg.titleRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s6))
        Text(
            text = stringResource(msg.subRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Light — Normal", showBackground = true)
@Preview(name = "Dark — Normal", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AddPackCellNormalPreview() {
    SynapseTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.screen)
        ) {
            AddPackCell(isLocked = false, packCount = 3, onClick = {})
            AddPackCell(
                isLocked = false, packCount = 3, onClick = {},
                modifier = Modifier.fillMaxWidth(0.5f)
            )
        }
    }
}

@Preview(name = "Light — Locked", showBackground = true)
@Preview(name = "Dark — Locked", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AddPackCellLockedPreview() {
    SynapseTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.screen)
        ) {
            AddPackCell(isLocked = true, packCount = 5, onClick = {})
            AddPackCell(
                isLocked = true, packCount = 5, onClick = {},
                modifier = Modifier.fillMaxWidth(0.5f)
            )
        }
    }
}
