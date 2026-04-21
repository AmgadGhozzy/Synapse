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
    ]
)
data class PackEntity(
    @PrimaryKey
    val id: Long,

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
    val isDeleted: Boolean = false
)