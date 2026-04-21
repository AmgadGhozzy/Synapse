package io.synapse.ai.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "question_progress",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["nextReview"])]
)
data class QuestionProgressEntity(
    @PrimaryKey
    val questionId: Long,

    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReview: Long? = null,
    val lastReviewed: Long? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,

    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long = 0L
)
