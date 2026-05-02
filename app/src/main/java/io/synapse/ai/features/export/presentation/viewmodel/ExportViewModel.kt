package io.synapse.ai.features.export.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.data.repo.PremiumManager
import io.synapse.ai.domain.repo.IPackRepository
import io.synapse.ai.domain.repo.IQuestionRepository
import io.synapse.ai.domain.usecase.ExportPackToPdfUseCase
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
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val packRepository: IPackRepository,
    private val questionRepository: IQuestionRepository,
    private val exportUseCase: ExportPackToPdfUseCase,
    private val exportLimitTracker: ExportLimitTracker,
    private val premiumManager: PremiumManager,
    private val institutionPreferences: InstitutionPreferences,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

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
            val isPro = premiumManager.isPro.value
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
                    isPro = isPro,
                    freeTierExportsUsed = usedCount,
                    options = it.options.copy(institutionHeader = savedHeader)
                )
            }
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
            ExportEvent.ShareExport -> shareExported()
            ExportEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    // ── Step navigation ───────────────────────────────────────────────────────

    private fun applyTemplate(template: ExportTemplate) {
        _uiState.update { current ->
            val newOptions = current.options.copy(template = template)
            val newSteps = stepsFor(template)
            // Clamp step index in case we switched from 4-step to 3-step
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

        // Gate free-tier on-demand (final check — preflight done at screen entry)
        if (!state.isPro && state.freeTierExportsUsed >= state.freeTierExportLimit) {
            _effects.tryEmit(ExportEffect.NavigateToPremium)
            return
        }

        _uiState.update { it.copy(isExporting = true, error = null) }

        viewModelScope.launch {
            val result = exportUseCase(packId, state.options)
            when (result) {
                is ExportResult.Success -> {
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

    private fun shareExported() {
        val uri = _uiState.value.exportedUri ?: return
        _effects.tryEmit(ExportEffect.ShareFile(uri))
    }
}
