package io.synapse.ai.domains.config.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.synapse.ai.domains.config.data.RemoteConfigImpl
import io.synapse.ai.domains.config.repository.IRemoteConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigModule {
    @Binds
    @Singleton
    abstract fun bindRemoteConfig(impl: RemoteConfigImpl): IRemoteConfig
}
