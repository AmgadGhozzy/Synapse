package io.synapse.ai.data.repo

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.readLine
import io.synapse.ai.BuildConfig
import io.synapse.ai.data.remote.dto.GeneratePackRequest
import io.synapse.ai.data.remote.sse.SseEventParser
import io.synapse.ai.domain.model.GenerationConfig
import io.synapse.ai.domain.model.GenerationError
import io.synapse.ai.domain.model.GenerationStreamEvent
import io.synapse.ai.domain.repo.IAIRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class AIRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient,
) : IAIRepository {

    override fun generatePackStream(
        source: String,
        generationConfig: GenerationConfig,
    ): Flow<GenerationStreamEvent> = callbackFlow {

        try {
            val request = GeneratePackRequest(
                sourceType    = generationConfig.sourceType.wireValue,
                sourceContent = source,
                questionTypes = generationConfig.questionTypes.map { it.name },
                questionCount = generationConfig.maxQuestions,
                language      = generationConfig.language,
                difficulty    = generationConfig.difficulty
                    .lowercase()
                    .takeIf { it in setOf("easy", "medium", "hard") },
                thinking      = generationConfig.thinking.takeIf { it },
                instructions  = generationConfig.instructions?.takeIf { it.isNotBlank() },
            )

            val requestJson = lenientJson.encodeToString(GeneratePackRequest.serializer(), request)

            val session = supabase.auth.currentSessionOrNull()
            val token   = session?.accessToken
            if (token == null) {
                close(GenerationError.AuthenticationFailed("No active session. Please sign in and try again."))
                return@callbackFlow
            }

            val functionUrl = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/functions/v1/generate-ai-pack-stream"
            Log.d(TAG, "→ stream [${generationConfig.sourceType.wireValue}] count=${generationConfig.maxQuestions}")
            Log.d(TAG, "supabaseUrl = '${supabase.supabaseUrl}'")
            Log.d(TAG, "functionUrl = '$functionUrl'")
            // ── preparePost(url) is the correct Ktor call ─────────────────
            httpClient.preparePost(functionUrl) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Accept, "text/event-stream")
                header(HttpHeaders.Authorization, "Bearer $token")
                header("apikey", supabase.supabaseKey)
                timeout {
                    requestTimeoutMillis = 300_000L
                    socketTimeoutMillis  = 300_000L
                }
                setBody(requestJson)
            }.execute { response ->
                val status = response.status.value
                Log.d(TAG, "← HTTP $status")

                if (status != HttpStatusCode.OK.value) {
                    close(mapHttpError(status, response.bodyAsText()))
                    return@execute
                }

                val parser = SseEventParser { event -> trySend(event) }
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readLine() ?: break
                    parser.feedLine(line)
                }
                parser.flush()
            }

        } catch (e: GenerationError) {
            close(e)
        } catch (_: HttpRequestTimeoutException) {
            close(GenerationError.NetworkError("Request timed out. Please check your connection and try again."))
        } catch (_: java.net.UnknownHostException) {
            close(GenerationError.NetworkError("No internet connection. Please check your network and try again."))
        } catch (e: Exception) {
            Log.e(TAG, "generatePackStream failed: ${e.message}", e)
            close(
                if (isOfflineError(e))
                    GenerationError.NetworkError("No internet connection. Please check your network and try again.")
                else
                    GenerationError.Unknown(e.message ?: "An unexpected error occurred", e)
            )
        }

        awaitClose { /* HTTP response already consumed */ }
    }

    private fun mapHttpError(status: Int, body: String): GenerationError {
        val dto = parseErrorBody(body)
        return when (status) {
            400  -> when (dto.code) {
                "CONTENT_TOO_LONG", "URL_TOO_LONG" -> GenerationError.ContentTooLong(dto.error)
                "PDF_TOO_LARGE"                    -> GenerationError.FileTooLarge(dto.error)
                else                               -> GenerationError.InvalidRequest(dto.error)
            }
            401  -> GenerationError.AuthenticationFailed(dto.error)
            413  -> GenerationError.FileTooLarge(dto.error)
            429  -> GenerationError.QuotaExceeded(dto.code ?: "QUOTA_PACK_LIMIT", dto.error)
            in 500..599 -> GenerationError.ServerError(
                if (dto.error.contains("Vertex AI", ignoreCase = true))
                    "AI service temporarily unavailable — please try again"
                else dto.error.ifBlank { "Generation failed — please try again" }
            )
            else -> GenerationError.Unknown(dto.error.ifBlank { "Generation failed (HTTP $status)" })
        }
    }

    private data class ErrorBody(val error: String, val code: String?)

    private fun parseErrorBody(body: String): ErrorBody = try {
        val obj = lenientJson.parseToJsonElement(body) as? JsonObject
        ErrorBody(
            error = (obj?.get("error") as? JsonPrimitive)?.content ?: body.take(200),
            code  = (obj?.get("code")  as? JsonPrimitive)?.content,
        )
    } catch (_: Exception) {
        ErrorBody(error = body.take(200), code = null)
    }

    private fun isOfflineError(e: Throwable) =
        e is HttpRequestTimeoutException ||
                e is java.net.UnknownHostException ||
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("network",    ignoreCase = true) == true ||
                e.message?.contains("timeout",    ignoreCase = true) == true ||
                e.message?.contains("Connection refused", ignoreCase = true) == true

    private companion object {
        const val TAG = "AIRepositoryImpl"
        val lenientJson = Json { ignoreUnknownKeys = true }
    }
}