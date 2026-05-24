package io.synapse.ai.features.premium.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.synapse.ai.data.repo.PremiumManager
import io.synapse.ai.domain.model.Entitlement
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntitlementViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
) : ViewModel() {

    val isPro: StateFlow<Boolean> = premiumManager.isPro
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = premiumManager.isPro.value,
        )

    val entitlement: StateFlow<Entitlement> = premiumManager.entitlement
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = premiumManager.entitlement.value,
        )

    fun onResume() = premiumManager.verifyWithServer()
    fun onRefresh() = premiumManager.verifyWithServer(force = true)

    fun onSignOut() {
        viewModelScope.launch { premiumManager.clearOnSignOut() }
    }
}