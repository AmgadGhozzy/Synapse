package io.synapse.ai.data.demo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.data.dao.PackDao
import io.synapse.ai.data.dao.QuestionDao
import io.synapse.ai.data.mapper.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoStudyPackSeeder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val packDao: PackDao,
    private val questionDao: QuestionDao,
) {
    suspend fun seedIfNeeded() {
        val pack = DemoStudyPack.buildPack(context)
        val questions = DemoStudyPack.buildQuestions(context)
        
        // Check if demo questions already exist
        val existingQuestions = questionDao.getByPackId(DemoStudyPack.PACK_ID)
        
        // Insert pack if it doesn't exist
        packDao.insertOrIgnore(pack.toEntity())
        
        if (existingQuestions.isEmpty()) {
            // No existing questions, insert new ones
            questionDao.insertBatchReturnIds(
                questions.map { it.toEntity() }
            )
        }
    }
}