package io.synapse.ai.features.premium.presentation.state

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.synapse.ai.R
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.domain.model.BillingPeriod
import io.synapse.ai.domain.model.FeatureColorRole
import io.synapse.ai.domain.model.PaywallFeature
import io.synapse.ai.domain.model.PaywallProduct
import io.synapse.ai.domain.model.ProductDetails

// ══════════════════════════════════════════════════════════════════
// UI Model — 100% computed locally from Google Play + strings.xml
// ══════════════════════════════════════════════════════════════════

@Immutable
data class PremiumPlanUiModel(
    val skuId: String,
    val title: UiText,
    val formattedPrice: String,         // Localized price from Google Play (e.g., "$59.99")
    val billingPeriodStr: UiText,       // "/YR" or "/MO" from strings.xml
    val freeTrialStr: String? = null,   // Localized trial text (e.g., "3 days") — null if no trial
    val savingsPercentage: Int? = null,  // Dynamically calculated — null hides the badge
    val isHighlighted: Boolean,
    val billingPeriod: BillingPeriod,
    val monthlyEquivalent: String? = null, // e.g., "$4.99/mo" for annual plans
    val noteDisplay: UiText,            // billing disclosure text
)

@Immutable
data class ProFeatureUiModel(
    val iconKey: String,
    val label: String,
    val sublabel: String,
    val colorRole: FeatureColorRole,
)

@DrawableRes
fun iconKeyToDrawableRes(iconKey: String): Int = when (iconKey) {
    "layers"        -> R.drawable.ic_layers
    "brain"         -> R.drawable.ic_brain
    "file_text"     -> R.drawable.ic_file_text
    "youtube"       -> R.drawable.ic_youtube
    "globe"         -> R.drawable.ic_globe
    "sparkles"      -> R.drawable.ic_sparkles
    "zap"           -> R.drawable.ic_zap
    "crown"         -> R.drawable.ic_crown
    "gem"           -> R.drawable.ic_gem
    "book_open"     -> R.drawable.ic_book_open
    "target"        -> R.drawable.ic_target
    "clock"         -> R.drawable.ic_clock
    "award"         -> R.drawable.ic_award
    "lock"          -> R.drawable.ic_lock
    "check"         -> R.drawable.ic_check
    "star"          -> R.drawable.ic_star
    "flame"         -> R.drawable.ic_flame
    "trending_up"   -> R.drawable.ic_trending_up
    "trending_down" -> R.drawable.ic_trending_down
    "cloud"         -> R.drawable.ic_cloud
    "smartphone"    -> R.drawable.ic_cloud
    else            -> R.drawable.ic_sparkles
}

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

// ══════════════════════════════════════════════════════════════════
// UI State
// ══════════════════════════════════════════════════════════════════

sealed interface PremiumUiState {

    data object Loading : PremiumUiState
    data object AlreadyPremium : PremiumUiState

    @Immutable
    data class Ready(
        val products: List<PremiumPlanUiModel>,
        val features: List<ProFeatureUiModel>,
        val selectedSkuId: String,
        val isPurchasing: Boolean = false,
        val socialProof: SocialProofData? = null,
    ) : PremiumUiState

    @Immutable
    data class Error(val message: String) : PremiumUiState
}

sealed interface PremiumEvent {
    data class PurchaseSuccess(val skuId: String) : PremiumEvent
    data class PurchaseFailed(val reason: String) : PremiumEvent
    data object Dismissed : PremiumEvent
    data class RequiresSignIn(val message: UiText) : PremiumEvent
    data object NavigateToProfile : PremiumEvent
    data class ShowSnackbar(val message: String) : PremiumEvent
}

// ══════════════════════════════════════════════════════════════════
// ISO 8601 Period Parser — uses Android <plurals> for localization
// ══════════════════════════════════════════════════════════════════

/**
 * Parses Google Play's ISO 8601 duration string and formats it
 * using Android's `<plurals>` resource system.
 *
 * Examples: "P3D" → "3 days", "P1W" → "7 days", "P1M" → "1 month"
 *
 * @return Localized human-readable string, or null if parsing fails.
 */
fun parseIsoPeriod(period: String?, resources: Resources): String? {
    if (period.isNullOrBlank()) return null
    val body = period.removePrefix("P")

    // Parse individual components
    val years  = Regex("(\\d+)Y").find(body)?.groupValues?.get(1)?.toIntOrNull()
    val months = Regex("(\\d+)M").find(body)?.groupValues?.get(1)?.toIntOrNull()
    val weeks  = Regex("(\\d+)W").find(body)?.groupValues?.get(1)?.toIntOrNull()
    val days   = Regex("(\\d+)D").find(body)?.groupValues?.get(1)?.toIntOrNull()

    return when {
        years != null && years > 0   -> resources.getQuantityString(R.plurals.trial_years, years, years)
        months != null && months > 0 -> resources.getQuantityString(R.plurals.trial_months, months, months)
        weeks != null && weeks > 0   -> {
            // Convert weeks to days for clarity (e.g., "7 days" not "1 week")
            val totalDays = weeks * 7
            resources.getQuantityString(R.plurals.trial_days, totalDays, totalDays)
        }
        days != null && days > 0     -> resources.getQuantityString(R.plurals.trial_days, days, days)
        else -> null
    }
}

