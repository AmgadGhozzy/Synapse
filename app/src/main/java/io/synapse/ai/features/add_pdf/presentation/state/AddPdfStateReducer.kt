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
            packUuid = null
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

    fun onGenerationCompleted(
        state: AddPdfUiState,
        sourceDescription: String,
        generatedQuestions: List<io.synapse.ai.core.ui.model.QuestionUiModel>
    ): AddPdfUiState {
        return state.copy(
            step = AddPdfStep.DONE,
            isLoading = false,
            generatingInBackground = false,
            generationProgress = 1f,
            packTitle = state.streamPackTitle,
            sourceDescription = sourceDescription,
            generatedQuestions = generatedQuestions,
            streamStage = "Pack ready!"
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
