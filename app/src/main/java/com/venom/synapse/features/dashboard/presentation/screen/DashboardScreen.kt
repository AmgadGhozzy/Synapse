package com.venom.synapse.features.dashboard.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.ui.components.DeletePackDialog
import com.venom.synapse.core.ui.components.GridPackCard
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.buildPackCardActions
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.core.ui.state.PackDisplayItem
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.features.dashboard.presentation.components.DailyGoalCard
import com.venom.synapse.features.dashboard.presentation.components.DashboardFab
import com.venom.synapse.features.dashboard.presentation.components.EmptyPacksState
import com.venom.synapse.features.dashboard.presentation.components.SectionHeader
import com.venom.synapse.features.dashboard.presentation.components.StatsRow
import com.venom.synapse.features.dashboard.presentation.state.DashboardUiState
import com.venom.synapse.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.venom.synapse.ui.viewmodel.RootViewModel
import kotlinx.coroutines.delay

private val WIDE_LAYOUT_BREAKPOINT = 600.dp

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    rootViewModel: RootViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val listState = rememberLazyGridState()
    val context = LocalContext.current

    val isFabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

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

    var isVisible by remember { mutableStateOf(false) }
    // Sync time-of-day greeting into the shell top-bar.
    LaunchedEffect(uiState.greetingRes) {
        rootViewModel.setSubtitleResOverride(uiState.greetingRes)
        delay(50)
        isVisible = true
    }
    DisposableEffect(Unit) {
        onDispose { rootViewModel.clearSubtitleResOverride() }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { snackbarController.SnackbarHost() },
        containerColor       = Color.Transparent,
        floatingActionButton = {
            DashboardFab(
                isLocked  = uiState.isPackLimitReached,
                isExpanded = isFabExpanded,
                onClick   = viewModel::onAddPack,
                isVisible = isVisible,
                modifier  = Modifier.padding(bottom = MaterialTheme.synapse.spacing.fabBottom),
            )
        },
    ) { innerPadding ->
        DashboardContent(
            uiState         = uiState,
            listState       = listState,
            onStartStudying = viewModel::onStartStudying,
            onPackTapped    = viewModel::onPackTapped,
            onDeletePack    = viewModel::onDeletePack,
            onEditPack      = viewModel::onEditPack,
            onExportPack    = viewModel::onExportPack,
            onSeeAllPacks   = viewModel::onSeeAllPacks,
            onAddPack       = viewModel::onAddPack,
            modifier        = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun DashboardContent(
    uiState         : DashboardUiState,
    listState       : LazyGridState,
    onStartStudying : () -> Unit,
    onPackTapped    : (Long) -> Unit,
    onDeletePack    : (Long) -> Unit,
    onEditPack      : (Long) -> Unit,
    onExportPack    : (Long) -> Unit,
    onSeeAllPacks   : () -> Unit,
    onAddPack       : () -> Unit,
    modifier        : Modifier = Modifier,
) {
    var openSwipedPackId   by remember { mutableStateOf<Long?>(null) }
    var pendingDeletePackId by remember { mutableStateOf<Long?>(null) }

    pendingDeletePackId?.let { packId ->
        DeletePackDialog(
            onConfirm = { onDeletePack(packId); pendingDeletePackId = null },
            onDismiss = { pendingDeletePackId = null },
        )
    }

    // Callbacks extracted so they aren't recreated on every recomposition.
    val onSwipeOpen  : (Long) -> Unit = remember { { id -> openSwipedPackId = id } }
    val onSwipeClose : (Long) -> Unit = remember { { id -> if (openSwipedPackId == id) openSwipedPackId = null } }
    val onPendingDel : (Long) -> Unit = remember { { id -> pendingDeletePackId = id } }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth > WIDE_LAYOUT_BREAKPOINT) {
            LandscapeLayout(
                uiState         = uiState,
                listState       = listState,
                openSwipedPackId = openSwipedPackId,
                onStartStudying = onStartStudying,
                onPackTapped    = onPackTapped,
                onEditPack      = onEditPack,
                onExportPack    = onExportPack,
                onSwipeOpen     = onSwipeOpen,
                onSwipeClose    = onSwipeClose,
                onPendingDelete = onPendingDel,
                onSeeAllPacks   = onSeeAllPacks,
                onAddPack       = onAddPack,
            )
        } else {
            PortraitLayout(
                uiState          = uiState,
                listState        = listState,
                openSwipedPackId = openSwipedPackId,
                onStartStudying  = onStartStudying,
                onPackTapped     = onPackTapped,
                onEditPack       = onEditPack,
                onExportPack     = onExportPack,
                onSwipeOpen      = onSwipeOpen,
                onSwipeClose     = onSwipeClose,
                onPendingDelete  = onPendingDel,
                onSeeAllPacks    = onSeeAllPacks,
                onAddPack        = onAddPack,
            )
        }
    }
}

