package io.synapse.ai.features.add_pdf.presentation.coordinator

import io.synapse.ai.core.ui.model.QuestionUiModel

sealed interface AddPdfGenerationUiEvent {
    data class Started(val isUploading: Boolean) : AddPdfGenerationUiEvent
    data object SourceResolved : AddPdfGenerationUiEvent
    data class SourceResolutionFailed(val errorMsg: String? = null) : AddPdfGenerationUiEvent

    // ── Pack events ──────────────────────────────────────────────
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

    /** Pack generation finished (summary may still follow). */
    data class PackCompleted(
        val totalCount: Int,
        val durationMs: Long,
        val sourceKey: String,
        val generatedQuestions: List<QuestionUiModel>
    ) : AddPdfGenerationUiEvent

    // ── Summary events ───────────────────────────────────────────
    /** Emitted when the coordinator transitions from pack → summary phase. */
    data object SummaryPhaseStarted : AddPdfGenerationUiEvent

    data class SummaryMetaCreated(
        val localSummaryId: Long,
        val title: String,
        val emoji: String,
        val color: String,
        val conceptsFound: Int,
        val sectionsExpected: Int,
    ) : AddPdfGenerationUiEvent

    data object SummarySectionAdded : AddPdfGenerationUiEvent

    data class SummaryProgressUpdated(
        val percent: Float,
        val message: String,
        val conceptsFound: Int,
    ) : AddPdfGenerationUiEvent

    data class SummaryCompleted(
        val totalSections: Int,
        val durationMs: Long,
    ) : AddPdfGenerationUiEvent

    // ── Shared ───────────────────────────────────────────────────
    /** Everything requested (pack, summary, or both) is complete. */
    data class AllCompleted(
        val packGenerated: Boolean,
        val summaryGenerated: Boolean,
    ) : AddPdfGenerationUiEvent

    data class GenerationError(
        val error: Throwable,
        val isRecoverable: Boolean,
        val currentAttempt: Int
    ) : AddPdfGenerationUiEvent
}
