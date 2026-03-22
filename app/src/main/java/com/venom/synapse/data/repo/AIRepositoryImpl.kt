package com.venom.synapse.data.repo

import com.venom.synapse.data.mapper.AiResponseParser
import com.venom.synapse.data.remote.dto.GeneratePackError
import com.venom.synapse.data.remote.dto.GeneratePackRequest
import com.venom.synapse.data.remote.dto.GeneratePackResponse
import com.venom.synapse.domain.model.GeneratedPackResult
import com.venom.synapse.domain.model.GenerationConfig
import com.venom.synapse.domain.model.SourceType
import com.venom.synapse.domain.repo.IAIRepository
import com.venom.synapse.domain.repo.IAuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.util.UUID
import javax.inject.Inject

/**
 * Thin client for the `generatePack` Edge Function.
 * All AI logic is server-side — this class invokes the function,
 * handles HTTP-level errors, and delegates JSON → domain mapping
 * entirely to [AiResponseParser].
 *
 * ─────────────────────────────────────────────────────────────────
 * BUG FIX — question types always sent as ["MIXED"]
 * ─────────────────────────────────────────────────────────────────
 * BEFORE (broken):
 *   val questionTypes = if (generationConfig.isMixed) {
 *       listOf("MIXED")               // ← isMixed defaults to true in GenerationConfig,
 *   } else {                          //   so this branch ALWAYS ran, regardless of what
 *       generationConfig.questionTypes.map { it.name }  //   the user selected.
 *   }
 *
 * AFTER (fixed):
 *   Always send the actual selected types. The server's resolveAllowedTypes()
 *   already handles the "all three selected = mixed distribution" case without
 *   needing a special "MIXED" sentinel. The isMixed field in GenerationConfig
 *   is now unused by this layer and exists only for potential local UI logic.
 */
class AIRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val authRepo: IAuthRepository,
) : IAIRepository {

    override suspend fun generatePack(
        sourceText: String?,
        sourceUrl: String?,
        generationConfig: GenerationConfig,
    ): Result<GeneratedPackResult> = runCatching {

        val idempotencyKey = UUID.randomUUID().toString()

        // FIX: always send the actual selected types — never send "MIXED".
        // The server resolves distribution logic from the type list directly.
        val questionTypes = generationConfig.questionTypes
            .map { it.name }
            .ifEmpty { listOf("MCQ", "TRUE_FALSE", "FLASHCARD") }

        val sourceType = when {
            sourceUrl != null -> SourceType.URL
            else -> SourceType.TEXT
        }

        val request = GeneratePackRequest(
            sourceText    = sourceText,
            sourceUrl     = sourceUrl,
            title         = null,
            note          = generationConfig.hintTone,
            language      = generationConfig.language,
            questionCount = generationConfig.maxQuestions,
            difficulty    = generationConfig.difficulty,
            questionTypes = questionTypes,
            userId        = authRepo.currentUserId(),
            userType      = authRepo.userType(),
            sourceType    = sourceType.name,
        )

        val response = supabase.functions.invoke(
            function = "generate-pack",
            body     = request,
            headers  = Headers.build {
                append(HttpHeaders.ContentType, "application/json")
                append("Idempotency-Key", idempotencyKey)
            },
        )

        when (response.status.value) {
            200, 201 -> {
                val dto = response.body<GeneratePackResponse>()
                AiResponseParser.parse(
                    response   = dto,
                    sourceType = sourceType,
                    language   = generationConfig.language,
                ).getOrElse { parseError ->
                    throw GenerationFailedException(
                        errorCode = "PARSE_ERROR",
                        message   = parseError.message ?: "Failed to parse server response",
                    )
                }
            }
            429 -> {
                val err = response.body<GeneratePackError>()
                throw QuotaExceededException(
                    message      = err.message,
                    currentUsage = err.currentUsage,
                    limit        = err.limit,
                    resetAt      = err.resetAt,
                )
            }
            else -> {
                val err = runCatching { response.body<GeneratePackError>() }.getOrNull()
                throw GenerationFailedException(
                    errorCode = err?.errorCode ?: "UNKNOWN",
                    message   = err?.message ?: "Generation failed (HTTP ${response.status.value})",
                )
            }
        }
    }
}

class QuotaExceededException(
    message: String,
    val currentUsage: Int?,
    val limit: Int?,
    val resetAt: String?,
) : Exception(message)

class GenerationFailedException(
    val errorCode: String,
    override val message: String,
) : Exception(message)