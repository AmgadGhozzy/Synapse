package io.synapse.ai.features.add_pdf.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.R
import io.synapse.ai.core.ui.model.toUiModel
import io.synapse.ai.core.ui.state.ToastType
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.core.ui.utils.ProgressMessageRotator
import io.synapse.ai.domains.config.data.AppConfigProvider
import io.synapse.ai.domains.study.model.QuestionType
import io.synapse.ai.domains.study.repository.IPackRepository
import io.synapse.ai.domains.study.repository.IQuestionRepository
import io.synapse.ai.features.add_pdf.domain.saver.DocumentSaver
import io.synapse.ai.features.add_pdf.domain.usecase.ValidateImportedDocumentUseCase
import io.synapse.ai.features.add_pdf.domain.usecase.ValidationResult
import io.synapse.ai.features.add_pdf.presentation.analytics.AddPdfAnalyticsTracker
import io.synapse.ai.features.add_pdf.presentation.coordinator.AddPdfGenerationCoordinator
import io.synapse.ai.features.add_pdf.presentation.coordinator.AddPdfGenerationUiEvent
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfStateReducer
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfStep
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiEvent
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiState
import io.synapse.ai.features.add_pdf.presentation.state.SourceTab
import io.synapse.ai.navigation.SynapseScreen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddPdfViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val generationCoordinator: AddPdfGenerationCoordinator,
    private val validateDocument: ValidateImportedDocumentUseCase,
    private val documentSaver: DocumentSaver,
    private val reducer: AddPdfStateReducer,
    private val analyticsTracker: AddPdfAnalyticsTracker,
    private val progressMessageRotator: ProgressMessageRotator,
    private val appConfig: AppConfigProvider,
    private val packRepo: IPackRepository,
    private val questionRepo: IQuestionRepository,
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
    private var packLimitJob: Job? = null

    init {
        observePremiumConfig()
        checkPackLimit()

        savedStateHandle.get<String>(SynapseScreen.AddPdf.ARG_URI)?.let { uriString ->
            handleSharedFileReceived(uriString)
        }
    }

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

    fun onEvent(event: AddPdfUiEvent) {
        when (event) {
            is AddPdfUiEvent.GoBack               -> goBack()
            is AddPdfUiEvent.DismissError         -> _uiState.update { it.copy(error = null) }
            is AddPdfUiEvent.SourceTabSelected    -> _uiState.update { it.copy(sourceTab = event.tab, error = null) }
            is AddPdfUiEvent.FileSelected         -> onFileSelected(event.uri, event.name, event.sizeMb)
            is AddPdfUiEvent.ClearFile -> _uiState.update { it.copy(fileUri = null, fileName = null, fileSizeMb = 0f, ocrEnabled = false, extractedText = null, error = null) }
            is AddPdfUiEvent.SharedFileReceived -> handleSharedFileReceived(event.uriString)
            is AddPdfUiEvent.PasteTextChanged -> _uiState.update { it.copy(pasteText = event.text, error = null) }
            is AddPdfUiEvent.WebUrlChanged        -> updateWebUrl(event.url)
            is AddPdfUiEvent.WebTabLockedClicked  -> _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_web_youtube_import)))
            is AddPdfUiEvent.ContinueToConfigure  -> continueToConfig()
            is AddPdfUiEvent.ScanDocumentClicked  -> Unit // launcher is invoked from screen layer
            is AddPdfUiEvent.ScannedPdfReady      -> onScannedPdfReady(event.uri, event.name, event.sizeMb, event.pageCount)
            is AddPdfUiEvent.QuestionCountChanged -> _uiState.update { it.copy(questionCount = event.count.coerceIn(3, 50)) }
            is AddPdfUiEvent.QuestionTypeToggled  -> toggleQuestionType(event.type)
            is AddPdfUiEvent.DifficultySelected   -> _uiState.update { it.copy(difficulty = event.difficulty) }
            is AddPdfUiEvent.FocusNotesChanged    -> _uiState.update { it.copy(focusNotes = event.notes) }
            is AddPdfUiEvent.LanguageSelected     -> _uiState.update { it.copy(language = event.languageCode) }
            is AddPdfUiEvent.ThinkingToggled      -> toggleThinking()
            is AddPdfUiEvent.GeneratePackToggled  -> _uiState.update { it.copy(generatePack = !it.generatePack) }
            is AddPdfUiEvent.GenerateSummaryToggled -> toggleSummaryGeneration()
            is AddPdfUiEvent.ShowSummaryPaywall   -> _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_pro_summary)))
            is AddPdfUiEvent.GeneratePack         -> if (_uiState.value.canGenerate) generateQuestions(event)
            is AddPdfUiEvent.StartStudyEarly      -> startStudyEarly()
            is AddPdfUiEvent.SavePdfClicked       -> savePdfToDownloads()
            is AddPdfUiEvent.OcrToggled           -> toggleOcr()
        }
    }

    private fun onFileSelected(uri: String, fileName: String, sizeMb: Float) {
        viewModelScope.launch {
            val result = validateDocument(fileName, sizeMb, null)
            handleValidationResult(result, uri, fileName, sizeMb, null)
        }
    }

    private fun onScannedPdfReady(uri: String, fileName: String, sizeMb: Float, pageCount: Int) {
        viewModelScope.launch {
            val result = validateDocument(fileName, sizeMb, pageCount)
            handleValidationResult(result, uri, fileName, sizeMb, pageCount, SourceTab.FILE)
        }
    }

    private fun handleValidationResult(
        result: ValidationResult,
        uri: String,
        fileName: String,
        sizeMb: Float,
        pageCount: Int?,
        tabToSet: SourceTab? = null
    ) {
        when (result) {
            is ValidationResult.Success -> {
                _uiState.update {
                    it.copy(
                        fileUri       = uri,
                        fileName      = fileName,
                        fileSizeMb    = sizeMb,
                        filePageCount = pageCount,
                        extractedText = null,
                        error         = null,
                        sourceTab     = tabToSet ?: it.sourceTab
                    )
                }
                continueToConfig()
            }
            is ValidationResult.UnsupportedType -> {
                _uiState.update { it.copy(error = UiText.Raw(R.string.add_pdf_error_unsupported_file_type)) }
            }
            is ValidationResult.FileTooLarge -> {
                if (!result.isPro) _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_large_pdf)))
                val msg = if (!result.isPro) R.string.add_pdf_error_file_too_large else R.string.add_pdf_error_file_too_large_pro
                _uiState.update { it.copy(error = UiText.Raw(msg, result.sizeMb, result.limitMb)) }
            }
            is ValidationResult.PageLimitExceeded -> {
                if (!result.isPro) _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_large_pdf)))
                val msg = if (!result.isPro) R.string.add_pdf_error_too_many_pages else R.string.add_pdf_error_too_many_pages_pro
                _uiState.update { it.copy(error = UiText.Raw(msg, result.pageCount, result.limit)) }
            }
        }
    }

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

    private fun toggleSummaryGeneration() {
        _uiState.update { it.copy(generateSummary = !it.generateSummary) }
    }

    private fun toggleOcr() {
        if (_uiState.value.isOcrFeatureLocked) {
            _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_pro_ocr)))
            return
        }
        _uiState.update { it.copy(ocrEnabled = !it.ocrEnabled, extractedText = null) }
    }

    private fun updateWebUrl(url: String) {
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

    private fun handleSharedFileReceived(uriString: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val uri = android.net.Uri.parse(uriString)
                var fileSizeMb = 0f
                var fileName = "shared_document.pdf"

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val sizeCol = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (sizeCol != -1) fileSizeMb = cursor.getLong(sizeCol) / (1024f * 1024f)
                        val nameCol = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameCol != -1) {
                            cursor.getString(nameCol)?.let { fileName = it }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(sourceTab = SourceTab.FILE) }
                    onEvent(AddPdfUiEvent.FileSelected(uriString, fileName, fileSizeMb))
                }
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    private fun continueToConfig() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_unlimited_packs)))
            return
        }
        val state = _uiState.value
        when (state.sourceTab) {
            SourceTab.FILE -> {
                if (state.fileUri != null) {
                    analyticsTracker.sourceSelected("pdf")
                    _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, error = null) }
                } else {
                    toast(UiText.Raw(R.string.add_pdf_error_no_content), ToastType.ERROR)
                }
            }
            SourceTab.TEXT -> {
                if (state.pasteText.trim().length < 10) {
                    toast(UiText.Raw(R.string.add_pdf_error_paste_more_text), ToastType.ERROR)
                } else {
                    analyticsTracker.sourceSelected("text")
                    _uiState.update {
                        it.copy(extractedText = state.pasteText.trim(), packTitle = "", step = AddPdfStep.CONFIGURE, error = null)
                    }
                }
            }
            SourceTab.WEB, SourceTab.YOUTUBE -> {
                if (state.webUrl.isBlank()) {
                    toast(UiText.Raw(R.string.add_pdf_error_empty_url), ToastType.ERROR)
                } else {
                    analyticsTracker.sourceSelected(if (state.sourceTab == SourceTab.YOUTUBE) "youtube" else "url")
                    _uiState.update { it.copy(packTitle = "", step = AddPdfStep.CONFIGURE, error = null) }
                }
            }
        }
    }

    private fun savePdfToDownloads() {
        val state = _uiState.value
        val uri = state.fileUri ?: run {
            toast(UiText.Raw(R.string.add_pdf_error_no_content), ToastType.ERROR)
            return
        }
        val destName = state.fileName ?: "document.pdf"

        viewModelScope.launch(ioDispatcher) {
            val result = documentSaver.savePdfToDownloads(uri, destName)
            if (result.isSuccess) {
                toast(UiText.Raw(R.string.save_pdf_success), ToastType.SUCCESS)
            } else {
                toast(UiText.Raw(R.string.save_pdf_error), ToastType.ERROR)
            }
        }
    }

    private fun checkPackLimit() {
        packLimitJob?.cancel()
        packLimitJob = viewModelScope.launch {
            combine(packRepo.observeAllPacks(), appConfig.libraryFreePackLimitFlow) { packs, limit ->
                packs.size >= limit
            }.distinctUntilChanged().collect { isLimitReached ->
                _uiState.update { it.copy(isPackLimitReached = isLimitReached) }
                if (isLimitReached) {
                    _uiEffects.emit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_unlimited_packs)))
                }
            }
        }
    }

    private fun generateQuestions(event: AddPdfUiEvent.GeneratePack) {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.ShowPaywall(UiText.Raw(R.string.feature_unlimited_packs)))
            return
        }

        _uiState.update {
            it.copy(
                summaryFocus = event.summaryFocus,
                summaryDepth = event.summaryDepth,
                summaryLanguage = event.summaryLanguage
            )
        }

        progressMessageRotator.start(viewModelScope) { idx ->
            val currentState = _uiState.value
            if (currentState.streamStage.isBlank() && currentState.streamPackTitle.isBlank()) {
                _uiState.update { it.copy(progressMessageIndex = idx) }
            }
        }

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            generationCoordinator.startGeneration(_uiState.value).collect { event ->
                when (event) {
                    is AddPdfGenerationUiEvent.Started -> {
                        _uiState.update { reducer.onGenerationStarted(it, event.isUploading) }
                    }
                    is AddPdfGenerationUiEvent.SourceResolutionFailed -> {
                        progressMessageRotator.stop()
                        toast(UiText.Raw(R.string.add_pdf_error_extraction_failed, event.errorMsg ?: ""), ToastType.ERROR)
                        _uiState.update { reducer.onGenerationFailed(it, false) }
                    }
                    is AddPdfGenerationUiEvent.SourceResolved -> {
                        _uiState.update { it.copy(isUploading = false) }
                    }

                    // ── Pack events ──────────────────────────────────
                    is AddPdfGenerationUiEvent.PackMetaCreated -> {
                        progressMessageRotator.stop()
                        _uiState.update {
                            reducer.onPackMetaCreated(it, event.localPackId, event.packUuid, event.title, event.emoji, event.color, event.conceptsFound, event.questionsExpected)
                        }
                    }
                    is AddPdfGenerationUiEvent.CurriculumOrganized -> {
                        _uiState.update { reducer.onCurriculumOrganized(it, event.moduleCount) }
                    }
                    is AddPdfGenerationUiEvent.QuestionAdded -> {
                        _uiState.update { reducer.onQuestionAdded(it) }
                    }
                    is AddPdfGenerationUiEvent.ProgressUpdated -> {
                        _uiState.update { reducer.onProgressUpdated(it, event.percent, event.message, event.conceptsFound) }
                    }
                    is AddPdfGenerationUiEvent.PackCompleted -> {
                        progressMessageRotator.stop()
                        val sourceDesc = when (_uiState.value.sourceTab) {
                            SourceTab.FILE    -> _uiState.value.fileName ?: "PDF"
                            SourceTab.TEXT    -> "Text"
                            SourceTab.WEB     -> _uiState.value.webUrl.takeLast(30)
                            SourceTab.YOUTUBE -> "YouTube"
                        }
                        _uiState.update { reducer.onPackCompleted(it, sourceDesc, event.generatedQuestions) }
                    }

                    // ── Summary events ───────────────────────────────
                    is AddPdfGenerationUiEvent.SummaryPhaseStarted -> {
                        // Restart progress rotator for summary phase
                        progressMessageRotator.start(viewModelScope) { idx ->
                            val currentState = _uiState.value
                            if (currentState.summaryStage.isBlank() && currentState.streamSummaryTitle.isBlank()) {
                                _uiState.update { it.copy(progressMessageIndex = idx) }
                            }
                        }
                        _uiState.update { reducer.onSummaryPhaseStarted(it) }
                    }
                    is AddPdfGenerationUiEvent.SummaryMetaCreated -> {
                        _uiState.update {
                            reducer.onSummaryMetaCreated(
                                it,
                                localSummaryId = event.localSummaryId,
                                title = event.title,
                                emoji = event.emoji,
                                color = event.color,
                                conceptsFound = event.conceptsFound,
                                sectionsExpected = event.sectionsExpected,
                            )
                        }
                    }
                    is AddPdfGenerationUiEvent.SummarySectionAdded -> {
                        _uiState.update { reducer.onSummarySectionAdded(it) }
                    }
                    is AddPdfGenerationUiEvent.SummaryProgressUpdated -> {
                        _uiState.update {
                            reducer.onSummaryProgressUpdated(it, event.percent, event.message, event.conceptsFound)
                        }
                    }
                    is AddPdfGenerationUiEvent.SummaryCompleted -> {
                        progressMessageRotator.stop()
                        _uiState.update { reducer.onSummaryCompleted(it) }
                    }

                    // ── All done ─────────────────────────────────────
                    is AddPdfGenerationUiEvent.AllCompleted -> {
                        progressMessageRotator.stop()
                        _uiState.update { reducer.onAllCompleted(it) }
                    }

                    is AddPdfGenerationUiEvent.GenerationError -> {
                        progressMessageRotator.stop()
                        val msg = UiText.Dynamic(event.error.message ?: "Unknown error")
                        toast(msg, ToastType.ERROR)

                        if (!_uiState.value.generatingInBackground) {
                            _uiState.update { reducer.onGenerationFailed(it, false) }
                        } else {
                            viewModelScope.launch(ioDispatcher) {
                                val packId = _uiState.value.packId
                                val uiQuestions = if (packId > 0) {
                                    questionRepo.observeQuestionsForPack(packId).first().map { it.toUiModel() }
                                } else emptyList()
                                _uiState.update { reducer.onGenerationFailed(it, true, uiQuestions) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startStudyEarly() {
        val s = _uiState.value
        _uiState.update { it.copy(generatingInBackground = true) }
        _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Quiz.createRoute(s.packId)))
    }

    private fun goBack() {
        when (_uiState.value.step) {
            AddPdfStep.SELECT_PDF -> _uiEffects.tryEmit(UiEffect.NavigateBack)
            AddPdfStep.CONFIGURE  -> _uiState.update {
                it.copy(step = AddPdfStep.SELECT_PDF, extractedText = null, error = null)
            }
            AddPdfStep.GENERATING -> {
                generationJob?.cancel()
                progressMessageRotator.stop()
                _uiState.update { it.copy(step = AddPdfStep.CONFIGURE, isLoading = false) }
            }
            AddPdfStep.DONE -> _uiEffects.tryEmit(UiEffect.NavigateBack)
        }
    }

    override fun onCleared() {
        generationJob?.cancel()
        progressMessageRotator.stop()
        super.onCleared()
    }

    private fun toast(text: UiText, type: ToastType) = _uiEffects.tryEmit(UiEffect.ShowToast(text, type))

    private companion object {
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
