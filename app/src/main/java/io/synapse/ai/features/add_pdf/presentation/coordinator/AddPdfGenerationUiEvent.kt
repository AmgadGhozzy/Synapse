package io.synapse.ai.features.add_pdf.presentation.coordinator

import io.synapse.ai.core.ui.model.QuestionUiModel

sealed interface AddPdfGenerationUiEvent {
    data class Started(val isUploading: Boolean) : AddPdfGenerationUiEvent
    data object SourceResolved : AddPdfGenerationUiEvent
    data class SourceResolutionFailed(val errorMsg: String? = null) : AddPdfGenerationUiEvent

    data class PackMetaCreated(
        val localPackId: Long,
        val packUuid: String,
        val title: String,
        val emoji: String,
        val color: String,
        val conceptsFound: Int,
        val questionsExpected: Int
    ) : AddPdfGenerationUiEvent

    data class CurriculumOrganized(val moduleCount: Int) : AddPdfGenerationUiEvent

    data object QuestionAdded : AddPdfGenerationUiEvent

    data class ProgressUpdated(val percent: Float, val message: String, val conceptsFound: Int) : AddPdfGenerationUiEvent

    data class Completed(
        val totalCount: Int,
        val durationMs: Long,
        val sourceKey: String,
        val generatedQuestions: List<QuestionUiModel>
    ) : AddPdfGenerationUiEvent

    data class GenerationError(
        val error: Throwable,
        val isRecoverable: Boolean,
        val currentAttempt: Int
    ) : AddPdfGenerationUiEvent
}
