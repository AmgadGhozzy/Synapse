package io.synapse.ai.features.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.R
import io.synapse.ai.core.ui.components.PackDisplayItemBuilder
import io.synapse.ai.core.ui.state.PackDisplayItem
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.data.repo.AppConfigProvider
import io.synapse.ai.data.repo.EntitlementManager
import io.synapse.ai.domain.model.PackModel
import io.synapse.ai.domain.repo.IAuthRepository
import io.synapse.ai.domain.repo.IPackRepository
import io.synapse.ai.domain.repo.IProgressRepository
import io.synapse.ai.domain.repo.IQuestionRepository
import io.synapse.ai.features.library.presentation.state.LibrarySortOption
import io.synapse.ai.features.library.presentation.state.LibraryUiState
import io.synapse.ai.navigation.SynapseScreen
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
    private val entitlementManager: EntitlementManager,
    private val appConfigProvider: AppConfigProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _searchQuery    = MutableStateFlow("")
    private val _activeCategory = MutableStateFlow(LibraryUiState.ALL_CATEGORY)
    private val _sortBy         = MutableStateFlow(LibrarySortOption.RECENT)

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        packRepo.observeAllPacks(),
        _searchQuery,
        _activeCategory,
        _sortBy,
        combine(authRepo.userState, entitlementManager.entitlement, ::Pair)
    ) { packs, query, category, sort, (userState, entitlement) ->

        val isPremium       = entitlement.isAccessGranted
        val packLimit       = appConfigProvider.libraryFreePackLimit
        val totalPackCount  = packs.size
        val isLimitReached  = totalPackCount >= packLimit

        val items      = enrichPacks(packs)
        val categories = buildCategoryList(items)
        val filtered   = items
            .filter { pack ->
                (query.isBlank() || pack.title.contains(query, ignoreCase = true)) &&
                        (category == LibraryUiState.ALL_CATEGORY || pack.category == category)
            }
            .let { sortPacks(it, sort) }

        LibraryUiState(
            packs               = filtered,
            searchQuery         = query,
            activeCategory      = category,
            availableCategories = categories,
            sortBy              = sort,
            isPremium           = isPremium,
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

    // ── User events ──────────────────────────────────────────────
    fun onSearchQueryChanged(query: String)    { _searchQuery.value    = query    }
    fun onCategoryChanged(category: String)    { _activeCategory.value = category }
    fun onSortChanged(sort: LibrarySortOption) { _sortBy.value         = sort     }

    fun onPackTapped(packId: Long) {
        val pack = uiState.value.packs.find { it.id == packId }
        if (pack != null && pack.cardsToReview == 0) {
            _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.dashboard_pack_all_caught_up)))
            return
        }
        _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Quiz.createRoute(packId)))
    }

    /**
     * Tapping the AddPackCell.
     * Free-tier users who have reached the limit are routed to the
     * premium screen.  All others go to the Add-PDF wizard.
     */
    fun onImportFromPdf() {
        if (uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Premium.route))
        } else {
            _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.AddPdf.createRoute()))
        }
    }

    fun onEditPack(packId: Long) {
        _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.coming_soon)))
    }

    fun onExportPack(packId: Long) {
        _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.coming_soon)))
    }

    fun onDeletePack(packId: Long) {
        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) { packRepo.deletePack(packId) }
                _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.library_pack_deleted)))
            }.onFailure {
                val errorText = it.message?.takeIf { msg -> msg.isNotBlank() }?.let { msg ->
                    UiText.Raw(R.string.library_delete_pack_error, msg)
                } ?: UiText.Raw(R.string.library_delete_pack_error_generic)
                _uiEffects.tryEmit(UiEffect.ShowError(text = errorText))
            }
        }
    }

    // ── Private helpers ──────────────────────────────────────────
    private suspend fun enrichPacks(packs: List<PackModel>): List<PackDisplayItem> =
        withContext(ioDispatcher) {
            packs.map { pack ->
                PackDisplayItemBuilder.build(
                    pack         = pack,
                    questionRepo = questionRepo,
                    progressRepo = progressRepo,
                )
            }
        }

    private fun sortPacks(
        packs: List<PackDisplayItem>,
        sort : LibrarySortOption,
    ): List<PackDisplayItem> = when (sort) {
        LibrarySortOption.RECENT       -> packs.sortedByDescending { it.id }
        LibrarySortOption.ALPHABETICAL -> packs.sortedBy { it.title.lowercase() }
        LibrarySortOption.MOST_DUE     -> packs.sortedByDescending { it.cardsToReview }
    }

    private fun buildCategoryList(items: List<PackDisplayItem>): List<String> =
        listOf(LibraryUiState.ALL_CATEGORY) +
                items.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
}
