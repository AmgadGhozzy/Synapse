package com.venom.synapse.data.repo

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that retries a failed entitlement sync.
 */
@HiltWorker
class EntitlementSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val manager: EntitlementManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return manager.forceSyncAwait().fold(
            onSuccess = { Result.success() },
            onFailure = {
                if (runAttemptCount < 4) Result.retry() else Result.failure()
            }
        )
    }
}
