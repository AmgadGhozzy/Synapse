package com.venom.synapse.core.ui.state

import androidx.compose.runtime.Immutable
import com.venom.synapse.domain.model.QuestionContent
import com.venom.synapse.domain.model.QuestionModel
import com.venom.synapse.domain.model.QuestionType

// QUESTION UI MODEL
@Immutable
data class QuestionUiModel(
    val id: Long,
    val type: QuestionType,
    val questionText: String,
    val content: QuestionUiContent,
    val explanation: String? = null
)

@Immutable
sealed class QuestionUiContent {
    data class Mcq(
        val options: List<String>,
        val correctIndex: Int
    ) : QuestionUiContent()

    data class TrueFalse(
        val correctAnswer: Boolean
    ) : QuestionUiContent()

    data class Flashcard(
        val front: String,
        val back: String,
        val imageUris: List<String> = emptyList()
    ) : QuestionUiContent()

    data class Unsupported(
        val rawData: Map<String, Any> = emptyMap()
    ) : QuestionUiContent()
}

// MAPPING: Domain → UI

fun QuestionModel.toUiModel(): QuestionUiModel {
    val c = content
    return QuestionUiModel(
        id = id,
        type = type,
        questionText = questionText,
        content = c.toUiContent(),
        explanation = when (c) {
            is QuestionContent.McqContent -> c.explanation
            is QuestionContent.TfContent  -> c.explanation
            else                          -> null
        }
    )
}

private fun QuestionContent.toUiContent(): QuestionUiContent = when (this) {
    is QuestionContent.McqContent       -> QuestionUiContent.Mcq(options, correctIndex)
    is QuestionContent.TfContent        -> QuestionUiContent.TrueFalse(answer)
    is QuestionContent.FlashcardContent -> QuestionUiContent.Flashcard(front, back, imageUris ?: emptyList())
    is QuestionContent.MapContent       -> QuestionUiContent.Unsupported(data)
}
