package com.venom.synapse.domain.repo

interface IPdfRepository {
    /**
     * Extracts text from a local file URI — handles both PDF and image sources.
     * Uses OCR for images and PDFs (since PDFs are rendered as bitmaps).
     */
    suspend fun extractText(
        path: String,
        isImage: Boolean = false,
        useOcr: Boolean = false,
        maxPages: Int = 20,
    ): Result<String>
}
