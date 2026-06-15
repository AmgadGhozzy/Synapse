package io.synapse.ai.features.marketplace.data.mapper

import io.synapse.ai.core.database.entity.PackEntity
import io.synapse.ai.domains.study.model.PackModule
import io.synapse.ai.domains.study.model.QuestionContent
import io.synapse.ai.domains.study.model.QuestionModel
import io.synapse.ai.domains.study.model.QuestionType
import io.synapse.ai.features.marketplace.data.dto.MarketplacePackDto
import io.synapse.ai.features.marketplace.data.dto.PreviewQuestionDto
import io.synapse.ai.features.marketplace.domain.MarketplacePack
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }



internal fun MarketplacePackDto.toDomain(): MarketplacePack = MarketplacePack(
    id               = id,
    title            = title,
    description      = description,
    emoji            = emoji,
    category         = category,
    difficulty       = difficulty,
    language         = language,
    questionCount    = questionCount,
    estimatedMinutes = estimatedMinutes,
    isPremium        = isPremium,
    isFeatured       = isFeatured,
    tags             = tags,
    modules          = parseModulesFromJsonArray(modules),
    downloadCount    = downloadCount,
)

internal fun PreviewQuestionDto.toDomain(localPackId: Long = 0L): QuestionModel {
    val content = runCatching {
        lenientJson.decodeFromJsonElement<QuestionContent>(contentJson)
    }.getOrElse { QuestionContent.FlashcardContent(front = questionText, back = "") }

    val qType = runCatching { QuestionType.valueOf(type) }.getOrElse { QuestionType.FLASHCARD }

    return QuestionModel(
        id           = 0L,
        packId       = localPackId,
        type         = qType,
        questionText = questionText,
        content      = content,
        createdAt    = 0L,
        remoteId     = id,
        reference    = reference,
        moduleTitle  = moduleTitle,
        level        = level,
        objective    = objective,
    )
}

internal fun MarketplacePack.toCacheEntity(packType: String = "marketplace"): PackEntity = PackEntity(
    uuid             = id,
    title            = title,
    sourceType       = "marketplace",
    createdAt        = System.currentTimeMillis(),
    note             = description ?: "",
    category         = category,
    emoji            = emoji,
    language         = language,
    difficulty       = difficulty,
    questionCount    = questionCount,
    isPremium        = isPremium,
    estimatedMinutes = estimatedMinutes,
    tags             = lenientJson.encodeToString(tags),
    packType         = packType
)



internal fun PackEntity.toMarketplaceDomain(): MarketplacePack = MarketplacePack(
    id               = uuid ?: id.toString(),
    title            = title,
    description      = note.ifBlank { null },
    emoji            = emoji,
    category         = category,
    difficulty       = difficulty,
    language         = language,
    questionCount    = questionCount,
    estimatedMinutes = estimatedMinutes,
    isPremium        = isPremium,
    isFeatured       = false,
    tags             = parseTags(tags),
    modules          = parseModules(modules),
    downloadCount    = 0,
)



private fun parseModulesFromJsonArray(jsonArray: JsonArray?): List<PackModule> {
    if (jsonArray == null) return emptyList()
    return runCatching {
        jsonArray.map { element ->
            val obj = element.jsonObject
            PackModule(
                title = obj["title"]?.jsonPrimitive?.content ?: "",
                description = obj["description"]?.jsonPrimitive?.content,
                order = obj["order"]?.jsonPrimitive?.int ?: 0
            )
        }
    }.getOrElse { emptyList() }
}

private fun parseModules(json: String?): List<PackModule> {
    if (json.isNullOrBlank()) return emptyList()
    // It's a string, so we need to parse it as a JsonArray first
    return runCatching {
        parseModulesFromJsonArray(lenientJson.parseToJsonElement(json).jsonArray)
    }.getOrElse { emptyList() }
}

private fun parseTags(json: String): List<String> {
    if (json.isBlank() || json == "[]") return emptyList()
    return runCatching {
        lenientJson.decodeFromString<List<String>>(json)
    }.getOrElse { emptyList() }
}
