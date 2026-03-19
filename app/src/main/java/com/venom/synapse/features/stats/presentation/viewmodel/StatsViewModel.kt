package com.venom.synapse.features.stats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.domain.model.DayActivityModel
import com.venom.synapse.domain.repo.IPackRepository
import com.venom.synapse.domain.repo.IQuestionRepository
import com.venom.synapse.domain.repo.ISessionRepository
import com.venom.synapse.domain.stats.StreakCalculator
import com.venom.synapse.features.stats.presentation.state.DayActivity
import com.venom.synapse.features.stats.presentation.state.StatsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val packRepo: IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val sessionRepo: ISessionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var streakObserverJob: Job? = null

    init { 
        loadStats()
        observeStreaks()
    }

    fun refresh() = loadStats()

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(ioDispatcher) {
                    val allPacks = packRepo.observeAllPacks().first()
                    val totalPacks = allPacks.size

                    var totalQuestions = 0
                    var dueToday = 0
                    for (pack in allPacks) {
                        totalQuestions += questionRepo.countByPack(pack.id)
                        dueToday += questionRepo.getDueQuestions(pack.id, limit = 200).size
                    }

                    // Weekly activity: from session repository.
                    val nowMs = System.currentTimeMillis()
                    val (mondayMs, nextMondayMs) = StreakCalculator.currentWeekBounds(nowMs)
                    
                    val weeklyModels = sessionRepo.getDailyActivity(mondayMs, nextMondayMs)
                    val weeklyActivity = buildWeeklyActivityUi(weeklyModels, mondayMs)

                    val totalWeeklyCards = weeklyActivity.sumOf { it.questionsStudied }
                    val avgAccuracy = if (weeklyActivity.isEmpty()) 0f
                    else weeklyActivity.map { it.accuracy }.average().toFloat()

                    // Previous week for delta calculation
                    val lastMondayMs = mondayMs - 7 * 86_400_000L
                    val lastWeekModels = sessionRepo.getDailyActivity(lastMondayMs, mondayMs)
                    val lastWeekAvgAccuracy = if (lastWeekModels.isEmpty()) 0f
                    else lastWeekModels.map { it.accuracy }.average().toFloat()

                    val deltaPct = if (lastWeekAvgAccuracy == 0f && avgAccuracy == 0f) 0 
                    else ((avgAccuracy - lastWeekAvgAccuracy) * 100).toInt()

                    // Best day — highest card count in the week
                    val bestDay = weeklyActivity.maxByOrNull { it.questionsStudied }

                    val totalDurationMs = sessionRepo.getDailyActivity(0, Long.MAX_VALUE).sumOf { it.totalDurationMs }
                    val timeStudiedHours = totalDurationMs / 3600000f

                    _uiState.update {
                        it.copy(
                            totalPacks = totalPacks,
                            totalQuestions = totalQuestions,
                            dueToday = dueToday,
                            averageAccuracy = avgAccuracy,
                            retentionDeltaPct = deltaPct,
                            weeklyActivity = weeklyActivity,
                            totalWeeklyCards = totalWeeklyCards,
                            timeStudiedHours = timeStudiedHours,
                            bestDayLabel = bestDay?.dayLabel ?: "",
                            bestDayCards = bestDay?.questionsStudied ?: 0,
                            isLoading = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observeStreaks() {
        streakObserverJob?.cancel()
        streakObserverJob = viewModelScope.launch {
            sessionRepo.observeStudiedDayIndices().collect { indices ->
                val todayIndex = System.currentTimeMillis() / 86_400_000L
                val currentStreak = StreakCalculator.currentStreak(indices, todayIndex)
                val bestStreak = StreakCalculator.bestStreak(indices)
                val streakActiveDays = StreakCalculator.weekDots(indices, todayIndex)
                val todayDow = ((todayIndex % 7) + 3).toInt() % 7 // Mon=0..Sun=6

                _uiState.update { state ->
                    state.copy(
                        currentStreak = currentStreak,
                        bestStreak = bestStreak,
                        streakActiveDays = streakActiveDays,
                        todayDayIndex = todayDow
                    )
                }
            }
        }
    }

    /**
     * Builds 7-day activity UI models from the current week's domain models.
     */
    private fun buildWeeklyActivityUi(
        models: List<DayActivityModel>,
        mondayMs: Long
    ): List<DayActivity> {
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val MS_PER_DAY = 86_400_000L

        return labels.indices.map { i ->
            val dayMs = mondayMs + (i * MS_PER_DAY)
            val modelForDay = models.find { it.dayEpochMs == dayMs }

            DayActivity(
                dayLabel = labels[i],
                questionsStudied = modelForDay?.questionsStudied ?: 0,
                accuracy = modelForDay?.accuracy ?: 0f
            )
        }
    }
}