@Composable
private fun PortraitLayout(
    uiState          : DashboardUiState,
    listState        : LazyGridState,
    openSwipedPackId : Long?,
    onStartStudying  : () -> Unit,
    onPackTapped     : (Long) -> Unit,
    onEditPack       : (Long) -> Unit,
    onExportPack     : (Long) -> Unit,
    onSwipeOpen      : (Long) -> Unit,
    onSwipeClose     : (Long) -> Unit,
    onPendingDelete  : (Long) -> Unit,
    onSeeAllPacks    : () -> Unit,
    onAddPack        : () -> Unit,
    modifier         : Modifier = Modifier,
) {
    val spacing      = MaterialTheme.synapse.spacing
    val sectionTitle = stringResource(R.string.section_jump_back_in)

    LazyVerticalGrid(
        columns              = GridCells.Adaptive(minSize = 180.dp),
        state                = listState,
        contentPadding       = PaddingValues(
            start  = spacing.screen,
            end    = spacing.screen,
            top    = spacing.screenContentTop,
            bottom = spacing.screenContentBottom,
        ),
        verticalArrangement  = Arrangement.spacedBy(spacing.s12),
        horizontalArrangement = Arrangement.spacedBy(spacing.s12),
        modifier             = modifier.fillMaxSize(),
    ) {

        // ── Daily Goal Card ───────────────────────────────────────────
        item(span = { GridItemSpan(maxLineSpan) }) {
            DailyGoalCard(
                todayStudied    = uiState.todayStudied,
                dailyGoal       = uiState.dailyGoal,
                streakDays      = uiState.streakDays,
                totalDue        = uiState.totalDue,
                onStartStudying = onStartStudying,
            )
        }

        // ── Stats Row ─────────────────────────────────────────────────
        item(span = { GridItemSpan(maxLineSpan) }) {
            StatsRow(
                streak          = uiState.streakDays,
                accuracyPercent = uiState.accuracyPercent,
                accuracyDelta   = uiState.accuracyDelta,
                accuracyDeltaRes = uiState.accuracyDeltaRes,
                timeMinutes     = uiState.timeStudiedMinutes,
            )
        }

        // ── Section header ────────────────────────────────────────────
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title    = sectionTitle,
                onSeeAll = onSeeAllPacks,
            )
        }

        if (uiState.packs.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyPacksState(onAddPack = onAddPack)
            }
        } else {
            items(uiState.packs, key = { it.id }) { pack ->
                PackCard(
                    pack             = pack,
                    openSwipedPackId = openSwipedPackId,
                    onPackTapped     = onPackTapped,
                    onEditPack       = onEditPack,
                    onExportPack     = onExportPack,
                    onSwipeOpen      = onSwipeOpen,
                    onSwipeClose     = onSwipeClose,
                    onPendingDelete  = onPendingDelete,
                    modifier         = Modifier.fillMaxWidth().animateItem(),
                )
            }
        }
    }
}

