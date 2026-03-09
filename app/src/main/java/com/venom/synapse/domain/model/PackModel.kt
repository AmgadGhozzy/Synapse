package com.venom.synapse.domain.model

data class PackModel(
    val id: Long = 0L,
    val title: String,
    val sourceType: SourceType,
    val createdAt: Long,
    val note: String = "",
    /** User-defined category e.g. "Biology". Null if unset. */
    val category: String? = null,
    /** Emoji icon for the pack card e.g. "📚". Null = theme default. */
    val emoji: String? = null,
    /** Hex color e.g. "#FF5733". Null = theme default. */
    val color: String? = null,
    /** BCP-47 language code. Default "en". */
    val language: String = "en"
)
