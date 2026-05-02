package io.synapse.ai.features.marketplace.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.components.LoadingContent
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.features.marketplace.presentation.components.MarketplaceErrorPlaceholder
import io.synapse.ai.features.marketplace.presentation.components.MarketplaceFiltersBottomSheet
import io.synapse.ai.features.marketplace.presentation.components.MarketplacePackCard
import io.synapse.ai.features.marketplace.presentation.components.MarketplaceSearchBar
import io.synapse.ai.features.marketplace.presentation.components.MarketplaceSectionHeader
import io.synapse.ai.features.marketplace.presentation.state.MarketplaceEvent
import io.synapse.ai.features.marketplace.presentation.state.MarketplaceUiState
import io.synapse.ai.features.marketplace.presentation.viewmodel.MarketplaceViewModel

@Composable
fun MarketplaceScreen(
    onNavigateToPack: (String) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val context = LocalContext.current

    // One-time effects
    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigateToPack(effect.route)
                is UiEffect.ShowPaywall -> onNavigateToPremium()
                is UiEffect.ShowToast -> snackbarController.showToast(
                    effect.type,
                    effect.text.asString(context)
                )

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
        MarketplaceContent(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(innerPadding),
        )
    }

    // Pack detail bottom sheet
    if (state.showDetailSheet) {
        PackDetailsBottomSheet(
            detail      = state.selectedDetail,
            isAcquiring = state.isAcquiring,
            onDismiss   = { viewModel.onEvent(MarketplaceEvent.DismissDetail) },
            onAcquire   = { 
                state.selectedDetail?.pack?.id?.let {
                    viewModel.onEvent(MarketplaceEvent.OnAcquireClicked(it))
                } 
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure-UI content — no ViewModel dependency
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarketplaceContent(
    state: MarketplaceUiState,
    onEvent: (MarketplaceEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }
    val spacing = MaterialTheme.synapse.spacing

    LazyColumn(
        contentPadding = PaddingValues(
            start = spacing.screen,
            end = spacing.screen,
            top = spacing.screenContentTop,
            bottom = spacing.screenContentBottom,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.s12),
        modifier = modifier.fillMaxSize(),
    ) {
        stickyHeader {
            MarketplaceSearchBar(
                query = state.filter.searchQuery,
                onQuery = { onEvent(MarketplaceEvent.OnSearchChanged(it)) },
                onFilters = { showFilters = true },
                hasActiveFilters = state.filter.category != null || state.filter.difficulty != null,
            )
        }

        if (state.isLoading && state.packs.isEmpty()) {
            item {
                LoadingContent(modifier = Modifier.fillParentMaxSize())
            }
        } else if (state.error != null && state.packs.isEmpty()) {
            item {
                MarketplaceErrorPlaceholder(
                    message = state.error,
                    onRetry = { onEvent(MarketplaceEvent.Retry) },
                    modifier = Modifier.fillParentMaxSize()
                )
            }
        } else {
            // Featured row
            if (state.featuredPacks.isNotEmpty()) {
                item(key = "featured_header") {
                    MarketplaceSectionHeader(
                        title = stringResource(R.string.synapse_marketplace_featured),
                        modifier = Modifier.padding(horizontal = 20.adp, vertical = 16.adp),
                    )
                }
                item(key = "featured_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.adp),
                        horizontalArrangement = Arrangement.spacedBy(14.adp),
                    ) {
                        items(state.featuredPacks, key = { it.id }) { pack ->
                            MarketplacePackCard(
                                pack = pack,
                                wide = true,
                                onClick = { onEvent(MarketplaceEvent.OnPackClicked(pack.id)) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.adp))
                }
            }

            // All packs header
            item(key = "all_header") {
                MarketplaceSectionHeader(
                    title = if (state.filter.isActive) stringResource(
                        R.string.synapse_marketplace_results_count,
                        state.packs.size
                    ) else stringResource(R.string.synapse_marketplace_all_packs),
                    modifier = Modifier.padding(horizontal = 20.adp, vertical = 16.adp),
                )
            }

            // All packs 2-column grid
            items(state.packs.chunked(2), key = { rowPacks -> rowPacks.first().id }) { rowPacks ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.adp),
                    horizontalArrangement = Arrangement.spacedBy(12.adp)
                ) {
                    for (pack in rowPacks) {
                        MarketplacePackCard(
                            pack = pack,
                            wide = false,
                            onClick = { onEvent(MarketplaceEvent.OnPackClicked(pack.id)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Fill remaining space if the last row is incomplete
                    if (rowPacks.size < 2) {
                        Spacer(Modifier.weight((2 - rowPacks.size).toFloat()))
                    }
                }
            }
        }
    }

    if (showFilters) {
        MarketplaceFiltersBottomSheet(
            currentCategory = state.filter.category,
            currentDifficulty = state.filter.difficulty,
            categories = state.availableCategories,
            onCategory = { onEvent(MarketplaceEvent.OnCategoryChanged(it)) },
            onDifficulty = { onEvent(MarketplaceEvent.OnDifficultyChanged(it)) },
            onClear = { onEvent(MarketplaceEvent.ClearFilters) },
            onClose = { showFilters = false },
        )
    }
}
