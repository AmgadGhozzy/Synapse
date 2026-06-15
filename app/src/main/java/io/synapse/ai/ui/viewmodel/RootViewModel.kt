package io.synapse.ai.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.domains.config.data.AppSettingsRepository
import io.synapse.ai.domains.premium.repository.IPremiumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SubtitleOverrideState {
    data object Default : SubtitleOverrideState
    data class Resource(val resId: Int) : SubtitleOverrideState
}

@HiltViewModel
class RootViewModel @Inject constructor(
    private val settingsRepository: AppSettingsRepository,
    private val premiumRepository: IPremiumRepository,
) : ViewModel() {

    // ── Splash gate ──────────────────────────────────────────────────────────
    var isLoadingOnboardingState: Boolean by mutableStateOf(true)
        private set

    // ── Onboarding ───────────────────────────────────────────────────────────
    private val _onboardingDone = MutableStateFlow(false)
    val onboardingDone: StateFlow<Boolean> = _onboardingDone.asStateFlow()

    // ── Shell Overrides ──────────────────────────────────────────────────────
    private val _subtitleOverride =
        MutableStateFlow<SubtitleOverrideState>(SubtitleOverrideState.Default)
    val subtitleOverride: StateFlow<SubtitleOverrideState> = _subtitleOverride.asStateFlow()

    // ── Shared Intent ────────────────────────────────────────────────────────
    private val _sharedUri = MutableStateFlow<String?>(null)
    val sharedUri: StateFlow<String?> = _sharedUri.asStateFlow()

    // ── Paywall sheet ─────────────────────────────────────────────
    // Single source of truth for showing the Premium bottom sheet.
    // Any screen calls rootViewModel.showPaywall() — no nav needed.
    private val _paywallVisible = MutableStateFlow(false)
    val paywallVisible: StateFlow<Boolean> = _paywallVisible.asStateFlow()

    fun showPaywall() { _paywallVisible.value = true  }
    fun hidePaywall() { _paywallVisible.value = false }

    fun setSharedUri(uri: String) {
        _sharedUri.value = uri
    }

    fun consumeSharedUri() {
        _sharedUri.value = null
    }

    // Init
    init {
        viewModelScope.launch {
            val isFirstLaunch = settingsRepository.isFirstLaunch()
            _onboardingDone.value = !isFirstLaunch
            isLoadingOnboardingState = false
            
            // Warm the paywall cache so the first open has no loading spinner
            premiumRepository.warmCache()
        }
    }

    // ── Onboarding completion ─────────────────────────────────────────────────
    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setFirstLaunchComplete()  // persists to DataStore
            _onboardingDone.value = true
        }
    }

    // ── Shell override helpers ────────────────────────────────────────────────
    fun setSubtitleResOverride(resId: Int) {
        _subtitleOverride.value = SubtitleOverrideState.Resource(resId)
    }

    fun clearSubtitleResOverride() {
        _subtitleOverride.value = SubtitleOverrideState.Default
    }
}

