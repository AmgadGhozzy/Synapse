package com.venom.synapse.domain.model

enum class SourceType {
    PDF,
    TEXT,
    YOUTUBE;

    companion object {
        fun fromString(value: String): SourceType =
            entries.find { it.name == value } ?: TEXT
    }
}
