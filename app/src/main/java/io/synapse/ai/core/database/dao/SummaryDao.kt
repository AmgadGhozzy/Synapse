package io.synapse.ai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.synapse.ai.core.database.entity.SummaryEntity
import io.synapse.ai.core.database.entity.SummarySectionEntity
import io.synapse.ai.core.database.entity.SummaryWithSectionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    // ── Summaries ─────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(entity: SummaryEntity): Long

    @Update
    suspend fun updateSummary(entity: SummaryEntity)

    @Query("UPDATE summaries SET sectionCount = :count WHERE id = :id")
    suspend fun updateSectionCount(id: Long, count: Int)

    @Query("SELECT * FROM summaries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SummaryEntity>>

    @Transaction
    @Query("SELECT * FROM summaries WHERE id = :id")
    fun observeWithSections(id: Long): Flow<SummaryWithSectionsEntity?>

    @Query("SELECT * FROM summaries WHERE id = :id")
    suspend fun getById(id: Long): SummaryEntity?

    @Query("DELETE FROM summaries WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ── Sections ──────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(entity: SummarySectionEntity): Long

    @Query("SELECT * FROM summary_sections WHERE summaryId = :summaryId ORDER BY sortOrder ASC")
    fun observeSections(summaryId: Long): Flow<List<SummarySectionEntity>>
}
