package io.synapse.ai.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.synapse.ai.data.entity.QuestionEntity
import io.synapse.ai.data.entity.SessionQuestionCrossRef
import io.synapse.ai.data.entity.SessionWithQuestions
import io.synapse.ai.data.entity.StudySessionEntity
import io.synapse.ai.domain.model.DayActivityModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: StudySessionEntity): Long

    @Query(
        """
        UPDATE study_sessions 
        SET finishedAt = :finishedAtEpochMs, 
            summaryJson = :summaryJson,
            totalQuestions = :totalQuestions,
            correctCount = :correctCount,
            durationMs = :durationMs
        WHERE id = :sessionId
        """
    )
    suspend fun updateSessionEnd(
        sessionId: Long,
        finishedAtEpochMs: Long,
        summaryJson: String?,
        totalQuestions: Int,
        correctCount: Int,
        durationMs: Long
    )

    @Query(
        """
        UPDATE study_sessions 
        SET totalQuestions = :totalQuestions,
            correctCount = :correctCount,
            durationMs = :durationMs,
            finishedAt = :finishedAtEpochMs
        WHERE id = :sessionId
        """
    )
    suspend fun updateSessionProgress(
        sessionId: Long,
        totalQuestions: Int,
        correctCount: Int,
        durationMs: Long,
        finishedAtEpochMs: Long
    )

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestionSnapshot(refs: List<SessionQuestionCrossRef>)

    @Transaction
    suspend fun getSessionWithQuestions(sessionId: Long): SessionWithQuestions? {
        val session = getSessionById(sessionId) ?: return null
        val questions = if (session.snapshotQuestions) {
            getSnapshotQuestions(sessionId)
        } else {
            emptyList()
        }
        return SessionWithQuestions(session = session, questions = questions)
    }

    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): StudySessionEntity?

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN session_question_cross_ref ref ON q.id = ref.questionId
        WHERE ref.sessionId = :sessionId
        """
    )
    suspend fun getSnapshotQuestions(sessionId: Long): List<QuestionEntity>

    @Query("SELECT * FROM study_sessions WHERE packId = :packId ORDER BY startedAt DESC")
    suspend fun getSessionHistoryForPack(packId: Long): List<StudySessionEntity>

    @Query("SELECT * FROM study_sessions WHERE packId = :packId ORDER BY startedAt DESC")
    fun observeSessionHistoryForPack(packId: Long): Flow<List<StudySessionEntity>>

    @Query("""
        SELECT
            (startedAt / 86400000) * 86400000       AS dayEpochMs,
            COALESCE(SUM(totalQuestions), 0)         AS questionsStudied,
            COALESCE(SUM(correctCount), 0)           AS correctCount,
            COUNT(*)                                  AS sessionCount,
            COALESCE(SUM(durationMs), 0)             AS totalDurationMs
        FROM study_sessions
        WHERE finishedAt IS NOT NULL
          AND startedAt >= :fromMs
          AND startedAt <  :toMs
        GROUP BY dayEpochMs
        ORDER BY dayEpochMs ASC
    """)
    suspend fun getDailyActivity(fromMs: Long, toMs: Long): List<DayActivityModel>

    @Query("""
        SELECT
            (startedAt / 86400000) * 86400000       AS dayEpochMs,
            COALESCE(SUM(totalQuestions), 0)         AS questionsStudied,
            COALESCE(SUM(correctCount), 0)           AS correctCount,
            COUNT(*)                                  AS sessionCount,
            COALESCE(SUM(durationMs), 0)             AS totalDurationMs
        FROM study_sessions
        WHERE finishedAt IS NOT NULL
          AND packId = :packId
          AND startedAt >= :fromMs
          AND startedAt <  :toMs
        GROUP BY dayEpochMs
        ORDER BY dayEpochMs ASC
    """)
    suspend fun getDailyActivityForPack(
        packId: Long,
        fromMs: Long,
        toMs: Long,
    ): List<DayActivityModel>

    @Query("""
        SELECT DISTINCT (startedAt / 86400000) AS dayIndex
        FROM study_sessions
        WHERE finishedAt IS NOT NULL
        ORDER BY dayIndex DESC
    """)
    suspend fun getStudiedDayIndices(): List<Long>

    @Query("""
        SELECT DISTINCT (startedAt / 86400000) AS dayIndex
        FROM study_sessions
        WHERE finishedAt IS NOT NULL
        ORDER BY dayIndex DESC
    """)
    fun observeStudiedDayIndices(): Flow<List<Long>>

    @Query("SELECT MAX(finishedAt) FROM study_sessions WHERE finishedAt IS NOT NULL")
    fun observeLastSessionFinishedAt(): Flow<Long?>

    @Query("SELECT * FROM study_sessions")
    suspend fun getAllSessions(): List<StudySessionEntity>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceBatch(sessions: List<StudySessionEntity>)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()
}
