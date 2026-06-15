package io.synapse.ai.features.library.presentation.screen

import io.synapse.ai.core.ui.components.study.buildPackCardActions

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.features.library.presentation.components.DeletePackDialog
import io.synapse.ai.core.ui.components.ErrorBanner
import io.synapse.ai.core.ui.components.study.GridPackCard
import io.synapse.ai.features.summary.presentation.components.GridSummaryCard
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.domains.study.model.PackDisplayItem
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.features.library.presentation.components.AddPackCell
import io.synapse.ai.features.library.presentation.components.FilterTabRow
import io.synapse.ai.features.library.presentation.components.LibraryFilter
import io.synapse.ai.features.library.presentation.components.LibrarySearchBar
import io.synapse.ai.features.library.presentation.components.PackCountRow
import io.synapse.ai.features.library.presentation.components.PackEmptyState
import io.synapse.ai.features.library.presentation.state.LibraryFeedItem
import io.synapse.ai.features.library.presentation.state.LibrarySortOption
import io.synapse.ai.features.library.presentation.state.LibraryUiState
import io.synapse.ai.features.library.presentation.viewmodel.LibraryViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
fun LibraryScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val listState = rememberLazyGridState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate(effect.route)
                is UiEffect.ShowToast -> snackbarController.showToast(effect.type, effect.text.asString(context))
                is UiEffect.ShowError -> snackbarController.error(effect.text.asString(context))
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { snackbarController.SnackbarHost() },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        LibraryContent(
            uiState = uiState,
            listState = listState,
            onSearchChanged = viewModel::onSearchQueryChanged,
            onSortChanged = viewModel::onSortChanged,
            onPackTapped = viewModel::onPackTapped,
            onEditPack = viewModel::onEditPack,
            onExportPack = viewModel::onExportPack,
            onDeletePack = viewModel::onDeletePack,
            onSummaryTapped = viewModel::onSummaryTapped,
            onImportPdf = viewModel::onImportFromPdf,
            modifier = Modifier.padding(innerPadding),
        )
    }
}


@Composable
private fun LibraryContent(
    uiState: LibraryUiState,
    listState: LazyGridState,
    onSearchChanged: (String) -> Unit,
    onSortChanged: (LibrarySortOption) -> Unit,
    onPackTapped: (Long) -> Unit,
    onEditPack: (Long) -> Unit,
    onExportPack: (Long) -> Unit,
    onDeletePack: (Long) -> Unit,
    onSummaryTapped: (Long) -> Unit,
    onImportPdf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeFilter by remember { mutableStateOf(LibraryFilter.ALL) }
    var isErrorDismissed by remember(uiState.error) { mutableStateOf(false) }

    val displayedItems by remember(uiState.feedItems, activeFilter) {
        derivedStateOf { uiState.feedItems.filter(activeFilter.predicate) }
    }

    var openSwipedPackId by remember { mutableStateOf<Long?>(null) }
    var pendingDeletePackId by remember { mutableStateOf<Long?>(null) }

    // Delete confirmation dialog
    pendingDeletePackId?.let { packId ->
        DeletePackDialog(
            onConfirm = {
                onDeletePack(packId)
                pendingDeletePackId = null
            },
            onDismiss = { pendingDeletePackId = null },
        )
    }

    fun staggerDelay(index: Int) = index.coerceAtMost(5) * 60

    val spacing = MaterialTheme.synapse.spacing


    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.adp),
        state = listState,
        contentPadding = PaddingValues(
            start = spacing.screen,
            end = spacing.screen,
            top = spacing.screenContentTop,
            bottom = spacing.screenContentBottom,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.s12),
        horizontalArrangement = Arrangement.spacedBy(spacing.s12),
        modifier = modifier.fillMaxSize(),
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            LibrarySearchBar(
                query = uiState.searchQuery,
                onChanged = onSearchChanged,
                modifier = Modifier.padding(bottom = spacing.s8),
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = spacing.s8),
            ) {
                FilterTabRow(
                    activeFilter = activeFilter,
                    onSelect = { tab ->
                        activeFilter = tab
                        onSortChanged(tab.sort)
                    },
                )
                PackCountRow(
                    packCount = displayedItems.size,
                    totalDue = if (activeFilter == LibraryFilter.DUE) displayedItems.filterIsInstance<LibraryFeedItem.Pack>().sumOf { it.pack.cardsToReview } else 0,
                    showDueSum = activeFilter == LibraryFilter.DUE && displayedItems.isNotEmpty(),
                )
            }
        }

        if (uiState.error != null && !isErrorDismissed) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorBanner(
                    message = uiState.error.resolve(),
                    onDismiss = { isErrorDismissed = true },
                    modifier = Modifier.padding(bottom = spacing.s8),
                )
            }
        }

        if (displayedItems.isEmpty() && !uiState.isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PackEmptyState(filter = activeFilter)
            }
        }

        items(displayedItems, key = { it.id }) { item ->
            val index = displayedItems.indexOf(item)
            
            when (item) {
                is LibraryFeedItem.Pack -> {
                    val pack = item.pack
                    val onEditCard = remember(pack.id) { { onEditPack(pack.id) } }
                    val onExportCard = remember(pack.id) { { onExportPack(pack.id) } }
                    val onDeleteCard = remember(pack.id) { { pendingDeletePackId = pack.id } }
                    val actions = buildPackCardActions(
                        onEdit = onEditCard,
                        onExport = onExportCard,
                        onDelete = onDeleteCard
                    )
                    GridPackCard(
                        pack = pack,
                        animDelayMs = staggerDelay(index),
                        onClick = { onPackTapped(pack.id) },
                        actions = actions,
                        isSwiped = openSwipedPackId == pack.id,
                        onSwipeOpen = { openSwipedPackId = pack.id },
                        onSwipeClose = { if (openSwipedPackId == pack.id) openSwipedPackId = null },
                        enableSwipeActions = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                    )
                }
                is LibraryFeedItem.Summary -> {
                    GridSummaryCard(
                        summary = item.summary,
                        onClick = { onSummaryTapped(item.summary.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                    )
                }
            }
        }

        item(span = { GridItemSpan(if (displayedItems.size % 2 == 0) maxLineSpan else 1) }) {
            AddPackCell(
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
                feedItems = PackDisplayItem.Mocks.map { LibraryFeedItem.Pack(it) }.toImmutableList(),
                searchQuery = "",
                activeCategory = LibraryUiState.ALL_CATEGORY,
                availableCategories = listOf("All", "Science", "History", "Law"),
                isLoading = false,
            ),
            listState = rememberLazyGridState(),
            onSearchChanged = {},
            onSortChanged = {},
            onPackTapped = {},
            onEditPack = {},
            onExportPack = {},
            onDeletePack = {},
            onSummaryTapped = {},
            onImportPdf = {}
        )
    }
}

