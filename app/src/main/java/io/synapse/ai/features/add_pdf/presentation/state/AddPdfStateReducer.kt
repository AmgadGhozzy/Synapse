package io.synapse.ai.features.add_pdf.presentation.state

import javax.inject.Inject

class AddPdfStateReducer @Inject constructor() {
    
    fun onGenerationStarted(state: AddPdfUiState, isUploading: Boolean): AddPdfUiState {
        return state.copy(
            step = AddPdfStep.GENERATING,
            isLoading = true,
            isUploading = isUploading,
            uploadProgress = null,
            generationProgress = 0f,
            error = null,
            // Pack fields
            questionsCompleted = 0,
            questionsExpected = state.questionCount,
            conceptsFound = 0,
            streamStage = "",
            progressMessageIndex = 0,
            streamPackTitle = "",
            streamPackEmoji = "",
            streamPackColor = "",
            canStartEarly = false,
            generatingInBackground = false,
            packId = 0L,
            packUuid = null,
            // Summary fields
            streamSummaryTitle = "",
            streamSummaryEmoji = "",
            streamSummaryColor = "",
            sectionsCompleted = 0,
            sectionsExpected = 0,
            summaryConcepts = 0,
            summaryProgress = 0f,
            summaryStage = "",
            summaryId = 0L,
            // Phase
            generationPhase = DualGenerationPhase.PACK,
            packWasGenerated = false,
            summaryWasGenerated = false,
        )
    }

    fun onPackMetaCreated(
        state: AddPdfUiState,
        localPackId: Long,
        packUuid: String,
        title: String,
        emoji: String,
        color: String,
        conceptsFound: Int,
        questionsExpected: Int
    ): AddPdfUiState {
        return state.copy(
            packId = localPackId,
            packUuid = packUuid,
            streamPackTitle = title,
            streamPackEmoji = emoji,
            streamPackColor = color,
            conceptsFound = conceptsFound,
            questionsExpected = questionsExpected,
            streamStage = "Extracted \$conceptsFound concepts — building curriculum…",
            generationProgress = 0.08f
        )
    }

    fun onCurriculumOrganized(state: AddPdfUiState, moduleCount: Int): AddPdfUiState {
        return state.copy(
            streamStage = "Organized \$moduleCount modules — generating questions…",
            generationProgress = 0.15f
        )
    }

    fun onQuestionAdded(state: AddPdfUiState): AddPdfUiState {
        val done = state.questionsCompleted + 1
        val expected = state.questionsExpected.coerceAtLeast(1)
        val canStart = !state.generatingInBackground && done >= (expected * 0.20f).toInt().coerceAtLeast(1)
        return state.copy(
            questionsCompleted = done,
            canStartEarly = canStart
        )
    }

    fun onProgressUpdated(state: AddPdfUiState, percent: Float, message: String, conceptsFound: Int): AddPdfUiState {
        return state.copy(
            generationProgress = percent,
            streamStage = message.ifBlank { state.streamStage },
            conceptsFound = conceptsFound.takeIf { it > 0 } ?: state.conceptsFound
        )
    }

    /**
     * Pack finished. If summary is also requested, we stay in GENERATING step
     * and just mark pack as done; the coordinator will proceed to summary.
     */
    fun onPackCompleted(
        state: AddPdfUiState,
        sourceDescription: String,
        generatedQuestions: List<io.synapse.ai.core.ui.model.QuestionUiModel>
    ): AddPdfUiState {
        return state.copy(
            generationProgress = 1f,
            packTitle = state.streamPackTitle,
            sourceDescription = sourceDescription,
            generatedQuestions = generatedQuestions,
            streamStage = "Pack ready!",
            packWasGenerated = true,
        )
    }

    // ── Summary reducers ─────────────────────────────────────────

    fun onSummaryPhaseStarted(state: AddPdfUiState): AddPdfUiState {
        return state.copy(
            generationPhase = DualGenerationPhase.SUMMARY,
            summaryProgress = 0f,
            summaryStage = "",
            sectionsCompleted = 0,
            sectionsExpected = 0,
            summaryConcepts = 0,
            streamSummaryTitle = "",
            streamSummaryEmoji = "",
            streamSummaryColor = "",
        )
    }

    fun onSummaryMetaCreated(
        state: AddPdfUiState,
        localSummaryId: Long,
        title: String,
        emoji: String,
        color: String,
        conceptsFound: Int,
        sectionsExpected: Int,
    ): AddPdfUiState {
        return state.copy(
            summaryId = localSummaryId,
            streamSummaryTitle = title,
            streamSummaryEmoji = emoji,
            streamSummaryColor = color,
            summaryConcepts = conceptsFound,
            sectionsExpected = sectionsExpected,
            summaryStage = "Extracted \$conceptsFound concepts — writing sections…",
            summaryProgress = 0.08f,
        )
    }

    fun onSummarySectionAdded(state: AddPdfUiState): AddPdfUiState {
        val done = state.sectionsCompleted + 1
        val expected = state.sectionsExpected.coerceAtLeast(1)
        val progress = (done.toFloat() / expected).coerceIn(0.1f, 0.95f)
        return state.copy(
            sectionsCompleted = done,
            summaryProgress = progress,
        )
    }

    fun onSummaryProgressUpdated(
        state: AddPdfUiState,
        percent: Float,
        message: String,
        conceptsFound: Int,
    ): AddPdfUiState {
        return state.copy(
            summaryProgress = percent,
            summaryStage = message.ifBlank { state.summaryStage },
            summaryConcepts = conceptsFound.takeIf { it > 0 } ?: state.summaryConcepts,
        )
    }

    fun onSummaryCompleted(state: AddPdfUiState): AddPdfUiState {
        return state.copy(
            summaryProgress = 1f,
            summaryStage = "Summary ready!",
            summaryWasGenerated = true,
        )
    }

    // ── All completed ────────────────────────────────────────────

    fun onAllCompleted(state: AddPdfUiState): AddPdfUiState {
        return state.copy(
            step = AddPdfStep.DONE,
            isLoading = false,
            generatingInBackground = false,
        )
    }

    fun onGenerationFailed(
        state: AddPdfUiState,
        isBackground: Boolean,
        partialQuestions: List<io.synapse.ai.core.ui.model.QuestionUiModel> = emptyList()
    ): AddPdfUiState {
        return if (!isBackground) {
            state.copy(step = AddPdfStep.CONFIGURE, isLoading = false, generationProgress = 0f, error = null)
        } else {
            state.copy(
                step = AddPdfStep.DONE,
                isLoading = false,
                generatingInBackground = false,
                generatedQuestions = partialQuestions
            )
        }
    }
}
