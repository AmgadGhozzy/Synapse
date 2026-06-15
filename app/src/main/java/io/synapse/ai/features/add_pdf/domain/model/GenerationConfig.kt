package io.synapse.ai.features.add_pdf.domain.model

import io.synapse.ai.domains.study.model.*

data class GenerationConfig(

    /** Which source format the AI should interpret. */
    val sourceType: SourceType = SourceType.TEXT,

    /** Which question types to include in the generated pack. */
    val questionTypes: List<QuestionType> = QuestionType.entries,

    /** Total number of questions to generate (3–30). */
    val maxQuestions: Int = 10,

    /** Bloom's difficulty tier: "easy" | "medium" | "hard". */
    val difficulty: String = "medium",

    /** BCP-47 language code for all generated content, e.g. "en", "ar". */
    val language: String = "en",

    val thinking: Boolean = false,

    /** Optional custom instructions/focus notes provided by the user. */
    val instructions: String? = null,

    /** Client-reported PDF page count — sent to server for validation. */
    val pageCount: Int? = null,
)