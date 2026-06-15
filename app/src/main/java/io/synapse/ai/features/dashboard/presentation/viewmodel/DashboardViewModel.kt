package io.synapse.ai.features.dashboard.presentation.viewmodel

import android.icu.util.Calendar
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.ToastType
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.domains.config.data.AppConfigProvider
import io.synapse.ai.domains.premium.data.PremiumManager
import io.synapse.ai.domains.study.usecase.BuildPackDisplayItemsUseCase
import io.synapse.ai.domains.study.usecase.StreakCalculator
import io.synapse.ai.domains.study.data.QuizSessionManager
import io.synapse.ai.domains.study.model.PackOverviewModel
import io.synapse.ai.domains.study.repository.IPackRepository
import io.synapse.ai.domains.study.repository.IProgressRepository
import io.synapse.ai.domains.study.repository.IQuestionRepository
import io.synapse.ai.domains.study.repository.ISessionRepository
import io.synapse.ai.features.dashboard.presentation.state.DashboardUiState
import io.synapse.ai.navigation.SynapseScreen
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val packRepo: IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val progressRepo: IProgressRepository,
    private val sessionRepo: ISessionRepository,
    private val premiumManager: PremiumManager,
    private val appConfigProvider: AppConfigProvider,
    @param:Named("study_settings") private val dataStore: DataStore<Preferences>,
    private val quizSessionManager: QuizSessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    init { loadDashboard() }

    fun onStartStudying() {
        val state = _uiState.value
        if (state.totalDue == 0) {
            _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.dashboard_no_cards_to_review), ToastType.INFO))
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
            _uiEffects.tryEmit(UiEffect.ShowToast(UiText.Raw(R.string.dashboard_pack_all_caught_up), ToastType.INFO))
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
            _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.AddPdf.createRoute()))
        }
    }
    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                combine(
                    packRepo.observePackOverviews(),
                    premiumManager.isPro,
                    sessionRepo.observeLastSessionFinishedAt(),
                    dataStore.data,
                    appConfigProvider.libraryFreePackLimitFlow,
                ) { packs, isPremiumUser, _, prefs, packLimit ->
                    DashboardData(packs, isPremiumUser, prefs[intPreferencesKey("daily_goal")] ?: 20, packLimit, prefs[booleanPreferencesKey("swipe_hint_shown")] != true)
                }
                    .collectLatest { data ->
                        val isPremium = data.isPremium
                        val totalPackCount = data.packs.size
                        val isPackLimitReached = totalPackCount >= data.packLimit

                        val MS_PER_DAY = 86_400_000L
                        val nowMs = System.currentTimeMillis()
                        val todayIndex = nowMs / MS_PER_DAY
                        val todayMidnightMs = todayIndex * MS_PER_DAY

                        val (allActivity, allDisplayItems) = coroutineScope {
                            val activityDef = async { sessionRepo.getDailyActivity(0, Long.MAX_VALUE) }
                            val buildBatchDef = async {
                                BuildPackDisplayItemsUseCase.buildBatch(
                                    packs = data.packs,
                                    questionRepo = questionRepo,
                                    progressRepo = progressRepo
                                )
                            }
                            Pair(activityDef.await(), buildBatchDef.await())
                        }
                        
                        val studiedIndices = allActivity.map { it.dayEpochMs / MS_PER_DAY }.sortedDescending()
                        val currentStreak = StreakCalculator.currentStreak(studiedIndices, todayIndex)

                        val todayStudied = allActivity.firstOrNull { it.dayEpochMs == todayMidnightMs }?.questionsStudied ?: 0

                        val (thisMondayMs, thisNextMondayMs) = StreakCalculator.currentWeekBounds(nowMs)
                        val lastMondayMs = thisMondayMs - 7 * MS_PER_DAY

                        val thisWeekActivity = allActivity.filter { it.dayEpochMs in thisMondayMs until thisNextMondayMs }
                        val lastWeekActivity = allActivity.filter { it.dayEpochMs in lastMondayMs until thisMondayMs }

                        val accuracyPct = if (thisWeekActivity.isEmpty()) 0
                        else (thisWeekActivity.map { it.accuracy }.average() * 100).toInt()

                        val lastAccuracyPct = if (lastWeekActivity.isEmpty()) 0
                        else (lastWeekActivity.map { it.accuracy }.average() * 100).toInt()

                        val accuracyDelta = if (lastWeekActivity.isEmpty()) null
                        else accuracyPct - lastAccuracyPct

                        val totalDurationMs = allActivity.sumOf { it.totalDurationMs }
                        val timeStudiedMinutes = (totalDurationMs / 60_000L).toInt()
                        val displayedPacks = allDisplayItems.take(2)
                        val totalDue = allDisplayItems.sumOf { it.cardsToReview }
                        val totalCardsCount = allDisplayItems.sumOf { it.totalCards }
                        val masteredCardsCount = allDisplayItems.sumOf { it.masteredCards }
                        _uiState.update {
                            DashboardUiState(
                                greetingRes = resolveGreeting(),
                                todayStudied = todayStudied,
                                dailyGoal = data.dailyGoal,
                                streakDays = currentStreak,
                                accuracyPercent = accuracyPct,
                                masteredCardsCount = masteredCardsCount,
                                totalDue = totalDue,
                                totalCardsCount = totalCardsCount,
                                packs = displayedPacks.toImmutableList(),
                                allPackIds = allDisplayItems.map { it.id }.toImmutableList(),
                                isPremium = isPremium,
                                isPackLimitReached = isPackLimitReached,
                                totalPackCount = totalPackCount,
                                isLoading = false,
                                showSwipeHint = data.showSwipeHint,
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSwipeHintComplete() {
        viewModelScope.launch {
            dataStore.edit { it[booleanPreferencesKey("swipe_hint_shown")] = true }
        }
    }

    fun onEditPack(packId: Long) = _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Overview.createRoute(packId)))

    fun onExportPack(packId: Long) = _uiEffects.tryEmit(UiEffect.Navigate(SynapseScreen.Export.createRoute(packId)))

    private fun resolveGreeting(): Int = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> R.string.synapse_subtitle_greeting_morning
        in 12..16 -> R.string.synapse_subtitle_greeting_afternoon
        else -> R.string.synapse_subtitle_greeting_evening
    }

    private data class DashboardData(
        val packs: List<PackOverviewModel>,
        val isPremium: Boolean,
        val dailyGoal: Int,
        val packLimit: Int,
        val showSwipeHint: Boolean,
    )
}







