package com.venom.synapse.features.dashboard.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.ui.components.GridPackCard
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.buildPackCardActions
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.features.dashboard.presentation.components.DailyGoalCard
import com.venom.synapse.features.dashboard.presentation.components.DashboardFab
import com.venom.synapse.features.dashboard.presentation.components.EmptyPacksState
import com.venom.synapse.features.dashboard.presentation.components.SectionHeader
import com.venom.synapse.features.dashboard.presentation.components.StatsRow
import com.venom.synapse.features.dashboard.presentation.state.DashboardUiState
import com.venom.synapse.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.venom.synapse.ui.viewmodel.RootViewModel

@Composable
fun DashboardScreen(
    onNavigate   : (String) -> Unit = {},
    viewModel    : DashboardViewModel = hiltViewModel(),
    rootViewModel: RootViewModel = hiltViewModel(),
    modifier     : Modifier = Modifier,
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val listState          = rememberLazyGridState()
    val context            = LocalContext.current

    val isFabExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate  -> onNavigate(effect.route)
                is UiEffect.ShowToast -> snackbarController.success(effect.text.asString(context))
                is UiEffect.ShowError -> snackbarController.error(effect.text.asString(context))
                else                  -> Unit
            }
        }
    }

    LaunchedEffect(uiState.greetingRes) {
        rootViewModel.setSubtitleResOverride(uiState.greetingRes)
    }

    DisposableEffect(Unit) {
        onDispose { rootViewModel.setSubtitleResOverride(null) }
    }

    Scaffold(
        modifier            = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor      = MaterialTheme.colorScheme.background,
        snackbarHost        = { snackbarController.SnackbarHost() },
        floatingActionButton = {
            DashboardFab(
                isLocked   = uiState.isPackLimitReached,
                isExpanded = isFabExpanded,
                onClick    = viewModel::onAddPack,
                modifier   = Modifier.padding(bottom = MaterialTheme.synapse.spacing.fabBottom),
            )
        },
    ) { innerPadding ->
        DashboardContent(
            uiState         = uiState,
            listState       = listState,
            onStartStudying = viewModel::onStartStudying,
            onPackTapped    = viewModel::onPackTapped,
            onDeletePack    = viewModel::onDeletePack,
            onSeeAllPacks   = viewModel::onSeeAllPacks,
            onAddPack       = viewModel::onAddPack,
            modifier        = Modifier.padding(innerPadding),
        )
    }
}


@Composable
private fun DashboardContent(
    uiState        : DashboardUiState,
    listState      : LazyGridState,
    onStartStudying: () -> Unit,
    onPackTapped   : (Long) -> Unit,
    onDeletePack   : (Long) -> Unit,
    onSeeAllPacks  : () -> Unit,
    onAddPack      : () -> Unit,
    modifier       : Modifier = Modifier,
) {
    var openSwipedPackId by remember { mutableStateOf<Long?>(null) }

    LazyVerticalGrid(
        columns              = GridCells.Fixed(2),
        state                = listState,
        modifier             = modifier,
        contentPadding       = PaddingValues(
            start  = MaterialTheme.synapse.spacing.screen,
            end    = MaterialTheme.synapse.spacing.screen,
            top    = MaterialTheme.synapse.spacing.screenContentTop,
            bottom = MaterialTheme.synapse.spacing.screenContentBottom,
        ),
        verticalArrangement   = Arrangement.spacedBy(MaterialTheme.synapse.spacing.listItemGap),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            DailyGoalCard(
                todayStudied    = uiState.todayStudied,
                dailyGoal       = uiState.dailyGoal,
                streakDays      = uiState.streakDays,
                totalDue        = uiState.totalDue,
                onStartStudying = onStartStudying,
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            StatsRow(
                streak           = uiState.streakDays,
                accuracyPercent  = uiState.accuracyPercent,
                accuracyDelta    = uiState.accuracyDelta,
                accuracyDeltaRes = uiState.accuracyDeltaRes,
                timeMinutes      = uiState.timeStudiedMinutes,
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title    = stringResource(R.string.section_jump_back_in),
                onSeeAll = onSeeAllPacks,
            )
        }

        if (uiState.packs.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyPacksState(
                    onAddPack = onAddPack,
                    modifier  = Modifier,
                )
            }
        } else {
            val displayedPacks = uiState.packs.take(4)
            items(displayedPacks, key = { it.id }) { pack ->
                val actions = remember(pack.id, onDeletePack) {
                    buildPackCardActions(
                        onEdit   = { /* TODO */ },
                        onExport = { /* TODO */ },
                        onDelete = { onDeletePack(pack.id) },
                    )
                }
                GridPackCard(
                    pack               = pack,
                    animDelayMs        = 0,
                    onClick            = { onPackTapped(pack.id) },
                    actions            = actions,
                    isSwiped           = openSwipedPackId == pack.id,
                    onSwipeOpen        = { openSwipedPackId = pack.id },
                    onSwipeClose       = { if (openSwipedPackId == pack.id) openSwipedPackId = null },
                    enableSwipeActions = true,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                )
            }
        }
    }
}
