package com.venom.synapse.features.add_pdf.presentation.state

import androidx.compose.runtime.Immutable
import com.venom.synapse.core.ui.state.QuestionUiModel
import com.venom.synapse.core.ui.state.UiText
import com.venom.synapse.domain.model.QuestionType

enum class AddPdfStep {
    SELECT_PDF,
    EXTRACTING,
    CONFIGURE,
    GENERATING,
    DONE
}

enum class SourceTab { FILE, WEB, TEXT }

fun AddPdfStep.toIndicatorIndex() = when (this) {
    AddPdfStep.SELECT_PDF,
    AddPdfStep.EXTRACTING -> 0

    AddPdfStep.CONFIGURE -> 1
    AddPdfStep.GENERATING -> 2
    AddPdfStep.DONE -> 3
}

@Immutable
data class AddPdfUiState(
    val step: AddPdfStep = AddPdfStep.SELECT_PDF,
    val sourceTab: SourceTab = SourceTab.FILE,
    val fileUri: String? = null,
    val fileName: String? = null,
    val ocrEnabled: Boolean = false,
    val pasteText: String = "",
    val webUrl: String = "",
    val extractedText: String? = null,
    val extractionProgress: Float = 0f,
    val generationProgress: Float = 0f,
    val packTitle: String = "",
    val questionCount: Int = 20,
    val selectedTypes: Set<QuestionType> = setOf(
        QuestionType.MCQ, QuestionType.TRUE_FALSE, QuestionType.FLASHCARD
    ),
    val language: String = "en",
    val focusNotes: String = "",
    val packId: Long = 0L,
    val generatedQuestions: List<QuestionUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val isPackLimitReached: Boolean = false,
    val isOcrFeatureLocked: Boolean = false,
    val isPro: Boolean = false,
)
