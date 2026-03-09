package com.venom.synapse.domain.repo

import com.venom.synapse.domain.model.GenerationConfig

interface IAIRepository {
    /**
     * Sends source text to AI and returns structured JSON of generated questions.
     */
    suspend fun generateQuestionsFromText(
        textChunk: String,
        generationConfig: GenerationConfig,
    ): Result<String>
}
