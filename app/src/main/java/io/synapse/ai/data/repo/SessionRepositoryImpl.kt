package io.synapse.ai.data.repo

import io.synapse.ai.data.dao.SessionDao
import io.synapse.ai.data.mapper.toDomain
import io.synapse.ai.data.mapper.toEntity
import io.synapse.ai.domain.model.DayActivityModel
import io.synapse.ai.domain.model.SessionMode
import io.synapse.ai.domain.model.SessionWithQuestionsModel
import io.synapse.ai.domain.model.StudySessionModel
import io.synapse.ai.domain.repo.ISessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Room-backed implementation of [ISessionRepository].
 */
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : ISessionRepository {

    override suspend fun startSession(packId: Long?, mode: SessionMode): Long =
        withContext(Dispatchers.IO) {
            val session = StudySessionModel(
                packId = packId,
                mode = mode,
                startedAt = System.currentTimeMillis()
            )
            sessionDao.insert(session.toEntity())
        }

    override suspend fun endSession(
        sessionId: Long,
        summaryJson: String?,
        totalQuestions: Int,
        correctCount: Int,
        durationMs: Long
    ) = withContext(Dispatchers.IO) {
            sessionDao.updateSessionEnd(
                sessionId = sessionId,
                finishedAtEpochMs = System.currentTimeMillis(),
                summaryJson = summaryJson,
                totalQuestions = totalQuestions,
                correctCount = correctCount,
                durationMs = durationMs
            )
        }

    override suspend fun savePartialSession(
        sessionId: Long,
        totalQuestions: Int,
        correctCount: Int,
        durationMs: Long
    ) = withContext(Dispatchers.IO) {
            sessionDao.updateSessionProgress(
                sessionId = sessionId,
                totalQuestions = totalQuestions,
                correctCount = correctCount,
                durationMs = durationMs,
                finishedAtEpochMs = System.currentTimeMillis()
            )
        }

    override suspend fun getDailyActivity(fromMs: Long, toMs: Long): List<DayActivityModel> =
        withContext(Dispatchers.IO) {
            sessionDao.getDailyActivity(fromMs, toMs).map {
                DayActivityModel(
                    dayEpochMs = it.dayEpochMs,
                    questionsStudied = it.questionsStudied,
                    correctCount = it.correctCount,
                    sessionCount = it.sessionCount,
                    totalDurationMs = it.totalDurationMs
                )
            }
        }

    override suspend fun getDailyActivityForPack(
        packId: Long,
        fromMs: Long,
        toMs: Long,
    ): List<DayActivityModel> =
        withContext(Dispatchers.IO) {
            sessionDao.getDailyActivityForPack(packId, fromMs, toMs).map {
                DayActivityModel(
                    dayEpochMs = it.dayEpochMs,
                    questionsStudied = it.questionsStudied,
                    correctCount = it.correctCount,
                    sessionCount = it.sessionCount,
                    totalDurationMs = it.totalDurationMs
                )
            }
        }

    override suspend fun getStudiedDayIndices(): List<Long> =
        withContext(Dispatchers.IO) { sessionDao.getStudiedDayIndices() }

    override fun observeStudiedDayIndices(): Flow<List<Long>> =
        sessionDao.observeStudiedDayIndices()

    override fun observeLastSessionFinishedAt(): Flow<Long?> =
        sessionDao.observeLastSessionFinishedAt()

    override fun observeSessionsForPack(packId: Long): Flow<List<StudySessionModel>> =
        sessionDao.observeSessionHistoryForPack(packId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getSessionWithQuestions(
        sessionId: Long
    ): SessionWithQuestionsModel? =
        withContext(Dispatchers.IO) {
            sessionDao.getSessionWithQuestions(sessionId)?.let { result ->
                SessionWithQuestionsModel(
                    session = result.session.toDomain(),
                    questions = result.questions.map { it.toDomain() }
                )
            }
        }
}
