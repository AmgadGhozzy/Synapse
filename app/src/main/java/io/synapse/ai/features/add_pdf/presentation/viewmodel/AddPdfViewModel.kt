package io.synapse.ai.features.add_pdf.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.core.ui.state.toUiModel
import io.synapse.ai.data.repo.AppConfigProvider
import io.synapse.ai.data.repo.DailyLimitException
import io.synapse.ai.data.repo.GenerationFailedException
import io.synapse.ai.domain.model.GeneratedPackResult
import io.synapse.ai.domain.model.GenerationConfig
import io.synapse.ai.domain.model.PackModel
import io.synapse.ai.domain.model.QuestionType
import io.synapse.ai.domain.model.SourceType
import io.synapse.ai.domain.repo.IAIRepository
import io.synapse.ai.domain.repo.IAuthRepository
import io.synapse.ai.domain.repo.IPackRepository
import io.synapse.ai.domain.repo.IQuestionRepository
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfStep
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiEvent
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiState
import io.synapse.ai.features.add_pdf.presentation.state.SourceTab
import io.synapse.ai.navigation.SynapseScreen
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
    @param:ApplicationContext private val context: Context,
    private val aiRepo      : IAIRepository,
    private val packRepo    : IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val authRepo    : IAuthRepository,
    private val appConfig   : AppConfigProvider,
    savedStateHandle        : SavedStateHandle,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    // Restore the initial source tab from navigation arguments.
    private val initialSourceTab = when (savedStateHandle.get<String>(SynapseScreen.AddPdf.ARG_SOURCE)) {
        SynapseScreen.AddPdf.SOURCE_TEXT -> SourceTab.TEXT
        else                             -> SourceTab.FILE
    }

    private val _uiState   = MutableStateFlow(AddPdfUiState(sourceTab = initialSourceTab))
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
                isPro              = appConfig.isPremium,
                isThinkingLocked   = appConfig.isThinkingLocked,
                maxPages           = appConfig.ocrMaxPages,
                maxFileSizeMb      = appConfig.addPdfMaxFileSizeMb,
            )
        }
    }

    // ── Event dispatcher ──────────────────────────────────────────────────
    fun onEvent(event: AddPdfUiEvent) {
        when (event) {
            is AddPdfUiEvent.GoBack               -> goBack()
            is AddPdfUiEvent.DismissError         -> clearError()
            is AddPdfUiEvent.SourceTabSelected    -> updateSourceTab(event.tab)
            is AddPdfUiEvent.FileSelected         -> onFileSelected(event.uri, event.name, event.sizeMb)
            is AddPdfUiEvent.ClearFile            -> clearSelectedFile()
            is AddPdfUiEvent.OcrToggled           -> toggleOcr()
            is AddPdfUiEvent.PasteTextChanged     -> updatePasteText(event.text)
            is AddPdfUiEvent.WebUrlChanged        -> updateWebUrl(event.url)
            is AddPdfUiEvent.WebTabLockedClicked  -> onWebTabLockedClicked()
            is AddPdfUiEvent.ContinueToConfigure  -> continueToConfig()
            is AddPdfUiEvent.QuestionCountChanged -> updateQuestionCount(event.count)
            is AddPdfUiEvent.QuestionTypeToggled  -> toggleQuestionType(event.type)
            is AddPdfUiEvent.DifficultySelected   -> updateDifficulty(event.difficulty)
            is AddPdfUiEvent.FocusNotesChanged    -> updateFocusNotes(event.notes)
            is AddPdfUiEvent.LanguageSelected     -> updateLanguage(event.languageCode)
            is AddPdfUiEvent.ThinkingToggled      -> toggleThinking()
            is AddPdfUiEvent.GeneratePack         -> generateQuestions()
        }
    }

    // ── Source tab ────────────────────────────────────────────────────────
    private fun updateSourceTab(tab: SourceTab) {
        _uiState.update { it.copy(sourceTab = tab, error = null) }
    }

    // ── File (PDF) selection ──────────────────────────────────────────────
    private fun onFileSelected(uri: String, fileName: String, sizeMb: Float) {
        // Reject non-PDF files immediately.
        if (!fileName.endsWith(".pdf", ignoreCase = true)) {
            _uiState.update {
                it.copy(error = UiText.Raw(R.string.add_pdf_error_unsupported_file_type))
            }
            return
        }

        // Reject files that exceed the user's plan limit.
        if (sizeMb > _uiState.value.maxFileSizeMb) {
            // Show upgrade paywall instead of just error
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_large_pdf)))
            _uiState.update {
                it.copy(
                    error = UiText.Raw(
                        R.string.add_pdf_error_file_too_large,
                        sizeMb,
                        _uiState.value.maxFileSizeMb,
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                fileUri       = uri,
                fileName      = fileName,
                fileSizeMb    = sizeMb,
                extractedText = null,
                error         = null,
            )
        }

        // Native Gemini PDF flow: skip local extraction, go straight to Configure.
        continueToConfig()
    }

    private fun clearSelectedFile() {
        _uiState.update {
            it.copy(
                fileUri       = null,
                fileName      = null,
                fileSizeMb    = 0f,
                ocrEnabled    = false,
                extractedText = null,
                isLoading     = false,
                error         = null,
            )
        }
    }

    // ── OCR toggle ────────────────────────────────────────────────────────
    private fun toggleOcr() {
        val state = _uiState.value
        if (state.isOcrFeatureLocked) {
            // Show upgrade prompt instead of toggling.
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_pro_ocr)))
            return
        }
        _uiState.update {
            it.copy(
                ocrEnabled    = !it.ocrEnabled,
                extractedText = null
            )
        }
    }

    // ── Thinking toggle ───────────────────────────────────────────────────
    private fun toggleThinking() {
        val state = _uiState.value

        if (state.isThinkingLocked) {
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_deep_thinking)))
            return
        }
        _uiState.update { it.copy(thinkingEnabled = !it.thinkingEnabled) }
    }

    // ── Continue to configure ─────────────────────────────────────────────
    private fun continueToConfig() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_unlimited_packs)))
            return
        }

        val state = _uiState.value
        when (state.sourceTab) {

            SourceTab.FILE -> {
                if (state.fileUri != null) {
                    _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, error = null) }
                } else {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_no_content)) }
                }
            }

            SourceTab.TEXT -> {
                val text = state.pasteText.trim()
                if (text.length < 10) {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_paste_more_text)) }
                    return
                }
                _uiState.update {
                    it.copy(extractedText = text, packTitle = "", step = AddPdfStep.CONFIGURE, error = null)
                }
            }

            SourceTab.WEB, SourceTab.YOUTUBE -> {
                val url = state.webUrl.trim()
                if (url.isBlank()) {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_empty_url)) }
                    return
                }
                _uiState.update { it.copy(packTitle = "", step = AddPdfStep.CONFIGURE, error = null) }
            }
        }
    }

    // ── Generation ────────────────────────────────────────────────────────
    private fun generateQuestions() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_unlimited_packs)))
            return
        }

        val state = _uiState.value

        // Resolve the (source string, SourceType) pair — returns null on validation failure.
        val (source, sourceType) = resolveSource(state) ?: return

        _uiState.update {
            it.copy(step = AddPdfStep.GENERATING, isLoading = true, generationProgress = 0f, error = null)
        }

        generationJob?.cancel()
        generationJob = viewModelScope.launch {

            // Fake progress ticks while waiting for the edge function.
            val progressJob = launch {
                var p = 0f
                while (p < 0.85f) {
                    delay(600)
                    p = (p + 0.04f).coerceAtMost(0.85f)
                    _uiState.update { it.copy(generationProgress = p) }
                }
            }

            try {
                val userId = withContext(ioDispatcher) {
                    runCatching { authRepo.currentUserId() }.getOrNull()
                } ?: ""

                val config = GenerationConfig(
                    sourceType    = sourceType,
                    questionTypes = state.selectedTypes.toList(),
                    maxQuestions  = state.questionCount,
                    difficulty    = state.difficulty,
                    language      = state.language,
                    thinking      = state.thinkingEnabled && !state.isThinkingLocked,
                    userId        = userId,
                    hintTone      = state.focusNotes.takeIf { it.isNotBlank() },
                )

                val result = withContext(ioDispatcher) {
                    aiRepo.generatePack(source = source, generationConfig = config)
                }

                progressJob.cancel()
                _uiState.update { it.copy(generationProgress = 1f) }

                result.fold(
                    onSuccess = { generated ->
                        generatedResult = generated
                        savePack(sourceType)
                    },
                    onFailure = { error ->
                        handleGenerationError(error)
                    },
                )

            } catch (e: CancellationException) {
                progressJob.cancel()
                _uiState.update {
                    it.copy(step = AddPdfStep.CONFIGURE, isLoading = false, generationProgress = 0f)
                }
                throw e
            } catch (e: Exception) {
                progressJob.cancel()
                handleGenerationError(e)
            }
        }
    }

    private fun handleGenerationError(error: Throwable) {
        val msg = when {

            // HTTP 429 — user has hit their daily generation limit.
            // Show upgrade prompt + contextual message (not a generic snackbar).
            error is DailyLimitException -> {
                _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_daily_limit)))
                UiText.Raw(R.string.add_pdf_error_daily_limit)
            }

            // HTTP 400 — malformed request (missing field, invalid source type, etc).
            error is GenerationFailedException && error.httpStatus == 400 ->
                UiText.Raw(R.string.add_pdf_error_invalid_request)

            // Vertex AI service unavailable.
            error.message?.contains("AI service error", ignoreCase = true) == true ->
                UiText.Raw(R.string.add_pdf_error_ai_service)

            // Any other limit/upgrade message embedded in error text.
            error.message?.contains("upgrade", ignoreCase = true) == true ||
                    error.message?.contains("limit",   ignoreCase = true) == true -> {
                _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_ai_generation)))
                UiText.Raw(R.string.add_pdf_error_generation_failed, error.message ?: "")
            }

            else -> UiText.Raw(R.string.add_pdf_error_generation_failed, error.message ?: "")
        }

        _uiState.update {
            it.copy(step = AddPdfStep.CONFIGURE, isLoading = false, generationProgress = 0f, error = msg)
        }
    }

    // ── Source resolution ─────────────────────────────────────────────────
    private fun resolveSource(state: AddPdfUiState): Pair<String, SourceType>? {
        return when (state.sourceTab) {

            SourceTab.FILE -> {
                val uri = state.fileUri ?: run {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_no_content)) }
                    return null
                }
                val base64 = readFileBytes(uri).map { bytes ->
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                }.getOrElse { e ->
                    _uiState.update {
                        it.copy(error = UiText.Raw(R.string.add_pdf_error_extraction_failed, e.message ?: ""))
                    }
                    return null
                }
                Pair(base64, SourceType.PDF)
            }

            SourceTab.TEXT -> {
                val text = state.pasteText.trim()
                if (text.isBlank()) {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_no_content)) }
                    return null
                }
                Pair(text, SourceType.TEXT)
            }

            SourceTab.WEB -> {
                val url = state.webUrl.trim()
                if (url.isBlank()) {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_empty_url)) }
                    return null
                }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_invalid_url)) }
                    return null
                }
                Pair(url, SourceType.URL)
            }

            SourceTab.YOUTUBE -> {
                val url = state.webUrl.trim()
                if (url.isBlank()) {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_empty_url)) }
                    return null
                }
                if (!YOUTUBE_PATTERN.containsMatchIn(url)) {
                    _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_invalid_youtube_url)) }
                    return null
                }
                Pair(url, SourceType.YOUTUBE)
            }
        }
    }

    // ── Save generated pack to Room ───────────────────────────────────────

    private fun savePack(sourceType: SourceType) {
        val state  = _uiState.value
        val result = generatedResult ?: run {
            _uiState.update {
                it.copy(
                    step      = AddPdfStep.CONFIGURE,
                    isLoading = false,
                    error     = UiText.Raw(R.string.add_pdf_error_no_generation_result),
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val saved = withContext(ioDispatcher) {
                    val pack = PackModel(
                        id         = result.pack.id,
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

                Log.d(TAG, "Saved packId=${saved.first} | ${saved.third.size} questions")

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
                    _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_unlimited_packs)))
                } else {
                    _uiState.update {
                        it.copy(
                            step      = AddPdfStep.CONFIGURE,
                            isLoading = false,
                            error     = UiText.Raw(R.string.add_pdf_error_save_failed, e.message ?: ""),
                        )
                    }
                }
            }
        }
    }

    // ── Pack limit check ──────────────────────────────────────────────────

    private fun checkPackLimit() {
        viewModelScope.launch {
            val count = withContext(ioDispatcher) { packRepo.observeAllPacks().first().size }
            if (count >= appConfig.libraryFreePackLimit) {
                _uiState.update { it.copy(isPackLimitReached = true) }
                _uiEffects.emit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_unlimited_packs)))
            }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private fun goBack() {
        when (_uiState.value.step) {
            AddPdfStep.SELECT_PDF -> {
                _uiEffects.tryEmit(UiEffect.NavigateBack)
            }
            AddPdfStep.CONFIGURE -> {
                _uiState.update {
                    it.copy(step = AddPdfStep.SELECT_PDF, extractedText = null,
                        extractionProgress = 0f, error = null)
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

    // ── Simple field updates ──────────────────────────────────────────────

    private fun onWebTabLockedClicked() =
        _uiEffects.tryEmit(UiEffect.ShowUpgradePrompt(UiText.Raw(R.string.feature_web_youtube_import)))

    private fun updatePasteText(text: String)   { _uiState.update { it.copy(pasteText = text, error = null) } }
    private fun updateWebUrl(url: String) {
        // Enforce "max one url" by taking the first space-separated token.
        val cleanedUrl = url.trim().split(Regex("\\s+")).firstOrNull() ?: ""

        _uiState.update { state ->
            // Auto-detect if it's a YouTube URL.
            val isYouTube = YOUTUBE_PATTERN.containsMatchIn(cleanedUrl)

            // Switch to the appropriate tab based on the detected URL type.
            val detectedTab = if (isYouTube) SourceTab.YOUTUBE else SourceTab.WEB

            state.copy(
                webUrl    = cleanedUrl,
                sourceTab = detectedTab,
                error     = null
            )
        }
    }
    private fun updateQuestionCount(count: Int)  { _uiState.update { it.copy(questionCount = count.coerceIn(3, 50)) } }
    private fun updateLanguage(code: String)     { _uiState.update { it.copy(language = code) } }
    private fun updateFocusNotes(notes: String)  { _uiState.update { it.copy(focusNotes = notes) } }
    private fun updateDifficulty(d: String)      { _uiState.update { it.copy(difficulty = d) } }
    private fun clearError()                     { _uiState.update { it.copy(error = null) } }

    private fun toggleQuestionType(type: QuestionType) {
        _uiState.update { state ->
            val current = state.selectedTypes
            // Always keep at least one type selected.
            val updated = if (type in current && current.size > 1) current - type else current + type
            state.copy(selectedTypes = updated)
        }
    }

    override fun onCleared() {
        generationJob?.cancel()
        super.onCleared()
    }

    // ── File helpers ──────────────────────────────────────────────────────

    private fun readFileBytes(fileUri: String): Result<ByteArray> {
        val uri = fileUri.toUri()
        val bytes = readBytes(uri)
            ?: return Result.failure(IllegalStateException("Cannot open file: $fileUri"))
        
        if (bytes.isEmpty()) {
            return Result.failure(IllegalStateException("PDF file is empty"))
        }
        
        if (!isValidPdf(bytes)) {
            return Result.failure(IllegalStateException("File is not a valid PDF"))
        }
        
        return Result.success(bytes)
    }
    
    private fun isValidPdf(bytes: ByteArray): Boolean {
        if (bytes.size < 5) return false
        val header = bytes.sliceArray(0..4)
        return header.contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)) || // %PDF-
               header.contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x20))    // %PDF- (with space)
    }

    private fun readBytes(uri: Uri): ByteArray? = try {
        when (uri.scheme) {
            "content" -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            "file"    -> uri.path?.let { java.io.File(it).readBytes() }
            else      -> null
        }
    } catch (e: Exception) {
        Log.e(TAG, "readBytes() failed for $uri: ${e.message}")
        null
    }

    private companion object {
        const val TAG = "AddPdfViewModel"

        /** Client-side YouTube URL pattern — mirrors the server-side validation. */
        val YOUTUBE_PATTERN = Regex(
            "^https?://(www\\.)?youtube\\.com/watch\\?v=[\\w-]{11}" +
            "|^https?://youtu\\.be/[\\w-]{11}" +
            "|^https?://(www\\.)?youtube\\.com/shorts/[\\w-]{11}"
        )
    }
}