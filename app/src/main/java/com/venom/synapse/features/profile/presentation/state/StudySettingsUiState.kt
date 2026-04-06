package com.venom.synapse.features.profile.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class StudySettingsUiState(
    val dailyGoal: Int = 20,
    val newCardsPerDay: Int = 10,
    val reviewLimit: Int = 100,
    val dailyReminderEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
)
