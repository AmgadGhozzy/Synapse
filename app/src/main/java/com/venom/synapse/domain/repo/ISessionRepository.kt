package com.venom.synapse.domain.repo

import com.venom.synapse.domain.model.SessionMode
import com.venom.synapse.domain.model.SessionWithQuestionsModel
import com.venom.synapse.domain.model.StudySessionModel
import kotlinx.coroutines.flow.Flow

interface ISessionRepository {
    suspend fun startSession(packId: Long, mode: SessionMode): Long
    suspend fun endSession(sessionId: Long, summaryJson: String?)
    fun observeSessionsForPack(packId: Long): Flow<List<StudySessionModel>>
    suspend fun getSessionWithQuestions(sessionId: Long): SessionWithQuestionsModel?
}
