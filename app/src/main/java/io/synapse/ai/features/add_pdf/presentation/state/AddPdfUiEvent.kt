package io.synapse.ai.features.add_pdf.presentation.state

import io.synapse.ai.domains.study.model.QuestionType

sealed interface AddPdfUiEvent {
    data object GoBack : AddPdfUiEvent
    data object DismissError : AddPdfUiEvent

    // ── Source selection ──────────────────────────────────────────
    data class  SourceTabSelected(val tab: SourceTab) : AddPdfUiEvent
    data class  FileSelected(val uri: String, val name: String, val sizeMb: Float) : AddPdfUiEvent
    data object ClearFile : AddPdfUiEvent
    data class  SharedFileReceived(val uriString: String) : AddPdfUiEvent
    data object OcrToggled : AddPdfUiEvent
    data class  PasteTextChanged(val text: String) : AddPdfUiEvent
    data class  WebUrlChanged(val url: String) : AddPdfUiEvent
    data object WebTabLockedClicked : AddPdfUiEvent
    data object ContinueToConfigure : AddPdfUiEvent

    // ── Document Scanner ─────────────────────────────────────────
    /** User taps "Scan Document" button to open ML Kit scanner. */
    data object ScanDocumentClicked : AddPdfUiEvent
    /** ML Kit scanner returned a scanned PDF URI. */
    data class  ScannedPdfReady(val uri: String, val name: String, val sizeMb: Float, val pageCount: Int) : AddPdfUiEvent

    // ── Configure step ────────────────────────────────────────────
    data class  QuestionCountChanged(val count: Int) : AddPdfUiEvent
    data class  QuestionTypeToggled(val type: QuestionType) : AddPdfUiEvent
    data class  DifficultySelected(val difficulty: String) : AddPdfUiEvent
    data class  FocusNotesChanged(val notes: String) : AddPdfUiEvent
    data class  LanguageSelected(val languageCode: String) : AddPdfUiEvent

    // ── Pro / Thinking ────────────────────────────────────────────
    data object ThinkingToggled : AddPdfUiEvent

    // ── Generation ────────────────────────────────────────────────
    data object GeneratePackToggled : AddPdfUiEvent
    data object GenerateSummaryToggled : AddPdfUiEvent
    data object ShowSummaryPaywall : AddPdfUiEvent
    data class GeneratePack(
        val summaryFocus: Set<String> = emptySet(),
        val summaryDepth: String = "standard",
        val summaryLanguage: String = "en"
    ) : AddPdfUiEvent

    /** User taps "Start Now" before generation finishes. */
    data object StartStudyEarly : AddPdfUiEvent

    /** User taps "Save to Device" to copy the imported PDF to Downloads. */
    data object SavePdfClicked : AddPdfUiEvent
}

