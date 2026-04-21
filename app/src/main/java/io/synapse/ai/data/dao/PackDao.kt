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
    suspend fun insertPack(pack: PackEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(pack: PackEntity): Long

    @Query("SELECT * FROM packs WHERE id = :id")
    suspend fun getPackById(id: Long): PackEntity?

    @Query("SELECT * FROM packs WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun observeAllPacks(): Flow<List<PackEntity>>

    @Query("UPDATE packs SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteById(id: Long)

    @Query("DELETE FROM packs WHERE id = :id")
    suspend fun hardDeleteById(id: Long)

    @Query("SELECT * FROM packs")
    suspend fun getAllPacks(): List<PackEntity>

    @Query("DELETE FROM packs")
    suspend fun deleteAll()
}