// ══════════════════════════════════════════════════════════════════
// Savings Calculator
// ══════════════════════════════════════════════════════════════════

/**
 * Dynamically calculates savings percentage by comparing annual vs monthly pricing.
 *
 * Formula: 100 - (annualMicros / (monthlyMicros * 12)) * 100
 *
 * @return Savings percentage (e.g., 50 for "Save 50%"), or null if not calculable.
 */
fun calculateSavingsPercentage(
    annualMicros: Long,
    monthlyMicros: Long,
): Int? {
    if (monthlyMicros <= 0 || annualMicros <= 0) return null
    val monthlyEquivalent = monthlyMicros * 12
    val savings = 100 - (annualMicros * 100 / monthlyEquivalent).toInt()
    return if (savings > 0) savings else null
}

// ══════════════════════════════════════════════════════════════════
// Mapper: Domain → UI Models
// ══════════════════════════════════════════════════════════════════

/**
 * Builds the complete list of [PremiumPlanUiModel] from domain products
 * and Google Play product details. All text is localized via strings.xml.
 */
fun buildPlanUiModels(
    products: List<PaywallProduct>,
    productDetailsMap: Map<String, ProductDetails>,
    resources: Resources,
): List<PremiumPlanUiModel> {
    // Find monthly price for savings calculation
    val monthlyMicros = products
        .firstOrNull { it.billingPeriod == BillingPeriod.MONTHLY }
        ?.let { productDetailsMap[it.skuId]?.priceAmountMicros }
        ?: 0L

    return products.map { product ->
        val details = productDetailsMap[product.skuId]
        val freeTrialStr = details?.freeTrialPeriodIso?.let { parseIsoPeriod(it, resources) }

        val savingsPercentage = if (product.billingPeriod == BillingPeriod.ANNUAL && details != null) {
            calculateSavingsPercentage(details.priceAmountMicros, monthlyMicros)
        } else null

        val monthlyEquiv = if (product.billingPeriod == BillingPeriod.ANNUAL && details != null && details.priceAmountMicros > 0) {
            val monthlyMicrosEquiv = details.priceAmountMicros / 12
            val amount = monthlyMicrosEquiv / 1_000_000.0
            resources.getString(R.string.plan_subprice_monthly, String.format("%.2f", amount))
        } else null

        val noteDisplay = when {
            freeTrialStr != null && product.billingPeriod == BillingPeriod.ANNUAL ->
                UiText.Raw(R.string.plan_note_annual)
            freeTrialStr != null && product.billingPeriod == BillingPeriod.MONTHLY ->
                UiText.Raw(R.string.plan_note_monthly)
            product.billingPeriod == BillingPeriod.ANNUAL ->
                UiText.Raw(R.string.plan_note_annual_no_trial)
            else ->
                UiText.Raw(R.string.plan_note_monthly_no_trial)
        }

        PremiumPlanUiModel(
            skuId = product.skuId,
            title = when (product.billingPeriod) {
                BillingPeriod.ANNUAL -> UiText.Raw(R.string.plan_title_annual)
                BillingPeriod.MONTHLY -> UiText.Raw(R.string.plan_title_monthly)
            },
            formattedPrice = details?.price ?: "",
            billingPeriodStr = when (product.billingPeriod) {
                BillingPeriod.ANNUAL -> UiText.Raw(R.string.plan_period_per_year)
                BillingPeriod.MONTHLY -> UiText.Raw(R.string.plan_period_per_month)
            },
            freeTrialStr = freeTrialStr,
            savingsPercentage = savingsPercentage,
            isHighlighted = product.isHighlighted,
            billingPeriod = product.billingPeriod,
            monthlyEquivalent = monthlyEquiv,
            noteDisplay = noteDisplay,
        )
    }
}

/**
 * Maps domain features to UI models, respecting locale for labels.
 */
fun buildFeatureUiModels(
    features: List<PaywallFeature>,
    isArabic: Boolean,
): List<ProFeatureUiModel> = features.map { feature ->
    ProFeatureUiModel(
        iconKey = feature.iconKey,
        label = if (isArabic) feature.labelAr.takeUnless { it.isNullOrBlank() } ?: feature.label else feature.label,
        sublabel = if (isArabic) feature.sublabelAr.takeUnless { it.isNullOrBlank() } ?: feature.sublabel else feature.sublabel,
        colorRole = feature.colorRole,
    )
}

@Immutable
data class SocialProofData(
    val userCountLabel: String,
    val avatarInitials: List<String> = listOf("A", "J", "M", "S", "K"),
)
