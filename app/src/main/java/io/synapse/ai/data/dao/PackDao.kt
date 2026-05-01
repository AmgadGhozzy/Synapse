package io.synapse.ai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.synapse.ai.data.entity.PackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPack(pack: PackEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(pack: PackEntity): Long

    @Query("SELECT * FROM packs WHERE id = :id")
    suspend fun getPackById(id: Long): PackEntity?

    @Query("UPDATE packs SET questionCount = :count WHERE id = :packId")
    suspend fun updateQuestionCount(packId: Long, count: Int)

    @Query("SELECT * FROM packs WHERE isDeleted = 0 AND packType != 'marketplace' ORDER BY createdAt DESC")
    fun observeAllPacks(): Flow<List<PackEntity>>

    @Query("UPDATE packs SET isDeleted = 1 WHERE id = :id")
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
            sourceSummary = :sourceSummary
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
}