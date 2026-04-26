package io.synapse.ai.domain.model

data class PackModel(
    val id: Long = 0L,
    val title: String,
    val sourceType: SourceType,
    val createdAt: Long,
    val note: String = "",
    val category: String? = null,
    val emoji: String? = null,
    val color: String? = null,
    val language: String = "en",
    // Supabase packs.id (UUID) — populated after edge function saves to DB.
    val uuid: String? = null,
    // Supabase fields
    val difficulty: String? = null,
    val sourceUrl: String? = null,
    val sourceSummary: String? = null,
    val sourceHash: String? = null,
    val questionCount: Int = 0,
)