@Composable
private fun LandscapeLayout(
    uiState          : DashboardUiState,
    listState        : LazyGridState,
    openSwipedPackId : Long?,
    onStartStudying  : () -> Unit,
    onPackTapped     : (Long) -> Unit,
    onEditPack       : (Long) -> Unit,
    onExportPack     : (Long) -> Unit,
    onSwipeOpen      : (Long) -> Unit,
    onSwipeClose     : (Long) -> Unit,
    onPendingDelete  : (Long) -> Unit,
    onSeeAllPacks    : () -> Unit,
    onAddPack        : () -> Unit,
    modifier         : Modifier = Modifier,
) {
    val spacing = MaterialTheme.synapse.spacing

    Row(
        modifier              = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(spacing.screen),
    ) {
        SummaryPanel(
            uiState         = uiState,
            onStartStudying = onStartStudying,
            onSeeAllPacks   = onSeeAllPacks,
            modifier        = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )

        // ── Right: Pack grid ──────────────────────────────────────────
        PackGrid(
            packs            = uiState.packs,
            listState        = listState,
            openSwipedPackId = openSwipedPackId,
            onPackTapped     = onPackTapped,
            onEditPack       = onEditPack,
            onExportPack     = onExportPack,
            onSwipeOpen      = onSwipeOpen,
            onSwipeClose     = onSwipeClose,
            onPendingDelete  = onPendingDelete,
            onAddPack        = onAddPack,
            modifier         = Modifier
                .weight(2f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun SummaryPanel(
    uiState         : DashboardUiState,
    onStartStudying : () -> Unit,
    onSeeAllPacks   : () -> Unit,
    modifier        : Modifier = Modifier,
) {
    val spacing = MaterialTheme.synapse.spacing

    Column(
        modifier              = modifier
            .verticalScroll(rememberScrollState())
            .padding(top = spacing.screenContentTop, bottom = spacing.screenContentBottom),
        verticalArrangement   = Arrangement.spacedBy(spacing.listItemGap),
    ) {
        DailyGoalCard(
            todayStudied    = uiState.todayStudied,
            dailyGoal       = uiState.dailyGoal,
            streakDays      = uiState.streakDays,
            totalDue        = uiState.totalDue,
            onStartStudying = onStartStudying,
        )
        StatsRow(
            streak           = uiState.streakDays,
            accuracyPercent  = uiState.accuracyPercent,
            accuracyDelta    = uiState.accuracyDelta,
            accuracyDeltaRes = uiState.accuracyDeltaRes,
            timeMinutes      = uiState.timeStudiedMinutes,
        )
        SectionHeader(
            title    = stringResource(R.string.section_jump_back_in),
            onSeeAll = onSeeAllPacks,
        )
    }
}

@Composable
private fun PackGrid(
    packs            : List<PackDisplayItem>,
    listState        : LazyGridState,
    openSwipedPackId : Long?,
    onPackTapped     : (Long) -> Unit,
    onEditPack       : (Long) -> Unit,
    onExportPack     : (Long) -> Unit,
    onSwipeOpen      : (Long) -> Unit,
    onSwipeClose     : (Long) -> Unit,
    onPendingDelete  : (Long) -> Unit,
    onAddPack        : () -> Unit,
    modifier         : Modifier = Modifier,
) {
    val spacing = MaterialTheme.synapse.spacing

    if (packs.isEmpty()) {
        Box(
            modifier = modifier.padding(
                top    = spacing.screenContentTop,
                bottom = spacing.screenContentBottom,
            ),
        ) {
            EmptyPacksState(
                onAddPack = onAddPack,
                modifier  = Modifier.fillMaxWidth(),
            )
        }
        return
    }

    LazyVerticalGrid(
        columns               = GridCells.Adaptive(minSize = 180.dp),
        state                 = listState,
        contentPadding        = PaddingValues(
            top    = spacing.screenContentTop,
            bottom = spacing.screenContentBottom,
        ),
        verticalArrangement   = Arrangement.spacedBy(spacing.listItemGap),
        horizontalArrangement = Arrangement.spacedBy(spacing.s12),
        modifier              = modifier,
    ) {
        items(packs, key = { it.id }) { pack ->
            PackCard(
                pack             = pack,
                openSwipedPackId = openSwipedPackId,
                onPackTapped     = onPackTapped,
                onEditPack       = onEditPack,
                onExportPack     = onExportPack,
                onSwipeOpen      = onSwipeOpen,
                onSwipeClose     = onSwipeClose,
                onPendingDelete  = onPendingDelete,
                modifier         = Modifier.fillMaxWidth().animateItem(),
            )
        }
    }
}

@Composable
private fun PackCard(
    pack             : PackDisplayItem,
    openSwipedPackId : Long?,
    onPackTapped     : (Long) -> Unit,
    onEditPack       : (Long) -> Unit,
    onExportPack     : (Long) -> Unit,
    onSwipeOpen      : (Long) -> Unit,
    onSwipeClose     : (Long) -> Unit,
    onPendingDelete  : (Long) -> Unit,
    modifier         : Modifier = Modifier,
) {
    val actions = remember(pack.id) {
        buildPackCardActions(
            onEdit   = { onEditPack(pack.id) },
            onExport = { onExportPack(pack.id) },
            onDelete = { onPendingDelete(pack.id) },
        )
    }
    GridPackCard(
        pack               = pack,
        animDelayMs        = 0,
        onClick            = { onPackTapped(pack.id) },
        actions            = actions,
        isSwiped           = openSwipedPackId == pack.id,
        onSwipeOpen        = { onSwipeOpen(pack.id) },
        onSwipeClose       = { onSwipeClose(pack.id) },
        enableSwipeActions = true,
        modifier           = modifier,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Portrait · Light", showBackground = true)
@Preview(name = "Portrait · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DashboardScreenPortraitPreview() {
    SynapseTheme {
        PortraitLayout(
            uiState          = DashboardUiState(isLoading = false),
            listState        = rememberLazyGridState(),
            openSwipedPackId = null,
            onStartStudying  = {},
            onPackTapped     = {},
            onEditPack       = {},
            onExportPack     = {},
            onSwipeOpen      = {},
            onSwipeClose     = {},
            onPendingDelete  = {},
            onSeeAllPacks    = {},
            onAddPack        = {},
        )
    }
}

@Preview(
    name        = "Landscape · Light",
    showBackground = true,
    widthDp     = 800,
    heightDp    = 400,
)
@Preview(
    name        = "Landscape · Dark",
    uiMode      = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp     = 800,
    heightDp    = 400,
)
@Composable
private fun DashboardScreenLandscapePreview() {
    SynapseTheme {
        LandscapeLayout(
            uiState          = DashboardUiState(isLoading = false),
            listState        = rememberLazyGridState(),
            openSwipedPackId = null,
            onStartStudying  = {},
            onPackTapped     = {},
            onEditPack       = {},
            onExportPack     = {},
            onSwipeOpen      = {},
            onSwipeClose     = {},
            onPendingDelete  = {},
            onSeeAllPacks    = {},
            onAddPack        = {},
        )
    }
}