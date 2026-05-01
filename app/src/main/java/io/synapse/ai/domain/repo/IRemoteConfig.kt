package io.synapse.ai.domain.repo

/**
 * Interface for fetching and accessing remote configuration values.
 */
interface IRemoteConfig {
    /**
     * Fetch and activate remote config values from the remote source.
     */
    suspend fun fetchAndActivate()

    /**
     * Get a boolean value from remote config or the provided default.
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    /**
     * Get a long value from remote config or the provided default.
     */
    fun getLong(key: String, defaultValue: Long): Long

    /**
     * Get a double value from remote config or the provided default.
     */
    fun getDouble(key: String, defaultValue: Double): Double

    /**
     * Get a string value from remote config or the provided default.
     */
    fun getString(key: String, defaultValue: String): String
}
