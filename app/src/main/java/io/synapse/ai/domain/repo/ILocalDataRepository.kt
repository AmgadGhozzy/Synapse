package io.synapse.ai.domain.repo


interface ILocalDataRepository {

    /**
     * Deletes ALL local user data
     */
    suspend fun clearAllLocalData()
}
