package com.venom.synapse.domain.model

/**
 * Sealed class for polymorphic question payloads.
 * Exhaustiveness checking Ensures every type is handled in UI renderers and scoring logic.
 */
sealed class QuestionContent {

    data class McqContent(
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String? = null
    ) : QuestionContent()

    data class TfContent(
        val answer: Boolean,
        val explanation: String? = null
    ) : QuestionContent()

    data class FlashcardContent(
        val front: String,
        val back: String,
        val imageUris: List<String>? = null
    ) : QuestionContent()

    /** Catch-all for unknown or future question types. */
    data class MapContent(
        val data: Map<String, Any> = emptyMap()
    ) : QuestionContent()
}
