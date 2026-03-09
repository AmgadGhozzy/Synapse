package com.venom.synapse.domain.repo

import com.venom.synapse.domain.model.QuestionModel
import kotlinx.coroutines.flow.Flow

interface IQuestionRepository {
    fun observeQuestionsForPack(packId: Long): Flow<List<QuestionModel>>
    suspend fun insertQuestions(questions: List<QuestionModel>)
    suspend fun getDueQuestions(packId: Long, limit: Int): List<QuestionModel>
    suspend fun getQuestionById(id: Long): QuestionModel?
    suspend fun countByPack(packId: Long): Int
}
