package com.venom.synapse.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.venom.synapse.data.dao.PackDao
import com.venom.synapse.data.dao.ProgressDao
import com.venom.synapse.data.dao.QuestionDao
import com.venom.synapse.data.dao.SessionDao
import com.venom.synapse.data.entity.PackEntity
import com.venom.synapse.data.entity.QuestionEntity
import com.venom.synapse.data.entity.QuestionProgressEntity
import com.venom.synapse.data.entity.SessionQuestionCrossRef
import com.venom.synapse.data.entity.StudySessionEntity

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
        const val VERSION = 1
        const val DB_NAME = "synapse_study.db"
    }
}
