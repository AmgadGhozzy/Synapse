package io.synapse.ai.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = PackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["packId"]), Index(value = ["startedAt"])]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val packId: Long?,
    val mode: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val summaryJson: String? = null,

    @ColumnInfo(defaultValue = "0")
    val snapshotQuestions: Boolean = false,

    @ColumnInfo(defaultValue = "0")
    val totalQuestions: Int = 0,

    @ColumnInfo(defaultValue = "0")
    val correctCount: Int = 0,

    @ColumnInfo(defaultValue = "0")
    val durationMs: Long = 0L,

    @ColumnInfo(defaultValue = "NULL")
    val remoteId: String? = null,

    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long = 0L
)
