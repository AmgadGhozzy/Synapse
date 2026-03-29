package com.venom.synapse.features.add_pdf.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.R
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.core.ui.state.UiText
import com.venom.synapse.core.ui.state.toUiModel
import com.venom.synapse.data.repo.AppConfigProvider
import com.venom.synapse.domain.model.GeneratedPackResult
import com.venom.synapse.domain.model.GenerationConfig
import com.venom.synapse.domain.model.OcrEngine
import com.venom.synapse.domain.model.PackModel
import com.venom.synapse.domain.model.QuestionType
import com.venom.synapse.domain.model.SourceType
import com.venom.synapse.domain.repo.IAIRepository
import com.venom.synapse.domain.repo.IPackRepository
import com.venom.synapse.domain.repo.IQuestionRepository
import com.venom.synapse.domain.repo.IVisionRepository
import com.venom.synapse.features.add_pdf.presentation.state.AddPdfStep
import com.venom.synapse.features.add_pdf.presentation.state.AddPdfUiEvent
import com.venom.synapse.features.add_pdf.presentation.state.AddPdfUiState
import com.venom.synapse.features.add_pdf.presentation.state.SourceTab
import com.venom.synapse.navigation.SynapseScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddPdfViewModel @Inject constructor(
    private val visionRepo   : IVisionRepository,
    private val aiRepo       : IAIRepository,
    private val packRepo     : IPackRepository,
    private val questionRepo : IQuestionRepository,
    private val appConfig    : AppConfigProvider,
    private val ioDispatcher : CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState   = MutableStateFlow(AddPdfUiState())
    val uiState: StateFlow<AddPdfUiState> = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    private var generationJob  : Job? = null
    private var generatedResult: GeneratedPackResult? = null

    init {
        checkPackLimit()
        _uiState.update {
            it.copy(
                isOcrFeatureLocked = appConfig.isOcrProLocked,
                isPro              = !appConfig.isOcrProLocked,
            )
        }
    }

    fun onEvent(event: AddPdfUiEvent) {
        when (event) {
            is AddPdfUiEvent.GoBack               -> goBack()
            is AddPdfUiEvent.DismissError         -> clearError()
            is AddPdfUiEvent.SourceTabSelected    -> updateSourceTab(event.tab)
            is AddPdfUiEvent.FileSelected         -> onFileSelected(event.uri, event.name)
            is AddPdfUiEvent.ClearFile            -> clearSelectedFile()
            is AddPdfUiEvent.OcrToggled           -> toggleOcr()
            is AddPdfUiEvent.PasteTextChanged     -> updatePasteText(event.text)
            is AddPdfUiEvent.WebUrlChanged        -> updateWebUrl(event.url)
            is AddPdfUiEvent.WebTabLockedClicked  -> onWebTabLockedClicked()
            is AddPdfUiEvent.ContinueToConfigure  -> continueToConfig()
            is AddPdfUiEvent.QuestionCountChanged -> updateQuestionCount(event.count)
            is AddPdfUiEvent.QuestionTypeToggled  -> toggleQuestionType(event.type)
            is AddPdfUiEvent.FocusNotesChanged    -> updateFocusNotes(event.notes)
            is AddPdfUiEvent.GeneratePack         -> generateQuestions()
            is AddPdfUiEvent.LanguageSelected     -> updateLanguage(event.languageCode)
        }
    }


    private fun updateSourceTab(tab: SourceTab) {
        _uiState.update { it.copy(sourceTab = tab, error = null) }
    }

    private fun onFileSelected(uri: String, fileName: String) {
        val ocrOnAtPickTime = _uiState.value.ocrEnabled
        _uiState.update {
            it.copy(
                fileUri       = uri,
                fileName      = fileName,
                extractedText = null,
                error         = null,
            )
        }
        if (!ocrOnAtPickTime) {
            extractPdf(
                fileUri = uri,
                useCloudOcr = false,
                successStep = AddPdfStep.SELECT_PDF,
            )
        }
    }

    private fun clearSelectedFile() {
        _uiState.update {
            it.copy(
                fileUri       = null,
                fileName      = null,
                ocrEnabled    = false,
                extractedText = null,
                isLoading     = false,
                error         = null,
            )
        }
    }

    private fun onWebTabLockedClicked() {
        _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_web_youtube_import)))
    }

    private fun toggleOcr() {
        val state = _uiState.value
        if (state.isOcrFeatureLocked) {
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_pro_ocr)))
            return
        }
        val newOcr = !state.ocrEnabled

        val clearExtracted = !newOcr && state.fileUri != null
        _uiState.update {
            it.copy(
                ocrEnabled    = newOcr,
                extractedText = if (clearExtracted) null else it.extractedText,
            )
        }

        // If OCR turned OFF and a file is already selected, auto-run local extraction now.
        if (!newOcr && state.fileUri != null) {
            extractPdf(
                fileUri = state.fileUri,
                useCloudOcr = false,
                successStep = AddPdfStep.SELECT_PDF,
            )
        }
    }

    private fun continueToConfig() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_unlimited_packs)))
            return
        }

        val state = _uiState.value
        when (state.sourceTab) {

            SourceTab.FILE -> {
                when {
                    state.ocrEnabled -> {
                        if (appConfig.isOcrProLocked) {
                            _uiEffects.tryEmit(
                                UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_pro_ocr))
                            )
                        } else {
                            val fileUri = state.fileUri ?: return
                            _uiState.update { it.copy(extractedText = null) }
                            extractPdf(
                                fileUri = fileUri,
                                useCloudOcr = true,
                                successStep = AddPdfStep.CONFIGURE,
                            )
                        }
                    }

                    state.extractedText != null -> {
                        _uiState.update { it.copy(step = AddPdfStep.CONFIGURE) }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(error = UiText.Raw(R.string.add_pdf_error_file_still_reading))
                        }
                    }
                }
            }

            SourceTab.TEXT -> {
                val text = state.pasteText.trim()
                if (text.length < 10) {
                    _uiState.update {
                        it.copy(error = UiText.Raw(R.string.add_pdf_error_paste_more_text))
                    }
                    return
                }
                _uiState.update {
                    it.copy(
                        extractedText = text,
                        packTitle     = "",
                        step          = AddPdfStep.CONFIGURE,
                        error         = null,
                    )
                }
            }

            SourceTab.WEB -> {

                val url = state.webUrl.trim()
                if (url.isBlank()) {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_empty_url)) }
                    return
                }
                _uiState.update {
                    it.copy(
                        packTitle = "",
                        step      = AddPdfStep.CONFIGURE,
                        error     = null,
                    )
                }
            }
        }
    }

    private fun extractPdf(
        fileUri: String,
        useCloudOcr: Boolean,
        successStep: AddPdfStep,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    step               = AddPdfStep.EXTRACTING,
                    isLoading          = true,
                    extractionProgress = 0f,
                    error              = null,
                )
            }

            val result = withContext(ioDispatcher) {
                visionRepo.extractText(
                    fileUri     = fileUri,
                    isImage     = false,
                    maxPages    = appConfig.ocrMaxPages,
                    forceEngine = if (useCloudOcr) null else OcrEngine.ML_KIT,
                )
            }

            result.fold(
                onSuccess = { ocrResult ->
                    val fullText = ocrResult.pages
                        .joinToString("\n\n") { it.fullText }
                        .trim()

                    val derivedTitle = _uiState.value.fileName
                        ?.substringBeforeLast(".")
                        ?.replace(Regex("[_\\-]+"), " ")
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?: ""

                    _uiState.update {
                        it.copy(
                            extractedText      = fullText.ifBlank { null },
                            packTitle          = derivedTitle,
                            step               = if (fullText.isBlank()) {
                                AddPdfStep.SELECT_PDF
                            } else {
                                successStep
                            },
                            isLoading          = false,
                            extractionProgress = 1f,
                            error = if (fullText.isBlank())
                                UiText.Raw(R.string.add_pdf_error_no_text_extracted)
                            else null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            step      = AddPdfStep.SELECT_PDF,
                            isLoading = false,
                            error     = UiText.Raw(
                                R.string.add_pdf_error_extraction_failed,
                                error.message ?: ""
                            ),
                        )
                    }
                },
            )
        }
    }

    private fun generateQuestions() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_unlimited_packs)))
            return
        }

        val state     = _uiState.value
        val isWebTab  = state.sourceTab == SourceTab.WEB
        val sourceUrl = if (isWebTab) state.webUrl.ifBlank { null } else null
        val sourceText = if (!isWebTab) state.extractedText else null

        when {
            isWebTab && sourceUrl == null -> {
                _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_empty_url)) }
                return
            }
            !isWebTab && sourceText.isNullOrBlank() -> {
                _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_no_content)) }
                return
            }
        }

        _uiState.update {
            it.copy(step = AddPdfStep.GENERATING, isLoading = true, generationProgress = 0f, error = null)
        }

        generationJob?.cancel()
        generationJob = viewModelScope.launch {

            val progressJob = launch {
                var p = 0f
                while (p < 0.85f) {
                    delay(600)
                    p = (p + 0.04f).coerceAtMost(0.85f)
                    _uiState.update { it.copy(generationProgress = p) }
                }
            }

            try {
                val config = GenerationConfig(
                    questionTypes = state.selectedTypes.toList(),
                    maxQuestions  = state.questionCount,
                    difficulty    = "medium",
                    language      = state.language,
                    tone          = "neutral",
                    hintTone      = state.focusNotes.takeIf { it.isNotBlank() },
                )

                val result = withContext(ioDispatcher) {
                    aiRepo.generatePack(
                        sourceText       = sourceText ?: "",
                        sourceUrl        = sourceUrl,
                        generationConfig = config,
                    )
                }

                progressJob.cancel()
                _uiState.update { it.copy(generationProgress = 1f) }

                result.fold(
                    onSuccess = { generated ->
                        generatedResult = generated
                        savePack()
                    },
                    onFailure = { error ->
                        val isQuota = error.message?.contains("upgrade", ignoreCase = true) == true
                                || error.message?.contains("limit",   ignoreCase = true) == true
                        if (isQuota) {
                            _uiEffects.tryEmit(
                                UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_ai_generation))
                            )
                        }
                        _uiState.update {
                            it.copy(
                                step               = AddPdfStep.CONFIGURE,
                                isLoading          = false,
                                generationProgress = 0f,
                                error = UiText.Raw(
                                    R.string.add_pdf_error_generation_failed,
                                    error.message ?: ""
                                ),
                            )
                        }
                    }
                )
            } catch (e: CancellationException) {
                progressJob.cancel()
                _uiState.update {
                    it.copy(step = AddPdfStep.CONFIGURE, isLoading = false, generationProgress = 0f)
                }
                throw e
            } catch (e: Exception) {
                progressJob.cancel()
                _uiState.update {
                    it.copy(
                        step               = AddPdfStep.CONFIGURE,
                        isLoading          = false,
                        generationProgress = 0f,
                        error = UiText.Raw(
                            R.string.add_pdf_error_generation_generic,
                            e.message ?: ""
                        ),
                    )
                }
            }
        }
    }

    private fun savePack() {
        val state  = _uiState.value
        val result = generatedResult
        if (result == null) {
            _uiState.update {
                it.copy(
                    step      = AddPdfStep.CONFIGURE,
                    isLoading = false,
                    error     = UiText.Raw(R.string.add_pdf_error_no_generation_result)
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val saved = withContext(ioDispatcher) {
                    val sourceType = when (state.sourceTab) {
                        SourceTab.FILE -> SourceType.PDF
                        SourceTab.TEXT -> SourceType.TEXT
                        SourceTab.WEB  -> SourceType.YOUTUBE
                    }
                    val pack = PackModel(
                        title      = result.pack.title.ifBlank { state.packTitle },
                        sourceType = sourceType,
                        createdAt  = System.currentTimeMillis(),
                        note       = result.pack.note,
                        category   = result.pack.category,
                        emoji      = result.pack.emoji,
                        color      = result.pack.color,
                        language   = result.pack.language.ifBlank { state.language },
                    )
                    val localPackId = packRepo.createPack(pack)
                    val questions   = result.questions.map { it.copy(packId = localPackId) }
                    if (questions.isNotEmpty()) questionRepo.insertQuestions(questions)
                    Triple(localPackId, pack.title, questions.map { it.toUiModel() })
                }

                _uiState.update {
                    it.copy(
                        step               = AddPdfStep.DONE,
                        isLoading          = false,
                        packId             = saved.first,
                        packTitle          = saved.second,
                        generatedQuestions = saved.third,
                    )
                }
                _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.add_pdf_pack_created)))
                checkPackLimit()

            } catch (e: Exception) {
                val isLimitError =
                    e.message?.contains("FREE_PACK_LIMIT_REACHED", ignoreCase = true) == true ||
                            e.message?.contains("free plan limit",         ignoreCase = true) == true
                if (isLimitError) {
                    _uiState.update { it.copy(isPackLimitReached = true, isLoading = false) }
                    _uiEffects.tryEmit(
                        UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_unlimited_packs))
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            step      = AddPdfStep.CONFIGURE,
                            isLoading = false,
                            error     = UiText.Raw(
                                R.string.add_pdf_error_save_failed,
                                e.message ?: ""
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun checkPackLimit() {
        viewModelScope.launch {
            val count = withContext(ioDispatcher) { packRepo.observeAllPacks().first().size }
            if (count >= appConfig.libraryFreePackLimit) {
                _uiState.update { it.copy(isPackLimitReached = true) }
                _uiEffects.emit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_unlimited_packs)))
            }
        }
    }

    private fun goBack() {
        when (_uiState.value.step) {
            AddPdfStep.SELECT_PDF,
            AddPdfStep.EXTRACTING -> {
                _uiState.update { it.copy(step = AddPdfStep.SELECT_PDF, isLoading = false) }
                _uiEffects.tryEmit(UiEffect.NavigateBack)
            }
            AddPdfStep.CONFIGURE -> {
                _uiState.update {
                    it.copy(
                        step               = AddPdfStep.SELECT_PDF,
                        extractedText      = null,
                        extractionProgress = 0f,
                        error              = null,
                    )
                }
            }
            AddPdfStep.GENERATING -> {
                generationJob?.cancel()
                _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, isLoading = false) }
            }
            AddPdfStep.DONE -> {
                _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Library.route))
            }
        }
    }

    private fun updatePasteText(text: String)  { _uiState.update { it.copy(pasteText = text, error = null) } }
    private fun updateWebUrl(url: String)       { _uiState.update { it.copy(webUrl = url, error = null) } }
    private fun updateQuestionCount(count: Int) { _uiState.update { it.copy(questionCount = count.coerceIn(5, 50)) } }
    private fun updateLanguage(code: String)    { _uiState.update { it.copy(language = code) } }
    private fun updateFocusNotes(notes: String) { _uiState.update { it.copy(focusNotes = notes) } }
    private fun clearError()                    { _uiState.update { it.copy(error = null) } }

    private fun toggleQuestionType(type: QuestionType) {
        _uiState.update { state ->
            val current = state.selectedTypes
            val updated = if (type in current && current.size > 1) current - type else current + type
            state.copy(selectedTypes = updated)
        }
    }

    override fun onCleared() {
        generationJob?.cancel()
        super.onCleared()
    }
}
