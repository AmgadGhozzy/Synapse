package com.venom.synapse.features.library.presentation.screen

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.tokens.Radius
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.ui.components.GridPackCard
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.buildPackCardActions
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.core.ui.state.PackDisplayItem
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.core.ui.utils.animatedDashedBorder
import com.venom.synapse.features.library.presentation.state.LibrarySortOption
import com.venom.synapse.features.library.presentation.state.LibraryUiState
import com.venom.synapse.features.library.presentation.state.LibraryUiState.Companion.FREE_PACK_LIMIT
import com.venom.synapse.features.library.presentation.viewmodel.LibraryViewModel
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

// ── Filter tab definition (UI-layer only) ─────────────────────────────────────
private enum class LibraryFilter(
    val labelRes: Int,
    val sort    : LibrarySortOption,
    val onlyDue : Boolean = false,
) {
    ALL   (R.string.library_filter_all,    LibrarySortOption.RECENT),
    RECENT(R.string.library_filter_recent, LibrarySortOption.RECENT),
    DUE   (R.string.library_filter_due,    LibrarySortOption.MOST_DUE, onlyDue = true),
}

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun LibraryScreen(
    onNavigate: (String) -> Unit = {},
    viewModel : LibraryViewModel = hiltViewModel(),
    modifier  : Modifier         = Modifier,
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate  -> onNavigate(effect.route)
                is UiEffect.ShowToast -> snackbarController.success(effect.message)
                is UiEffect.ShowError -> snackbarController.error(effect.message)
                else                  -> Unit
            }
        }
    }

    Scaffold(
        modifier            = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor      = MaterialTheme.colorScheme.background,
        snackbarHost        = { snackbarController.SnackbarHost() },
    ) { innerPadding ->
        LibraryContent(
            uiState         = uiState,
            onSearchChanged = viewModel::onSearchQueryChanged,
            onSortChanged   = viewModel::onSortChanged,
            onPackTapped    = viewModel::onPackTapped,
            onEditPack      = viewModel::onEditPack,
            onExportPack    = viewModel::onExportPack,
            onDeletePack    = viewModel::onDeletePack,
            onImportPdf     = viewModel::onImportFromPdf,
            modifier        = Modifier.padding(innerPadding),
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────
@Composable
private fun LibraryContent(
    uiState        : LibraryUiState,
    onSearchChanged: (String) -> Unit,
    onSortChanged  : (LibrarySortOption) -> Unit,
    onPackTapped   : (Long) -> Unit,
    onEditPack     : (Long) -> Unit,
    onExportPack   : (Long) -> Unit,
    onDeletePack   : (Long) -> Unit,
    onImportPdf    : () -> Unit,
    modifier       : Modifier = Modifier,
) {
    var activeFilter     by remember { mutableStateOf(LibraryFilter.ALL) }
    var openSwipedPackId by remember { mutableStateOf<Long?>(null) }

    val displayedPacks by remember(uiState.packs, activeFilter) {
        derivedStateOf {
            if (activeFilter.onlyDue) uiState.packs.filter { it.cardsToReview > 0 }
            else uiState.packs
        }
    }

    val totalDue by remember(uiState.packs) {
        derivedStateOf { uiState.packs.sumOf { it.cardsToReview } }
    }

    fun staggerDelay(index: Int) = index.coerceAtMost(5) * 60

    LazyVerticalGrid(
        columns              = GridCells.Fixed(2),
        modifier             = modifier,
        contentPadding       = PaddingValues(
            start  = Spacing.ScreenHorizontalPadding,
            end    = Spacing.ScreenHorizontalPadding,
            top    = 132.adp,
            bottom = 96.adp,
        ),
        verticalArrangement   = Arrangement.spacedBy(Spacing.Spacing12),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            LibrarySearchBar(
                query     = uiState.searchQuery,
                onChanged = onSearchChanged,
                modifier  = Modifier.padding(bottom = Spacing.Spacing12),
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            FilterTabRow(
                activeFilter = activeFilter,
                totalDue     = totalDue,
                onSelect     = { tab ->
                    activeFilter = tab
                    onSortChanged(tab.sort)
                },
                modifier = Modifier.padding(bottom = Spacing.Spacing8),
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            PackCountRow(
                packCount  = displayedPacks.size,
                totalDue   = if (activeFilter == LibraryFilter.DUE) displayedPacks.sumOf { it.cardsToReview } else 0,
                showDueSum = activeFilter == LibraryFilter.DUE && displayedPacks.isNotEmpty(),
                modifier   = Modifier.padding(bottom = Spacing.Spacing8),
            )
        }

        if (displayedPacks.isEmpty() && !uiState.isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PackEmptyState(filter = activeFilter)
            }
        }

        items(displayedPacks, key = { it.id }) { pack ->
            val index   = displayedPacks.indexOf(pack)
            val actions = remember(pack.id, onEditPack, onExportPack, onDeletePack) {
                buildPackCardActions(
                    onEdit   = { onEditPack(pack.id) },
                    onExport = { onExportPack(pack.id) },
                    onDelete = { onDeletePack(pack.id) },
                )
            }
            GridPackCard(
                pack               = pack,
                animDelayMs        = staggerDelay(index),
                onClick            = { onPackTapped(pack.id) },
                actions            = actions,
                isSwiped           = openSwipedPackId == pack.id,
                onSwipeOpen        = { openSwipedPackId = pack.id },
                onSwipeClose       = { if (openSwipedPackId == pack.id) openSwipedPackId = null },
                enableSwipeActions = true,
                modifier           = Modifier.animateItem(),
            )
        }

        // ── Add-pack cell — spans full row if list is even-length ──────────
        item(span = { GridItemSpan(if (displayedPacks.size % 2 == 0) maxLineSpan else 1) }) {
            AddPackCell(
                isWide    = displayedPacks.size % 2 == 0,
                isLocked  = uiState.isPackLimitReached,
                packCount = uiState.totalPackCount,
                onClick   = onImportPdf,
            )
        }
    }
}

// ── Add-Pack Cell ─────────────────────────────────────────────────────────────

/**
 * Tappable cell that opens the Add-PDF wizard, or — when [isLocked] — shows an
 * upgrade nudge and routes to the premium screen (via [onClick] → ViewModel).
 *
 * Two layout modes driven by [isWide]:
 *   isWide = true  → Row  layout (70dp tall,  full-width, 2-column span)
 *   isWide = false → Column layout (140dp tall, single-column slot)
 *
 * Visual states:
 *   Normal  — primary tint, dashed border, plus icon
 *   Locked  — tertiary tint, dashed border, lock icon + "Upgrade to Pro" label
 */
@Composable
private fun AddPackCell(
    isWide   : Boolean,
    isLocked : Boolean,
    packCount: Int,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
) {
    // Colour roles — all sourced from MaterialTheme, zero hardcoded values
    val accentColor = if (isLocked) MaterialTheme.colorScheme.tertiary
    else          MaterialTheme.colorScheme.primary

    val bgColor     = accentColor.copy(alpha = 0.06f)
    val borderColor = accentColor.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isWide) 92.adp else 148.adp)
            .clip(Radius.ShapeXL)
            .background(bgColor)
            .animatedDashedBorder(color = borderColor, radius = Radius.RadiusXL)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isWide) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
            ) {
                AddPackIcon(accentColor = accentColor, isLocked = isLocked)
                AddPackCellText(
                    isLocked   = isLocked,
                    packCount  = packCount,
                    accentColor = accentColor,
                    isWide     = true,
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
            ) {
                AddPackIcon(accentColor = accentColor, isLocked = isLocked)
                AddPackCellText(
                    isLocked    = isLocked,
                    packCount   = packCount,
                    accentColor = accentColor,
                    isWide      = false,
                )
            }
        }
    }
}

