package com.venom.synapse.features.dashboard.presentation.viewmodel

import android.icu.util.Calendar
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.R
import com.venom.synapse.core.ui.components.PackDisplayItemBuilder
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.core.ui.state.UiText
import com.venom.synapse.data.repo.AppConfigProvider
import com.venom.synapse.data.repo.EntitlementManager
import com.venom.synapse.data.repo.QuizSessionManager
import com.venom.synapse.domain.repo.IPackRepository
import com.venom.synapse.domain.repo.IProgressRepository
import com.venom.synapse.domain.repo.IQuestionRepository
import com.venom.synapse.domain.repo.ISessionRepository
import com.venom.synapse.features.dashboard.presentation.state.DashboardUiState
import com.venom.synapse.navigation.SynapseScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val packRepo          : IPackRepository,
    private val questionRepo      : IQuestionRepository,
    private val progressRepo      : IProgressRepository,
    private val sessionRepo       : ISessionRepository,
    private val entitlementManager: EntitlementManager,
    private val appConfigProvider : AppConfigProvider,
    private val dataStore         : DataStore<Preferences>,
    private val quizSessionManager: QuizSessionManager,
    private val ioDispatcher      : CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState   = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    init { loadDashboard() }

    fun onStartStudying() {
        val state = _uiState.value
        if (state.totalDue == 0) {
            _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.dashboard_no_cards_to_review)))
            return
        }
        // Store the daily-goal cap so SessionViewModel can limit the queue.
        // packId = 0L signals "all packs" to SessionViewModel.
        quizSessionManager.maxCards = state.dailyGoal
        _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Quiz.createRoute(0L)))
    }

    fun onPackTapped(packId: Long) {
        val pack = _uiState.value.packs.find { it.id == packId }
        if (pack != null && pack.cardsToReview == 0) {
            _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.dashboard_pack_all_caught_up)))
            return
        }
        _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Quiz.createRoute(packId)))
    }

    fun onDeletePack(packId: Long) {
        viewModelScope.launch {
            try {
                packRepo.deletePack(packId)
            } catch (e: Exception) {
                val message = e.message?.takeIf { it.isNotBlank() }
                _uiEffects.emit(
                    UiEffect.ShowError(
                        text = if (message != null) UiText.Dynamic(message)
                        else UiText.Raw(R.string.dashboard_delete_pack_error),
                    )
                )
            }
        }
    }

    fun onSeeAllPacks() = _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Library.route))

    fun onAddPack() {
        if (_uiState.value.isPackLimitReached) {
            _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Premium.route))
        } else {
            _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.AddPdf.route))
        }
    }


    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                combine(
                    packRepo.observeAllPacks(),
                    entitlementManager.entitlement,
                    sessionRepo.observeLastSessionFinishedAt(),
                    dataStore.data,
                ) { packs, entitlement, _, prefs ->
                    Triple(packs, entitlement, prefs[intPreferencesKey("daily_goal")] ?: 20)
                }
                    .collectLatest { (rawPacks, entitlement, dailyGoal) ->
                        withContext(ioDispatcher) {
                            val allPacks = rawPacks.sortedByDescending { it.createdAt }

                            val isPremium          = entitlement.isAccessGranted
                            val totalPackCount     = allPacks.size
                            val isPackLimitReached = totalPackCount >= appConfigProvider.libraryFreePackLimit

                            val MS_PER_DAY      = 86_400_000L
                            val nowMs           = System.currentTimeMillis()
                            val todayIndex      = nowMs / MS_PER_DAY
                            val todayMidnightMs = todayIndex * MS_PER_DAY

                            val studiedIndices = sessionRepo.getStudiedDayIndices()
                            val currentStreak  = com.venom.synapse.domain.stats.StreakCalculator
                                .currentStreak(studiedIndices, todayIndex)

                            val todayStudied = sessionRepo
                                .getDailyActivity(todayMidnightMs, todayMidnightMs + MS_PER_DAY)
                                .firstOrNull()?.questionsStudied ?: 0

                            val (thisMondayMs, thisNextMondayMs) = com.venom.synapse.domain.stats.StreakCalculator
                                .currentWeekBounds(nowMs)
                            val lastMondayMs = thisMondayMs - 7 * MS_PER_DAY

                            val thisWeekActivity = sessionRepo.getDailyActivity(thisMondayMs, thisNextMondayMs)
                            val lastWeekActivity = sessionRepo.getDailyActivity(lastMondayMs, thisMondayMs)

                            val accuracyPct = if (thisWeekActivity.isEmpty()) 0
                            else (thisWeekActivity.map { it.accuracy }.average() * 100).toInt()

                            val lastAccuracyPct = if (lastWeekActivity.isEmpty()) 0
                            else (lastWeekActivity.map { it.accuracy }.average() * 100).toInt()

                            val accuracyDelta = if (lastWeekActivity.isEmpty()) null
                            else accuracyPct - lastAccuracyPct

                            val totalDurationMs = sessionRepo
                                .getDailyActivity(0, Long.MAX_VALUE)
                                .sumOf { it.totalDurationMs }
                            val timeStudiedMinutes = (totalDurationMs / 60_000L).toInt()

                            val allDisplayItems = allPacks.map { pack ->
                                PackDisplayItemBuilder.build(
                                    pack         = pack,
                                    questionRepo = questionRepo,
                                    progressRepo = progressRepo,
                                )
                            }
                            val displayedPacks     = allDisplayItems.take(2)
                            val totalDue           = allDisplayItems.sumOf { it.cardsToReview }
                            val totalCardsCount    = allDisplayItems.sumOf { it.totalCards }
                            val masteredCardsCount = allDisplayItems.sumOf { it.masteredCards }

                            _uiState.update {
                                DashboardUiState(
                                    greetingRes         = resolveGreeting(),
                                    todayStudied        = todayStudied,
                                    dailyGoal           = dailyGoal,
                                    streakDays          = currentStreak,
                                    accuracyPercent     = accuracyPct,
                                    accuracyDeltaRes    = R.string.stats_card_this_week,
                                    accuracyDelta       = accuracyDelta,
                                    timeStudiedMinutes  = timeStudiedMinutes,
                                    totalDue            = totalDue,
                                    totalCardsCount     = totalCardsCount,
                                    masteredCardsCount  = masteredCardsCount,
                                    packs               = displayedPacks,
                                    allPackIds          = allDisplayItems.map { it.id },
                                    isPremium           = isPremium,
                                    isPackLimitReached  = isPackLimitReached,
                                    totalPackCount      = totalPackCount,
                                    isLoading           = false,
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onEditPack(packId: Long) {
        _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.coming_soon)))
    }

    fun onExportPack(packId: Long) {
        _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.coming_soon)))
    }

    private fun resolveGreeting(): Int =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> R.string.synapse_subtitle_greeting_morning
            in 12..16 -> R.string.synapse_subtitle_greeting_afternoon
            else      -> R.string.synapse_subtitle_greeting_evening
        }
}
