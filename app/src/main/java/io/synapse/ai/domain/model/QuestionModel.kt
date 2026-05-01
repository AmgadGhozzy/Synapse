package io.synapse.ai.domain.model

data class QuestionModel(
    val id: Long = 0L,
    val packId: Long,
    val type: QuestionType,
    val questionText: String,
    val content: QuestionContent,
    val createdAt: Long,
    // Supabase questions.id
    val remoteId: String? = null,
    val reference: String? = null,

    // Unified fields
    val moduleTitle: String? = null,
    val level: String? = null,
    val objective: String? = null,
)