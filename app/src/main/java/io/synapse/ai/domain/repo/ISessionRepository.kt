package io.synapse.ai.domain.repo

import io.synapse.ai.domain.model.DayActivityModel
import io.synapse.ai.domain.model.SessionMode
import io.synapse.ai.domain.model.SessionWithQuestionsModel
import io.synapse.ai.domain.model.StudySessionModel
import kotlinx.coroutines.flow.Flow

interface ISessionRepository {
    /** @param packId Null for multipack sessions — NULL is exempt from the FK constraint. */
    suspend fun startSession(packId: Long?, mode: SessionMode): Long
    suspend fun endSession(
        sessionId: Long,
        summaryJson: String?,
        totalQuestions: Int,
        correctCount: Int,
        durationMs: Long
    )
    /** Saves partial progress and marks the session finished (for cancel/autosave). */
    suspend fun savePartialSession(
        sessionId: Long,
        totalQuestions: Int,
        correctCount: Int,
        durationMs: Long
    )
    fun observeSessionsForPack(packId: Long): Flow<List<StudySessionModel>>
    suspend fun getSessionWithQuestions(sessionId: Long): SessionWithQuestionsModel?

    suspend fun getDailyActivity(fromMs: Long, toMs: Long): List<DayActivityModel>
    suspend fun getDailyActivityForPack(packId: Long, fromMs: Long, toMs: Long): List<DayActivityModel>
    suspend fun getStudiedDayIndices(): List<Long>
    fun observeStudiedDayIndices(): Flow<List<Long>>
    fun observeLastSessionFinishedAt(): Flow<Long?>
}
