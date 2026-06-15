package io.synapse.ai.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.synapse.ai.domains.premium.data.PremiumPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SynapseUtilModule {

    @Provides
    @Singleton
    @Named("study_settings")
    fun provideStudySettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("study_settings") },
    )

    @Provides
    @Singleton
    @Named("gold")
    fun providePremiumDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("gold") },
    )

    @Provides
    @Singleton
    @Named("app_settings")
    fun provideAppSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("app_settings") },
    )

    @Provides
    @Singleton
    fun providePremiumPreferences(
        @Named("gold") dataStore: DataStore<Preferences>,
    ): PremiumPreferences = PremiumPreferences(dataStore)

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideNowProvider(): () -> Long = { System.currentTimeMillis() }

    @Provides
    fun provideProgressMessageRotator(): io.synapse.ai.core.ui.util.ProgressMessageRotator {
        return io.synapse.ai.core.ui.util.DefaultProgressMessageRotator()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NetworkEntryPoint {
    @Named("RegularOkHttpClient")
    fun getRegularClient(): OkHttpClient

    @Named("AiOkHttpClient")
    fun getAiClient(): OkHttpClient
}
