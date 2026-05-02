package io.synapse.ai.features.marketplace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.ToastType
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.features.marketplace.domain.AcquireResult
import io.synapse.ai.features.marketplace.domain.MarketplaceFilter
import io.synapse.ai.features.marketplace.domain.usecase.AcquirePackUseCase
import io.synapse.ai.features.marketplace.domain.usecase.GetMarketplacePacksUseCase
import io.synapse.ai.features.marketplace.domain.usecase.GetPackDetailUseCase
import io.synapse.ai.features.marketplace.domain.usecase.IncrementViewUseCase
import io.synapse.ai.features.marketplace.presentation.state.MarketplaceEvent
import io.synapse.ai.features.marketplace.presentation.state.MarketplaceUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val getPacksUseCase: GetMarketplacePacksUseCase,
    private val getDetailUseCase: GetPackDetailUseCase,
    private val acquirePackUseCase: AcquirePackUseCase,
    private val incrementViewUseCase: IncrementViewUseCase,
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    // ── Effects (one-shot) ────────────────────────────────────────────────────

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    // ── Internal search debounce flow ─────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    private var loadJob: Job? = null

    init {
        // Debounce search: only re-fetch 350 ms after the user stops typing
        viewModelScope.launch {
            _searchQuery
                .debounce(350)
                .distinctUntilChanged()
                .collect { query ->
                    _uiState.update { it.copy(filter = it.filter.copy(searchQuery = query)) }
                    loadPacks()
                }
        }
        // Initial load
        loadPacks()
    }

    // ── Event handler ─────────────────────────────────────────────────────────

    fun onEvent(event: MarketplaceEvent) {
        when (event) {
            is MarketplaceEvent.LoadPacks            -> loadPacks()
            is MarketplaceEvent.Retry                -> loadPacks()
            is MarketplaceEvent.OnSearchChanged      -> onSearchChanged(event.query)
            is MarketplaceEvent.OnCategoryChanged    -> onFilterChanged(category = event.category)
            is MarketplaceEvent.OnDifficultyChanged  -> onFilterChanged(difficulty = event.difficulty)
            is MarketplaceEvent.ClearFilters         -> clearFilters()
            is MarketplaceEvent.OnPackClicked        -> openPackDetail(event.packId)
            is MarketplaceEvent.OnAcquireClicked     -> acquirePack(event.templateId)
            is MarketplaceEvent.DismissDetail        -> _uiState.update { it.copy(showDetailSheet = false, selectedDetail = null) }
            is MarketplaceEvent.DismissError         -> _uiState.update { it.copy(error = null) }
        }
    }

    // ── Private actions ───────────────────────────────────────────────────────

    private fun loadPacks() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val filter = _uiState.value.filter
                val packs  = getPacksUseCase(filter)
                _uiState.update {
                    it.copy(
                        isLoading     = false,
                        packs         = packs,
                        featuredPacks = packs.filter { p -> p.isFeatured },
                        error         = null,
                    )
                }
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.synapse_marketplace_error_default), ToastType.ERROR))
            }
        }
    }

    private fun onSearchChanged(query: String) {
        _searchQuery.value = query          // debounce collector drives re-fetch
    }

    private fun onFilterChanged(
        category: String?  = _uiState.value.filter.category,
        difficulty: String? = _uiState.value.filter.difficulty,
    ) {
        _uiState.update {
            it.copy(filter = it.filter.copy(category = category, difficulty = difficulty))
        }
        loadPacks()
    }

    private fun clearFilters() {
        _uiState.update { it.copy(filter = MarketplaceFilter()) }
        _searchQuery.value = ""
        loadPacks()
    }

    private fun openPackDetail(packId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(showDetailSheet = true, isDetailLoading = true, selectedDetail = null) }

            // Fire-and-forget view increment (non-blocking)
            launch { incrementViewUseCase(packId) }

            runCatching {
                val detail = getDetailUseCase(packId)
                _uiState.update { it.copy(isDetailLoading = false, selectedDetail = detail) }
            }.onFailure { e ->
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { it.copy(isDetailLoading = false, showDetailSheet = false) }
                _uiEffects.tryEmit(UiEffect.ShowError(UiText.Dynamic(e.message ?: "Failed to load pack details")))
            }
        }
    }

    private fun acquirePack(templateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAcquiring = true) }

            when (val result = acquirePackUseCase(templateId)) {
                is AcquireResult.Success -> {
                    acquirePackUseCase.pullPackAfterAcquire(result.packId)
                    _uiState.update { it.copy(isAcquiring = false, showDetailSheet = false, selectedDetail = null) }
                    _uiEffects.tryEmit(UiEffect.Navigate(result.packId))
                }
                is AcquireResult.AlreadyOwned -> {
                    acquirePackUseCase.pullPackAfterAcquire(result.packId)
                    _uiState.update { it.copy(isAcquiring = false, showDetailSheet = false, selectedDetail = null) }
                    _uiEffects.tryEmit(UiEffect.Navigate(result.packId))
                }
                is AcquireResult.ProRequired -> {
                    _uiState.update { it.copy(isAcquiring = false) }
                    _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.synapse_marketplace_unlock_pro)))
                }
                is AcquireResult.Failure -> {
                    _uiState.update { it.copy(isAcquiring = false) }
                    _uiEffects.tryEmit(UiEffect.ShowError(UiText.Dynamic(result.message)))
                }
            }
        }
    }
}
