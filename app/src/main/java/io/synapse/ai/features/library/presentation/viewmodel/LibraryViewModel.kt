package io.synapse.ai.features.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.R
import io.synapse.ai.core.ui.components.PackDisplayItemBuilder
import io.synapse.ai.core.ui.state.PackDisplayItem
import io.synapse.ai.core.ui.state.ToastType
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.core.ui.state.toDisplayItem
import io.synapse.ai.data.repo.AppConfigProvider
import io.synapse.ai.data.repo.PremiumManager
import io.synapse.ai.domain.model.PackOverviewModel
import io.synapse.ai.domain.repo.IAuthRepository
import io.synapse.ai.domain.repo.IPackRepository
import io.synapse.ai.domain.repo.IProgressRepository
import io.synapse.ai.domain.repo.IQuestionRepository
import io.synapse.ai.domain.repo.ISummaryRepository
import io.synapse.ai.features.library.presentation.state.LibraryFeedItem
import io.synapse.ai.features.library.presentation.state.LibrarySortOption
import io.synapse.ai.features.library.presentation.state.LibraryUiState
import io.synapse.ai.navigation.SynapseScreen
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val packRepo    : IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val progressRepo: IProgressRepository,
    private val authRepo    : IAuthRepository,
    private val summaryRepo : ISummaryRepository,
    private val premiumManager: PremiumManager,
    private val appConfigProvider: AppConfigProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _searchQuery    = MutableStateFlow("")
    private val _activeCategory = MutableStateFlow(LibraryUiState.ALL_CATEGORY)
    private val _sortBy         = MutableStateFlow(LibrarySortOption.RECENT)

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        combine(packRepo.observePackOverviews(), summaryRepo.observeSummaries(), ::Pair),
        _searchQuery,
        _activeCategory,
        _sortBy,
        combine(authRepo.userState, premiumManager.isPro, appConfigProvider.libraryFreePackLimitFlow, ::Triple)
    ) { (packs, summaries), query, category, sort, (userState, isPremium, packLimit) ->

        val isPremiumValue = isPremium
        val totalPackCount  = packs.size
        val isLimitReached  = totalPackCount >= packLimit

        val packItems  = enrichPacks(packs)
        val summaryItems = summaries.map { it.toDisplayItem() }
        val feedItems  = packItems.map { LibraryFeedItem.Pack(it) } + 
                         summaryItems.map { LibraryFeedItem.Summary(it) }
                         
        val categories = buildCategoryList(feedItems)
        val filtered   = feedItems
            .filter { item ->
                (query.isBlank() || item.titleForSort.contains(query, ignoreCase = true)) &&
                        (category == LibraryUiState.ALL_CATEGORY || item.category == category)
            }
            .let { sortFeed(it, sort) }

        LibraryUiState(
            feedItems           = filtered.toImmutableList(),
            searchQuery         = query,
            activeCategory      = category,
            availableCategories = categories,
            sortBy              = sort,
            isPremium           = isPremiumValue,
            isPackLimitReached  = isLimitReached,
            totalPackCount      = totalPackCount,
            isLoading           = false,
        )
    }
        .catch { e ->
            val errorText = e.message?.takeIf { it.isNotBlank() }?.let {
                UiText.Raw(R.string.library_load_failed, it)
            } ?: UiText.Raw(R.string.library_load_failed_generic)
            emit(LibraryUiState(isLoading = false, error = errorText))
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(),
        )


    fun onSearchQueryChanged(query: String)    { _searchQuery.value    = query    }
    fun onCategoryChanged(category: String)    { _activeCategory.value = category }
    fun onSortChanged(sort: LibrarySortOption) { _sortBy.value         = sort     }

    fun onPackTapped(packId: Long) {
        val item = uiState.value.feedItems.find { it.id == packId } as? LibraryFeedItem.Pack
        if (item != null && item.pack.cardsToReview == 0) {
            _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.dashboard_pack_all_caught_up), ToastType.INFO))
            return
        }
        _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Quiz.createRoute(packId)))
    }

    fun onSummaryTapped(summaryId: Long) {
        _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.SummaryViewer.createRoute(summaryId)))
    }


    fun onImportFromPdf() {
        if (uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Premium.route))
        } else {
            _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.AddPdf.createRoute()))
        }
    }

    fun onEditPack(packId: Long) {
        _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Overview.createRoute(packId)))
    }

    fun onExportPack(packId: Long) {
        _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Export.createRoute(packId)))
    }

    fun onDeletePack(packId: Long) {
        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) { packRepo.deletePack(packId) }
                _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.library_pack_deleted), ToastType.SUCCESS))
            }.onFailure {
                val errorText = it.message?.takeIf { msg -> msg.isNotBlank() }?.let { msg ->
                    UiText.Raw(R.string.library_delete_pack_error, msg)
                } ?: UiText.Raw(R.string.library_delete_pack_error_generic)
                _uiEffects.tryEmit(UiEffect.ShowError(text = errorText))
            }
        }
    }


    private suspend fun enrichPacks(packs: List<PackOverviewModel>): List<PackDisplayItem> =
        withContext(ioDispatcher) {
            PackDisplayItemBuilder.buildBatch(
                packs        = packs,
                questionRepo = questionRepo,
                progressRepo = progressRepo,
            )
        }

    private fun sortFeed(
        feedItems: List<LibraryFeedItem>,
        sort : LibrarySortOption,
    ): List<LibraryFeedItem> = when (sort) {
        LibrarySortOption.RECENT       -> feedItems.sortedByDescending { it.id }
        LibrarySortOption.ALPHABETICAL -> feedItems.sortedBy { it.titleForSort }
        LibrarySortOption.MOST_DUE     -> feedItems.sortedByDescending { if (it is LibraryFeedItem.Pack) it.pack.cardsToReview else 0 }
    }

    private fun buildCategoryList(items: List<LibraryFeedItem>): List<String> =
        listOf(LibraryUiState.ALL_CATEGORY) +
                items.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
}
