package io.synapse.ai.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = PackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["packId", "type"]),
        Index(value = ["packId", "createdAt"]),
        Index(value = ["packId", "moduleTitle"])
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(defaultValue = "NULL")
    val remoteId: String? = null,

    val packId: Long,
    val type: String,
    val questionText: String,
    val contentJson: String,
    val createdAt: Long,

    @ColumnInfo(defaultValue = "NULL")
    val reference: String? = null,

    @ColumnInfo(defaultValue = "0")
    val isDeleted: Int = 0,

    // Unified fields
    @ColumnInfo(defaultValue = "NULL")
    val moduleTitle: String? = null,

    /** Per-question difficulty: "easy" | "medium" | "hard". */
    @ColumnInfo(defaultValue = "NULL")
    val level: String? = null,

    @ColumnInfo(defaultValue = "NULL")
    val objective: String? = null,
)