package com.venom.synapse.features.library.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.ui.components.ErrorBanner
import com.venom.synapse.core.ui.components.GridPackCard
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.buildPackCardActions
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.core.ui.state.PackDisplayItem
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.features.library.presentation.components.AddPackCell
import com.venom.synapse.features.library.presentation.components.FilterTabRow
import com.venom.synapse.features.library.presentation.components.LibraryFilter
import com.venom.synapse.features.library.presentation.components.LibrarySearchBar
import com.venom.synapse.features.library.presentation.components.PackCountRow
import com.venom.synapse.features.library.presentation.components.PackEmptyState
import com.venom.synapse.features.library.presentation.state.LibrarySortOption
import com.venom.synapse.features.library.presentation.state.LibraryUiState
import com.venom.synapse.features.library.presentation.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate(effect.route)
                is UiEffect.ShowToast -> snackbarController.success(effect.text.asString(context))
                is UiEffect.ShowError -> snackbarController.error(effect.text.asString(context))
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { snackbarController.SnackbarHost() },
    ) { innerPadding ->
        LibraryContent(
            uiState = uiState,
            onSearchChanged = viewModel::onSearchQueryChanged,
            onSortChanged = viewModel::onSortChanged,
            onPackTapped = viewModel::onPackTapped,
            onEditPack = viewModel::onEditPack,
            onExportPack = viewModel::onExportPack,
            onDeletePack = viewModel::onDeletePack,
            onImportPdf = viewModel::onImportFromPdf,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────
@Composable
private fun LibraryContent(
    uiState: LibraryUiState,
    onSearchChanged: (String) -> Unit,
    onSortChanged: (LibrarySortOption) -> Unit,
    onPackTapped: (Long) -> Unit,
    onEditPack: (Long) -> Unit,
    onExportPack: (Long) -> Unit,
    onDeletePack: (Long) -> Unit,
    onImportPdf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    var openSwipedPackId by remember { mutableStateOf<Long?>(null) }
    var isErrorDismissed by remember(uiState.error) { mutableStateOf(false) }

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
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(
            start = MaterialTheme.synapse.spacing.screen,
            end = MaterialTheme.synapse.spacing.screen,
            top = MaterialTheme.synapse.spacing.screenContentTop,
            bottom = MaterialTheme.synapse.spacing.screenContentBottom,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            LibrarySearchBar(
                query = uiState.searchQuery,
                onChanged = onSearchChanged,
                modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s12),
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            FilterTabRow(
                activeFilter = activeFilter,
                totalDue = totalDue,
                onSelect = { tab ->
                    activeFilter = tab
                    onSortChanged(tab.sort)
                },
                modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s8),
            )
        }

        if (uiState.error != null && !isErrorDismissed) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorBanner(
                    message = uiState.error.resolve(),
                    onDismiss = { isErrorDismissed = true },
                    modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s8),
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            PackCountRow(
                packCount = displayedPacks.size,
                totalDue = if (activeFilter == LibraryFilter.DUE) displayedPacks.sumOf { it.cardsToReview } else 0,
                showDueSum = activeFilter == LibraryFilter.DUE && displayedPacks.isNotEmpty(),
                modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s8),
            )
        }

        if (displayedPacks.isEmpty() && !uiState.isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PackEmptyState(filter = activeFilter)
            }
        }

        items(displayedPacks, key = { it.id }) { pack ->
            val index = displayedPacks.indexOf(pack)
            val actions = remember(pack.id, onEditPack, onExportPack, onDeletePack) {
                buildPackCardActions(
                    onEdit = { onEditPack(pack.id) },
                    onExport = { onExportPack(pack.id) },
                    onDelete = { onDeletePack(pack.id) },
                )
            }
            GridPackCard(
                pack = pack,
                animDelayMs = staggerDelay(index),
                onClick = { onPackTapped(pack.id) },
                actions = actions,
                isSwiped = openSwipedPackId == pack.id,
                onSwipeOpen = { openSwipedPackId = pack.id },
                onSwipeClose = { if (openSwipedPackId == pack.id) openSwipedPackId = null },
                enableSwipeActions = true,
                modifier = Modifier.animateItem(),
            )
        }

        item(span = { GridItemSpan(if (displayedPacks.size % 2 == 0) maxLineSpan else 1) }) {
            AddPackCell(
                isWide = displayedPacks.size % 2 == 0,
                isLocked = uiState.isPackLimitReached,
                packCount = uiState.totalPackCount,
                onClick = onImportPdf,
            )
        }
    }
}


@Preview(name = "Library — Light", showBackground = true)
@Preview(name = "Library — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LibraryScreenPreview() {
    SynapseTheme {
        LibraryContent(
            uiState = LibraryUiState(
                packs = PackDisplayItem.Mocks,
                searchQuery = "",
                activeCategory = LibraryUiState.ALL_CATEGORY,
                availableCategories = listOf("All", "Science", "History", "Law"),
                isLoading = false,
            ),
            onSearchChanged = {},
            onSortChanged = {},
            onPackTapped = {},
            onEditPack = {},
            onExportPack = {},
            onDeletePack = {},
            onImportPdf = {},
        )
    }
}