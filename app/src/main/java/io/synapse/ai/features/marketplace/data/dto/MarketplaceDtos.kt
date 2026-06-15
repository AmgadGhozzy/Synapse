package io.synapse.ai.features.marketplace.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

/**
 * Raw DTO mirroring the `packs` Supabase row for marketplace entries.
 * Only fields needed for the marketplace feature are mapped here.
 */
@Serializable
internal data class MarketplacePackDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val emoji: String? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val language: String = "en",
    @SerialName("question_count")  val questionCount: Int = 0,
    @SerialName("estimated_minutes") val estimatedMinutes: Int? = null,
    @SerialName("is_premium")      val isPremium: Boolean = false,
    @SerialName("is_featured")     val isFeatured: Boolean = false,
    val tags: List<String> = emptyList(),

    val modules: JsonArray? = null,
    @SerialName("download_count") val downloadCount: Int = 0,
)

/** Minimal question preview returned alongside pack details. */
@Serializable
internal data class PreviewQuestionDto(
    val id: String,
    @SerialName("pack_id")       val packId: String,
    val type: String,
    @SerialName("question_text") val questionText: String,
    @SerialName("content_json")  val contentJson: JsonElement,
    @SerialName("sort_order")    val sortOrder: Int = 0,
    val reference: String? = null,
    @SerialName("module_title")  val moduleTitle: String? = null,
    val level: String? = null,
    val objective: String? = null,
)

/** Response body from the `acquire-pack` Edge Function. */
@Serializable
internal data class AcquirePackResponse(
    @SerialName("packId")      val packId: String,
    @SerialName("alreadyOwned") val alreadyOwned: Boolean,
)
