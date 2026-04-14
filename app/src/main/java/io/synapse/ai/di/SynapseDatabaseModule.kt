package io.synapse.ai.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.synapse.ai.data.SynapseDatabase
import io.synapse.ai.data.dao.PackDao
import io.synapse.ai.data.dao.ProgressDao
import io.synapse.ai.data.dao.QuestionDao
import io.synapse.ai.data.dao.SessionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SynapseDatabaseModule {

    @Provides
    @Singleton
    fun provideSynapseDatabase(
        @ApplicationContext context: Context
    ): SynapseDatabase = Room.databaseBuilder(
        context,
        SynapseDatabase::class.java,
        SynapseDatabase.DB_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun providePackDao(db: SynapseDatabase): PackDao = db.packDao()

    @Provides
    fun provideQuestionDao(db: SynapseDatabase): QuestionDao = db.questionDao()

    @Provides
    fun provideProgressDao(db: SynapseDatabase): ProgressDao = db.progressDao()

    @Provides
    fun provideSessionDao(db: SynapseDatabase): SessionDao = db.sessionDao()
}