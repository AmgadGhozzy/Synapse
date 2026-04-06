package com.venom.synapse.data

import androidx.room.TypeConverter
import com.venom.synapse.domain.model.QuestionType
import com.venom.synapse.domain.model.SessionMode
import com.venom.synapse.domain.model.SourceType

class SynapseConverters {

    @TypeConverter
    fun fromQuestionType(value: QuestionType): String = value.name

    @TypeConverter
    fun toQuestionType(value: String): QuestionType =
        try {
            QuestionType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            QuestionType.MCQ
        }

    @TypeConverter
    fun fromSessionMode(value: SessionMode): String = value.name

    @TypeConverter
    fun toSessionMode(value: String): SessionMode =
        try {
            SessionMode.valueOf(value)
        } catch (_: IllegalArgumentException) {
            SessionMode.MIXED
        }

    @TypeConverter
    fun fromSourceType(value: SourceType): String = value.name

    @TypeConverter
    fun toSourceType(value: String): SourceType =
        try {
            SourceType.valueOf(value)
        } catch (_: IllegalArgumentException) {
            SourceType.TEXT
        }
}