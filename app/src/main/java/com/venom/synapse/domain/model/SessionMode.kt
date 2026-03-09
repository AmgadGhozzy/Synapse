package com.venom.synapse.domain.model

enum class SessionMode {
    MIXED,
    MCQ,
    FLASHCARD,
    TRUE_FALSE,
    SMART;

    companion object {
        fun fromString(value: String): SessionMode =
            entries.find { it.name == value } ?: MIXED
    }
}
