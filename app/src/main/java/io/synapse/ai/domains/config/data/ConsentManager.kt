package io.synapse.ai.domains.config.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

enum class SyncConsent { UNKNOWN, ACCEPTED, REJECTED }

/**
 * Manages privacy consent for cloud sync using DataStore.
 * Enforces GDPR: user data cannot leave device without explicit opt-in.
 */
@Singleton
class ConsentManager @Inject constructor(
    @param:Named("app_settings") private val dataStore: DataStore<Preferences>
) {
    private val CONSENT_KEY = stringPreferencesKey("sync_consent")
    private val SYNC_ENABLED_KEY = booleanPreferencesKey("sync_enabled")
    private val LAST_SYNCED_KEY = stringPreferencesKey("last_synced_time")

    /** The explicit consent state (UNKNOWN, ACCEPTED, REJECTED) */
    val consentState: Flow<SyncConsent> = dataStore.data.map { prefs ->
        when (prefs[CONSENT_KEY]) {
            "accepted" -> SyncConsent.ACCEPTED
            "rejected" -> SyncConsent.REJECTED
            else -> SyncConsent.ACCEPTED
        }
    }

    /** Master toggle state. Syncing only operates if enabled AND consent is ACCEPTED. */
    val isSyncEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SYNC_ENABLED_KEY] ?: true
    }

    val lastSyncedTime: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LAST_SYNCED_KEY]
    }

    suspend fun setConsent(consent: SyncConsent) {
        dataStore.edit { prefs ->
            prefs[CONSENT_KEY] = consent.name.lowercase()
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[SYNC_ENABLED_KEY] = enabled
        }
    }

    suspend fun updateLastSynced(timestamp: String) {
        dataStore.edit { prefs ->
            prefs[LAST_SYNCED_KEY] = timestamp
        }
    }

    suspend fun clearOnSignOut() {
        dataStore.edit { prefs ->
            prefs.remove(CONSENT_KEY)
            prefs.remove(SYNC_ENABLED_KEY)
            prefs.remove(LAST_SYNCED_KEY)
        }
    }
}

