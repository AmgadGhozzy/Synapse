package io.synapse.ai.features.add_pdf.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════════════
// REQUEST — matches the Zod schema in generate-ai-pack v4.0
//
// IMPORTANT: [userId] is no longer sent in the body.
// The Edge Function extracts the user from the JWT in the
// Authorization header (handled automatically by supabase-kt).
// ══════════════════════════════════════════════════════════════════

@Serializable
data class GeneratePackRequest(
    @SerialName("sourceType")     val sourceType: String,
    @SerialName("sourceContent")  val sourceContent: String,
    @SerialName("questionTypes")  val questionTypes: List<String>,
    @SerialName("questionCount")  val questionCount: Int,
    @SerialName("language")       val language: String = "en",
    @SerialName("difficulty")     val difficulty: String? = null,
    @SerialName("thinking")       val thinking: Boolean? = null,
    @SerialName("instructions")   val instructions: String? = null,
    @SerialName("pageCount")      val pageCount: Int? = null,
)

// ══════════════════════════════════════════════════════════════════
// RESPONSE  { pack, questions[], meta }
//
// The Edge Function persists the pack and questions to Supabase DB
// before returning. The response contains the full DB rows, so
// the client has UUIDs and numeric IDs for local caching.
// ══════════════════════════════════════════════════════════════════

@Serializable
data class GeneratePackResponse(
    @SerialName("pack")      val pack: PackDto,
    @SerialName("questions") val questions: List<QuestionDto>,
    @SerialName("meta")      val meta: MetaDto,
)

// ── Pack (mirrors Supabase packs row) ─────────────────────────────

@Serializable
data class PackDto(
    /** Supabase packs.id (UUID) — used as sync key. */
    @SerialName("id")              val uuid: String,
    /** Supabase packs.numeric_id (bigint sequence) — used as Room PK. */
    @SerialName("numeric_id")      val numericId: Long,
    @SerialName("title")           val title: String,
    @SerialName("description")     val description: String? = null,
    @SerialName("language")        val language: String = "en",
    @SerialName("difficulty")      val difficulty: String? = null,
    @SerialName("category")        val category: String? = null,
    @SerialName("emoji")           val emoji: String? = null,
    @SerialName("color")           val color: String? = null,
    @SerialName("source_type")     val sourceType: String? = null,
    @SerialName("source_summary")  val sourceSummary: String? = null,
    @SerialName("question_count")  val questionCount: Int = 0,
    @SerialName("created_at")      val createdAt: String? = null,
    @SerialName("modules")         val modules: List<ModuleDto>? = null,
)

// ── Question (mirrors Supabase questions row) ─────────────────────

@Serializable
data class QuestionDto(
    /** Supabase questions.id (UUID) — stored as remoteId in Room. */
    @SerialName("id")              val remoteId: String,
    @SerialName("type")            val type: String,
    @SerialName("question_text")   val questionText: String,
    @SerialName("content_json")    val contentJson: ContentJsonDto,
    @SerialName("sort_order")      val sortOrder: Int = 0,
    @SerialName("reference")       val reference: String? = null,
)

// ── Content JSON (polymorphic payload per question type) ──────────
//
// New field names from v4.0 backend:
//   MCQ:        choices[], correct_index, explanation
//   TRUE_FALSE: correct_answer, explanation
//   FLASHCARD:  answer, hint

@Serializable
data class ContentJsonDto(
    // MCQ
    @SerialName("choices")        val choices: List<String>? = null,
    @SerialName("correct_index")  val correctIndex: Int? = null,
    @SerialName("explanation")    val explanation: String? = null,
    // TRUE_FALSE
    @SerialName("correct_answer") val correctAnswer: Boolean? = null,
    // FLASHCARD
    @SerialName("answer")         val answer: String? = null,
    @SerialName("hint")           val hint: String? = null,
)

// ── Meta ──────────────────────────────────────────────────────────

@Serializable
data class MetaDto(
    @SerialName("tier")       val tier: String = "anonymous",
    @SerialName("cached")     val cached: Boolean = false,
    @SerialName("tokensUsed") val tokensUsed: Int = 0,
)

// ══════════════════════════════════════════════════════════════════
// ERROR  { "error": "...", "code": "..." }
// ══════════════════════════════════════════════════════════════════

@Serializable
data class GeneratePackErrorDto(
    @SerialName("error") val error: String,
    @SerialName("code")  val code: String? = null,
)

@Serializable
data class ModuleDto(
    @SerialName("title")       val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("order")       val order: Int = 1,
    @SerialName("diagram")     val diagram: DiagramDto? = null,
)

@Serializable
data class DiagramDto(
    @SerialName("type")        val type: String = "mermaid",
    @SerialName("title")       val title: String? = null,
    @SerialName("content")     val content: String,
    @SerialName("explanation") val explanation: String? = null,
)
