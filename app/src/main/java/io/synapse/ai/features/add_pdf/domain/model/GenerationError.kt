package io.synapse.ai.features.add_pdf.domain.model

import io.synapse.ai.domains.study.model.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * GenerationError — typed error hierarchy for AI pack generation.
 *
 * Replaces the old GenerationFailedException / DailyLimitException
 * pair with a sealed class that maps 1:1 to the backend error codes
 * returned by the generate-ai-pack Edge Function (v4.0+).
 *
 * Usage in ViewModel:
 *   result.onFailure { error ->
 *       when (error) {
 *           is GenerationError.QuotaExceeded -> showPaywall()
 *           is GenerationError.FileTooLarge  -> showPaywall()
 *           ...
 *       }
 *   }
 * ═══════════════════════════════════════════════════════════════════
 */
sealed class GenerationError(message: String) : Exception(message) {

    /** HTTP 429 — user hit their daily pack or token limit. */
    data class DailyLimitExceeded(
        override val message: String,
    ) : GenerationError(message)

    /** HTTP 429 — user hit their monthly pack limit. */
    data class MonthlyLimitExceeded(
        override val message: String,
    ) : GenerationError(message)

    /** HTTP 400 `CONTENT_TOO_LONG` or `URL_TOO_LONG` — source text exceeds tier limit. */
    data class ContentTooLong(
        override val message: String,
    ) : GenerationError(message)

    /** HTTP 400 `PDF_TOO_LARGE` — PDF file exceeds tier limit. */
    data class FileTooLarge(
        override val message: String,
    ) : GenerationError(message)

    /** HTTP 401 — JWT missing, invalid, or expired. */
    data class AuthenticationFailed(
        override val message: String,
    ) : GenerationError(message)

    /** HTTP 400 — validation error, invalid JSON, or malformed request. */
    data class InvalidRequest(
        override val message: String,
    ) : GenerationError(message)

    /** HTTP 5xx — server-side / Vertex AI infrastructure error. */
    data class ServerError(
        override val message: String,
    ) : GenerationError(message)

    /** Network error - no internet connection or timeout. */
    data class NetworkError(
        override val message: String,
    ) : GenerationError(message)

    /** Catch-all for unexpected or unclassified errors. */
    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null,
    ) : GenerationError(message)
}
