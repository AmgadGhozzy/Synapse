package io.synapse.ai.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "packs",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["language"]),
        Index(value = ["isDeleted"]),
        Index(value = ["packType"]),
    ]
)
data class PackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(defaultValue = "NULL")
    val uuid: String? = null,

    val title: String,

    val sourceType: String,

    val createdAt: Long,

    @ColumnInfo(defaultValue = "")
    val note: String = "",

    @ColumnInfo(defaultValue = "NULL")
    val category: String? = null,

    @ColumnInfo(defaultValue = "NULL")
    val emoji: String? = null,

    @ColumnInfo(defaultValue = "NULL")
    val color: String? = null,

    @ColumnInfo(defaultValue = "en")
    val language: String = "en",

    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,

    @ColumnInfo(defaultValue = "NULL")
    val difficulty: String? = null,

    @ColumnInfo(defaultValue = "NULL")
    val sourceUrl: String? = null,

    @ColumnInfo(defaultValue = "NULL")
    val sourceSummary: String? = null,

    @ColumnInfo(defaultValue = "NULL")
    val sourceHash: String? = null,

    @ColumnInfo(defaultValue = "0")
    val questionCount: Int = 0,

    // Unified fields
    @ColumnInfo(defaultValue = "ai_generated")
    val packType: String = "ai_generated",

    @ColumnInfo(defaultValue = "NULL")
    val modules: String? = null,

    @ColumnInfo(defaultValue = "[]")
    val tags: String = "[]",

    @ColumnInfo(defaultValue = "NULL")
    val estimatedMinutes: Int? = null,

    @ColumnInfo(defaultValue = "0")
    val isPremium: Boolean = false,

    @ColumnInfo(defaultValue = "1")
    val version: Int = 1,

    @ColumnInfo(defaultValue = "NULL")
    val templateId: String? = null,
)