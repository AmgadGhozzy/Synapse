package com.venom.synapse.ui.viewmodel

// auto-visible across packages — must be
// explicitly imported. Caused all 6 VM errors.

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom.synapse.domain.repo.PremiumRepository
import com.venom.synapse.features.premium.presentation.state.PlayPriceInfo
import com.venom.synapse.features.premium.presentation.state.PremiumEvent
import com.venom.synapse.features.premium.presentation.state.PremiumUiState
import com.venom.synapse.features.premium.presentation.state.toUiModels
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val repository: PremiumRepository,
    @ApplicationContext private val context: Context,
    // private val billing: BillingRepository,  // uncomment when ready
) : ViewModel() {

    private val _uiState = MutableStateFlow<PremiumUiState>(PremiumUiState.Loading)
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    // SharedFlow — never StateFlow — for one-shot events (navigation, snackbar)
    private val _events = MutableSharedFlow<PremiumEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    init {
        loadConfig()
    }

    // ── Load config from domain ───────────────────────────────────

    fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = PremiumUiState.Loading

            repository.loadPaywallConfig()
                .onSuccess { config ->

                    // Swap for real billing prices once BillingRepository is wired:
                    // val playPrices = billing.queryPrices(config.plans.map { it.skuId })
                    //     .getOrDefault(emptyMap())
                    val playPrices = emptyMap<String, PlayPriceInfo>()

                    // toUiModels() is an extension on PaywallConfig defined in
                    // PremiumUiModels.kt — now correctly imported above.
                    val (planUiModels, featureUiModels) = config.toUiModels(context, playPrices)

                    _uiState.value = PremiumUiState.Ready(
                        plans = planUiModels,
                        features = featureUiModels,
                        selectedPlanId = planUiModels.firstOrNull { it.isHighlighted }?.id
                            ?: planUiModels.firstOrNull()?.id
                            ?: "",
                        trialDays = config.trialDays,
                    )
                }
                .onFailure { error ->
                    _uiState.value = PremiumUiState.Error(
                        message = error.message ?: "Failed to load plans"
                    )
                }
        }
    }

    // ── Plan selection ────────────────────────────────────────────

    fun selectPlan(planId: String) {
        _uiState.update { state ->
            if (state is PremiumUiState.Ready) state.copy(selectedPlanId = planId) else state
        }
    }

    // ── Purchase ──────────────────────────────────────────────────

    fun startPurchase() {
        val state = _uiState.value as? PremiumUiState.Ready ?: return
        val plan = state.plans.firstOrNull { it.id == state.selectedPlanId } ?: return

        viewModelScope.launch {
            _uiState.update { current ->
                (current as? PremiumUiState.Ready)?.copy(isPurchasing = true) ?: current
            }

            // Replace with real billing call when BillingRepository is ready:
            //   billing.launchBillingFlow(plan.skuId)
            //       .onSuccess { _events.emit(PremiumEvent.PurchaseSuccess(plan.id)) }
            //       .onFailure { _events.emit(PremiumEvent.PurchaseFailed(it.message ?: "Error")) }
            delay(1_200) // placeholder — remove when billing is wired

            // BUG 5 FIX: clear isPurchasing BEFORE emitting success.
            // The screen is dismissed upon PurchaseSuccess — if we cleared it after
            // emitting, the update would race against (or follow) screen destruction,
            // causing a redundant recomposition on a dead observer.
            _uiState.update { current ->
                (current as? PremiumUiState.Ready)?.copy(isPurchasing = false) ?: current
            }

            _events.emit(PremiumEvent.PurchaseSuccess(plan.id))
        }
    }

    // ── Dismiss ───────────────────────────────────────────────────

    fun dismiss() {
        viewModelScope.launch { _events.emit(PremiumEvent.Dismissed) }
    }
}