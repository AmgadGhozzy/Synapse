package com.venom.synapse.features.add_pdf.presentation.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.core.ui.state.toUiModel
import com.venom.synapse.domain.model.GeneratedPackResult
import com.venom.synapse.domain.model.GenerationConfig
import com.venom.synapse.domain.model.PackModel
import com.venom.synapse.domain.model.QuestionType
import com.venom.synapse.domain.model.SourceType
import com.venom.synapse.domain.repo.IAIRepository
import com.venom.synapse.domain.repo.IAuthRepository
import com.venom.synapse.domain.repo.IPackRepository
import com.venom.synapse.domain.repo.IQuestionRepository
import com.venom.synapse.domain.repo.IVisionRepository
import com.venom.synapse.features.add_pdf.presentation.state.AddPdfStep
import com.venom.synapse.features.add_pdf.presentation.state.AddPdfUiState
import com.venom.synapse.features.add_pdf.presentation.state.AddPdfUiState.Companion.FREE_PACK_LIMIT
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
    private val visionRepo  : IVisionRepository,
    private val aiRepo      : IAIRepository,
    private val packRepo    : IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val authRepo    : IAuthRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState   = MutableStateFlow(AddPdfUiState())
    val uiState: StateFlow<AddPdfUiState> = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    private var generationJob: Job? = null
    private var generatedResult: GeneratedPackResult? = null

    init { checkPackLimit() }

    // ── Events ────────────────────────────────────────────────────────────────

    fun onEvent(event: AddPdfUiEvent) {
        when (event) {
            is AddPdfUiEvent.GoBack               -> goBack()
            is AddPdfUiEvent.DismissError         -> clearError()
            is AddPdfUiEvent.SourceTabSelected    -> updateSourceTab(event.tab)
            is AddPdfUiEvent.FileSelected         -> onFileSelected(event.uri, event.name)
            is AddPdfUiEvent.ClearFile            -> clearSelectedFile()
            is AddPdfUiEvent.OcrToggled           -> toggleOcr()
            is AddPdfUiEvent.PasteTextChanged     -> updatePasteText(event.text)
            is AddPdfUiEvent.ContinueToConfigure  -> continueToConfig()
            is AddPdfUiEvent.QuestionCountChanged -> updateQuestionCount(event.count)
            is AddPdfUiEvent.QuestionTypeToggled  -> toggleQuestionType(event.type)
            is AddPdfUiEvent.FocusNotesChanged    -> updateFocusNotes(event.notes)
            is AddPdfUiEvent.GeneratePack         -> generateQuestions()
            is AddPdfUiEvent.LanguageSelected     -> updateLanguage(event.languageCode)
        }
    }

    // ── Pack limit ────────────────────────────────────────────────────────────

    private fun checkPackLimit() {
        viewModelScope.launch {
            if (authRepo.userState.value.isPremium) return@launch
            val count = withContext(ioDispatcher) { packRepo.observeAllPacks().first().size }
            if (count >= FREE_PACK_LIMIT) {
                _uiState.update { it.copy(isPackLimitReached = true) }
                _uiEffects.emit(UiEffect.ShowUpgradePrompt("Unlimited Packs"))
            }
        }
    }

    // ── Source handling ───────────────────────────────────────────────────────

    private fun updateSourceTab(tab: SourceTab) {
        _uiState.update { it.copy(sourceTab = tab, error = null) }
    }

    private fun onFileSelected(uri: Uri, fileName: String) {
        val isImage = IMAGE_EXTENSIONS.any { fileName.lowercase().endsWith(".$it") }
        _uiState.update {
            it.copy(
                fileUri       = uri.toString(),
                fileName      = fileName,
                isImageUpload = isImage,
                ocrEnabled    = false,
                extractedText = null,
                error         = null,
            )
        }
        if (!isImage) extractFile(uri, isImage = false)
    }

    private fun clearSelectedFile() {
        _uiState.update {
            it.copy(
                fileUri       = null,
                fileName      = null,
                isImageUpload = false,
                ocrEnabled    = false,
                extractedText = null,
                isLoading     = false,
                error         = null,
            )
        }
    }

    private fun toggleOcr() {
        _uiState.update { it.copy(ocrEnabled = !it.ocrEnabled) }
    }

    private fun updatePasteText(text: String) {
        _uiState.update { it.copy(pasteText = text, error = null) }
    }

    // ── Continue to Configure ─────────────────────────────────────────────────

    private fun continueToConfig() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt("Unlimited Packs"))
            return
        }

        val state = _uiState.value
        when (state.sourceTab) {
            SourceTab.FILE -> {
                when {
                    state.isImageUpload && !state.ocrEnabled -> {
                        _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt("PRO OCR"))
                    }
                    state.isImageUpload && state.ocrEnabled -> {
                        val uri = state.fileUri?.toUri() ?: return
                        extractFile(uri, isImage = true)
                    }
                    state.extractedText != null -> {
                        _uiState.update { it.copy(step = AddPdfStep.CONFIGURE) }
                    }
                    else -> {
                        _uiState.update { it.copy(error = "File is still being read. Please wait.") }
                    }
                }
            }
            SourceTab.TEXT -> {
                val text = state.pasteText.trim()
                if (text.length < 10) {
                    _uiState.update { it.copy(error = "Please paste at least a few sentences of text.") }
                    return
                }
                _uiState.update {
                    it.copy(
                        extractedText = text,
                        packTitle     = "New Pack",
                        step          = AddPdfStep.CONFIGURE,
                        error         = null,
                    )
                }
            }
            SourceTab.WEB -> {
                _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt("Web & YouTube import"))
            }
        }
    }

    // ── Extraction ────────────────────────────────────────────────────────────

    /**
     * BUG FIX — compile error "Unresolved reference 'text'" / "Unresolved reference 'title'":
     *
     * `visionRepo.extractText()` returns `Result<OcrResult>`, not `Result<String>`.
     * `OcrResult` has `.pages: List<OcrPage>` where each `OcrPage.fullText` is
     * the extracted text for that page. There is no `.text` or `.title` property.
     *
     * Previous (broken):
     *   extracted.text   → compile error
     *   extracted.title  → compile error
     *
     * Fixed:
     *   fullText  = pages joined — Vision already inserts page markers in ML Kit path
     *   packTitle = derived from fileName stored in state (extension stripped, _ → space)
     *
     * Step returns to SELECT_PDF (not CONFIGURE) so the user sees the "file ready"
     * card and can verify before tapping Continue.
     */
    private fun extractFile(uri: Uri, isImage: Boolean) {
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
                    fileUri     = uri.toString(),
                    isImage     = isImage,
                    maxPages    = 20,
                    forceEngine = null,
                )
            }

            result.fold(
                onSuccess = { ocrResult ->
                    // Join all page texts — OcrPage.fullText has the per-page content.
                    val fullText = ocrResult.pages
                        .joinToString("\n\n") { it.fullText }
                        .trim()

                    // Derive a clean title from the file name already in state.
                    // Strips extension, replaces _ and - with spaces, trims.
                    val derivedTitle = _uiState.value.fileName
                        ?.substringBeforeLast(".")
                        ?.replace(Regex("[_\\-]+"), " ")
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?: "New Pack"

                    _uiState.update {
                        it.copy(
                            extractedText      = fullText.ifBlank { null },
                            packTitle          = derivedTitle,
                            step               = AddPdfStep.SELECT_PDF,
                            isLoading          = false,
                            extractionProgress = 1f,
                            error              = if (fullText.isBlank())
                                "No text could be extracted. Try a clearer file or enable OCR."
                            else null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            step      = AddPdfStep.SELECT_PDF,
                            isLoading = false,
                            error     = "Extraction failed: ${error.message}",
                        )
                    }
                },
            )
        }
    }

    // ── Configure step ────────────────────────────────────────────────────────

    private fun updateQuestionCount(count: Int) {
        _uiState.update { it.copy(questionCount = count.coerceIn(5, 50)) }
    }

    private fun toggleQuestionType(type: QuestionType) {
        _uiState.update { state ->
            val current = state.selectedTypes
            val updated = if (type in current && current.size > 1) current - type else current + type
            state.copy(selectedTypes = updated)
        }
    }

    private fun updateLanguage(code: String) {
        _uiState.update { it.copy(language = code) }
    }

    private fun updateFocusNotes(notes: String) {
        _uiState.update { it.copy(focusNotes = notes) }
    }

    // ── Generation ────────────────────────────────────────────────────────────

    private fun generateQuestions() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt("Unlimited Packs"))
            return
        }

        val state = _uiState.value
        val text  = state.extractedText
        if (text.isNullOrBlank()) {
            _uiState.update { it.copy(error = "No content to generate from.") }
            return
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
                    // isMixed is intentionally NOT set — AIRepositoryImpl no longer
                    // uses it; the actual questionTypes list drives the server request.
                )

                val result = withContext(ioDispatcher) {
                    aiRepo.generatePack(
                        sourceText       = text,
                        sourceUrl        = null,
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
                                || error.message?.contains("limit", ignoreCase = true) == true
                        if (isQuota) _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt("AI Generation"))
                        _uiState.update {
                            it.copy(
                                step               = AddPdfStep.CONFIGURE,
                                isLoading          = false,
                                generationProgress = 0f,
                                error              = "Generation failed: ${error.message}",
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
                        error              = "Generation error: ${e.message}",
                    )
                }
            }
        }
    }

    // ── Save pack ─────────────────────────────────────────────────────────────

    private fun savePack() {
        val state  = _uiState.value
        val result = generatedResult
        if (result == null) {
            _uiState.update {
                it.copy(step = AddPdfStep.CONFIGURE, isLoading = false, error = "Internal error: no generation result.")
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
                        title      = result.pack.title.ifBlank { state.packTitle.ifBlank { "New Pack" } },
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
                _uiEffects.tryEmit(UiEffect.ShowToast("Pack created successfully!"))

                // Re-check pack limit so the NEXT attempt in this session is correctly gated.
                checkPackLimit()

            } catch (e: Exception) {
                val isLimitError = e.message?.contains("FREE_PACK_LIMIT_REACHED", ignoreCase = true) == true
                        || e.message?.contains("free plan limit", ignoreCase = true) == true
                if (isLimitError) {
                    _uiState.update { it.copy(isPackLimitReached = true, isLoading = false) }
                    _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt("Unlimited Packs"))
                } else {
                    _uiState.update {
                        it.copy(
                            step      = AddPdfStep.CONFIGURE,
                            isLoading = false,
                            error     = "Failed to save: ${e.message}",
                        )
                    }
                }
            }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun goBack() {
        when (_uiState.value.step) {
            AddPdfStep.SELECT_PDF,
            AddPdfStep.EXTRACTING -> {
                _uiState.update { it.copy(step = AddPdfStep.SELECT_PDF, isLoading = false) }
                _uiEffects.tryEmit(UiEffect.NavigateBack)
            }
            AddPdfStep.CONFIGURE -> {
                _uiState.update {
                    it.copy(step = AddPdfStep.SELECT_PDF, extractedText = null, extractionProgress = 0f, error = null)
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

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        generationJob?.cancel()
        super.onCleared()
    }

    private companion object {
        val IMAGE_EXTENSIONS = setOf(
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "tiff", "tif", "heic", "heif"
        )
    }
}