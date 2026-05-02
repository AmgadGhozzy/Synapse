package io.synapse.ai.features.marketplace.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.features.marketplace.domain.MarketplaceFilter
import io.synapse.ai.features.marketplace.domain.MarketplacePack
import io.synapse.ai.features.marketplace.domain.MarketplacePackDetail

// ── UI State ──────────────────────────────────────────────────────────────────

@Immutable
data class MarketplaceUiState(
    val isLoading: Boolean = true,
    val packs: List<MarketplacePack> = emptyList(),
    val featuredPacks: List<MarketplacePack> = emptyList(),
    val filter: MarketplaceFilter = MarketplaceFilter(),
    val selectedDetail: MarketplacePackDetail? = null,
    val showDetailSheet: Boolean = false,
    val isDetailLoading: Boolean = false,
    val isAcquiring: Boolean = false,
    val error: String? = null,
) {
    /** All distinct categories present in the loaded pack list. */
    val availableCategories: List<String>
        get() = packs.mapNotNull { it.category }.distinct().sorted()
}

// ── Events (UI → ViewModel) ───────────────────────────────────────────────────

sealed interface MarketplaceEvent {
    data object LoadPacks                                     : MarketplaceEvent
    data class  OnSearchChanged(val query: String)           : MarketplaceEvent
    data class  OnCategoryChanged(val category: String?)     : MarketplaceEvent
    data class  OnDifficultyChanged(val difficulty: String?) : MarketplaceEvent
    data object ClearFilters                                  : MarketplaceEvent
    data class  OnPackClicked(val packId: String)            : MarketplaceEvent
    data class  OnAcquireClicked(val templateId: String)     : MarketplaceEvent
    data object DismissDetail                                 : MarketplaceEvent
    data object DismissError                                  : MarketplaceEvent
    data object Retry                                         : MarketplaceEvent
}

