package io.synapse.ai.features.dashboard.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.PackDisplayItem

@Immutable
data class DashboardUiState(
    val greetingRes: Int = R.string.synapse_subtitle_greeting_morning,

    val todayStudied: Int = 0,
    val dailyGoal: Int = 30,
    val streakDays: Int = 0,
    val accuracyPercent: Int = 0,
    val masteredCardsCount: Int = 0,
    val totalDue: Int = 0,
    val totalCardsCount: Int = 0,

    val packs: List<PackDisplayItem> = emptyList(),
    val allPackIds: List<Long> = emptyList(),
    val isPremium: Boolean = false,
    val isPackLimitReached: Boolean = false,
    val totalPackCount: Int = 0,

    val isLoading: Boolean = true,
    val error: String? = null,
)