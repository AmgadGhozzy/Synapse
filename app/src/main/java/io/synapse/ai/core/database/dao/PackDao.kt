package io.synapse.ai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.synapse.ai.core.database.entity.PackEntity
import io.synapse.ai.core.database.entity.PackOverviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPack(pack: PackEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(pack: PackEntity): Long

    @Query("SELECT * FROM packs WHERE id = :id")
    suspend fun getPackById(id: Long): PackEntity?

    @Query("SELECT * FROM packs WHERE uuid = :uuid LIMIT 1")
    suspend fun getPackByUuid(uuid: String): PackEntity?

    /**
     * Server-side metadata update. Does NOT touch isDirty so we don't
     * accidentally re-upload a server-originated change.
     */
    @Query("""
        UPDATE packs SET
            title         = :title,
            note          = :note,
            category      = :category,
            emoji         = :emoji,
            color         = :color,
            language      = :language,
            difficulty    = :difficulty,
            isDeleted     = :isDeleted,
            questionCount = :questionCount,
            isDirty       = 0
        WHERE uuid = :uuid
    """)
    suspend fun updatePackFromServer(
        uuid: String,
        title: String,
        note: String,
        category: String?,
        emoji: String?,
        color: String?,
        language: String,
        difficulty: String?,
        isDeleted: Boolean,
        questionCount: Int,
    )

    @Query("UPDATE packs SET questionCount = :count, isDirty = 1, updatedAt = (cast(strftime('%s','now') as INT)*1000) WHERE id = :packId")
    suspend fun updateQuestionCount(packId: Long, count: Int)

    @Query("UPDATE packs SET modules = :modulesJson, isDirty = 1, updatedAt = (cast(strftime('%s','now') as INT)*1000) WHERE id = :packId")
    suspend fun updateModules(packId: Long, modulesJson: String)

    @Query("SELECT * FROM packs WHERE isDeleted = 0 AND packType != 'marketplace' ORDER BY createdAt DESC")
    fun observeAllPacks(): Flow<List<PackEntity>>

    @Query("SELECT id, title, category, emoji, color, questionCount, createdAt, packType FROM packs WHERE isDeleted = 0 AND packType != 'marketplace' ORDER BY createdAt DESC")
    fun observePackOverviews(): Flow<List<PackOverviewEntity>>

    @Query("UPDATE packs SET isDeleted = 1, isDirty = 1, updatedAt = (cast(strftime('%s','now') as INT)*1000) WHERE id = :id")
    suspend fun softDeleteById(id: Long)

    @Query("DELETE FROM packs WHERE id = :id")
    suspend fun hardDeleteById(id: Long)

    @Query("SELECT * FROM packs WHERE packType != 'marketplace'")
    suspend fun getAllPacks(): List<PackEntity>

    @Query("DELETE FROM packs")
    suspend fun deleteAll()

    @Query(
        """
        UPDATE packs SET
            title         = :title,
            note          = :note,
            category      = :category,
            emoji         = :emoji,
            color         = :color,
            language      = :language,
            difficulty    = :difficulty,
            sourceUrl     = :sourceUrl,
            sourceSummary = :sourceSummary,
            isDirty       = 1,
            updatedAt     = (cast(strftime('%s','now') as INT)*1000)
        WHERE id = :id
        """
    )
    suspend fun updatePack(
        id: Long,
        title: String,
        note: String,
        category: String?,
        emoji: String?,
        color: String?,
        language: String,
        difficulty: String?,
        sourceUrl: String?,
        sourceSummary: String?,
    )

    @Query("UPDATE packs SET isDirty = 0 WHERE id IN (:ids)")
    suspend fun clearDirtyFlags(ids: List<Long>)

    @Query("SELECT * FROM packs WHERE isDirty = 1")
    suspend fun getDirtyPacks(): List<PackEntity>
}
