package io.synapse.ai.features.add_pdf.domain.saver

interface DocumentSaver {
    suspend fun savePdfToDownloads(uriString: String, destName: String): Result<Unit>
}
