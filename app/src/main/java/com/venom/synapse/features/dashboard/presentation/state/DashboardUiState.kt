package com.venom.synapse.features.dashboard.presentation.state

import androidx.compose.runtime.Immutable
import com.venom.synapse.R
import com.venom.synapse.core.ui.state.PackDisplayItem
import com.venom.synapse.features.dashboard.presentation.state.DashboardUiState.Companion.FREE_PACK_LIMIT

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

    /** True when the signed-in user holds an active premium subscription. */
    val isPremium: Boolean = false,
    /**
     * True when a free-tier user has reached [FREE_PACK_LIMIT].
     * Used to lock the "Add pack" FAB and surface an upgrade nudge.
     */
    val isPackLimitReached: Boolean = false,
    /** Total packs owned (across all pages, not just displayed 4). */
    val totalPackCount: Int = 0,

    val isLoading: Boolean = true,
    val error: String? = null,
) {
    companion object {
        /** Maximum packs allowed on the free tier. */
        const val FREE_PACK_LIMIT = 5
    }
}