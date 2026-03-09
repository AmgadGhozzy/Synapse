package com.venom.synapse.domain.repo

import com.venom.synapse.domain.model.PackModel
import kotlinx.coroutines.flow.Flow

interface IPackRepository {
    fun observeAllPacks(): Flow<List<PackModel>>
    suspend fun createPack(pack: PackModel): Long
    suspend fun getPackById(id: Long): PackModel?
    suspend fun deletePack(id: Long)
}
