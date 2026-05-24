package io.synapse.ai.domain.model

enum class SourceType(val wireValue: String) {
    TEXT("text"),
    YOUTUBE("youtube"),
    PDF("pdf"),
    DOC("doc"),
    URL("url");

    companion object {
        fun fromString(value: String): SourceType =
            entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.wireValue.equals(value, ignoreCase = true)
            } ?: TEXT
    }
}
