package com.venom.synapse.features.dashboard.presentation.viewmodel

import android.icu.util.Calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.R
import com.venom.synapse.core.ui.components.PackDisplayItemBuilder
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.domain.repo.IPackRepository
import com.venom.synapse.domain.repo.IProgressRepository
import com.venom.synapse.domain.repo.IQuestionRepository
import com.venom.synapse.domain.repo.ISessionRepository
import com.venom.synapse.features.dashboard.presentation.state.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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

/**
 * Dashboard ViewModel.
 *
 * Derives "recent packs" from the pack repository (sorted by createdAt DESC,
 * limited to 4). Stats (streak, accuracy, time) should be sourced from a
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val packRepo: IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val progressRepo: IProgressRepository,
    private val sessionRepo: ISessionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // SharedFlow: one-shot events that must never be conflated (navigation, toasts)
    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    init { loadDashboard() }

    // ── Public actions ─────────────────────────────────────────

    fun refresh() = loadDashboard()

    fun onStartStudying() {
        val firstId = _uiState.value.packs.firstOrNull()?.id ?: return
        _uiEffects.tryEmit(UiEffect.Navigate("synapse/quiz/$firstId"))
    }

    fun onPackTapped(packId: Long) {
        _uiEffects.tryEmit(UiEffect.Navigate("synapse/quiz/$packId"))
    }

    fun onDeletePack(packId: Long) {
        viewModelScope.launch {
            try {
                packRepo.deletePack(packId)
                _uiState.update { s ->
                    s.copy(packs = s.packs.filter { it.id != packId })
                }
                _uiEffects.emit(UiEffect.ShowToast("Pack deleted"))
            } catch (e: Exception) {
                _uiEffects.emit(UiEffect.ShowError(message = e.message ?: "Could not delete pack"))
            }
        }
    }

    fun onSeeAllPacks() = _uiEffects.tryEmit(UiEffect.Navigate("synapse/library"))

    fun onAddPack() = _uiEffects.tryEmit(UiEffect.Navigate("synapse/add-pdf"))

    // ── Data loading ───────────────────────────────────────────

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                withContext(ioDispatcher) {
                    val allPacks = packRepo.observeAllPacks().first()
                        .sortedByDescending { it.createdAt }

                    val nowMs = System.currentTimeMillis()
                    val MS_PER_DAY = 86_400_000L
                    val todayIndex = nowMs / MS_PER_DAY
                    val todayMidnightMs = todayIndex * MS_PER_DAY

                    val studiedIndices = sessionRepo.getStudiedDayIndices()
                    val currentStreak = com.venom.synapse.domain.stats.StreakCalculator.currentStreak(studiedIndices, todayIndex)

                    val todayActivity = sessionRepo.getDailyActivity(todayMidnightMs, todayMidnightMs + MS_PER_DAY).firstOrNull()
                    val todayStudied = todayActivity?.questionsStudied ?: 0

                    val (thisMondayMs, thisNextMondayMs) = com.venom.synapse.domain.stats.StreakCalculator.currentWeekBounds(nowMs)
                    val lastMondayMs = thisMondayMs - 7 * 86_400_000L
                    
                    val thisWeekActivity = sessionRepo.getDailyActivity(thisMondayMs, thisNextMondayMs)
                    val lastWeekActivity = sessionRepo.getDailyActivity(lastMondayMs, thisMondayMs)
                    
                    val accuracyPct = if (thisWeekActivity.isEmpty()) 0
                    else (thisWeekActivity.map { it.accuracy }.average() * 100).toInt()

                    val lastAccuracyPct = if (lastWeekActivity.isEmpty()) 0
                    else (lastWeekActivity.map { it.accuracy }.average() * 100).toInt()

                    val accuracyDelta = if (lastWeekActivity.isEmpty()) null else accuracyPct - lastAccuracyPct

                    val dailyGoal = 30 

                    val totalDurationMs = sessionRepo.getDailyActivity(0, Long.MAX_VALUE).sumOf { it.totalDurationMs }
                    val timeStudiedMinutes = (totalDurationMs / 60000L).toInt()

                    val packs = allPacks.take(4).map { pack ->
                        PackDisplayItemBuilder.build(
                            pack = pack,
                            questionRepo = questionRepo,
                            progressRepo = progressRepo,
                        )
                    }

                    val totalDue = packs.sumOf { it.cardsToReview }
                    val totalCardsCount = packs.sumOf { it.totalCards }
                    val masteredCardsCount = packs.sumOf { it.masteredCards }

                    _uiState.update {
                        DashboardUiState(
                            greetingRes        = resolveGreeting(),
                            todayStudied       = todayStudied,
                            dailyGoal          = dailyGoal,
                            streakDays         = currentStreak,
                            accuracyPercent    = accuracyPct,
                            accuracyDeltaRes   = R.string.stats_card_this_week,
                            accuracyDelta      = accuracyDelta,
                            timeStudiedMinutes = timeStudiedMinutes,
                            totalDue           = totalDue,
                            totalCardsCount    = totalCardsCount,
                            masteredCardsCount = masteredCardsCount,
                            packs              = packs,
                            isLoading          = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun resolveGreeting(): Int =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> R.string.synapse_subtitle_greeting_morning
            in 12..16 -> R.string.synapse_subtitle_greeting_afternoon
            else      -> R.string.synapse_subtitle_greeting_evening
        }

}
