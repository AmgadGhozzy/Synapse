package io.synapse.ai.core.analytics.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import io.synapse.ai.core.analytics.TrackingManager
import io.synapse.ai.core.analytics.TrackingManagerImpl
import io.synapse.ai.core.analytics.data.ConsentRepository
import io.synapse.ai.core.analytics.providers.AnalyticsProvider
import io.synapse.ai.core.analytics.providers.CrashProvider
import io.synapse.ai.core.analytics.providers.FirebaseAnalyticsProvider
import io.synapse.ai.core.analytics.providers.FirebaseCrashProvider
import io.synapse.ai.core.analytics.providers.FirebasePushProvider
import io.synapse.ai.core.analytics.providers.PushProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsBindsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsProvider(impl: FirebaseAnalyticsProvider): AnalyticsProvider

    @Binds
    @Singleton
    abstract fun bindCrashProvider(impl: FirebaseCrashProvider): CrashProvider

    @Binds
    @Singleton
    abstract fun bindPushProvider(impl: FirebasePushProvider): PushProvider

    @Binds
    @Singleton
    abstract fun bindTrackingManager(impl: TrackingManagerImpl): TrackingManager
}

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsProvidesModule {

    @Provides
    @Singleton
    @Named("consent")
    fun provideConsentDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("consent_prefs")
    }

    @Provides
    @Singleton
    fun provideConsentRepository(
        @Named("consent") dataStore: DataStore<Preferences>,
    ): ConsentRepository = ConsentRepository(dataStore)
}
