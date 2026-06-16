package io.synapse.ai.domains.config.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import io.synapse.ai.BuildConfig
import io.synapse.ai.domains.config.model.DefaultConfig
import io.synapse.ai.domains.config.repository.IRemoteConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation for Firebase Remote Config.
 */
@Singleton
class RemoteConfigImpl @Inject constructor() : IRemoteConfig {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 60 else 600
            fetchTimeoutInSeconds = 10
        }
        
        // Use our Kotlin Map-based defaults instead of XML
        remoteConfig.setDefaultsAsync(DefaultConfig.toMap())
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    override suspend fun fetchAndActivate() {
        try {
            remoteConfig.fetchAndActivate().await()
            Log.d("RemoteConfig", "Config fetched and activated")
        } catch (e: Exception) {
            Log.e("RemoteConfig", "Fetch failed, using defaults: ${e.message}")
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (remoteConfig.all.containsKey(key)) {
            remoteConfig.getBoolean(key)
        } else {
            defaultValue
        }
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return if (remoteConfig.all.containsKey(key)) {
            remoteConfig.getLong(key)
        } else {
            defaultValue
        }
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return if (remoteConfig.all.containsKey(key)) {
            remoteConfig.getDouble(key)
        } else {
            defaultValue
        }
    }

    override fun getString(key: String, defaultValue: String): String {
        val value = remoteConfig.getString(key)
        // getString returns empty string if key is not found, so we check inclusion
        return if (value.isNotEmpty() && remoteConfig.all.containsKey(key)) {
            value
        } else {
            defaultValue
        }
    }
}


