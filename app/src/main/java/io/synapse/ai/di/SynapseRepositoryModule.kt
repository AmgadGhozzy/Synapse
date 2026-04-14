package io.synapse.ai.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.synapse.ai.data.repo.AIRepositoryImpl
import io.synapse.ai.data.repo.AuthRepositoryImpl
import io.synapse.ai.data.repo.LocalDataRepository
import io.synapse.ai.data.repo.PackRepositoryImpl
import io.synapse.ai.data.repo.PremiumRepositoryImpl
import io.synapse.ai.data.repo.ProgressRepositoryImpl
import io.synapse.ai.data.repo.QuestionRepositoryImpl
import io.synapse.ai.data.repo.RemoteConfigImpl
import io.synapse.ai.data.repo.SessionRepositoryImpl
import io.synapse.ai.domain.repo.IAIRepository
import io.synapse.ai.domain.repo.IAuthRepository
import io.synapse.ai.domain.repo.ILocalDataRepository
import io.synapse.ai.domain.repo.IPackRepository
import io.synapse.ai.domain.repo.IPremiumRepository
import io.synapse.ai.domain.repo.IProgressRepository
import io.synapse.ai.domain.repo.IQuestionRepository
import io.synapse.ai.domain.repo.IRemoteConfig
import io.synapse.ai.domain.repo.ISessionRepository
import io.synapse.ai.domain.repo.ISocialProofRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SynapseRepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    @Binds @Singleton
    abstract fun bindPackRepository(impl: PackRepositoryImpl): IPackRepository

    @Binds @Singleton
    abstract fun bindQuestionRepository(impl: QuestionRepositoryImpl): IQuestionRepository

    @Binds @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): IProgressRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): ISessionRepository

    @Binds @Singleton
    abstract fun bindAIRepository(impl: AIRepositoryImpl): IAIRepository

    @Binds @Singleton
    abstract fun bindPremiumRepository(impl: PremiumRepositoryImpl): IPremiumRepository

    @Binds @Singleton
    abstract fun bindSocialProofRepository(impl: PremiumRepositoryImpl): ISocialProofRepository

    @Binds @Singleton
    abstract fun bindLocalDataRepository(impl: LocalDataRepository): ILocalDataRepository

    @Binds @Singleton
    abstract fun bindRemoteConfig(impl: RemoteConfigImpl): IRemoteConfig
}