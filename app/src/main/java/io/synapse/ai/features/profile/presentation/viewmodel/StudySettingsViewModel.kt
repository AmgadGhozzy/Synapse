package io.synapse.ai.features.profile.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.core.theme.AppTheme
import io.synapse.ai.core.notifications.ReminderScheduler
import io.synapse.ai.domains.study.reminder.ReminderSettings
import io.synapse.ai.features.profile.presentation.state.StudySettingsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class StudySettingsViewModel @Inject constructor(
    @param:Named("study_settings") private val dataStore: DataStore<Preferences>,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    companion object {
        private val KEY_DAILY_GOAL      = intPreferencesKey("daily_goal")
        private val KEY_NEW_PER_DAY     = intPreferencesKey("new_per_day")
        private val KEY_REVIEW_LIMIT    = intPreferencesKey("review_limit")
        private val KEY_DAILY_REMINDER  = booleanPreferencesKey("daily_reminder")
        private val KEY_REMINDER_HOUR   = intPreferencesKey("reminder_hour")
        private val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        private val KEY_APP_THEME       = stringPreferencesKey("app_theme")

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
                appTheme             = prefs[KEY_APP_THEME]?.let { savedTheme ->
                    try { AppTheme.valueOf(savedTheme) } catch (e: Exception) { AppTheme.SYSTEM }
                } ?: AppTheme.SYSTEM,
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
    fun updateAppTheme(theme: AppTheme) = save { it[KEY_APP_THEME] = theme.name }

    /**
     * Toggle the daily reminder on / off.
     * Persists [enabled] to DataStore, then reads fresh prefs (avoids stale StateFlow
     * snapshot) and calls [ReminderScheduler.reschedule] to schedule or cancel the alarm.
     */
    fun updateDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_DAILY_REMINDER] = enabled }
            val prefs = dataStore.data.first()
            val settings = ReminderSettings(
                isEnabled = enabled,
                hour      = prefs[KEY_REMINDER_HOUR]   ?: ReminderSettings.DEFAULT_HOUR,
                minute    = prefs[KEY_REMINDER_MINUTE]  ?: ReminderSettings.DEFAULT_MINUTE,
            )
            reminderScheduler.reschedule(settings)
        }
    }

    /**
     * Update reminder time — persists and reschedules the alarm immediately.
     * The new time takes effect starting from the next calendar day (or today
     * if the time hasn't passed yet).
     */
    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[KEY_REMINDER_HOUR]   = hour
                it[KEY_REMINDER_MINUTE] = minute
            }
            val prefs = dataStore.data.first()
            val settings = ReminderSettings(
                isEnabled = prefs[KEY_DAILY_REMINDER] ?: true,
                hour      = prefs[KEY_REMINDER_HOUR]  ?: hour,
                minute    = prefs[KEY_REMINDER_MINUTE] ?: minute,
            )
            if (settings.isEnabled) reminderScheduler.reschedule(settings)
        }
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