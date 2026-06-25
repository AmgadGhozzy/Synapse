package io.synapse.ai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.synapse.ai.core.database.entity.PackEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for marketplace pack cache.
 * Marketplace packs are stored as `pack_type = 'marketplace'` and
 * never shown in the user's personal library.
 */
@Dao
interface MarketplacePackDao {

    @Query("SELECT * FROM packs WHERE packType = 'marketplace' AND isDeleted = 0 ORDER BY id DESC")
    fun observeMarketplacePacks(): Flow<List<PackEntity>>

    @Query("SELECT * FROM packs WHERE packType = 'marketplace' AND isDeleted = 0 ORDER BY id DESC")
    suspend fun getMarketplacePacks(): List<PackEntity>

    @Query("SELECT * FROM packs WHERE uuid = :uuid AND packType = 'marketplace' LIMIT 1")
    suspend fun getByUuid(uuid: String): PackEntity?

    /**
     * Upsert by UUID — replace existing row to keep cache fresh.
     * Uses REPLACE so the same UUID from Supabase doesn't generate duplicates.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(packs: List<PackEntity>)

    @Query("DELETE FROM packs WHERE packType = 'marketplace'")
    suspend fun clearCache()
}
