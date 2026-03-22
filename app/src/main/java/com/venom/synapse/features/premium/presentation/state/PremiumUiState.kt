package com.venom.synapse.features.premium.presentation.state

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.venom.synapse.domain.model.BillingPeriod
import com.venom.synapse.domain.model.FeatureColorRole
import com.venom.synapse.domain.model.PaywallConfig
import com.venom.synapse.domain.model.PremiumPlan
import com.venom.synapse.domain.model.ProFeature

/**
 * ═══════════════════════════════════════════════════════════════════
 * PREMIUM UI MODELS
 * ═══════════════════════════════════════════════════════════════════
 */

@Immutable
data class PremiumPlanUiModel(
    val id: String,
    val label: String,
    val badgeLabel: String?,
    val priceDisplay: String,
    val periodDisplay: String,
    val noteDisplay: String,
    val isHighlighted: Boolean,
    val skuId: String,
)

@Immutable
data class ProFeatureUiModel(
    @DrawableRes val iconRes: Int,
    val label: String,
    val sublabel: String,
    val colorRole: FeatureColorRole,
)

@Composable
fun FeatureColorRole.toColor(): Color {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        FeatureColorRole.PRIMARY   -> scheme.primary
        FeatureColorRole.SECONDARY -> scheme.secondary
        FeatureColorRole.TERTIARY  -> scheme.tertiary
        FeatureColorRole.ERROR     -> scheme.error
        FeatureColorRole.SUCCESS   -> scheme.inversePrimary
        FeatureColorRole.WARNING   -> scheme.tertiary
    }
}

/**
 * Thin wrapper around [com.android.billingclient.api.ProductDetails] price data.
 */
data class PlayPriceInfo(
    val formattedPrice: String,
    val billingNote: String,
)

// ══════════════════════════════════════════════════════════════════
// UI STATE
// ══════════════════════════════════════════════════════════════════

sealed interface PremiumUiState {
    data object Loading : PremiumUiState

    @Immutable
    data class Ready(
        val plans: List<PremiumPlanUiModel>,
        val features: List<ProFeatureUiModel>,
        val selectedPlanId: String,
        val trialDays: Int,
        val isPurchasing: Boolean = false,
    ) : PremiumUiState

    @Immutable
    data class Error(val message: String) : PremiumUiState
}

// ══════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════

sealed interface PremiumEvent {
    data class PurchaseSuccess(val planId: String) : PremiumEvent
    data class PurchaseFailed(val reason: String) : PremiumEvent
    data object Dismissed : PremiumEvent
    data class ShowSnackbar(val message: String) : PremiumEvent
}

// ══════════════════════════════════════════════════════════════════
// MAPPERS
// ══════════════════════════════════════════════════════════════════

internal fun PaywallConfig.toUiModels(
    context: Context,
    playPrices: Map<String, PlayPriceInfo> = emptyMap(),
): Pair<List<PremiumPlanUiModel>, List<ProFeatureUiModel>> {
    val planModels    = plans.map { it.toUiModel(playPrices[it.skuId]) }
    val featureModels = features.map { it.toUiModel(context) }
    return planModels to featureModels
}

internal fun PremiumPlan.toUiModel(playPrice: PlayPriceInfo? = null): PremiumPlanUiModel {
    val price  = playPrice?.formattedPrice ?: defaultPriceDisplay()
    val period = billingPeriod.toPeriodDisplay()
    val note   = playPrice?.billingNote    ?: billingPeriod.toNoteDisplay()

    return PremiumPlanUiModel(
        id            = id,
        label         = billingPeriod.toLabel(),
        badgeLabel    = savingsPercent?.let { "SAVE $it%" },
        priceDisplay  = price,
        periodDisplay = period,
        noteDisplay   = note,
        isHighlighted = isHighlighted,
        skuId         = skuId,
    )
}

internal fun ProFeature.toUiModel(context: Context): ProFeatureUiModel {
    val resolvedRes = context.resources.getIdentifier(
        "ic_${iconKey.replace("-", "_")}", "drawable", context.packageName,
    ).takeIf { it != 0 } ?: context.resources.getIdentifier(
        "ic_sparkles", "drawable", context.packageName,
    )
    return ProFeatureUiModel(
        iconRes   = resolvedRes,
        label     = label,
        sublabel  = sublabel,
        colorRole = colorRole,
    )
}

private fun BillingPeriod.toLabel() = when (this) {
    BillingPeriod.ANNUAL  -> "Annual"
    BillingPeriod.MONTHLY -> "Monthly"
}

private fun BillingPeriod.toPeriodDisplay() = "/mo"

private fun BillingPeriod.toNoteDisplay() = when (this) {
    BillingPeriod.ANNUAL  -> "Billed annually"
    BillingPeriod.MONTHLY -> "Cancel anytime"
}

private fun PremiumPlan.defaultPriceDisplay() = when (billingPeriod) {
    BillingPeriod.ANNUAL  -> "\$4.99"
    BillingPeriod.MONTHLY -> "\$9.99"
}
