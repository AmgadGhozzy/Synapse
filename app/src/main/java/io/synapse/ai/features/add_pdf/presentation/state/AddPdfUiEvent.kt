package io.synapse.ai.features.add_pdf.presentation.state

import io.synapse.ai.domain.model.QuestionType

sealed interface AddPdfUiEvent {
    data object GoBack : AddPdfUiEvent
    data object DismissError : AddPdfUiEvent

    // ── Source selection ──────────────────────────────────────────
    data class  SourceTabSelected(val tab: SourceTab) : AddPdfUiEvent
    data class  FileSelected(val uri: String, val name: String, val sizeMb: Float) : AddPdfUiEvent
    data object ClearFile : AddPdfUiEvent
    data object OcrToggled : AddPdfUiEvent
    data class  PasteTextChanged(val text: String) : AddPdfUiEvent
    data class  WebUrlChanged(val url: String) : AddPdfUiEvent
    data object WebTabLockedClicked : AddPdfUiEvent
    data object ContinueToConfigure : AddPdfUiEvent

    // ── Configure step ────────────────────────────────────────────
    data class  QuestionCountChanged(val count: Int) : AddPdfUiEvent
    data class  QuestionTypeToggled(val type: QuestionType) : AddPdfUiEvent
    data class  DifficultySelected(val difficulty: String) : AddPdfUiEvent
    data class  FocusNotesChanged(val notes: String) : AddPdfUiEvent
    data class  LanguageSelected(val languageCode: String) : AddPdfUiEvent

    // ── Pro / Thinking ────────────────────────────────────────────
    data object ThinkingToggled : AddPdfUiEvent

    // ── Generation ────────────────────────────────────────────────
    data object GeneratePack : AddPdfUiEvent
}
