package com.venom.synapse.features.stats.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class StatsUiState(
    val totalPacks: Int = 0,
    val totalQuestions: Int = 0,
    val totalSessions: Int = 0,
    val dueToday: Int = 0,
    val averageAccuracy: Float = 0f,
    val retentionDeltaPct: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val streakActiveDays: List<Boolean> = List(7) { false },
    val todayDayIndex: Int = 0,
    val weeklyActivity: List<DayActivity> = emptyList(),
    val totalWeeklyCards: Int = 0,
    val timeStudiedHours: Float = 0f,
    val bestDayLabel: String = "",
    val bestDayCards: Int = 0,
    val isLoading: Boolean = true
)

@Immutable
data class DayActivity(
    val dayLabel: String,
    val questionsStudied: Int,
    val accuracy: Float
)
