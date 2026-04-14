package io.synapse.ai.data.repo

import androidx.room.withTransaction
import io.synapse.ai.data.SynapseDatabase
import io.synapse.ai.domain.repo.ILocalDataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataRepository @Inject constructor(
    private val db: SynapseDatabase,
) : ILocalDataRepository {

    override suspend fun clearAllLocalData() {
        db.withTransaction {
            db.sessionDao().deleteAll()
            db.progressDao().deleteAll()
            db.questionDao().deleteAll()
            db.packDao().deleteAll()
        }
    }
}