package com.venom.synapse.domain.repo

import com.venom.synapse.domain.model.DayActivityModel
import com.venom.synapse.domain.model.SessionMode
import com.venom.synapse.domain.model.SessionWithQuestionsModel
import com.venom.synapse.domain.model.StudySessionModel
import kotlinx.coroutines.flow.Flow

interface ISessionRepository {
    suspend fun startSession(packId: Long, mode: SessionMode): Long
    suspend fun endSession(
        sessionId: Long,
        summaryJson: String?,
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
}
