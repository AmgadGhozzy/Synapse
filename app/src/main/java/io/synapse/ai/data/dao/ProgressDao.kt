package io.synapse.ai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.synapse.ai.data.entity.QuestionEntity
import io.synapse.ai.data.entity.QuestionProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM question_progress WHERE questionId = :questionId")
    suspend fun getByQuestion(questionId: Long): QuestionProgressEntity?

    @Query("SELECT * FROM question_progress WHERE questionId = :questionId")
    fun observeByQuestion(questionId: Long): Flow<QuestionProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: QuestionProgressEntity)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBatch(list: List<QuestionProgressEntity>)

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN question_progress qp ON q.id = qp.questionId
        INNER JOIN packs p ON q.packId = p.id AND p.isDeleted = 0
        WHERE q.packId = :packId
          AND qp.wrongCount >= :thresholdWrongCount
        ORDER BY qp.wrongCount DESC
        """
    )
    suspend fun getWeakQuestions(
        packId: String,
        thresholdWrongCount: Int
    ): List<QuestionEntity>

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN question_progress qp ON q.id = qp.questionId
        INNER JOIN packs p ON q.packId = p.id AND p.isDeleted = 0
        WHERE q.packId = :packId
          AND qp.easeFactor >= :minEaseFactor
        ORDER BY qp.easeFactor DESC
        """
    )
    suspend fun getMasteredQuestions(
        packId: String,
        minEaseFactor: Double
    ): List<QuestionEntity>

    @Query(
        """
        SELECT MAX(qp.lastReviewed) FROM question_progress qp
        INNER JOIN questions q ON q.id = qp.questionId
        WHERE q.packId = :packId
        """
    )
    suspend fun getLastReviewedForPack(packId: Long): Long?

    @Query("SELECT * FROM question_progress")
    suspend fun getAllProgress(): List<QuestionProgressEntity>

    @Query("SELECT * FROM question_progress WHERE questionId IN (:questionIds)")
    suspend fun getByQuestionIds(questionIds: List<Long>): List<QuestionProgressEntity>

    @Query("DELETE FROM question_progress")
    suspend fun deleteAll()
}