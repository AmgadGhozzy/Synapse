package io.synapse.ai.features.add_pdf.data.sse

import android.util.Log
import io.synapse.ai.features.add_pdf.domain.model.CurriculumDiagram
import io.synapse.ai.features.add_pdf.domain.model.CurriculumModule
import io.synapse.ai.features.add_pdf.domain.model.GenerationStreamEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

/**
 * Lightweight SSE line parser for generate-ai-pack-stream events.
 *
 * Handles the SSE wire format:
 *   event: <name>\n
 *   data: <json>\n
 *   \n
 *
 * Call [feedLine] for each line received from the HTTP response body.
 * When a complete event is parsed, [onEvent] is invoked.
 */
internal class SseEventParser(
    private val onEvent: (GenerationStreamEvent) -> Unit,
) {
    private var currentEvent: String? = null
    private var currentData = StringBuilder()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Feed a single line from the SSE stream.
     * Lines should NOT include the trailing \n.
     */
    fun feedLine(line: String) {
        when {
            line.startsWith("event:") -> {
                currentEvent = line.removePrefix("event:").trim()
            }

            line.startsWith("data:") -> {
                currentData.append(line.removePrefix("data:").trim())
            }

            line.isBlank() -> {
                // Empty line = end of event
                dispatchEvent()
                currentEvent = null
                currentData.clear()
            }
        }
    }

    /** Flush any buffered event (e.g. end of stream without trailing blank line). */
    fun flush() {
        if (currentEvent != null && currentData.isNotEmpty()) {
            dispatchEvent()
        }
        currentEvent = null
        currentData.clear()
    }

    private fun dispatchEvent() {
        val eventName = currentEvent ?: return
        val dataStr = currentData.toString().trim()
        if (dataStr.isEmpty()) return

        try {
            val obj = json.parseToJsonElement(dataStr).jsonObject
            val event = parseEvent(eventName, obj) ?: return
            onEvent(event)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse SSE event '$eventName': ${e.message}")
        }
    }

    private fun parseEvent(name: String, obj: JsonObject): GenerationStreamEvent? {
        return when (name) {
            "pack_meta" -> GenerationStreamEvent.PackMeta(
                packId = obj.str("pack_id") ?: "",
                title = obj.str("title") ?: "",
                description = obj.str("description"),
                emoji = obj.str("emoji"),
                color = obj.str("color"),
                difficulty = obj.str("difficulty"),
                category = obj.str("category"),
                language = obj.str("language"),
                sourceSummary = obj.str("source_summary"),
                tags = obj.strList("tags"),
                estimatedMinutes = obj.int("estimated_minutes"),
                expectedCount = obj.int("expected_count") ?: 0,
                conceptsFound = obj.int("concepts_found") ?: 0,
            )

            "pack_curriculum" -> {
                val packId = obj.str("pack_id") ?: ""
                val modulesArr = obj["modules"]?.jsonArray ?: return null
                val modules = modulesArr.mapNotNull { el ->
                    try {
                        val m = el.jsonObject
                        val diagram = m["diagram"]?.jsonObject?.let { d ->
                            CurriculumDiagram(
                                type        = d.str("type") ?: "mermaid",
                                title       = d.str("title"),
                                content     = d.str("content") ?: "",
                                explanation = d.str("explanation"),
                            )
                        }
                        CurriculumModule(
                            title       = m.str("title") ?: "",
                            description = m.str("description"),
                            order       = m.int("order") ?: 0,
                            diagram     = diagram,
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Skipping malformed module: ${e.message}")
                        null
                    }
                }
                GenerationStreamEvent.Curriculum(packId = packId, modules = modules)
            }

            "question" -> GenerationStreamEvent.Question(
                remoteId = obj.str("id") ?: "",
                packId = obj.str("pack_id") ?: "",
                index = obj.int("index") ?: 0,
                type = obj.str("type") ?: "MCQ",
                questionText = obj.str("question_text") ?: "",
                contentJson = obj.get("content_json")?.toAnyMap() ?: emptyMap(),
                reference = obj.str("reference"),
                sortOrder = obj.int("sort_order") ?: 0,
                moduleTitle = obj.str("module_title"),
                level = obj.str("level"),
                objective = obj.str("objective"),
            )

            "progress" -> GenerationStreamEvent.Progress(
                percent = obj.int("percent") ?: 0,
                stage = obj.str("stage") ?: "",
                message = obj.str("message") ?: "",
                conceptsFound = obj.int("concepts_found") ?: 0,
                questionsDone = obj.int("questions_done") ?: 0,
                questionsTotal = obj.int("questions_total") ?: 0,
            )

            "done" -> GenerationStreamEvent.Done(
                packId = obj.str("pack_id"),
                total = obj.int("total") ?: 0,
                expected = obj.int("expected") ?: 0,
                conceptsFound = obj.int("concepts_found") ?: 0,
                status = obj.str("status") ?: "complete",
                cached = obj.bool("cached") ?: false,
                tier = obj.str("tier") ?: "anonymous",
                tokensUsed = obj.int("tokens_used") ?: 0,
                latencyMs = obj.long("latency_ms") ?: 0L,
            )

            "error" -> GenerationStreamEvent.Error(
                message = obj.str("message") ?: "Unknown error",
                code = obj.str("code") ?: "UNKNOWN",
                recoverable = obj.bool("recoverable") ?: false,
                partialCount = obj.int("partial_count") ?: 0,
            )

            "heartbeat" -> null  // silently ignore heartbeats

            else -> {
                Log.d(TAG, "Unknown SSE event: $name")
                null
            }
        }
    }

    // ── JSON helpers ──────────────────────────────────────────────────

    private fun JsonObject.str(key: String): String? =
        get(key)?.jsonPrimitive?.takeIf { it.isString }?.content

    private fun JsonObject.int(key: String): Int? =
        get(key)?.jsonPrimitive?.intOrNull

    private fun JsonObject.long(key: String): Long? =
        get(key)?.jsonPrimitive?.longOrNull

    private fun JsonObject.bool(key: String): Boolean? =
        get(key)?.jsonPrimitive?.booleanOrNull

    private fun JsonObject.strList(key: String): List<String> {
        val arr = get(key)?.jsonArray ?: return emptyList()
        return arr.mapNotNull { it.jsonPrimitive.takeIf { p -> p.isString }?.content }
    }

    @Suppress("UNCHECKED_CAST")
    private fun JsonElement.toAnyMap(): Map<String, Any?> {
        if (this !is JsonObject) return emptyMap()
        return entries.associate { (k, v) -> k to v.toAny() }
    }

    private fun JsonElement.toAny(): Any? = when {
        this is JsonObject -> toAnyMap()
        this is kotlinx.serialization.json.JsonArray -> map { it.toAny() }
        jsonPrimitive.isString -> jsonPrimitive.content
        jsonPrimitive.booleanOrNull != null -> jsonPrimitive.boolean
        jsonPrimitive.intOrNull != null -> jsonPrimitive.int
        jsonPrimitive.longOrNull != null -> jsonPrimitive.long
        else -> jsonPrimitive.content
    }

    private companion object {
        const val TAG = "SseEventParser"
    }
}
