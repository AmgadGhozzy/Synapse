package io.synapse.ai.domain.repo

import io.synapse.ai.domain.model.QuestionProgressModel
import kotlinx.coroutines.flow.Flow

interface IProgressRepository {
    fun observeProgress(questionId: Long): Flow<QuestionProgressModel?>
    suspend fun saveProgress(progress: QuestionProgressModel)
    suspend fun saveProgressBatch(list: List<QuestionProgressModel>)
    suspend fun getProgress(questionId: Long): QuestionProgressModel?
    suspend fun getLastReviewedForPack(packId: Long): Long?
    suspend fun getReviewedEpochDaysForPack(packId: Long): List<Long>
    suspend fun getAllReviewedEpochDays(): List<io.synapse.ai.data.entity.PackReviewedDay>
    suspend fun getAllProgressForExport(): List<QuestionProgressModel>
}
