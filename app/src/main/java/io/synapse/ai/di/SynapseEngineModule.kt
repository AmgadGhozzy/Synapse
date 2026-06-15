package io.synapse.ai.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.synapse.ai.domains.study.srs.EngineConfig
import io.synapse.ai.domains.study.srs.StudyEngine
import io.synapse.ai.domains.study.srs.StudyEngineImpl
import io.synapse.ai.domains.study.srs.SystemTimeProvider
import io.synapse.ai.domains.study.srs.TimeProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SynapseEngineModule {

    @Binds
    @Singleton
    abstract fun bindStudyEngine(impl: StudyEngineImpl): StudyEngine

    @Binds
    @Singleton
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    companion object {

        @Provides
        @Singleton
        fun provideEngineConfig(): EngineConfig = EngineConfig()

        @Provides
        @Singleton
        fun provideSystemTimeProvider(): SystemTimeProvider = SystemTimeProvider()
    }
}

