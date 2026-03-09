package com.venom.synapse.domain.model

enum class QuestionType {
    MCQ,
    TRUE_FALSE,
    FLASHCARD,
    IMAGE;

    companion object {
        fun fromString(value: String): QuestionType =
            entries.find { it.name == value } ?: MCQ
    }
}
