package io.synapse.ai.domain.model

enum class QuestionType {
    MCQ,
    TRUE_FALSE,
    FLASHCARD;

    companion object {
        fun fromString(value: String): QuestionType =
            entries.find { it.name == value.uppercase().trim() } ?: MCQ
    }
}
