package io.synapse.ai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.synapse.ai.data.entity.QuestionEntity
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

    @Query("SELECT * FROM questions WHERE packId = :packId ORDER BY createdAt ASC")
    fun observeByPack(packId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: Long): QuestionEntity?

    @Query("SELECT COUNT(*) FROM questions WHERE packId = :packId")
    suspend fun countByPack(packId: Long): Int

    @Query("SELECT * FROM questions WHERE packId = :packId AND type = :type")
    suspend fun getQuestionsByType(packId: Long, type: String): List<QuestionEntity>

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN packs p ON q.packId = p.id AND p.isDeleted = 0
        LEFT JOIN question_progress qp ON q.id = qp.questionId
        WHERE q.packId = :packId
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
        limit: Int
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE packId = :packId")
    suspend fun getByPackId(packId: Long): List<QuestionEntity>

    @Query("DELETE FROM questions")
    suspend fun deleteAll()
}
