package com.venom.synapse.features.profile.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.features.profile.presentation.state.StudySettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages study preferences: daily goal, new-cards-per-day, review limit,
 * and daily reminder toggle + time. Persisted via DataStore.
 *
 * The [DataStore] is provided by [StudySettingsModule] — no Context is held
 * inside the ViewModel, which eliminates the context-leak lint warning.
 *
 * The fully-qualified [androidx.datastore.preferences.core.Preferences] type
 * is used in the constructor to avoid any import ambiguity with
 * [java.util.prefs.Preferences] that may exist on the classpath.
 */
@HiltViewModel
class StudySettingsViewModel @Inject constructor(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>,
) : ViewModel() {

    companion object {
        private val KEY_DAILY_GOAL      = intPreferencesKey("daily_goal")
        private val KEY_NEW_PER_DAY     = intPreferencesKey("new_per_day")
        private val KEY_REVIEW_LIMIT    = intPreferencesKey("review_limit")
        private val KEY_DAILY_REMINDER  = booleanPreferencesKey("daily_reminder")
        private val KEY_REMINDER_HOUR   = intPreferencesKey("reminder_hour")
        private val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")

        // ── Stepper bounds ────────────────────────────────────────────────────
        const val DAILY_GOAL_MIN  = 5;   const val DAILY_GOAL_MAX  = 200; const val DAILY_GOAL_STEP  = 5
        const val NEW_PER_DAY_MIN = 5;   const val NEW_PER_DAY_MAX = 100; const val NEW_PER_DAY_STEP = 5
        const val REVIEW_MIN      = 25;  const val REVIEW_MAX      = 500; const val REVIEW_STEP      = 25
    }

    val uiState: StateFlow<StudySettingsUiState> = dataStore.data
        .map { prefs ->
            StudySettingsUiState(
                dailyGoal            = prefs[KEY_DAILY_GOAL]      ?: 20,
                newCardsPerDay       = prefs[KEY_NEW_PER_DAY]     ?: 10,
                reviewLimit          = prefs[KEY_REVIEW_LIMIT]    ?: 100,
                dailyReminderEnabled = prefs[KEY_DAILY_REMINDER]  ?: true,
                reminderHour         = prefs[KEY_REMINDER_HOUR]   ?: 8,
                reminderMinute       = prefs[KEY_REMINDER_MINUTE] ?: 0,
            )
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = StudySettingsUiState(),
        )

    // ── Public actions ────────────────────────────────────────────────────────

    fun incrementDailyGoal()   = updateDailyGoal(uiState.value.dailyGoal + DAILY_GOAL_STEP)
    fun decrementDailyGoal()   = updateDailyGoal(uiState.value.dailyGoal - DAILY_GOAL_STEP)
    fun incrementNewPerDay()   = updateNewPerDay(uiState.value.newCardsPerDay + NEW_PER_DAY_STEP)
    fun decrementNewPerDay()   = updateNewPerDay(uiState.value.newCardsPerDay - NEW_PER_DAY_STEP)
    fun incrementReviewLimit() = updateReviewLimit(uiState.value.reviewLimit + REVIEW_STEP)
    fun decrementReviewLimit() = updateReviewLimit(uiState.value.reviewLimit - REVIEW_STEP)

    fun updateDailyReminder(enabled: Boolean) = save { it[KEY_DAILY_REMINDER] = enabled }

    fun updateReminderTime(hour: Int, minute: Int) = save {
        it[KEY_REMINDER_HOUR]   = hour
        it[KEY_REMINDER_MINUTE] = minute
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun updateDailyGoal(value: Int) =
        save { it[KEY_DAILY_GOAL] = value.coerceIn(DAILY_GOAL_MIN, DAILY_GOAL_MAX) }

    private fun updateNewPerDay(value: Int) =
        save { it[KEY_NEW_PER_DAY] = value.coerceIn(NEW_PER_DAY_MIN, NEW_PER_DAY_MAX) }

    private fun updateReviewLimit(value: Int) =
        save { it[KEY_REVIEW_LIMIT] = value.coerceIn(REVIEW_MIN, REVIEW_MAX) }

    private fun save(transform: suspend (MutablePreferences) -> Unit) {
        viewModelScope.launch { dataStore.edit { transform(it) } }
    }
}