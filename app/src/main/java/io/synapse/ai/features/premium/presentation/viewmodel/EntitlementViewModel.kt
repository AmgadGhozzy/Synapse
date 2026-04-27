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

/**
 * Exposes gold/premium status to any Compose screen.
 *
 * S-1 FIX: Screens must observe premium state through the ViewModel layer,
 * not by injecting PremiumManager directly. Exposes isPro and entitlement
 * as StateFlow so Compose can use collectAsStateWithLifecycle().
 */
@HiltViewModel
class EntitlementViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
) : ViewModel() {

    /** True when the user holds an active Pro entitlement. */
    val isPro: StateFlow<Boolean> = premiumManager.isPro
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = premiumManager.isPro.value,
        )

    /** Full entitlement details (tier, expiry, status). */
    val entitlement: StateFlow<Entitlement> = premiumManager.entitlement
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = premiumManager.entitlement.value,
        )

    init {
        premiumManager.initialize()
    }

    fun onResume() = premiumManager.verifyWithServer()

    fun onRefresh() = premiumManager.verifyWithServer(force = true)

    fun onSignOut() {
        viewModelScope.launch {
            premiumManager.clearOnSignOut()
        }
    }
}