package io.synapse.ai.core.analytics.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.synapse.ai.core.analytics.model.UserConsent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Single source of truth for user privacy consent.
 *
 * - Backed by DataStore (thread-safe, async).
 * - **Default = Analytics + Crash ENABLED** — users must opt-out.
 * - Observable via [consent] Flow for runtime toggling.
 * - Each flag is independently toggleable.
 *
 * Firebase MUST NOT initialise before [consent] emits its first value.
 */
@Singleton
class ConsentRepository @Inject constructor(
    @param:Named("consent") private val dataStore: DataStore<Preferences>,
) {

    private object Keys {
        val ANALYTICS = booleanPreferencesKey("consent_analytics")
        val CRASH     = booleanPreferencesKey("consent_crash")
        val PUSH      = booleanPreferencesKey("consent_push")
    }

    val consent: Flow<UserConsent> = dataStore.data
        .map { prefs ->
            UserConsent(
                analyticsEnabled = prefs[Keys.ANALYTICS] ?: true,
                crashEnabled     = prefs[Keys.CRASH]     ?: true,
                pushEnabled      = prefs[Keys.PUSH]      ?: true,
            )
        }
        .distinctUntilChanged()

    suspend fun updateAnalytics(enabled: Boolean) {
        dataStore.edit { it[Keys.ANALYTICS] = enabled }
    }

    suspend fun updateCrash(enabled: Boolean) {
        dataStore.edit { it[Keys.CRASH] = enabled }
    }

    suspend fun updatePush(enabled: Boolean) {
        dataStore.edit { it[Keys.PUSH] = enabled }
    }
}
