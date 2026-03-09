package com.venom.synapse.domain.model

data class QuestionModel(
    val id: Long = 0L,
    val packId: Long,
    val type: QuestionType,
    val questionText: String,
    /** Parsed from contentJson — never raw JSON at domain level. */
    val content: QuestionContent,
    val createdAt: Long,
    val sourcePage: Int? = null,
    val paragraphIndex: Int? = null
)
