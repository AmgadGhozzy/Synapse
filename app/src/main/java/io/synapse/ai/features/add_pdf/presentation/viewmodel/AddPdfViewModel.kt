package io.synapse.ai.features.add_pdf.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.ToastType
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.core.ui.state.toUiModel
import io.synapse.ai.data.repo.AppConfigProvider
import io.synapse.ai.domain.model.GenerationConfig
import io.synapse.ai.domain.model.GenerationError
import io.synapse.ai.domain.model.GenerationStreamEvent
import io.synapse.ai.domain.model.PackModel
import io.synapse.ai.domain.model.QuestionType
import io.synapse.ai.domain.model.SourceType
import io.synapse.ai.domain.model.toQuestionModel
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddPdfViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val aiRepo: IAIRepository,
    private val packRepo: IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val authRepo: IAuthRepository,
    private val appConfig: AppConfigProvider,
    savedStateHandle: SavedStateHandle,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val initialSourceTab =
        when (savedStateHandle.get<String>(SynapseScreen.AddPdf.ARG_SOURCE)) {
            SynapseScreen.AddPdf.SOURCE_TEXT -> SourceTab.TEXT
            else -> SourceTab.FILE
        }

    private val _uiState = MutableStateFlow(AddPdfUiState(sourceTab = initialSourceTab))
    val uiState: StateFlow<AddPdfUiState> = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    private var generationJob: Job? = null
    private var messageRotationJob: Job? = null
    private var packLimitJob: Job? = null

    init {
        observePremiumConfig()
        checkPackLimit()
    }

    // ── Premium config observer ───────────────────────────────────────────
    private fun observePremiumConfig() {
        viewModelScope.launch {
            combine(
                appConfig.isPremiumFlow,
                appConfig.isOcrProLockedFlow,
                appConfig.isThinkingLockedFlow,
                appConfig.ocrMaxPagesFlow,
                appConfig.addPdfMaxFileSizeMbFlow,
            ) { isPremium, isOcrLocked, isThinkingLocked, maxPages, maxFileSize ->
                PremiumConfig(isPremium, isOcrLocked, isThinkingLocked, maxPages, maxFileSize)
            }.collect { config ->
                _uiState.update {
                    it.copy(
                        isOcrFeatureLocked = config.isOcrLocked,
                        isPro              = config.isPremium,
                        isThinkingLocked   = config.isThinkingLocked,
                        maxPages           = config.maxPages,
                        maxFileSizeMb      = config.maxFileSizeMb,
                    )
                }
            }
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
            is AddPdfUiEvent.StartStudyEarly      -> startStudyEarly()
        }
    }

    // ── Source tab ────────────────────────────────────────────────────────
    private fun updateSourceTab(tab: SourceTab) =
        _uiState.update { it.copy(sourceTab = tab, error = null) }

    // ── File (PDF) ────────────────────────────────────────────────────────
    private fun onFileSelected(uri: String, fileName: String, sizeMb: Float) {
        if (!fileName.endsWith(".pdf", ignoreCase = true)) {
            _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_unsupported_file_type)) }
            return
        }
        if (sizeMb > _uiState.value.maxFileSizeMb) {
            _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_large_pdf)))
            _uiState.update {
                it.copy(error = UiText.Raw(R.string.add_pdf_error_file_too_large, sizeMb, appConfig.proMaxFileSizeMb))
            }
            return
        }
        _uiState.update {
            it.copy(fileUri = uri, fileName = fileName, fileSizeMb = sizeMb, extractedText = null, error = null)
        }
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
        if (_uiState.value.isOcrFeatureLocked) {
            _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_pro_ocr)))
            return
        }
        _uiState.update { it.copy(ocrEnabled = !it.ocrEnabled, extractedText = null) }
    }

    // ── Text / URL updates ────────────────────────────────────────────────
    private fun updatePasteText(text: String) =
        _uiState.update { it.copy(pasteText = text, error = null) }

    private fun updateWebUrl(url: String) {
        // Strip accidental whitespace / pasted multi-word content
        val cleaned = url.trim().split(Regex("\\s+")).firstOrNull() ?: ""
        _uiState.update { state ->
            val isYouTube = YOUTUBE_PATTERN.containsMatchIn(cleaned)
            state.copy(
                webUrl    = cleaned,
                sourceTab = if (isYouTube) SourceTab.YOUTUBE else SourceTab.WEB,
                error     = null,
            )
        }
    }

    private fun onWebTabLockedClicked() =
        _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_web_youtube_import)))

    // ── Configure fields ──────────────────────────────────────────────────
    private fun updateQuestionCount(count: Int) = _uiState.update { it.copy(questionCount = count.coerceIn(3, 50)) }
    private fun updateLanguage(code: String)    = _uiState.update { it.copy(language = code) }
    private fun updateFocusNotes(notes: String) = _uiState.update { it.copy(focusNotes = notes) }
    private fun updateDifficulty(d: String)     = _uiState.update { it.copy(difficulty = d) }
    private fun clearError()                    = _uiState.update { it.copy(error = null) }

    private fun toggleQuestionType(type: QuestionType) {
        _uiState.update { state ->
            val current = state.selectedTypes
            val updated = if (type in current && current.size > 1) current - type else current + type
            state.copy(selectedTypes = updated)
        }
    }

    private fun toggleThinking() {
        if (_uiState.value.isThinkingLocked) {
            _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_deep_thinking)))
            return
        }
        _uiState.update { it.copy(thinkingEnabled = !it.thinkingEnabled) }
    }

    // ── Continue to Configure ─────────────────────────────────────────────
    private fun continueToConfig() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_unlimited_packs)))
            return
        }
        val state = _uiState.value
        when (state.sourceTab) {
            SourceTab.FILE -> {
                if (state.fileUri != null) {
                    _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, error = null) }
                } else {
                    toast(UiText.Raw(R.string.add_pdf_error_no_content), ToastType.ERROR)
                }
            }
            SourceTab.TEXT -> {
                if (state.pasteText.trim().length < 10) {
                    toast(UiText.Raw(R.string.add_pdf_error_paste_more_text), ToastType.ERROR)
                } else {
                    _uiState.update {
                        it.copy(extractedText = state.pasteText.trim(), packTitle = "", step = AddPdfStep.CONFIGURE, error = null)
                    }
                }
            }
            SourceTab.WEB, SourceTab.YOUTUBE -> {
                if (state.webUrl.isBlank()) {
                    toast(UiText.Raw(R.string.add_pdf_error_empty_url), ToastType.ERROR)
                } else {
                    _uiState.update { it.copy(packTitle = "", step = AddPdfStep.CONFIGURE, error = null) }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GENERATION — SSE Streaming
    // ══════════════════════════════════════════════════════════════════════

    private fun generateQuestions() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_unlimited_packs)))
            return
        }
        val state = _uiState.value

        _uiState.update {
            it.copy(
                step                   = AddPdfStep.GENERATING,
                isLoading              = true,
                isUploading            = state.sourceTab == SourceTab.FILE,
                uploadProgress         = null,
                generationProgress     = 0f,
                error                  = null,
                questionsCompleted     = 0,
                questionsExpected      = state.questionCount,
                conceptsFound          = 0,
                streamStage            = "",
                progressMessageIndex   = 0,
                streamPackTitle        = "",
                streamPackEmoji        = "",
                streamPackColor        = "",
                canStartEarly          = false,
                generatingInBackground = false,
                packId                 = 0L,
                packUuid               = null,
            )
        }

        startMessageRotation()

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            try {
                val resolved = withContext(ioDispatcher) { resolveSource(state) }
                delay(350) // Smooth visual transition from uploading to preparing
                _uiState.update { it.copy(isUploading = false) }
                if (resolved == null) {
                    stopMessageRotation()
                    _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, isLoading = false, generationProgress = 0f) }
                    return@launch
                }
                val (source, sourceType) = resolved

                val config = GenerationConfig(
                    sourceType    = sourceType,
                    questionTypes = state.selectedTypes.toList(),
                    maxQuestions  = state.questionCount,
                    difficulty    = state.difficulty,
                    language      = state.language,
                    thinking      = state.thinkingEnabled && !state.isThinkingLocked,
                    instructions  = state.focusNotes.takeIf { it.isNotBlank() },
                )

                aiRepo.generatePackStream(source = source, generationConfig = config)
                    .collect { event -> handleStreamEvent(event, sourceType, state) }

            } catch (e: CancellationException) {
                stopMessageRotation()
                _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, isLoading = false, generationProgress = 0f) }
                throw e
            } catch (e: GenerationError) {
                stopMessageRotation()
                handleGenerationError(e)
            } catch (e: Exception) {
                stopMessageRotation()
                handleGenerationError(e)
            }
        }
    }

    /**
     * Dispatches each SSE event to the appropriate handler.
     * Suspended so DB writes happen inline without extra job overhead.
     */
    private suspend fun handleStreamEvent(
        event: GenerationStreamEvent,
        sourceType: SourceType,
        originalState: AddPdfUiState,
    ) {
        when (event) {

            // ── pack_meta: create pack row in Room immediately ─────────────
            is GenerationStreamEvent.PackMeta -> {
                stopMessageRotation() // server messages take over

                val pack = PackModel(
                    title         = event.title.ifBlank { originalState.packTitle },
                    sourceType    = sourceType,
                    createdAt     = System.currentTimeMillis(),
                    note          = event.description ?: "",
                    sourceSummary = event.sourceSummary,
                    category      = event.category,
                    emoji         = event.emoji,
                    color         = event.color,
                    language      = event.language?.ifBlank { originalState.language } ?: originalState.language,
                    uuid          = event.packId,
                    difficulty    = event.difficulty,
                    questionCount = event.expectedCount,
                )
                val localPackId = withContext(ioDispatcher) { packRepo.createPack(pack) }

                _uiState.update {
                    it.copy(
                        packId             = localPackId,
                        packUuid           = event.packId,
                        streamPackTitle    = pack.title,
                        streamPackEmoji    = pack.emoji ?: "",
                        streamPackColor    = pack.color ?: "",
                        conceptsFound      = event.conceptsFound,
                        questionsExpected  = event.expectedCount,
                        streamStage        = "Extracted ${event.conceptsFound} concepts — building questions…",
                        generationProgress = 0.12f,
                    )
                }
            }

            // ── question: save to Room + update live counter ───────────────
            is GenerationStreamEvent.Question -> {
                val packId = _uiState.value.packId
                if (packId > 0) {
                    val model = event.toQuestionModel(packId)
                    withContext(ioDispatcher) { questionRepo.insertQuestions(listOf(model)) }
                }

                _uiState.update { current ->
                    val done     = current.questionsCompleted + 1
                    val expected = current.questionsExpected.coerceAtLeast(1)
                    val canStart = !current.generatingInBackground &&
                            done >= (expected * EARLY_START_THRESHOLD).toInt().coerceAtLeast(1)
                    current.copy(
                        questionsCompleted = done,
                        canStartEarly      = canStart,
                    )
                }
            }

            // ── progress: server-driven progress bar + stage label ─────────
            is GenerationStreamEvent.Progress -> {
                _uiState.update {
                    it.copy(
                        generationProgress = event.percent / 100f,
                        streamStage        = event.message.ifBlank { it.streamStage },
                        conceptsFound      = event.conceptsFound.takeIf { c -> c > 0 } ?: it.conceptsFound,
                    )
                }
            }

            // ── done: finalize Room pack, transition to Done step ──────────
            is GenerationStreamEvent.Done -> {
                stopMessageRotation()

                val currentState = _uiState.value
                val finalCount   = event.total

                // Always update the pack's question_count in Room
                if (currentState.packId > 0) {
                    withContext(ioDispatcher) {
                        packRepo.updateQuestionCount(currentState.packId, finalCount)
                    }
                }

                checkPackLimit()

                // FIX: resolve common values once — shared by both branches
                val sourceDesc = when (currentState.sourceTab) {
                    SourceTab.FILE    -> currentState.fileName ?: "PDF"
                    SourceTab.TEXT    -> "Text"
                    SourceTab.WEB     -> currentState.webUrl.takeLast(30)
                    SourceTab.YOUTUBE -> "YouTube"
                }
                val uiQuestions = withContext(ioDispatcher) {
                    questionRepo.getDueQuestions(currentState.packId, limit = 10).map { it.toUiModel() }
                }

                if (!currentState.generatingInBackground) {
                    // User is still on generating screen → transition to Done
                    _uiState.update {
                        it.copy(
                            step               = AddPdfStep.DONE,
                            isLoading          = false,
                            packTitle          = it.streamPackTitle,
                            sourceDescription  = sourceDesc,
                            generatedQuestions = uiQuestions,
                            generationProgress = 1f,
                            streamStage        = "Pack ready!",
                        )
                    }
                    toast(UiText.Raw(R.string.add_pdf_pack_created), ToastType.SUCCESS)
                } else {
                    // User navigated away — silently complete in background
                    _uiState.update {
                        it.copy(
                            step                   = AddPdfStep.DONE,
                            isLoading              = false,
                            generatingInBackground = false,
                            generationProgress     = 1f,
                            packTitle              = it.streamPackTitle,
                            sourceDescription      = sourceDesc,
                            generatedQuestions     = uiQuestions,
                            streamStage            = "Pack ready!",
                        )
                    }
                    Log.d(TAG, "Background generation complete — $finalCount questions saved")
                }
            }

            // ── error: partial recovery or full failure ────────────────────
            is GenerationStreamEvent.Error -> {
                stopMessageRotation()
                Log.w(TAG, "Stream error [${event.code}] recoverable=${event.recoverable}: ${event.message}")

                if (event.recoverable && _uiState.value.questionsCompleted > 0) {
                    // Partial but usable result — let user continue
                    val currentState = _uiState.value
                    if (!currentState.generatingInBackground) {
                        toast(UiText.Raw(R.string.add_pdf_pack_created), ToastType.SUCCESS)
                    }
                    val uiQuestions = withContext(ioDispatcher) {
                        questionRepo.getDueQuestions(currentState.packId, limit = 10).map { it.toUiModel() }
                    }
                    _uiState.update {
                        it.copy(
                            step                   = AddPdfStep.DONE,
                            isLoading              = false,
                            generatingInBackground = false,
                            generatedQuestions     = uiQuestions,
                            generationProgress     = it.questionsCompleted / it.questionsExpected.coerceAtLeast(1).toFloat(),
                        )
                    }
                } else {
                    handleGenerationError(GenerationError.ServerError(event.message))
                }
            }
        }
    }

    /**
     * User taps "Start Now". Navigate to study screen immediately.
     * Generation continues in background, saving remaining questions to Room.
     */
    private fun startStudyEarly() {
        val packId = _uiState.value.packId
        if (packId <= 0) return
        _uiState.update { it.copy(generatingInBackground = true, canStartEarly = false) }
        _uiEffects.tryEmit(UiEffect.Navigate(packId.toString()))
    }

    /**
     * Rotates through contextual messages locally while waiting for server events.
     * Stops as soon as server starts sending real progress/question events.
     */
    private fun startMessageRotation() {
        stopMessageRotation()
        messageRotationJob = viewModelScope.launch {
            var idx = 0
            while (true) {
                if (_uiState.value.questionsCompleted == 0 && _uiState.value.conceptsFound == 0) {
                    _uiState.update { it.copy(progressMessageIndex = idx) }
                    idx++
                }
                delay(MESSAGE_ROTATION_MS)
            }
        }
    }

    private fun stopMessageRotation() {
        messageRotationJob?.cancel()
        messageRotationJob = null
    }

    // ── Error handler ─────────────────────────────────────────────────────
    private fun handleGenerationError(error: Throwable) {
        val msg: UiText = when (error) {
            is GenerationError.NetworkError         -> UiText.Raw(R.string.error_network)
            is GenerationError.QuotaExceeded        -> {
                _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_daily_limit)))
                UiText.Raw(R.string.add_pdf_error_daily_limit)
            }
            is GenerationError.ContentTooLong       -> {
                _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_ai_generation)))
                UiText.Raw(R.string.add_pdf_error_invalid_request)
            }
            is GenerationError.FileTooLarge         -> {
                _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_large_pdf)))
                UiText.Raw(R.string.add_pdf_error_invalid_request)
            }
            is GenerationError.AuthenticationFailed -> UiText.Raw(R.string.add_pdf_error_ai_service)
            is GenerationError.InvalidRequest       -> UiText.Raw(R.string.add_pdf_error_invalid_request)
            is GenerationError.ServerError          -> UiText.Raw(R.string.add_pdf_error_ai_service)
            else                                    -> UiText.Raw(R.string.add_pdf_error_ai_service)
        }

        if (error !is GenerationError.NetworkError && error !is GenerationError.QuotaExceeded) {
            Log.e(TAG, "Generation error [${error::class.simpleName}]: ${error.message}", error)
        } else {
            Log.w(TAG, "Generation error [${error::class.simpleName}]: ${error.message}")
        }

        toast(msg, ToastType.ERROR)

        if (!_uiState.value.generatingInBackground) {
            _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, isLoading = false, generationProgress = 0f, error = null) }
        } else {
            // Unrecoverable error during background generation — fetch what we have and show DONE
            viewModelScope.launch(ioDispatcher) {
                val packId     = _uiState.value.packId
                val uiQuestions = if (packId > 0) {
                    questionRepo.getDueQuestions(packId, limit = 10).map { it.toUiModel() }
                } else emptyList()

                _uiState.update {
                    it.copy(
                        step                   = AddPdfStep.DONE,
                        isLoading              = false,
                        generatingInBackground = false,
                        generatedQuestions     = uiQuestions,
                    )
                }
            }
        }
    }

    // ── Source resolution ─────────────────────────────────────────────────
    private fun resolveSource(state: AddPdfUiState): Pair<String, SourceType>? {
        return when (state.sourceTab) {
            SourceTab.FILE -> {
                val uri = state.fileUri ?: run {
                    toast(UiText.Raw(R.string.add_pdf_error_no_content), ToastType.ERROR)
                    return null
                }
                val base64 = readFileBytes(uri).map { bytes ->
                    android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                }.getOrElse { e ->
                    Log.e(TAG, "resolveSource: Failed to read file", e)
                    toast(UiText.Raw(R.string.add_pdf_error_extraction_failed, e.message ?: ""), ToastType.ERROR)
                    return null
                }
                Pair(base64, SourceType.PDF)
            }
            SourceTab.TEXT -> {
                val text = state.pasteText.trim()
                if (text.isBlank()) {
                    toast(UiText.Raw(R.string.add_pdf_error_no_content), ToastType.ERROR)
                    return null
                }
                Pair(text, SourceType.TEXT)
            }
            SourceTab.WEB -> {
                val url = state.webUrl.trim()
                if (url.isBlank()) { toast(UiText.Raw(R.string.add_pdf_error_empty_url), ToastType.ERROR); return null }
                if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
                    toast(UiText.Raw(R.string.add_pdf_error_invalid_url), ToastType.ERROR); return null
                }
                Pair(url, SourceType.URL)
            }
            SourceTab.YOUTUBE -> {
                val url = state.webUrl.trim()
                if (url.isBlank()) { toast(UiText.Raw(R.string.add_pdf_error_empty_url), ToastType.ERROR); return null }
                if (!YOUTUBE_PATTERN.containsMatchIn(url)) {
                    toast(UiText.Raw(R.string.add_pdf_error_invalid_youtube_url), ToastType.ERROR); return null
                }
                Pair(url, SourceType.YOUTUBE)
            }
        }
    }

    // ── Pack limit check ──────────────────────────────────────────────────
    private fun checkPackLimit() {
        packLimitJob?.cancel()
        packLimitJob = viewModelScope.launch {
            combine(packRepo.observeAllPacks(), appConfig.libraryFreePackLimitFlow) { packs, limit ->
                packs.size >= limit
            }
                .distinctUntilChanged() // FIX: only emit when the boolean flips, not on every pack update
                .collect { isLimitReached ->
                    _uiState.update { it.copy(isPackLimitReached = isLimitReached) }
                    if (isLimitReached) {
                        _uiEffects.emit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_unlimited_packs)))
                    }
                }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────
    private fun goBack() {
        when (_uiState.value.step) {
            AddPdfStep.SELECT_PDF -> _uiEffects.tryEmit(UiEffect.NavigateBack)
            AddPdfStep.CONFIGURE  -> _uiState.update {
                it.copy(step = AddPdfStep.SELECT_PDF, extractedText = null, extractionProgress = 0f, error = null)
            }
            AddPdfStep.GENERATING -> {
                generationJob?.cancel()
                stopMessageRotation()
                _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, isLoading = false) }
            }
            AddPdfStep.DONE -> _uiEffects.tryEmit(UiEffect.NavigateBack)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────
    override fun onCleared() {
        generationJob?.cancel()
        stopMessageRotation()
        super.onCleared()
    }

    // ── File helpers ──────────────────────────────────────────────────────
    private fun readFileBytes(fileUri: String): Result<ByteArray> {
        val uri   = fileUri.toUri()
        val bytes = readBytes(uri) ?: return Result.failure(IllegalStateException("Cannot open file: $fileUri"))
        if (bytes.isEmpty()) return Result.failure(IllegalStateException("PDF file is empty"))
        if (!isValidPdf(bytes)) return Result.failure(IllegalStateException("File is not a valid PDF"))
        return Result.success(bytes)
    }

    private fun isValidPdf(bytes: ByteArray): Boolean {
        if (bytes.size < 5) return false
        // Matches both "%PDF-" and "%PDF " magic bytes
        val header = bytes.sliceArray(0..4)
        return header.contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)) ||
                header.contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x20))
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

    // ── Helpers ───────────────────────────────────────────────────────────
    private fun toast(text: UiText, type: ToastType) =
        _uiEffects.tryEmit(UiEffect.ShowToast(text, type))

    // ── Companion ─────────────────────────────────────────────────────────
    private companion object {
        const val TAG = "AddPdfViewModel"

        /** Allow early start when this fraction of questions is ready. */
        const val EARLY_START_THRESHOLD = 0.20f

        /** How long to wait between local message rotations. */
        const val MESSAGE_ROTATION_MS = 2_800L

        val YOUTUBE_PATTERN = Regex(
            "^https?://(www\\.)?youtube\\.com/watch\\?v=[\\w-]{11}" +
                    "|^https?://youtu\\.be/[\\w-]{11}" +
                    "|^https?://(www\\.)?youtube\\.com/shorts/[\\w-]{11}",
            RegexOption.IGNORE_CASE,
        )

        private data class PremiumConfig(
            val isPremium:        Boolean,
            val isOcrLocked:      Boolean,
            val isThinkingLocked: Boolean,
            val maxPages:         Int,
            val maxFileSizeMb:    Int,
        )
    }
}