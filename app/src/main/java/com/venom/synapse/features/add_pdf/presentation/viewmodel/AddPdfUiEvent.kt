package com.venom.synapse.features.add_pdf.presentation.viewmodel

import android.net.Uri
import com.venom.synapse.features.add_pdf.presentation.state.SourceTab
import com.venom.synapse.domain.model.QuestionType

sealed interface AddPdfUiEvent {
    data object GoBack : AddPdfUiEvent
    data object DismissError : AddPdfUiEvent
    data class SourceTabSelected(val tab: SourceTab) : AddPdfUiEvent
    data class FileSelected(val uri: Uri, val name: String) : AddPdfUiEvent
    data object ClearFile : AddPdfUiEvent
    data object OcrToggled : AddPdfUiEvent
    data class PasteTextChanged(val text: String) : AddPdfUiEvent
    data object ContinueToConfigure : AddPdfUiEvent
    
    data class QuestionCountChanged(val count: Int) : AddPdfUiEvent
    data class QuestionTypeToggled(val type: QuestionType) : AddPdfUiEvent
    data class FocusNotesChanged(val notes: String) : AddPdfUiEvent
    data object GeneratePack : AddPdfUiEvent
    data class LanguageSelected(val languageCode: String) : AddPdfUiEvent
}
