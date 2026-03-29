package com.venom.synapse.features.add_pdf.presentation.state

import com.venom.synapse.domain.model.QuestionType

sealed interface AddPdfUiEvent {
    data object GoBack : AddPdfUiEvent
    data object DismissError : AddPdfUiEvent
    data class  SourceTabSelected(val tab: SourceTab) : AddPdfUiEvent
    data class  FileSelected(val uri: String, val name: String) : AddPdfUiEvent
    data object ClearFile : AddPdfUiEvent
    data object OcrToggled : AddPdfUiEvent
    data class  PasteTextChanged(val text: String) : AddPdfUiEvent
    data class  WebUrlChanged(val url: String) : AddPdfUiEvent
    data object WebTabLockedClicked : AddPdfUiEvent
    data object ContinueToConfigure : AddPdfUiEvent

    data class  QuestionCountChanged(val count: Int) : AddPdfUiEvent
    data class  QuestionTypeToggled(val type: QuestionType) : AddPdfUiEvent
    data class  FocusNotesChanged(val notes: String) : AddPdfUiEvent
    data object GeneratePack : AddPdfUiEvent
    data class  LanguageSelected(val languageCode: String) : AddPdfUiEvent
}
