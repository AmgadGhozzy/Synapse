package io.synapse.ai.features.library.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.domains.study.model.PackDisplayItem
import io.synapse.ai.features.summary.presentation.state.SummaryDisplayItem

@Immutable
sealed interface LibraryFeedItem {
    val id: Long
    val titleForSort: String
    val category: String

    data class Pack(val pack: PackDisplayItem) : LibraryFeedItem {
        override val id: Long = pack.id
        override val titleForSort: String = pack.title.lowercase()
        override val category: String = pack.category
    }

    data class Summary(val summary: SummaryDisplayItem) : LibraryFeedItem {
        override val id: Long = summary.id
        override val titleForSort: String = summary.title.lowercase()
        override val category: String = summary.category
    }
}
