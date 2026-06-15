package io.synapse.ai.features.add_pdf.domain.model

import io.synapse.ai.domains.study.model.*

/**
 * Wraps the outcome of a single `generatePack` call so that both the
 * persisted pack metadata and the full question list travel together
 * through the domain → presentation boundary.
 *
 * WHY a dedicated wrapper instead of Pair<PackModel, List<QuestionModel>>:
 *   • Names the concept explicitly — easier to read at call sites.
 *   • Decouples the ViewModel from knowing which tuple index means what.
 *   • Carries server-provided [meta] (tier, caching, token usage).
 */
data class GeneratedPackResult(
    val pack: PackModel,
    val questions: List<QuestionModel>,
    val meta: GenerationMeta = GenerationMeta(),
)

/**
 * Server-provided metadata about the generation run.
 * Populated from the `meta` block of the Edge Function response.
 */
data class GenerationMeta(
    /** User's resolved tier: "anonymous", "registered", or "gold". */
    val tier: String = "anonymous",
    /** True when the server returned a cached result instead of calling the AI. */
    val cached: Boolean = false,
    /** Total tokens consumed by the AI model (0 on cache hits). */
    val tokensUsed: Int = 0,
)
