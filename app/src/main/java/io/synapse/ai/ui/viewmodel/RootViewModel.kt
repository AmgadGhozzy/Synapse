package io.synapse.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.core.ui.state.UiEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed interface SubtitleOverrideState {
    data object Default : SubtitleOverrideState
    data class Resource(val resId: Int) : SubtitleOverrideState
}

@HiltViewModel
class RootViewModel @Inject constructor() : ViewModel() {

    private val _onboardingDone = MutableStateFlow(false)
    val onboardingDone: StateFlow<Boolean> = _onboardingDone.asStateFlow()

    // ── UI Effects ──────────────────────────────────────────────
    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val uiEffects: SharedFlow<UiEffect> = _uiEffects.asSharedFlow()

    // ── Shell Overrides ─────────────────────────────────────────
    private val _subtitleOverride = MutableStateFlow<SubtitleOverrideState>(SubtitleOverrideState.Default)
    val subtitleOverride: StateFlow<SubtitleOverrideState> = _subtitleOverride.asStateFlow()

    fun setSubtitleResOverride(resId: Int) {
        _subtitleOverride.value = SubtitleOverrideState.Resource(resId)
    }

    fun clearSubtitleResOverride() {
        _subtitleOverride.value = SubtitleOverrideState.Default
    }

    fun navigateTo(route: String) {
        _uiEffects.tryEmit(UiEffect.Navigate(route))
    }

    fun navigateBack() {
        _uiEffects.tryEmit(UiEffect.NavigateBack)
    }

    fun setOnboardingState(showOnboarding: Boolean) {
        _onboardingDone.value = !showOnboarding
    }
}
