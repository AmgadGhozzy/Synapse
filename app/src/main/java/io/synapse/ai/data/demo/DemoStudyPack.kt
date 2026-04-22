package io.synapse.ai.data.demo

import android.content.Context
import io.synapse.ai.R
import io.synapse.ai.domain.model.PackModel
import io.synapse.ai.domain.model.QuestionContent
import io.synapse.ai.domain.model.QuestionModel
import io.synapse.ai.domain.model.QuestionType
import io.synapse.ai.domain.model.SourceType
import java.util.Locale

object DemoStudyPack {
    const val PACK_ID = -1L

    private const val PACK_CREATED_AT = 1L
    private const val QUESTION_MEMORY_CREATED_AT = 2L
    private const val QUESTION_FEEDBACK_CREATED_AT = 3L
    private const val QUESTION_SPACING_CREATED_AT = 4L

    const val QUESTION_MEMORY_ID = -101L
    const val QUESTION_FEEDBACK_ID = -102L
    const val QUESTION_SPACING_ID = -103L

    fun buildPack(context: Context): PackModel = PackModel(
        id = PACK_ID,
        title = context.getString(R.string.session_demo_pack_title),
        sourceType = SourceType.TEXT,
        createdAt = PACK_CREATED_AT,
        language = Locale.getDefault().language.ifBlank { "en" },
    )

    fun buildQuestions(context: Context): List<QuestionModel> = listOf(
        QuestionModel(
            id = QUESTION_MEMORY_ID,
            packId = PACK_ID,
            type = QuestionType.MCQ,
            questionText = context.getString(R.string.session_demo_question_memory),
            content = QuestionContent.McqContent(
                options = listOf(
                    context.getString(R.string.session_demo_option_spaced_reviews),
                    context.getString(R.string.session_demo_option_cram_once),
                    context.getString(R.string.session_demo_option_read_only),
                    context.getString(R.string.session_demo_option_skip_hard),
                ),
                correctIndex = 0,
                explanation = context.getString(R.string.session_demo_explanation_memory),
                hint = context.getString(R.string.session_demo_hint_memory),
            ),
            createdAt = QUESTION_MEMORY_CREATED_AT,
        ),
        QuestionModel(
            id = QUESTION_FEEDBACK_ID,
            packId = PACK_ID,
            type = QuestionType.TRUE_FALSE,
            questionText = context.getString(R.string.session_demo_question_feedback),
            content = QuestionContent.TfContent(
                answer = true,
                explanation = context.getString(R.string.session_demo_explanation_feedback),
                hint = context.getString(R.string.session_demo_hint_feedback),
            ),
            createdAt = QUESTION_FEEDBACK_CREATED_AT,
        ),
        QuestionModel(
            id = QUESTION_SPACING_ID,
            packId = PACK_ID,
            type = QuestionType.FLASHCARD,
            questionText = context.getString(R.string.session_demo_question_spacing),
            content = QuestionContent.FlashcardContent(
                front = context.getString(R.string.session_demo_flashcard_front),
                back = context.getString(R.string.session_demo_flashcard_back),
            ),
            createdAt = QUESTION_SPACING_CREATED_AT,
        ),
    )
}
