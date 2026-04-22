package io.synapse.ai.domain.model

enum class SessionMode {
    MIXED,
    MCQ,
    FLASHCARD,
    TRUE_FALSE,
    SMART,
    DEMO;

    companion object {
        fun fromString(value: String): SessionMode =
            entries.find { it.name == value } ?: MIXED
    }
}
