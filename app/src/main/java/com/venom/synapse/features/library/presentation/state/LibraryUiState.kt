package com.venom.synapse.features.library.presentation.state

import androidx.compose.runtime.Immutable
import com.venom.synapse.core.ui.state.PackDisplayItem
import com.venom.synapse.features.library.presentation.state.LibraryUiState.Companion.FREE_PACK_LIMIT

enum class LibrarySortOption {
    RECENT,
    ALPHABETICAL,
    MOST_DUE,
}

@Immutable
data class LibraryUiState(
    val packs               : List<PackDisplayItem> = emptyList(),
    val searchQuery         : String                = "",
    val activeCategory      : String                = ALL_CATEGORY,
    val availableCategories : List<String>          = listOf(ALL_CATEGORY),
    val sortBy              : LibrarySortOption     = LibrarySortOption.RECENT,

    // ── Premium / pack-limit gating ──────────────────────────────
    /** True when the signed-in user holds an active premium subscription. */
    val isPremium           : Boolean               = false,
    /**
     * True when a free-tier user owns ≥ [FREE_PACK_LIMIT] packs.
     * Drives the locked visual on [AddPackCell] and routes the tap
     * to the premium screen instead of the Add-PDF wizard.
     */
    val isPackLimitReached  : Boolean               = false,
    /** Total packs owned — used in the AddPackCell locked subtitle. */
    val totalPackCount      : Int                   = 0,

    val isLoading           : Boolean               = true,
    val error               : String?               = null,
) {
    companion object {
        const val ALL_CATEGORY  = "All"
        const val FREE_PACK_LIMIT = 5
    }
}