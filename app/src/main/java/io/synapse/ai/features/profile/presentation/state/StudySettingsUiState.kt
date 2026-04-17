package io.synapse.ai.features.profile.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.core.theme.AppTheme

@Immutable
data class StudySettingsUiState(
    val dailyGoal: Int = 20,
    val newCardsPerDay: Int = 10,
    val reviewLimit: Int = 100,
    val dailyReminderEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val appTheme: AppTheme = AppTheme.DARK,
)
