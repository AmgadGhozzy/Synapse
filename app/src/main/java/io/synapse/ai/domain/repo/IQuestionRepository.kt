package io.synapse.ai.domain.repo

import io.synapse.ai.domain.model.QuestionModel
import kotlinx.coroutines.flow.Flow

interface IQuestionRepository {

    fun observeQuestionsForPack(packId: Long): Flow<List<QuestionModel>>

    suspend fun insertQuestions(questions: List<QuestionModel>)

    suspend fun getDueQuestions(packId: Long, limit: Int): List<QuestionModel>

    suspend fun getQuestionById(id: Long): QuestionModel?

    suspend fun countByPack(packId: Long): Int

    suspend fun getAllQuestionsForExport(): List<QuestionModel>

    suspend fun updateQuestion(question: QuestionModel)

    suspend fun softDeleteQuestion(questionId: Long)

    suspend fun updateOrder(ordered: List<QuestionModel>)
}