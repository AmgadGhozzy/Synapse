package com.venom.synapse.domain.repo

import com.venom.synapse.domain.model.QuestionProgressModel
import kotlinx.coroutines.flow.Flow

interface IProgressRepository {
    fun observeProgress(questionId: Long): Flow<QuestionProgressModel?>
    suspend fun saveProgress(progress: QuestionProgressModel)
    suspend fun saveProgressBatch(list: List<QuestionProgressModel>)
    suspend fun getProgress(questionId: Long): QuestionProgressModel?
    suspend fun getLastReviewedForPack(packId: Long): Long?
}
