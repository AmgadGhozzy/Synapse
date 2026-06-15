package io.synapse.ai.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.synapse.ai.core.database.DatabaseMigrations
import io.synapse.ai.core.database.SynapseDatabase
import io.synapse.ai.core.database.dao.PackDao
import io.synapse.ai.core.database.dao.ProgressDao
import io.synapse.ai.core.database.dao.QuestionDao
import io.synapse.ai.core.database.dao.SessionDao
import io.synapse.ai.core.database.dao.SummaryDao
import io.synapse.ai.core.database.dao.MarketplacePackDao
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
        .addMigrations(
            DatabaseMigrations.MIGRATION_3_4,
            DatabaseMigrations.MIGRATION_4_5,
            DatabaseMigrations.MIGRATION_5_6,
        )
        .build()

    @Provides
    fun providePackDao(db: SynapseDatabase): PackDao = db.packDao()

    @Provides
    fun provideQuestionDao(db: SynapseDatabase): QuestionDao = db.questionDao()

    @Provides
    fun provideProgressDao(db: SynapseDatabase): ProgressDao = db.progressDao()

    @Provides
    fun provideSessionDao(db: SynapseDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideMarketplacePackDao(db: SynapseDatabase): MarketplacePackDao =
        db.marketplacePackDao()

    @Provides
    fun provideSummaryDao(db: SynapseDatabase): SummaryDao = db.summaryDao()
}


