package io.synapse.ai.domain.repo

import io.synapse.ai.domain.model.PackModel
import kotlinx.coroutines.flow.Flow

interface IPackRepository {

    fun observeAllPacks(): Flow<List<PackModel>>

    suspend fun createPack(pack: PackModel): Long

    suspend fun getPackById(id: Long): PackModel?

    suspend fun updateQuestionCount(packId: Long, count: Int)

    suspend fun updateModules(packId: Long, modulesJson: String)

    suspend fun updatePack(pack: PackModel)

    suspend fun deletePack(id: Long)

    suspend fun getAllPacksForExport(): List<PackModel>
}