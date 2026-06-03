package io.synapse.ai.domain.model

data class PackProgressStats(
    val packId: Long,
    val totalCards: Int,
    val dueCards: Int,
    val masteredCards: Int,
    val lastReviewed: Long?
)
