package io.synapse.ai.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AppSettingsRepository @Inject constructor(
    @param:Named("app_settings") private val dataStore: DataStore<Preferences>
) {
    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")

    suspend fun isFirstLaunch(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }.first()
    }

    suspend fun setFirstLaunchComplete() {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }
}
