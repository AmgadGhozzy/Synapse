package io.synapse.ai.features.add_pdf.domain.resolver

import io.synapse.ai.features.add_pdf.domain.model.ResolvedSource
import io.synapse.ai.features.add_pdf.domain.model.SourceRequest

interface SourceContentResolver {
    suspend fun resolve(request: SourceRequest): Result<ResolvedSource>
    suspend fun getPageCount(uri: String): Int?
}
