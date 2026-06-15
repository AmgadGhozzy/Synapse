package io.synapse.ai.features.add_pdf.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.synapse.ai.domains.config.data.AppConfigProvider
import io.synapse.ai.features.add_pdf.data.repository.AIRepositoryImpl
import io.synapse.ai.features.add_pdf.data.resolver.AndroidSourceContentResolver
import io.synapse.ai.features.add_pdf.data.saver.AndroidDocumentSaver
import io.synapse.ai.features.add_pdf.domain.repository.IAIRepository
import io.synapse.ai.features.add_pdf.domain.resolver.SourceContentResolver
import io.synapse.ai.features.add_pdf.domain.saver.DocumentSaver
import io.synapse.ai.features.add_pdf.domain.usecase.ValidationConfigProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AddPdfModule {
    @Binds
    @Singleton
    abstract fun bindAIRepository(impl: AIRepositoryImpl): IAIRepository

    @Binds
    @Singleton
    abstract fun bindSourceContentResolver(impl: AndroidSourceContentResolver): SourceContentResolver

    @Binds
    @Singleton
    abstract fun bindValidationConfigProvider(impl: AppConfigProvider): ValidationConfigProvider

    @Binds
    @Singleton
    abstract fun bindDocumentSaver(impl: AndroidDocumentSaver): DocumentSaver
}
