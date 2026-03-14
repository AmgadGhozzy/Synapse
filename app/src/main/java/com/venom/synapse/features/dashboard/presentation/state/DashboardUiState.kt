package com.venom.synapse.features.dashboard.presentation.state

import androidx.compose.runtime.Immutable
import com.venom.synapse.R
import com.venom.synapse.core.ui.state.PackDisplayItem

@Immutable
data class DashboardUiState(
    val greetingRes: Int = R.string.synapse_subtitle_greeting_morning,
    val todayStudied: Int = 0,
    val dailyGoal: Int = 30,
    val streakDays: Int = 0,
    val accuracyPercent: Int = 0,
    val accuracyDeltaRes: Int? = null,
    val accuracyDelta: Int? = null,
    val timeStudiedMinutes: Int = 0,
    val totalDue: Int = 0,
    val totalCardsCount: Int = 0,
    val masteredCardsCount: Int = 0,
    val packs: List<PackDisplayItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)
