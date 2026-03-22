package com.venom.synapse.features.session.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.core.ui.state.SoundType
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.domain.repo.IPackRepository
import com.venom.synapse.domain.srs.AnswerPayload
import com.venom.synapse.domain.srs.AnswerResult
import com.venom.synapse.domain.srs.SessionSummary
import com.venom.synapse.domain.srs.StudyEngine
import com.venom.synapse.domain.srs.StudyMode
import com.venom.synapse.features.session.presentation.state.QuestionUiModel
import com.venom.synapse.features.session.presentation.state.SessionSummaryUiState
import com.venom.synapse.features.session.presentation.state.SessionUiState
import com.venom.synapse.features.session.presentation.state.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════════════
 * SESSION VIEW MODEL — Orchestrates UI for an active study session
 * ═══════════════════════════════════════════════════════════════════
 *
 * RESPONSIBILITIES (and only these):
 *   1. Map [StudyEngine]'s SessionState → [SessionUiState] for Compose.
 *   2. Dispatch user actions (select answer, next, end) to StudyEngine.
 *   3. Emit one-time [UiEffect]s (navigation, sounds, errors).
 *   4. Manage autosave ticker for mid-session crash safety.
 *   5. Clean up resources in [onCleared].
 *
 * NOT responsible for:
 *   - SRS math (→ SynapseSmTwoAlgorithm)
 *   - Session orchestration logic (→ StudyEngineImpl)
 *   - Database writes (→ Repositories via StudyEngine)
 *   - JSON parsing (→ Layer 2 mappers)
 *
 * THREADING MODEL:
 *   - UI state updates: Main thread (StateFlow emission).
 *   - Engine calls: viewModelScope default (engine uses withContext internally).
 *   - Autosave flush: ioDispatcher (actual Room writes).
 *
 * TESTING:
 *   - Inject FakeStudyEngine + FakePackRepository.
 *   - Inject TestDispatcher for ioDispatcher.
 *   - Inject nowProvider lambda for deterministic time.
 *   - Uses SavedStateHandle for packId/mode so no need for Activity mocking.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val engine      : StudyEngine,
    private val packRepo    : IPackRepository,
    savedStateHandle        : SavedStateHandle,
    // Test hooks — default to production values
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val nowProvider : () -> Long = System::currentTimeMillis,
) : ViewModel() {

    // ── Navigation args from SavedStateHandle ────────────────────
    private val packId : Long   = savedStateHandle["packId"] ?: 0L
    private val modeArg: String = savedStateHandle["mode"]   ?: "MIXED"

    // ── UI State ─────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(SessionUiState(isLoading = true))
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    // ── Session Summary (shown on results screen) ─────────────────
    private val _summaryState = MutableStateFlow(SessionSummaryUiState())
    val summaryState: StateFlow<SessionSummaryUiState> = _summaryState.asStateFlow()

    /**
     * One-time UI effects.
     *
     * WHY extraBufferCapacity=8:
     *   During rapid answer submission, multiple effects fire (PlaySound,
     *   ShowToast for leech). Without buffer, slow Compose collection
     *   drops events. 8 is generous; real-world peak is ~3 per answer.
     */
    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    // ── Internal state ────────────────────────────────────────────
    private var autosaveJob        : Job?  = null
    private var observeJob         : Job?  = null
    private var questionStartTimeMs: Long  = 0L
    private var isInitialized             = false

    // ══════════════════════════════════════════════════════════════
    // INIT SESSION
    // ══════════════════════════════════════════════════════════════

    /**
     * Called once from the Compose screen's LaunchedEffect.
     * Guards against double-init on recomposition.
     *
     * Flow:
     *   1. Parse study mode from nav arg
     *   2. Fetch pack title for UI
     *   3. Create session plan (off-thread question selection + SRS scoring)
     *   4. Start session (persists session record, initialises engine state)
     *   5. Subscribe to engine's reactive state
     *   6. Start autosave ticker
     */
    fun initSession(requestedCount: Int = 20) {
        if (isInitialized) return
        isInitialized = true

        viewModelScope.launch {
            try {
                val mode = StudyMode.entries.find { it.name == modeArg } ?: StudyMode.MIXED

                val packTitle = withContext(ioDispatcher) {
                    packRepo.getPackById(packId)?.title ?: "Study Session"
                }

                _uiState.update {
                    it.copy(packTitle = packTitle, mode = mode.name, isLoading = true)
                }

                val plan = engine.createSessionPlan(packId, mode, requestedCount)

                if (plan.questions.isEmpty()) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "No questions available for this pack.")
                    }
                    return@launch
                }

                engine.startSession(plan)
                observeEngineState()
                startAutosaveTicker()
                questionStartTimeMs = nowProvider()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to start session: ${e.message}")
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // OBSERVE ENGINE STATE → MAP TO UI STATE
    // ══════════════════════════════════════════════════════════════

    /**
     * Subscribes to [StudyEngine.observeSessionState] and maps each
     * emission to [SessionUiState].
     *
     * WHY not use .map{}.stateIn():
     *   The engine emits SessionState (Layer 3 type). We need to
     *   transform questions to [QuestionUiModel] (Layer 4 type).
     *   A manual collect loop gives us control over error handling
     *   and avoids leaking Layer 3 types into the StateFlow signature.
     */
    private fun observeEngineState() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            engine.observeSessionState().collect { sessionState ->
                val currentQuestion: QuestionUiModel? = sessionState.currentQuestion?.toUiModel()

                _uiState.update { prev ->
                    prev.copy(
                        sessionId         = sessionState.sessionId,
                        currentQuestion   = currentQuestion,
                        questionIndex     = sessionState.currentIndex,
                        totalQuestions    = sessionState.totalQuestions,
                        answeredCount     = sessionState.answeredCount,
                        correctCount      = sessionState.correctCount,
                        progressPercent   = sessionState.progress,
                        accuracy          = sessionState.accuracy,
                        isLoading         = false,
                        isSessionFinished = sessionState.isFinished,
                        isInputEnabled    = true,
                        // Clear per-question feedback when moving to next
                        lastAnswerCorrect = if (prev.questionIndex != sessionState.currentIndex) null
                        else prev.lastAnswerCorrect,
                        lastExplanation   = if (prev.questionIndex != sessionState.currentIndex) null
                        else prev.lastExplanation,
                        showLeechAlert    = false,
                        error             = null,
                    )
                }

                if (sessionState.isFinished) finishSession()
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // SUBMIT ANSWER
    // ══════════════════════════════════════════════════════════════

    /**
     * Handles user answer submission.
     *
     * FLOW:
     *   1. Disable input immediately (prevent double-tap).
     *   2. Calculate response time from question start.
     *   3. Delegate to engine (pure computation, no DB write).
     *   4. Play sound effect based on correctness.
     *   5. If leech detected, show alert.
     *   6. Update UI with answer feedback.
     *   7. Re-enable input (user decides when to proceed via nextQuestion).
     *
     * WHY no DB write here:
     *   The engine buffers progress in memory. Writing 20 rows per
     *   session at the end (one @Transaction) vs 20 separate writes
     *   saves ~50× I/O and keeps the answer flow at <16ms per frame.
     */
    fun submitAnswer(questionId: Long, answer: AnswerPayload) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInputEnabled = false) }

            try {
                val responseTimeMs = nowProvider() - questionStartTimeMs

                val result: AnswerResult = engine.submitAnswer(
                    questionId     = questionId,
                    answer         = answer,
                    responseTimeMs = responseTimeMs,
                )

                _uiEffects.tryEmit(
                    UiEffect.PlaySound(if (result.isCorrect) SoundType.CORRECT else SoundType.WRONG)
                )

                if (result.isLeech) {
                    _uiState.update {
                        it.copy(showLeechAlert = true, leechQuestionId = questionId)
                    }
                    _uiEffects.tryEmit(UiEffect.PlaySound(SoundType.LEECH_WARNING))
                    _uiEffects.tryEmit(UiEffect.ShowToast("This question needs extra attention 🧠"))
                }

                _uiState.update {
                    it.copy(
                        lastAnswerCorrect = result.isCorrect,
                        lastExplanation   = result.explanation,
                        isInputEnabled    = true,
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isInputEnabled = true, error = "Failed to submit answer: ${e.message}")
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // NEXT QUESTION
    // ══════════════════════════════════════════════════════════════

    fun nextQuestion() {
        viewModelScope.launch {
            questionStartTimeMs = nowProvider()
            engine.nextQuestion()
        }
    }

    // ══════════════════════════════════════════════════════════════
    // END / FINISH SESSION
    // ══════════════════════════════════════════════════════════════

    /**
     * Explicitly ends the session (user taps "Finish" button).
     * Also called automatically when the engine signals isFinished.
     *
     * Captures pack title from current UI state so the summary screen
     * can display it without needing an extra repo call.
     */
    fun finishSession() {
        viewModelScope.launch {
            try {
                autosaveJob?.cancel()

                val summary: SessionSummary = engine.endSession()

                // Capture pack title from current UI state — avoids a
                // second repo round-trip just for a display string.
                val packTitle = _uiState.value.packTitle

                _summaryState.value = summary.toUiSummary(packTitle = packTitle)

                _uiEffects.tryEmit(UiEffect.PlaySound(SoundType.SESSION_COMPLETE))
                _uiEffects.tryEmit(UiEffect.Navigate("synapse/session/summary"))

            } catch (e: Exception) {
                _uiEffects.tryEmit(
                    UiEffect.ShowError(message = "Failed to save session: ${e.message}")
                )
            }
        }
    }

    fun cancelSession() {
        viewModelScope.launch {
            autosaveJob?.cancel()
            engine.cancelSession()
            _uiEffects.tryEmit(UiEffect.NavigateBack)
        }
    }

    // ══════════════════════════════════════════════════════════════
    // LEECH HANDLING
    // ══════════════════════════════════════════════════════════════

    fun dismissLeechAlert() {
        _uiState.update { it.copy(showLeechAlert = false, leechQuestionId = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ══════════════════════════════════════════════════════════════
    // AUTOSAVE TICKER
    // ══════════════════════════════════════════════════════════════

    /**
     * Periodically flushes in-memory progress to disk.
     *
     * WHY 30s (not per-answer):
     *   - Per-answer flush: 20 writes/session × WAL churn = poor battery.
     *   - End-only flush: Crash loses entire session's progress.
     *   - 30s ticker: At most ~60s of progress lost on crash (one or
     *     two questions). Compromise between safety and performance.
     */
    private fun startAutosaveTicker() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            while (true) {
                delay(AUTOSAVE_INTERVAL_MS)
                try {
                    engine.flushProgress()
                } catch (_: Exception) {
                    // Non-fatal — progress accumulates and flushes at session end.
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // CLEANUP
    // ══════════════════════════════════════════════════════════════

    override fun onCleared() {
        autosaveJob?.cancel()
        observeJob?.cancel()
        super.onCleared()
    }

    // ══════════════════════════════════════════════════════════════
    // MAPPING HELPERS
    // ══════════════════════════════════════════════════════════════

    /**
     * Maps domain [SessionSummary] → [SessionSummaryUiState].
     *
     * [packTitle] is injected here rather than stored in the domain layer
     * because pack display names are a UI concern only.
     *
     * [leechQuestionTexts]: When [StudyEngine] exposes leech question texts
     * in [SessionSummary], wire them here. Currently falls back to empty
     * list — the summary screen renders a generic count-only banner.
     */
    private fun SessionSummary.toUiSummary(packTitle: String) = SessionSummaryUiState(
        sessionId             = sessionId,
        packTitle             = packTitle,
        totalQuestions        = totalQuestions,
        answeredCount         = answeredCount,
        correctCount          = correctCount,
        accuracy              = accuracy,
        durationFormatted     = formatDuration(durationMs),
        leechCount            = leechCount,
        leechQuestionTexts    = emptyList(), // wire from engine when available
        newQuestionCount      = newQuestionCount,
        reviewedQuestionCount = reviewedQuestionCount,
    )

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes      = totalSeconds / 60
        val seconds      = totalSeconds % 60
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
    }

    companion object {
        private const val AUTOSAVE_INTERVAL_MS = 30_000L
    }
}