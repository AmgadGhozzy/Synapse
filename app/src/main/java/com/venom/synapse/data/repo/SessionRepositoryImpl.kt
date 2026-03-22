package com.venom.synapse.data.repo

import com.venom.synapse.data.mapper.toDomain
import com.venom.synapse.data.mapper.toEntity
import com.venom.synapse.data.dao.SessionDao
import com.venom.synapse.domain.model.DayActivityModel
import com.venom.synapse.domain.model.SessionMode
import com.venom.synapse.domain.model.SessionWithQuestionsModel
import com.venom.synapse.domain.model.StudySessionModel
import com.venom.synapse.domain.repo.ISessionRepository
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

    /**
     * Creates a new study session record.
     *
     * WHY we construct the entity here (not receive it):
     *   The caller only needs to specify packId and mode. Timestamps
     *   and defaults are set at the repository level to ensure consistency
     *   (e.g., startedAt is always System.currentTimeMillis(), never
     *   a stale value from a ViewModel state snapshot).
     */
    override suspend fun startSession(packId: Long, mode: SessionMode): Long =
        withContext(Dispatchers.IO) {
            val session = StudySessionModel(
                packId = packId,
                mode = mode,
                startedAt = System.currentTimeMillis()
            )
            sessionDao.insert(session.toEntity())
        }

    /**
     * Marks a session as complete with a finishedAt timestamp and
     * optional summary JSON.
     *
     * WHY targeted update (not full entity replace):
     *   Only finishedAt and summaryJson change. A targeted SQL UPDATE
     *   avoids overwriting fields like snapshotQuestions and prevents
     *   requiring the caller to hold the full entity.
     */
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

    /**
     * Reactive session history, newest first.
     * Flow automatically invalidates when study_sessions table changes.
     */
    override fun observeSessionsForPack(packId: Long): Flow<List<StudySessionModel>> =
        sessionDao.observeSessionHistoryForPack(packId).map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Assembles a session with its question snapshot.
     *
     * WHY the DAO handles the conditional JOIN:
     *   SessionDao.getSessionWithQuestions() checks snapshotQuestions
     *   and only runs the cross-ref JOIN when needed. This repository
     *   method simply maps the result to domain models.
     */
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
