package io.synapse.ai.data.mapper

import io.synapse.ai.data.entity.PackEntity
import io.synapse.ai.data.entity.QuestionEntity
import io.synapse.ai.data.entity.QuestionProgressEntity
import io.synapse.ai.data.entity.StudySessionEntity
import io.synapse.ai.domain.model.PackModel
import io.synapse.ai.domain.model.PackModule
import io.synapse.ai.domain.model.QuestionModel
import io.synapse.ai.domain.model.QuestionProgressModel
import io.synapse.ai.domain.model.QuestionType
import io.synapse.ai.domain.model.SessionMode
import io.synapse.ai.domain.model.SourceType
import io.synapse.ai.domain.model.StudySessionModel
import org.json.JSONArray
import org.json.JSONObject

// PACK
fun PackEntity.toDomain(): PackModel = PackModel(
    id            = id,
    uuid          = uuid,
    title         = title,
    sourceType    = SourceType.fromString(sourceType),
    createdAt     = createdAt,
    note          = note,
    category      = category,
    emoji         = emoji,
    color         = color,
    language      = language,
    difficulty    = difficulty,
    sourceUrl     = sourceUrl,
    sourceSummary = sourceSummary,
    sourceHash    = sourceHash,
    questionCount = questionCount,
    packType      = packType,
    modules       = parseModules(modules),
    tags          = parseTags(tags),
    estimatedMinutes = estimatedMinutes,
    isPremium     = isPremium,
    version       = version,
    templateId    = templateId,
)

fun PackModel.toEntity(): PackEntity = PackEntity(
    id            = id,
    uuid          = uuid,
    title         = title,
    sourceType    = sourceType.name,
    createdAt     = createdAt,
    note          = note,
    category      = category,
    emoji         = emoji,
    color         = color,
    language      = language,
    difficulty    = difficulty,
    sourceUrl     = sourceUrl,
    sourceSummary = sourceSummary,
    sourceHash    = sourceHash,
    questionCount = questionCount,
    packType      = packType,
    modules       = serializeModules(modules),
    tags          = serializeTags(tags),
    estimatedMinutes = estimatedMinutes,
    isPremium     = isPremium,
    version       = version,
    templateId    = templateId,
)

// QUESTION
// Parses contentJson via SynapseContentAdapter using type as discriminator.
fun QuestionEntity.toDomain(): QuestionModel {
    val questionType = QuestionType.fromString(type)
    return QuestionModel(
        id           = id,
        packId       = packId,
        type         = questionType,
        questionText = questionText,
        content      = SynapseContentAdapter.fromJson(questionType, contentJson),
        createdAt    = createdAt,
        remoteId     = remoteId,
        reference    = reference,
        moduleTitle  = moduleTitle,
        level        = level,
        objective    = objective,
    )
}

fun QuestionModel.toEntity(): QuestionEntity = QuestionEntity(
    id           = id,
    packId       = packId,
    type         = type.name,
    questionText = questionText,
    contentJson  = SynapseContentAdapter.toJson(content),
    createdAt    = createdAt,
    remoteId     = remoteId,
    reference    = reference,
    moduleTitle  = moduleTitle,
    level        = level,
    objective    = objective,
)

internal fun parseModules(json: String?): List<PackModule> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val title = obj.optString("title").takeIf { it.isNotBlank() } ?: continue
                add(
                    PackModule(
                        title       = title,
                        description = obj.optString("description").takeIf { it.isNotBlank() },
                        order       = obj.optInt("order", i + 1),
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

internal fun serializeModules(modules: List<PackModule>): String? {
    if (modules.isEmpty()) return null
    return try {
        JSONArray().apply {
            modules.forEach { m ->
                put(JSONObject().apply {
                    put("title", m.title)
                    m.description?.let { put("description", it) }
                    put("order", m.order)
                })
            }
        }.toString()
    } catch (_: Exception) {
        null
    }
}

internal fun parseTags(json: String?): List<String> {
    if (json.isNullOrBlank() || json == "[]") return emptyList()
    return try {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                val tag = arr.optString(i).takeIf { it.isNotBlank() } ?: continue
                add(tag)
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

internal fun serializeTags(tags: List<String>): String =
    try { JSONArray(tags).toString() } catch (_: Exception) { "[]" }

// QUESTION PROGRESS
fun QuestionProgressEntity.toDomain(): QuestionProgressModel = QuestionProgressModel(
    questionId   = questionId,
    easeFactor   = easeFactor,
    intervalDays = intervalDays,
    repetitions  = repetitions,
    nextReview   = nextReview,
    lastReviewed = lastReviewed,
    correctCount = correctCount,
    wrongCount   = wrongCount,
)

fun QuestionProgressModel.toEntity(): QuestionProgressEntity = QuestionProgressEntity(
    questionId   = questionId,
    easeFactor   = easeFactor,
    intervalDays = intervalDays,
    repetitions  = repetitions,
    nextReview   = nextReview,
    lastReviewed = lastReviewed,
    correctCount = correctCount,
    wrongCount   = wrongCount,
    updatedAt    = System.currentTimeMillis(),
)

// STUDY SESSION
fun StudySessionEntity.toDomain(): StudySessionModel = StudySessionModel(
    id                = id,
    packId            = packId,
    mode              = SessionMode.fromString(mode),
    startedAt         = startedAt,
    finishedAt        = finishedAt,
    summaryJson       = summaryJson,
    snapshotQuestions = snapshotQuestions,
)

fun StudySessionModel.toEntity(): StudySessionEntity = StudySessionEntity(
    id                = id,
    packId            = packId,
    mode              = mode.name,
    startedAt         = startedAt,
    finishedAt        = finishedAt,
    summaryJson       = summaryJson,
    snapshotQuestions = snapshotQuestions,
    updatedAt         = System.currentTimeMillis(),
)