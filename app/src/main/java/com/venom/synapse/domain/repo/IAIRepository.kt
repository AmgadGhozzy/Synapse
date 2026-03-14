package com.venom.synapse.domain.repo

import com.venom.synapse.domain.model.GeneratedPackResult
import com.venom.synapse.domain.model.GenerationConfig

interface IAIRepository {
    /**
     * Calls the generatePack Edge Function server-side.
     *
     * @param sourceText Direct text input (mutually exclusive with [sourceUrl]).
     * @param sourceUrl  URL to scrape text from (mutually exclusive with [sourceText]).
     * @param generationConfig Parameters controlling question generation.
     * @return The generated pack metadata + preview on success.
     */
    suspend fun generatePack(
        sourceText: String? = null,
        sourceUrl: String? = null,
        generationConfig: GenerationConfig,
    ): Result<GeneratedPackResult>
}