/**
 * Shared title + subtitle block used by both wide and narrow layouts.
 * Extracted to eliminate the duplicated Text/Text tree that lived in
 * each branch of the original if/else.
 */
@Composable
private fun AddPackCellText(
    isLocked   : Boolean,
    packCount  : Int,
    accentColor: Color,
    isWide     : Boolean,
    modifier   : Modifier = Modifier,
) {
    val titleRes    = if (isLocked) R.string.add_pack_cell_locked_title
    else if (isWide) R.string.library_add_pack_title
    else R.string.library_add_pack_short_title

    val subtitleRes = if (isLocked) R.string.add_pack_cell_locked_subtitle
    else R.string.library_add_pack_subtitle

    Column(
        modifier            = modifier,
        horizontalAlignment = if (isWide) Alignment.Start else Alignment.CenterHorizontally,
    ) {
        Text(
            text       = stringResource(titleRes),
            fontSize   = if (isWide) 13.asp else 12.asp,
            fontWeight = FontWeight.SemiBold,
            color      = accentColor,
        )
        Text(
            text     = if (isLocked) {
                stringResource(subtitleRes, packCount, FREE_PACK_LIMIT)
            } else {
                stringResource(subtitleRes)
            },
            fontSize = if (isWide) 11.asp else 10.asp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Icon container — switches between plus (unlocked) and lock (locked).
 * Kept as a private composable so the icon logic isn't duplicated across
 * wide and narrow branches.
 */
@Composable
private fun AddPackIcon(
    accentColor: Color,
    isLocked   : Boolean,
    modifier   : Modifier = Modifier,
) {
    Box(
        modifier         = modifier
            .size(36.adp)
            .clip(Radius.ShapeMedium)
            .background(accentColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter            = painterResource(
                if (isLocked) R.drawable.ic_lock else R.drawable.ic_plus
            ),
            contentDescription = null,
            tint               = accentColor,
            modifier           = Modifier.size(18.adp),
        )
    }
}

// ── Search Bar ────────────────────────────────────────────────────────────────
@Composable
private fun LibrarySearchBar(
    query    : String,
    onChanged: (String) -> Unit,
    modifier : Modifier = Modifier,
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onChanged,
        modifier      = modifier.fillMaxWidth(),
        placeholder   = {
            Text(
                text  = stringResource(R.string.library_search_placeholder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon  = {
            Icon(
                painter            = painterResource(R.drawable.ic_search),
                contentDescription = stringResource(R.string.library_search_description),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(15.adp),
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                Icon(
                    painter            = painterResource(R.drawable.ic_x),
                    contentDescription = stringResource(R.string.library_search_clear),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier
                        .size(15.adp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                        ) { onChanged("") },
                )
            }
        } else null,
        singleLine  = true,
        shape       = Radius.ShapeLarge,
        textStyle   = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        colors      = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor    = Color.Transparent,
            focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}

// ── Filter Tab Row ────────────────────────────────────────────────────────────
@Composable
private fun FilterTabRow(
    activeFilter: LibraryFilter,
    totalDue    : Int,
    onSelect    : (LibraryFilter) -> Unit,
    modifier    : Modifier = Modifier,
) {
    LazyRow(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
        contentPadding        = PaddingValues(end = Spacing.Spacing4),
    ) {
        items(LibraryFilter.entries.size) { i ->
            val tab = LibraryFilter.entries[i]
            FilterTab(
                labelRes = tab.labelRes,
                active   = tab == activeFilter,
                onClick  = { onSelect(tab) },
            )
        }
    }
}

@Composable
private fun FilterTab(
    @StringRes labelRes: Int,
    active  : Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue   = if (active) MaterialTheme.colorScheme.primary
        else        MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label         = "tab_bg",
    )
    val labelColor by animateColorAsState(
        targetValue   = if (active) MaterialTheme.colorScheme.onPrimary
        else        MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label         = "tab_label",
    )

    Row(
        modifier = modifier
            .clip(Radius.ShapePill)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = Spacing.Spacing14, vertical = Spacing.Spacing6),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing6),
    ) {
        Text(
            text       = stringResource(labelRes),
            fontSize   = 12.asp,
            fontWeight = FontWeight.SemiBold,
            color      = labelColor,
        )
    }
}

// ── Pack Count Row ────────────────────────────────────────────────────────────
@Composable
private fun PackCountRow(
    packCount : Int,
    totalDue  : Int,
    showDueSum: Boolean,
    modifier  : Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = stringResource(R.string.library_pack_count, packCount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showDueSum) {
            Text(
                text       = stringResource(R.string.library_cards_due, totalDue),
                fontSize   = 11.asp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────
@Composable
private fun PackEmptyState(
    filter  : LibraryFilter,
    modifier: Modifier = Modifier,
) {
    data class EmptyMsg(val icon: String, val titleRes: Int, val subRes: Int)

    val msg = when (filter) {
        LibraryFilter.ALL,
        LibraryFilter.RECENT -> EmptyMsg("📚", R.string.library_empty_title_all, R.string.library_empty_sub_all)
        LibraryFilter.DUE    -> EmptyMsg("✅", R.string.library_empty_title_due, R.string.library_empty_sub_due)
    }

    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Spacing32, horizontal = Spacing.Spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = msg.icon, fontSize = 40.asp)
        Spacer(Modifier.height(Spacing.Spacing12))
        Text(
            text       = stringResource(msg.titleRes),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.Spacing6))
        Text(
            text  = stringResource(msg.subRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Light — Normal", showBackground = true)
@Preview(name = "Dark — Normal",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AddPackCellNormalPreview() {
    SynapseTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
            modifier = Modifier.padding(Spacing.ScreenHorizontalPadding)) {
            AddPackCell(isWide = true,  isLocked = false, packCount = 3, onClick = {})
            AddPackCell(isWide = false, isLocked = false, packCount = 3, onClick = {},
                modifier = Modifier.fillMaxWidth(0.5f))
        }
    }
}

@Preview(name = "Light — Locked", showBackground = true)
@Preview(name = "Dark — Locked",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AddPackCellLockedPreview() {
    SynapseTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
            modifier = Modifier.padding(Spacing.ScreenHorizontalPadding)) {
            AddPackCell(isWide = true,  isLocked = true, packCount = 5, onClick = {})
            AddPackCell(isWide = false, isLocked = true, packCount = 5, onClick = {},
                modifier = Modifier.fillMaxWidth(0.5f))
        }
    }
}

@Preview(name = "Library — Light", showBackground = true)
@Preview(name = "Library — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    SynapseTheme {
        LibraryContent(
            uiState = LibraryUiState(
                packs               = PackDisplayItem.Mocks,
                searchQuery         = "",
                activeCategory      = LibraryUiState.ALL_CATEGORY,
                availableCategories = listOf("All", "Science", "History", "Law"),
                isLoading           = false,
            ),
            onSearchChanged = {},
            onSortChanged   = {},
            onPackTapped    = {},
            onEditPack      = {},
            onExportPack    = {},
            onDeletePack    = {},
            onImportPdf     = {},
        )
    }
}