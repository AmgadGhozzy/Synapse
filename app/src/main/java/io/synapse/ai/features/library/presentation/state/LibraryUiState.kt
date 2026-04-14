package io.synapse.ai.features.library.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.core.ui.state.PackDisplayItem
import io.synapse.ai.core.ui.state.UiText

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

    val isPremium           : Boolean               = false,
    val isPackLimitReached  : Boolean               = false,
    val totalPackCount      : Int                   = 0,

    val isLoading           : Boolean               = true,
    val error               : UiText?               = null,
) {
    companion object {
        const val ALL_CATEGORY  = "All"
    }
}
