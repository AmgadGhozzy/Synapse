package io.synapse.ai.domain.model

data class PackOverviewModel(
    val id: Long,
    val title: String,
    val category: String?,
    val emoji: String?,
    val color: String?,
    val questionCount: Int,
    val createdAt: Long,
    val packType: String
)
