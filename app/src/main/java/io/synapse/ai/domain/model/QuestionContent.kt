package io.synapse.ai.domain.model

sealed class QuestionContent {

    data class McqContent(
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String? = null,
        val hint: String? = null,
    ) : QuestionContent()

    data class TfContent(
        val answer: Boolean,
        val explanation: String? = null,
        val hint: String? = null,
    ) : QuestionContent()

    data class FlashcardContent(
        val front: String,
        val back: String,
        val example: String? = null,
        val imageUris: List<String>? = null,
    ) : QuestionContent()

//    /**
//     * Fill-in-the-blank question.
//     *
//     * [sentence] contains exactly one "___" placeholder.
//     * [textAnswer] is the canonical correct fill (was "answer" in v1 backend).
//     * [alternatives] are accepted synonyms (may be empty).
//     */
//    data class FillBlankContent(
//        val sentence: String,
//        val textAnswer: String,
//        val alternatives: List<String> = emptyList(),
//        val explanation: String? = null,
//    ) : QuestionContent()
//
//    /**
//     * Sequence-ordering question.
//     *
//     * [items] are presented in shuffled order; [correctOrder] gives the
//     * 0-based indices that restore the correct sequence.
//     */
//    data class OrderingContent(
//        val items: List<String>,
//        val correctOrder: List<Int>,
//        val explanation: String? = null,
//    ) : QuestionContent()

    /** Catch-all for unknown or future question types. */
    data class MapContent(
        val data: Map<String, Any> = emptyMap(),
    ) : QuestionContent()
}
