package io.synapse.ai.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.synapse.ai.data.dao.PackDao
import io.synapse.ai.data.dao.ProgressDao
import io.synapse.ai.data.dao.QuestionDao
import io.synapse.ai.data.dao.SessionDao
import io.synapse.ai.data.entity.PackEntity
import io.synapse.ai.data.entity.QuestionEntity
import io.synapse.ai.data.entity.QuestionProgressEntity
import io.synapse.ai.data.entity.SessionQuestionCrossRef
import io.synapse.ai.data.entity.StudySessionEntity

@Database(
    entities = [
        PackEntity::class,
        QuestionEntity::class,
        QuestionProgressEntity::class,
        StudySessionEntity::class,
        SessionQuestionCrossRef::class
    ],
    version = SynapseDatabase.VERSION,
    exportSchema = false
)
@TypeConverters(SynapseConverters::class)
abstract class SynapseDatabase : RoomDatabase() {

    abstract fun packDao(): PackDao
    abstract fun questionDao(): QuestionDao
    abstract fun progressDao(): ProgressDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val VERSION = 3
        const val DB_NAME = "synapse_study.db"
    }
}
