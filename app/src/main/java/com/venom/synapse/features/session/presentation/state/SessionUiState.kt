package com.venom.synapse.features.session.presentation.state

import androidx.compose.runtime.Immutable
import com.venom.synapse.domain.model.QuestionContent
import com.venom.synapse.domain.model.QuestionModel
import com.venom.synapse.domain.model.QuestionType

/**
 * ═══════════════════════════════════════════════════════════════════
 * SESSION UI MODELS — Layer 4 (ViewModel → Compose)
 * ═══════════════════════════════════════════════════════════════════
 */

@Immutable
data class QuestionUiModel(
    val id          : Long,
    val type        : QuestionType,
    val questionText: String,
    val content     : QuestionUiContent,
    val hint        : String? = null,
    val explanation : String? = null,
)

@Immutable
sealed class QuestionUiContent {

    data class Mcq(
        val options     : List<String>,
        val correctIndex: Int,
    ) : QuestionUiContent()

    data class TrueFalse(
        val correctAnswer: Boolean,
    ) : QuestionUiContent()

    data class Flashcard(
        val front    : String,
        val back     : String,
        val imageUris: List<String> = emptyList(),
    ) : QuestionUiContent()

    data class Unsupported(
        val rawData: Map<String, Any> = emptyMap(),
    ) : QuestionUiContent()
}

@Immutable
data class SessionUiState(
    val sessionId        : Long             = 0L,
    val packTitle        : String           = "",
    val mode             : String           = "",
    val currentQuestion  : QuestionUiModel? = null,
    val questionIndex    : Int              = 0,
    val totalQuestions   : Int              = 0,
    val answeredCount    : Int              = 0,
    val correctCount     : Int              = 0,
    val progressPercent  : Float            = 0f,
    val accuracy         : Float            = 0f,
    val isLoading        : Boolean          = false,
    val isInputEnabled   : Boolean          = true,
    val showLeechAlert   : Boolean          = false,
    val leechQuestionId  : Long?            = null,
    val lastAnswerCorrect: Boolean?         = null,
    val lastExplanation  : String?          = null,
    val isSessionFinished: Boolean          = false,
    val error            : String?          = null,
)

@Immutable
data class SessionSummaryUiState(
    val sessionId            : Long         = 0L,
    val packTitle            : String       = "",
    val totalQuestions       : Int          = 0,
    val answeredCount        : Int          = 0,
    val correctCount         : Int          = 0,
    val accuracy             : Float        = 0f,
    val durationFormatted    : String       = "",
    val leechCount           : Int          = 0,
    // Populated from engine when it exposes leech question texts.
    // Falls back to empty list — UI renders generic placeholders.
    val leechQuestionTexts   : List<String> = emptyList(),
    val newQuestionCount     : Int          = 0,
    val reviewedQuestionCount: Int          = 0,
)

// ══════════════════════════════════════════════════════════════════
// MAPPING: Domain → UI
// ══════════════════════════════════════════════════════════════════

fun QuestionModel.toUiModel(): QuestionUiModel {
    val c = content
    return QuestionUiModel(
        id          = id,
        type        = type,
        questionText= questionText,
        content     = c.toUiContent(),
        hint        = when (c) {
            is QuestionContent.McqContent -> c.hint
            is QuestionContent.TfContent  -> c.hint
            else                          -> null
        },
        explanation = when (c) {
            is QuestionContent.McqContent -> c.explanation
            is QuestionContent.TfContent  -> c.explanation
            else                          -> null
        },
    )
}

private fun QuestionContent.toUiContent(): QuestionUiContent = when (this) {
    is QuestionContent.McqContent       -> QuestionUiContent.Mcq(options, correctIndex)
    is QuestionContent.TfContent        -> QuestionUiContent.TrueFalse(answer)
    is QuestionContent.FlashcardContent -> QuestionUiContent.Flashcard(front, back, imageUris ?: emptyList())
    is QuestionContent.MapContent       -> QuestionUiContent.Unsupported(data)
}