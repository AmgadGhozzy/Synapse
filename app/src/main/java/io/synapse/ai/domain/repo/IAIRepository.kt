package io.synapse.ai.domain.repo

import io.synapse.ai.domain.model.GenerationConfig
import io.synapse.ai.domain.model.GenerationStreamEvent
import kotlinx.coroutines.flow.Flow

interface IAIRepository {

    /**
     * Calls the generate-ai-pack-stream Edge Function.
     *
     * Returns a [Flow] of [GenerationStreamEvent] for real-time UI updates.
     * The backend persists pack + questions to Supabase as they are generated.
     * @param source The content to generate from:
     *                         TEXT    → raw text string
     *                         URL    → web URL string
     *                         YOUTUBE → YouTube URL string
     *                         PDF    → Base64-encoded bytes
     * @param generationConfig Controls count, difficulty, types, thinking, etc.
     */
    fun generatePackStream(
        source: String,
        generationConfig: GenerationConfig,
    ): Flow<GenerationStreamEvent>
}
