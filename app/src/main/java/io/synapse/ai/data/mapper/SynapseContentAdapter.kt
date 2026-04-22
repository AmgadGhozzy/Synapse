package io.synapse.ai.data.mapper

import io.synapse.ai.domain.model.QuestionContent
import io.synapse.ai.domain.model.QuestionType
import org.json.JSONArray
import org.json.JSONObject

object SynapseContentAdapter {

    fun toJson(content: QuestionContent): String = when (content) {

        is QuestionContent.McqContent -> JSONObject().apply {
            put("options",      JSONArray(content.options))
            put("correctIndex", content.correctIndex)
            putOpt("explanation", content.explanation)
            putOpt("hint",        content.hint)
        }.toString()

        is QuestionContent.TfContent -> JSONObject().apply {
            put("answer", content.answer)
            putOpt("explanation", content.explanation)
            putOpt("hint",        content.hint)
        }.toString()

        is QuestionContent.FlashcardContent -> JSONObject().apply {
            put("front",   content.front)
            put("back",    content.back)
            putOpt("example", content.example)
            content.imageUris?.let { put("imageUris", JSONArray(it)) }
        }.toString()

        is QuestionContent.MapContent -> JSONObject(content.data).toString()
    }

    fun fromJson(type: QuestionType, json: String): QuestionContent = try {
        val obj = JSONObject(json)
        when (type) {
            QuestionType.MCQ        -> parseMcq(obj)
            QuestionType.TRUE_FALSE -> parseTf(obj)
            QuestionType.FLASHCARD  -> parseFlashcard(obj)
        }
    } catch (e: Exception) {
        QuestionContent.MapContent()
    }

    private fun parseMcq(obj: JSONObject): QuestionContent {
        val optionsArr   = obj.optJSONArray("options")
        val correctIndex = obj.optInt("correctIndex", 0)

        // Parse options safely
        val parsedOptions = mutableListOf<String>()
        if (optionsArr != null) {
            for (i in 0 until optionsArr.length()) {
                val opt = optionsArr.optString(i, "").trim()
                if (opt.isNotEmpty()) {
                    parsedOptions.add(opt)
                }
            }
        }

        // Provide fallbacks if parsing yielded bad results
        val finalOptions = if (parsedOptions.size >= 2) {
            parsedOptions
        } else {
            listOf("Option A", "Option B")
        }
        
        val safeIdx = if (correctIndex in finalOptions.indices) correctIndex else 0

        return QuestionContent.McqContent(
            options      = finalOptions,
            correctIndex = safeIdx,
            explanation  = obj.optString("explanation").takeIf { it.isNotEmpty() },
            hint         = obj.optString("hint").takeIf { it.isNotEmpty() },
        )
    }

    private fun parseTf(obj: JSONObject): QuestionContent {
        return QuestionContent.TfContent(
            answer      = obj.optBoolean("answer", true),
            explanation = obj.optString("explanation").takeIf { it.isNotEmpty() },
            hint        = obj.optString("hint").takeIf { it.isNotEmpty() },
        )
    }

    private fun parseFlashcard(obj: JSONObject): QuestionContent {
        val front = obj.optString("front", "Front")
        val back  = obj.optString("back", "Back")

        val imageUrisArr = obj.optJSONArray("imageUris")
        val imageUris = imageUrisArr?.let { arr ->
            (0 until arr.length()).map { arr.optString(it, "") }
        }

        return QuestionContent.FlashcardContent(
            front     = front,
            back      = back,
            example   = obj.optString("example").takeIf { it.isNotEmpty() },
            imageUris = imageUris,
        )
    }
}
