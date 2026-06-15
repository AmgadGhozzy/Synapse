package io.synapse.ai.features.add_pdf.domain.usecase

import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed interface ValidationResult {
    data object Success : ValidationResult
    data class FileTooLarge(val sizeMb: Float, val limitMb: Int, val isPro: Boolean) : ValidationResult
    data class PageLimitExceeded(val pageCount: Int, val limit: Int, val isPro: Boolean) : ValidationResult
    data class UnsupportedType(val ext: String) : ValidationResult
}

class ValidateImportedDocumentUseCase @Inject constructor(
    private val configProvider: ValidationConfigProvider
) {
    suspend operator fun invoke(
        fileName: String,
        sizeMb: Float,
        pageCount: Int?
    ): ValidationResult {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        if (ext !in setOf("pdf", "doc", "docx")) {
            return ValidationResult.UnsupportedType(ext)
        }

        val maxFileSizeMb = configProvider.addPdfMaxFileSizeMbFlow.first()
        val isPro = configProvider.isPremiumFlow.first()

        if (sizeMb > maxFileSizeMb) {
            val proLimit = configProvider.proMaxFileSizeMb
            return ValidationResult.FileTooLarge(sizeMb, if (isPro) maxFileSizeMb else proLimit, isPro)
        }

        if (pageCount != null) {
            val maxPages = configProvider.ocrMaxPagesFlow.first()
            if (pageCount > maxPages) {
                return ValidationResult.PageLimitExceeded(pageCount, maxPages, isPro)
            }
        }

        return ValidationResult.Success
    }
}
