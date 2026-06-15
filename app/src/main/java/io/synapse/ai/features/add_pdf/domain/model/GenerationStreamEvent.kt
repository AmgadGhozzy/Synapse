package io.synapse.ai.features.add_pdf.domain.model

import io.synapse.ai.domains.study.model.*

/**
 * Sealed hierarchy of SSE events emitted by generate-ai-pack-stream.
 *
 * Event sequence:
 *   PackMeta → Progress* → Question* → Done
 *   (Error can appear at any point)
 */
sealed interface GenerationStreamEvent {

    /**
     * First event. Carries pack metadata and the number of distinct
     * concepts the model found in the source content.
     */
    data class PackMeta(
        val packId:        String,
        val title:         String,
        val description:   String?,
        val emoji:         String?,
        val color:         String?,
        val difficulty:    String?,
        val category:      String?,
        val language:      String?,
        val sourceSummary: String?,
        val tags:          List<String>,
        val estimatedMinutes: Int?,
        val expectedCount: Int,
        val conceptsFound: Int,
    ) : GenerationStreamEvent

    /**
     * Stage 2: curriculum structure with modules and optional diagrams.
     * Arrives after PackMeta, before questions.
     */
    data class Curriculum(
        val packId:  String,
        val modules: List<CurriculumModule>,
    ) : GenerationStreamEvent

    /**
     * Live progress update. Arrives after PackMeta and between Question events.
     * [percent] is 0–100. [questionsDone] / [questionsTotal] mirror the
     * running question counter for UI stat chips.
     */
    data class Progress(
        val percent:        Int,
        val stage:          String,
        val message:        String,
        val conceptsFound:  Int,
        val questionsDone:  Int,
        val questionsTotal: Int,
        val emoji:          String? = null,
        val color:          String? = null,
    ) : GenerationStreamEvent

    /**
     * A fully validated, server-persisted question.
     * Arrives immediately as the model finishes each question.
     * [contentJson] is a raw Map — use [toDomainContent] to convert.
     */
    data class Question(
        val remoteId:     String,
        val packId:       String,
        val index:        Int,
        val type:         String,
        val questionText: String,
        val contentJson:  Map<String, Any?>,
        val reference:    String?,
        val sortOrder:    Int,
        val moduleTitle:  String?,
        val level:        String?,
        val objective:    String?,
    ) : GenerationStreamEvent

    /**
     * Stream ended. [status] is "complete" or "partial".
     * Always arrives last — use to finalize UI and Room pack row.
     */
    data class Done(
        val packId:        String?,
        val total:         Int,
        val expected:      Int,
        val conceptsFound: Int,
        val status:        String,
        val cached:        Boolean,
        val tier:          String,
        val tokensUsed:    Int,
        val latencyMs:     Long,
    ) : GenerationStreamEvent

    /**
     * Server-side or stream error. [recoverable] = true means the
     * partial result is usable; false means discard and retry.
     */
    data class Error(
        val message:      String,
        val code:         String,
        val recoverable:  Boolean,
        val partialCount: Int,
    ) : GenerationStreamEvent
}

/**
 * A module in the curriculum structure received from the AI pipeline.
 */
data class CurriculumModule(
    val title:       String,
    val description: String?,
    val order:       Int,
    val diagram:     CurriculumDiagram?,
)

data class CurriculumDiagram(
    val type:    String,
    val title:   String?,
    val content: String,
    val explanation: String? = null,
)

// ── Domain helpers ────────────────────────────────────────────────────────────

/**
 * Converts raw [contentJson] map to a typed [QuestionContent].
 * Safe against null/missing fields — always returns a valid object.
 */
fun GenerationStreamEvent.Question.toDomainContent(): QuestionContent {
    val c = contentJson
    return when {
        type.contains("TRUE", ignoreCase = true) || type == "TF" -> {
            QuestionContent.TfContent(
                answer      = c["correct_answer"] as? Boolean ?: true,
                explanation = c["explanation"]?.toString(),
                hint        = c["hint"]?.toString(),
            )
        }
        type.contains("FLASH", ignoreCase = true) -> {
            QuestionContent.FlashcardContent(
                front   = questionText.takeIf { it.isNotBlank() } ?: "?",
                back    = c["answer"]?.toString()?.takeIf { it.isNotBlank() } ?: "?",
                example = c["hint"]?.toString(),
            )
        }
        else -> { // MCQ
            val choices = (c["choices"] as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?.filter { it.isNotBlank() }
                ?.ifEmpty { listOf("A", "B", "C", "D") }
                ?: listOf("A", "B", "C", "D")
            val correctIdx = (c["correct_index"] as? Number)
                ?.toInt()
                ?.coerceIn(0, choices.lastIndex) ?: 0
            QuestionContent.McqContent(
                options      = choices,
                correctIndex = correctIdx,
                explanation  = c["explanation"]?.toString(),
                hint         = c["hint"]?.toString(),
            )
        }
    }
}

/** Converts a [GenerationStreamEvent.Question] to a [QuestionModel] for Room. */
fun GenerationStreamEvent.Question.toQuestionModel(localPackId: Long): QuestionModel =
    QuestionModel(
        id           = 0L,
        packId       = localPackId,
        type         = when {
            type.contains("TRUE", ignoreCase = true) || type == "TF" -> QuestionType.TRUE_FALSE
            type.contains("FLASH", ignoreCase = true)                 -> QuestionType.FLASHCARD
            else                                                       -> QuestionType.MCQ
        },
        questionText = questionText,
        content      = toDomainContent(),
        createdAt    = System.currentTimeMillis(),
        remoteId     = remoteId,
        reference    = reference,
        moduleTitle  = moduleTitle,
        level        = level,
        objective    = objective,
    )

/** Converts a [CurriculumModule] to a [PackModule] for domain use. */
fun CurriculumModule.toPackModule(): PackModule =
    PackModule(
        title       = title,
        description = description,
        order       = order,
        diagram     = diagram?.let {
            ModuleDiagram(
                type        = it.type,
                title       = it.title,
                content     = it.content,
                explanation = it.explanation,
            )
        },
    )