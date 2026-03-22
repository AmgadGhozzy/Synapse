package com.venom.synapse.di

import com.venom.synapse.data.repo.AIRepositoryImpl
import com.venom.synapse.data.repo.AuthRepositoryImpl
import com.venom.synapse.data.repo.LocalDataRepository
import com.venom.synapse.data.repo.PackRepositoryImpl
import com.venom.synapse.data.repo.PdfRepositoryImpl
import com.venom.synapse.data.repo.ProgressRepositoryImpl
import com.venom.synapse.data.repo.QuestionRepositoryImpl
import com.venom.synapse.data.repo.SessionRepositoryImpl
import com.venom.synapse.data.repo.SupabasePremiumRepository
import com.venom.synapse.data.repo.VisionRepositoryImpl
import com.venom.synapse.domain.repo.IAIRepository
import com.venom.synapse.domain.repo.IAuthRepository
import com.venom.synapse.domain.repo.ILocalDataRepository
import com.venom.synapse.domain.repo.IPackRepository
import com.venom.synapse.domain.repo.IPdfRepository
import com.venom.synapse.domain.repo.IProgressRepository
import com.venom.synapse.domain.repo.IQuestionRepository
import com.venom.synapse.domain.repo.ISessionRepository
import com.venom.synapse.domain.repo.IVisionRepository
import com.venom.synapse.domain.repo.PremiumRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class SynapseRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindPackRepository(
        impl: PackRepositoryImpl
    ): IPackRepository

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        impl: QuestionRepositoryImpl
    ): IQuestionRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): IProgressRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): ISessionRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(
        impl: AIRepositoryImpl
    ): IAIRepository

    @Binds
    @Singleton
    abstract fun bindPdfRepository(
        impl: PdfRepositoryImpl
    ): IPdfRepository

    @Binds
    @Singleton
    abstract fun bindVisionRepository(impl: VisionRepositoryImpl): IVisionRepository

    @Binds
    @Singleton
    abstract fun bindPremiumRepository(
        impl: SupabasePremiumRepository
    ): PremiumRepository

    @Binds
    @Singleton
    abstract fun bindLocalDataRepository(
        impl: LocalDataRepository,
    ): ILocalDataRepository
}
