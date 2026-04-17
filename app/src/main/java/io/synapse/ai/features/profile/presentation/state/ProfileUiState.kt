package io.synapse.ai.features.profile.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.R
import io.synapse.ai.data.sync.SyncConsent
import io.synapse.ai.data.sync.SyncStatus

@Immutable
data class ProfileUiState(
    val userName: String? = null,
    val userEmail: String? = null,
    val isPremium: Boolean = false,
    val isAnonymous: Boolean = true,
    val planLabelRes: Int = R.string.profile_plan_free,
    val avatarUrl: String? = null,
    val avatarInitial: Char = 'A',
    val packCount: Int = 0,
    val cardCount: Int = 0,
    val streakDays: Int = 0,
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalCardsLearned: Int = 0,
    val studyTimeHours: Float = 0f,
    val avgRetentionPct: Float = 0f,
    val isSyncEnabled: Boolean = false,
    val consentState: SyncConsent = SyncConsent.UNKNOWN,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val lastSyncedTime: String? = null,
    // Privacy consent toggles
    val analyticsEnabled: Boolean = false,
    val crashEnabled: Boolean = false,
    val pushEnabled: Boolean = false,
)
