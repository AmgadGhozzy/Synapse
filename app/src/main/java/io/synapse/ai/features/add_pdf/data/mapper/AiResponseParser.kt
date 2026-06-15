package io.synapse.ai.features.add_pdf.data.mapper

import io.synapse.ai.domains.study.model.ModuleDiagram


import io.synapse.ai.features.add_pdf.data.remote.ContentJsonDto
import io.synapse.ai.features.add_pdf.data.remote.GeneratePackResponse
import io.synapse.ai.features.add_pdf.data.remote.QuestionDto
import io.synapse.ai.features.add_pdf.domain.model.GeneratedPackResult
import io.synapse.ai.features.add_pdf.domain.model.GenerationMeta
import io.synapse.ai.domains.study.model.PackModel
import io.synapse.ai.domains.study.model.PackModule
import io.synapse.ai.domains.study.model.QuestionContent
import io.synapse.ai.domains.study.model.QuestionModel
import io.synapse.ai.domains.study.model.QuestionType
import io.synapse.ai.features.add_pdf.domain.model.SourceType

object AiResponseParser {

    fun parse(
        response: GeneratePackResponse,
        sourceType: SourceType,
        language: String,
    ): Result<GeneratedPackResult> = runCatching {

        val packDto = response.pack
        val now     = System.currentTimeMillis()

        val pack = PackModel(
            id            = packDto.numericId,
            uuid          = packDto.uuid,
            title         = packDto.title,
            sourceType    = sourceType,
            createdAt     = now,
            note          = packDto.description ?: "",
            category      = packDto.category,
            emoji         = packDto.emoji,
            color         = packDto.color,
            language      = packDto.language.ifBlank { language },
            questionCount = packDto.questionCount,
            difficulty    = packDto.difficulty,
            modules       = packDto.modules?.map { m ->
                PackModule(
                    title       = m.title,
                    description = m.description,
                    order       = m.order,
                    diagram     = m.diagram?.let { d ->
                        ModuleDiagram(
                            type        = d.type,
                            title       = d.title,
                            content     = d.content,
                            explanation = d.explanation,
                        )
                    }
                )
            } ?: emptyList(),
        )

        val questions = response.questions.mapIndexed { index, dto ->
            dto.toDomain(
                packId            = pack.id,
                fallbackCreatedAt = now + index,
            )
        }

        val meta = GenerationMeta(
            tier       = response.meta.tier,
            cached     = response.meta.cached,
            tokensUsed = response.meta.tokensUsed,
        )

        GeneratedPackResult(
            pack      = pack,
            questions = questions,
            meta      = meta,
        )
    }

    private fun QuestionDto.toDomain(
        packId: Long,
        fallbackCreatedAt: Long,
    ): QuestionModel {
        val questionType = resolveType(type)
        return QuestionModel(
            id           = 0L,
            packId       = packId,
            type         = questionType,
            questionText = questionText,
            content      = buildContent(questionType, contentJson, questionText),
            createdAt    = fallbackCreatedAt,
            remoteId     = remoteId,
            reference    = reference,
        )
    }

    private fun buildContent(
        type: QuestionType,
        c: ContentJsonDto,
        questionText: String,
    ): QuestionContent = when (type) {
        QuestionType.MCQ        -> buildMcqContent(c)
        QuestionType.TRUE_FALSE -> buildTfContent(c)
        QuestionType.FLASHCARD  -> buildFlashcardContent(c, questionText)
    }

    // MCQ
    private fun buildMcqContent(c: ContentJsonDto): QuestionContent {
        val validOpts = c.choices?.filter { it.isNotBlank() } ?: emptyList()
        val safeOpts  = validOpts.ifEmpty { listOf("A", "B", "C", "D") }
        val safeIdx   = c.correctIndex?.coerceIn(0, safeOpts.lastIndex) ?: 0
        return QuestionContent.McqContent(
            options      = safeOpts,
            correctIndex = safeIdx,
            explanation  = c.explanation,
            hint         = c.hint,
        )
    }

    // TRUE_FALSE

    private fun buildTfContent(c: ContentJsonDto): QuestionContent =
        QuestionContent.TfContent(
            answer      = c.correctAnswer ?: true,
            explanation = c.explanation,
            hint        = c.hint,
        )

    // FLASHCARD
    private fun buildFlashcardContent(
        c: ContentJsonDto,
        questionText: String,
    ): QuestionContent {
        val front = questionText.takeIf { it.isNotBlank() } ?: "?"
        val back  = c.answer?.takeIf { it.isNotBlank() } ?: "?"
        return QuestionContent.FlashcardContent(
            front   = front,
            back    = back,
            example = c.hint,
        )
    }

    // Type resolver
    private fun resolveType(raw: String): QuestionType {
        val upper = raw.uppercase().trim()
        return when {
            upper.contains("TRUE") || upper == "TF" -> QuestionType.TRUE_FALSE
            upper.contains("FLASH")                 -> QuestionType.FLASHCARD
            else                                    -> QuestionType.MCQ
        }
    }
}
