package io.synapse.ai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.synapse.ai.core.database.entity.QuestionEntity
import io.synapse.ai.domains.study.model.PackProgressStats
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(questions: List<QuestionEntity>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatchReturnIds(questions: List<QuestionEntity>): List<Long>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBatchIgnore(questions: List<QuestionEntity>)

    /**
     * Active (non-deleted) questions for a pack, ordered by [sortOrder] then [createdAt].
     * [sortOrder] defaults to 0 for legacy rows; tiebreak by [createdAt] ensures
     * deterministic ordering for packs created before reorder was supported.
     *         ORDER BY sortOrder ASC, createdAt ASC
     */
    @Query(
        """
        SELECT * FROM questions
        WHERE packId = :packId AND isDeleted = 0
        ORDER BY createdAt ASC
        """
    )
    fun observeByPack(packId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): QuestionEntity?

    @Query("SELECT COUNT(*) FROM questions WHERE packId = :packId AND isDeleted = 0")
    suspend fun countByPack(packId: Long): Int

    @Query("""
        SELECT
            :packId AS packId,
            COUNT(q.id) AS totalCards,
            SUM(CASE WHEN qp.nextReview <= :nowEpochMs OR qp.nextReview IS NULL THEN 1 ELSE 0 END) AS dueCards,
            SUM(CASE WHEN qp.repetitions >= 3 AND qp.intervalDays >= 7 THEN 1 ELSE 0 END) AS masteredCards,
            MAX(qp.lastReviewed) AS lastReviewed
        FROM questions q
        LEFT JOIN question_progress qp ON q.id = qp.questionId
        WHERE q.packId = :packId AND q.isDeleted = 0
    """)
    suspend fun getPackProgressStats(packId: Long, nowEpochMs: Long): PackProgressStats

    @Query("""
        SELECT
            q.packId AS packId,
            COUNT(q.id) AS totalCards,
            SUM(CASE WHEN qp.nextReview <= :nowEpochMs OR qp.nextReview IS NULL THEN 1 ELSE 0 END) AS dueCards,
            SUM(CASE WHEN qp.repetitions >= 3 AND qp.intervalDays >= 7 THEN 1 ELSE 0 END) AS masteredCards,
            MAX(qp.lastReviewed) AS lastReviewed
        FROM questions q
        LEFT JOIN question_progress qp ON q.id = qp.questionId
        WHERE q.isDeleted = 0
        GROUP BY q.packId
    """)
    suspend fun getAllPackProgressStats(nowEpochMs: Long): List<PackProgressStats>

    @Query("SELECT * FROM questions WHERE packId = :packId AND type = :type AND isDeleted = 0")
    suspend fun getQuestionsByType(packId: Long, type: String): List<QuestionEntity>

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN packs p ON q.packId = p.id AND p.isDeleted = 0
        LEFT JOIN question_progress qp ON q.id = qp.questionId
        WHERE q.packId = :packId
          AND q.isDeleted = 0
          AND (
              qp.nextReview <= :nowEpochMs
              OR qp.questionId IS NULL
              OR qp.nextReview IS NULL
          )
        ORDER BY
            CASE WHEN qp.questionId IS NULL THEN 1 ELSE 0 END ASC,
            COALESCE(qp.nextReview, :nowEpochMs + 1) ASC,
            q.createdAt ASC
        LIMIT :limit
        """
    )
    suspend fun getDueQuestionsJoined(
        packId: Long,
        nowEpochMs: Long,
        limit: Int,
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE isDeleted = 0")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE packId = :packId AND isDeleted = 0")
    suspend fun getByPackId(packId: Long): List<QuestionEntity>

    @Query("DELETE FROM questions")
    suspend fun deleteAll()

    // ── Soft delete ───────────────────────────────────────────────────────────

    /**
     * Marks a single question as deleted without removing SRS progress data.
     * The pack question count is updated separately by the repository.
     */
    @Query("UPDATE questions SET isDeleted = 1, isDirty = 1, updatedAt = (cast(strftime('%s','now') as INT)*1000) WHERE id = :id")
    suspend fun softDeleteById(id: Long)

    // ── Edit / update ─────────────────────────────────────────────────────────

    /**
     * Updates the mutable content of an existing question (the JSON payload
     * plus the shared [questionText] denormalised column).
     * [type] is immutable after creation and therefore excluded.
     */
    @Query(
        """
        UPDATE questions SET
            questionText = :questionText,
            contentJson  = :contentJson,
            reference    = :reference,
            isDirty      = 1,
            updatedAt    = (cast(strftime('%s','now') as INT)*1000)
        WHERE id = :id
        """
    )
    suspend fun updateContent(
        id: Long,
        questionText: String,
        contentJson: String,
        reference: String?,
    )

    // ── Reorder ───────────────────────────────────────────────────────────────

    /**
     * Persists the [sortOrder] for a single question.
     * Call this inside a transaction when reordering a pack.
     */
//    @Query("UPDATE questions SET sortOrder = :sortOrder WHERE id = :id")
//    suspend fun updateSortOrder(id: Long, sortOrder: Long)

    /**
     * Batch-reorders all questions in a pack by writing each question's new
     * [sortOrder] (0-based position index) in a single transaction.
     *
     * Callers pass a list of question IDs in the desired display order.
     */
    @Transaction
    suspend fun reorderQuestions(orderedIds: List<Long>) {
//        orderedIds.forEachIndexed { index, id ->
//            updateSortOrder(id = id, sortOrder = index.toLong())
//        }
    }

    @Query("UPDATE questions SET isDirty = 0 WHERE id IN (:ids)")
    suspend fun clearDirtyFlags(ids: List<Long>)

    @Query("SELECT * FROM questions WHERE isDirty = 1")
    suspend fun getDirtyQuestions(): List<QuestionEntity>
}
