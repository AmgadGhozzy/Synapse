package com.venom.synapse.domain.model

/**
 * Configuration passed to IAIRepository to control question generation.
 *
 * @property questionTypes Which formats to generate.
 * @property maxQuestions Target number of questions.
 * @property difficulty "easy", "mixed", or "hard".
 * @property language BCP-47 code for output language.
 * @property tone "neutral", "conversational", or "academic".
 * @property includeImages If true, requests Image-based questions.
 * @property hintTone Optional user hint.
 */
data class GenerationConfig(
    val questionTypes: List<QuestionType> = QuestionType.entries,
    val maxQuestions: Int = 20,
    val difficulty: String = "mixed",
    val language: String = "en",
    val tone: String = "neutral",
    val includeImages: Boolean = false,
    val hintTone: String? = null
)
