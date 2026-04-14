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
import io.synapse.ai.data.repo.EntitlementCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SynapseUtilModule {

    // ── Study Settings DataStore (existing) ───────────────────────
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
    @Named("entitlement")
    fun provideEntitlementDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("entitlement") },
    )

    @Provides
    @Singleton
    @Named("app_settings")
    fun provideAppSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("app_settings") },
    )

    // ── EntitlementCache ──────────────────────────────────────────
    @Provides
    @Singleton
    fun provideEntitlementCache(
        @Named("entitlement") dataStore: DataStore<Preferences>,
    ): EntitlementCache = EntitlementCache(dataStore)

    // ── WorkManager ───────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)

    // ── Dispatchers ───────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideNowProvider(): () -> Long = { System.currentTimeMillis() }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NetworkEntryPoint {
    @Named("RegularOkHttpClient")
    fun getRegularClient(): OkHttpClient

    @Named("AiOkHttpClient")
    fun getAiClient(): OkHttpClient
}
