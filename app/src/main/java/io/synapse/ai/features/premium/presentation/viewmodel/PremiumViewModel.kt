package io.synapse.ai.features.premium.presentation.viewmodel

import io.synapse.ai.domains.premium.model.*

import io.synapse.ai.domains.premium.model.PaywallFeature

import io.synapse.ai.domains.premium.model.PaywallProduct

import io.synapse.ai.domains.premium.model.PaywallConfig

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.R
import io.synapse.ai.core.analytics.TrackingManager
import io.synapse.ai.core.analytics.model.AnalyticsEvent
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.domains.premium.data.PremiumManager
import io.synapse.ai.domains.premium.model.ProductDetails
import io.synapse.ai.domains.auth.repository.IAuthRepository
import io.synapse.ai.domains.premium.repository.IBillingRepository
import io.synapse.ai.domains.premium.repository.IPremiumRepository
import io.synapse.ai.domains.premium.repository.ISocialProofRepository
import io.synapse.ai.features.premium.presentation.state.PremiumEvent
import io.synapse.ai.features.premium.presentation.state.PremiumUiState
import io.synapse.ai.features.premium.presentation.state.buildFeatureUiModels
import io.synapse.ai.features.premium.presentation.state.buildPlanUiModels
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumRepository: IPremiumRepository,
    private val socialProofRepository: ISocialProofRepository,
    private val billingRepository: IBillingRepository,
    private val premiumManager: PremiumManager,
    private val authRepository: IAuthRepository,
    private val trackingManager: TrackingManager,
    @param:ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PremiumUiState>(PremiumUiState.Loading)
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PremiumEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    private var cachedProductDetails: Map<String, ProductDetails> = emptyMap()

    init {
        loadPaywall()
        observeProStatus()
        observePurchaseResults()
    }
    private fun observeProStatus() {
        viewModelScope.launch {
            premiumManager.isPro.collect { isPro ->
                if (isPro && _uiState.value !is PremiumUiState.AlreadyPremium) {
                    _uiState.value = PremiumUiState.AlreadyPremium
                }
            }
        }
    }
    private fun observePurchaseResults() {
        // W-7 FIX: purchaseResults and billingErrors are declared on IBillingRepository.
        // The previous `is BillingRepositoryImpl` type-check broke the abstraction,
        // made unit testing impossible, and blocked collection on mocks.
        viewModelScope.launch {
            billingRepository.purchaseResults.collect { purchaseResult ->
                handlePurchaseSuccess(purchaseResult.skuId, purchaseResult.purchaseToken)
            }
        }
        viewModelScope.launch {
            billingRepository.billingErrors.collect { errorMsg ->
                _uiState.update { current ->
                    (current as? PremiumUiState.Ready)?.copy(isPurchasing = false) ?: current
                }
                when (errorMsg) {
                    "CANCELED" -> { /* User cancelled, no error needed */ }
                    "ALREADY_OWNED" -> {
                        premiumManager.verifyWithServer(force = true)
                        _events.emit(PremiumEvent.PurchaseFailed("You already own this subscription"))
                    }
                    else -> _events.emit(PremiumEvent.PurchaseFailed(errorMsg))
                }
            }
        }
    }

    fun loadPaywall() {
        viewModelScope.launch {
            _uiState.value = PremiumUiState.Loading
            premiumManager.isReady.first { it }
            // Check if already gold
            if (premiumManager.isPro.value) {
                _uiState.value = PremiumUiState.AlreadyPremium
                return@launch
            }

            // Parallel: fetch config from Supabase AND connect to Google Play
            val configDeferred = viewModelScope.async { premiumRepository.loadPaywallConfig() }
            val socialProofDeferred = viewModelScope.async { socialProofRepository.fetch() }

            // Connect to Google Play
            runCatching { billingRepository.connect() }
                .onFailure { e ->
                    _uiState.value = PremiumUiState.Error(appContext.getString(R.string.error_connection_prefix, e.message))
                    return@launch
                }

            // Get config result
            val configResult = configDeferred.await()
            val config = configResult.getOrElse {
                _uiState.value = PremiumUiState.Error(appContext.getString(R.string.error_load_paywall))
                return@launch
            }

            // Extract SKU IDs from products
            val skuIds = config.products.map { it.skuId }

            // Query Google Play for localized prices
            val productDetailsResult = billingRepository.queryProductDetails(skuIds)
            productDetailsResult.onSuccess { details ->
                cachedProductDetails = details
            }

            // Build UI models — all text computed locally from Google Play + strings.xml
            val isArabic = Locale.getDefault().language == "ar"
            val planModels = buildPlanUiModels(
                products = config.products,
                productDetailsMap = cachedProductDetails,
                resources = appContext.resources,
            )
            val featureModels = buildFeatureUiModels(config.features, isArabic)

            _uiState.value = PremiumUiState.Ready(
                products = planModels,
                features = featureModels,
                selectedSkuId = planModels.firstOrNull { it.isHighlighted }?.skuId
                    ?: planModels.firstOrNull()?.skuId
                    ?: "",
                socialProof = socialProofDeferred.await().getOrNull(),
            )
            trackingManager.logEvent(AnalyticsEvent.PaywallViewed("manual"))
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Step 2: User selects a plan
    // ═══════════════════════════════════════════════════════════════════

    fun selectPlan(skuId: String) {
        _uiState.update { state ->
            if (state is PremiumUiState.Ready) {
                state.copy(selectedSkuId = skuId)
            } else state
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Step 3: User clicks Subscribe - launch Google Play billing
    // ═══════════════════════════════════════════════════════════════════

    fun startPurchase(activity: Activity) {
        val state = _uiState.value as? PremiumUiState.Ready ?: return
        val skuId = state.selectedSkuId

        val productDetails = cachedProductDetails[skuId]
        if (productDetails == null) {
            viewModelScope.launch {
                _events.emit(PremiumEvent.PurchaseFailed("Product not found"))
            }
            return
        }

        // Check authentication
        if (!authRepository.isAuthenticated()) {
            viewModelScope.launch {
                _events.emit(PremiumEvent.NavigateToProfile)
                _events.emit(PremiumEvent.RequiresSignIn(UiText.Raw(R.string.premium_sign_in_required)))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { current ->
                (current as? PremiumUiState.Ready)?.copy(isPurchasing = true) ?: current
            }

            val accountId = authRepository.getUserId()
            trackingManager.logEvent(AnalyticsEvent.PurchaseStarted(skuId))

            // Launch billing flow asynchronously
            launchBillingFlowAsync(activity, productDetails, accountId)
        }
    }

    private fun launchBillingFlowAsync(
        activity: Activity,
        productDetails: ProductDetails,
        accountId: String?,
    ) {
        viewModelScope.launch {
            try {
                // فقط نقوم بفتح نافذة جوجل بلاي.
                // النتيجة ستذهب تلقائياً إلى observePurchaseResults()
                billingRepository.launchBillingFlow(activity, productDetails, accountId)
            } catch (e: Exception) {
                _uiState.update { current ->
                    (current as? PremiumUiState.Ready)?.copy(isPurchasing = false) ?: current
                }
                _events.emit(PremiumEvent.PurchaseFailed(e.message ?: appContext.getString(R.string.error_billing_failed)))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Step 4: Handle purchase success - verify with server
    // ═══════════════════════════════════════════════════════════════════

    private suspend fun handlePurchaseSuccess(skuId: String, purchaseToken: String) {
        _uiState.update { current ->
            (current as? PremiumUiState.Ready)?.copy(isPurchasing = true) ?: current
        }

        val verifyResult = premiumRepository.verifyPurchaseWithServer(skuId, purchaseToken)

        verifyResult.fold(
            onSuccess = {
                premiumManager.verifyWithServerAndWait()

                _uiState.update { current ->
                    (current as? PremiumUiState.Ready)?.copy(isPurchasing = false) ?: current
                }
                trackingManager.logEvent(AnalyticsEvent.PurchaseCompleted(skuId))
                _events.emit(PremiumEvent.PurchaseSuccess(skuId))
            },
            onFailure = { error ->
                _uiState.update { current ->
                    (current as? PremiumUiState.Ready)?.copy(isPurchasing = false) ?: current
                }
                trackingManager.logEvent(
                    AnalyticsEvent.PurchaseFailed(skuId, error::class.simpleName ?: "Unknown")
                )
                _events.emit(PremiumEvent.PurchaseFailed(error.message ?: appContext.getString(R.string.error_verification_failed)))
            }
        )
    }

    fun restorePurchases() {
        trackingManager.logEvent(AnalyticsEvent.SubscriptionRestored)
        premiumManager.verifyWithServer()
    }

    fun dismiss() = viewModelScope.launch { _events.emit(PremiumEvent.Dismissed) }

    fun refresh() {
        premiumManager.verifyWithServer()
        loadPaywall()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { billingRepository.disconnect() }
    }
}




