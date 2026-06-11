package io.synapse.ai.features.export.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.data.repo.PremiumManager
import io.synapse.ai.core.analytics.TrackingManager
import io.synapse.ai.core.analytics.model.AnalyticsEvent
import io.synapse.ai.domain.repo.IPackRepository
import io.synapse.ai.domain.repo.IQuestionRepository
import io.synapse.ai.domain.usecase.ExportPackToPdfUseCase
import io.synapse.ai.domain.usecase.ExportPackToWordUseCase
import io.synapse.ai.features.export.data.ExportLimitTracker
import io.synapse.ai.features.export.data.InstitutionPreferences
import io.synapse.ai.features.export.domain.ExportResult
import io.synapse.ai.features.export.domain.ExportTemplate
import io.synapse.ai.features.export.presentation.state.ExportEffect
import io.synapse.ai.features.export.presentation.state.ExportEvent
import io.synapse.ai.features.export.presentation.state.ExportUiState
import io.synapse.ai.features.export.presentation.state.stepsFor
import io.synapse.ai.navigation.SynapseScreen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val packRepository: IPackRepository,
    private val questionRepository: IQuestionRepository,
    private val exportUseCase: ExportPackToPdfUseCase,
    private val wordExportUseCase: ExportPackToWordUseCase,
    private val exportLimitTracker: ExportLimitTracker,
    private val premiumManager: PremiumManager,
    private val institutionPreferences: InstitutionPreferences,
    private val trackingManager: TrackingManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private var exportStartMs = 0L

    private val packId: Long = checkNotNull(savedStateHandle[SynapseScreen.Export.ARG_PACK_ID])

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ExportEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<ExportEffect> = _effects.asSharedFlow()

    init {
        loadInitialData()
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    private fun loadInitialData() {
        viewModelScope.launch {
            premiumManager.isPro.collect { isPro ->
                _uiState.update { it.copy(isPro = isPro) }
            }
        }
        viewModelScope.launch {
            val usedCount = withContext(ioDispatcher) { exportLimitTracker.currentCount() }
            val pack = withContext(ioDispatcher) { packRepository.getPackById(packId) }
            val questionCount = withContext(ioDispatcher) {
                questionRepository.countByPack(packId)
            }
            val savedHeader = institutionPreferences.header.first()
            _uiState.update {
                it.copy(
                    packTitle = pack?.title ?: "",
                    questionCount = questionCount,
                    freeTierExportsUsed = usedCount,
                    options = it.options.copy(institutionHeader = savedHeader)
                )
            }
            // Fire export_opened after data is ready
            trackingManager.logEvent(AnalyticsEvent.ExportOpened(questionCount))
            trackingManager.setCrashKey("export_template", _uiState.value.options.template.name.lowercase())
        }
    }

    // ── Event handler ─────────────────────────────────────────────────────────

    fun onEvent(event: ExportEvent) {
        when (event) {
            is ExportEvent.TemplateSelected -> applyTemplate(event.template)
            is ExportEvent.OptionsChanged -> _uiState.update { it.copy(options = event.options, exportedUri = null, exportedFileName = "") }
            is ExportEvent.HeaderChanged -> {
                _uiState.update {
                    it.copy(options = it.options.copy(institutionHeader = event.header), exportedUri = null, exportedFileName = "")
                }
                viewModelScope.launch {
                    institutionPreferences.saveHeader(event.header)
                }
            }
            ExportEvent.NextStep -> advanceStep()
            ExportEvent.PreviousStep -> retreatStep()
            ExportEvent.StartExport -> startExport()
            ExportEvent.StartWordExport -> startWordExport()
            is ExportEvent.DestinationPicked -> executeExport(event.uri)
            ExportEvent.ShareExport -> shareExported()
            ExportEvent.ClearExport -> _uiState.update { it.copy(exportedUri = null, exportedFileName = "") }
            ExportEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    // ── Step navigation ───────────────────────────────────────────────────────

    private fun applyTemplate(template: ExportTemplate) {
        val templateKey = template.name.lowercase()
        trackingManager.logEvent(AnalyticsEvent.TemplateSelected(templateKey))
        trackingManager.setCrashKey("export_template", templateKey)
        _uiState.update { current ->
            val newOptions = current.options.copy(template = template)
            val newSteps = stepsFor(template)
            val clampedIndex = current.currentStepIndex.coerceAtMost(newSteps.lastIndex)
            current.copy(
                options = newOptions,
                steps = newSteps,
                currentStepIndex = clampedIndex,
                exportedUri = null,
                exportedFileName = ""
            )
        }
    }

    private fun advanceStep() {
        _uiState.update {
            if (it.currentStepIndex < it.steps.lastIndex)
                it.copy(currentStepIndex = it.currentStepIndex + 1)
            else it
        }
    }

    private fun retreatStep() {
        val state = _uiState.value
        if (state.currentStepIndex > 0) {
            _uiState.update { it.copy(currentStepIndex = it.currentStepIndex - 1) }
        } else {
            _effects.tryEmit(ExportEffect.NavigateBack)
        }
    }

    // ── Export ────────────────────────────────────────────────────────────────

    private fun startExport() {
        val state = _uiState.value
        if (state.isExporting) return

        if (!state.isPro && state.freeTierExportsUsed >= state.freeTierExportLimit) {
            _effects.tryEmit(ExportEffect.NavigateToPremium)
            return
        }

        val fileName = buildFileName("pdf")
        exportStartMs = System.currentTimeMillis()
        trackingManager.logEvent(
            AnalyticsEvent.PdfExportStarted(
                templateType   = _uiState.value.options.template.name.lowercase(),
                includeAnswers = _uiState.value.options.includeAnswers,
            )
        )
        _uiState.update { it.copy(pendingExportType = "pdf") }
        _effects.tryEmit(ExportEffect.LaunchFilePicker("application/pdf", fileName))
    }

    private fun startWordExport() {
        val state = _uiState.value
        if (state.isExporting) return

        if (!state.isPro && state.freeTierExportsUsed >= state.freeTierExportLimit) {
            _effects.tryEmit(ExportEffect.NavigateToPremium)
            return
        }

        val fileName = buildFileName("doc")
        exportStartMs = System.currentTimeMillis()
        trackingManager.logEvent(
            AnalyticsEvent.PdfExportStarted(
                templateType   = _uiState.value.options.template.name.lowercase(),
                includeAnswers = _uiState.value.options.includeAnswers,
            )
        )
        _uiState.update { it.copy(pendingExportType = "word") }
        _effects.tryEmit(ExportEffect.LaunchFilePicker("application/msword", fileName))
    }

    private fun buildFileName(ext: String): String {
        val title = _uiState.value.packTitle
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9_\\-\\u0600-\\u06ff ]"), "_")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${safeTitle}_${timestamp}.$ext"
    }

    private fun executeExport(outputUri: Uri) {
        val pendingType = _uiState.value.pendingExportType ?: return
        _uiState.update { it.copy(pendingExportType = null, isExporting = true, error = null) }

        viewModelScope.launch {
            val currentOptions = _uiState.value.options
            val result = when (pendingType) {
                "pdf" -> exportUseCase(packId, currentOptions, outputUri)
                "word" -> wordExportUseCase.invoke(packId, currentOptions, outputUri)
                else -> return@launch
            }
            when (result) {
                is ExportResult.Success -> {
                    val durationMs = System.currentTimeMillis() - exportStartMs
                    val templateKey = currentOptions.template.name.lowercase()
                    trackingManager.logEvent(
                        AnalyticsEvent.PdfExportSuccess(
                            templateType  = templateKey,
                            questionCount = _uiState.value.questionCount,
                            durationMs    = durationMs,
                        )
                    )
                    if (durationMs > AnalyticsEvent.SLOW_PDF_RENDER_THRESHOLD_MS) {
                        trackingManager.logEvent(AnalyticsEvent.PdfRenderSlow(durationMs))
                    }
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportedUri = result.fileUri,
                            exportedFileName = result.fileName,
                            freeTierExportsUsed = it.freeTierExportsUsed + 1,
                        )
                    }
                }
                is ExportResult.Failure -> {
                    trackingManager.logEvent(
                        AnalyticsEvent.PdfExportFailed(
                            templateType = currentOptions.template.name.lowercase(),
                            reason       = result.cause::class.simpleName ?: "Unknown",
                        )
                    )
                    trackingManager.logException(result.cause, "PDF export failed")
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            error = result.cause.localizedMessage ?: "Export failed",
                        )
                    }
                }
            }
        }
    }

    // ── Share ─────────────────────────────────────────────────────────────────

    private fun shareExported() {
        val uri = _uiState.value.exportedUri ?: return
        val isWord = _uiState.value.exportedFileName.endsWith(".doc")
        val mimeType = if (isWord) "application/msword" else "application/pdf"
        trackingManager.logEvent(AnalyticsEvent.ShareClicked(if (isWord) "word" else "pdf"))
        _effects.tryEmit(ExportEffect.ShareFile(uri, mimeType))
    }
}
