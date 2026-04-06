package com.venom.synapse.features.premium.presentation.state

import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.venom.synapse.R
import com.venom.synapse.core.ui.state.UiText
import com.venom.synapse.domain.model.BillingPeriod
import com.venom.synapse.domain.model.FeatureColorRole
import com.venom.synapse.domain.model.PaywallConfig
import com.venom.synapse.domain.model.PremiumPlan
import com.venom.synapse.domain.model.ProFeature

@Immutable
data class PremiumPlanUiModel(
    val id: String,
    val label: UiText,
    val badgeLabel: UiText?,
    val priceDisplay: String,
    val periodDisplay: UiText,
    val noteDisplay: String,
    val isHighlighted: Boolean,
    val skuId: String,
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

data class PlayPriceInfo(
    val formattedPrice: String,
    val billingNote: String,
)

sealed interface PremiumUiState {

    data object Loading : PremiumUiState
    data object AlreadyPremium : PremiumUiState

    @Immutable
    data class Ready(
        val plans: List<PremiumPlanUiModel>,
        val features: List<ProFeatureUiModel>,
        val selectedPlanId: String,
        val trialDays: Int,
        val isPurchasing: Boolean = false,
        val socialProof: SocialProofData? = null,
    ) : PremiumUiState

    @Immutable
    data class Error(val message: String) : PremiumUiState
}

sealed interface PremiumEvent {
    data class PurchaseSuccess(val planId: String) : PremiumEvent
    data class PurchaseFailed(val reason: String) : PremiumEvent
    data object Dismissed : PremiumEvent
    data object RequiresSignIn : PremiumEvent
    data object NavigateToProfile : PremiumEvent
    data class ShowSnackbar(val message: String) : PremiumEvent
}

internal fun PaywallConfig.toUiModels(
    playPrices: Map<String, PlayPriceInfo> = emptyMap(),
    isArabic: Boolean = false,
): Pair<List<PremiumPlanUiModel>, List<ProFeatureUiModel>> {
    val planModels    = plans.map { it.toUiModel(playPrices[it.skuId]) }
    val featureModels = features.map { it.toUiModel(isArabic) }
    return planModels to featureModels
}

internal fun PremiumPlan.toUiModel(playPrice: PlayPriceInfo? = null): PremiumPlanUiModel =
    PremiumPlanUiModel(
        id            = id,
        label         = billingPeriod.toLabelUiText(),
        badgeLabel    = savingsPercent?.let { pct -> UiText.Raw(R.string.premium_badge_save, pct) },
        priceDisplay  = playPrice?.formattedPrice ?: defaultPriceDisplay(),
        periodDisplay = UiText.Raw(R.string.premium_period_per_month),
        noteDisplay   = playPrice?.billingNote ?: billingPeriod.toNoteDisplay(),
        isHighlighted = isHighlighted,
        skuId         = skuId,
    )

internal fun ProFeature.toUiModel(isArabic: Boolean): ProFeatureUiModel = ProFeatureUiModel(
    iconKey   = iconKey,
    label     = if (isArabic) labelAr.takeUnless { it.isNullOrBlank() } ?: label else label,
    sublabel  = if (isArabic) sublabelAr.takeUnless { it.isNullOrBlank() } ?: sublabel else sublabel,
    colorRole = colorRole,
)

private fun BillingPeriod.toLabelUiText(): UiText = when (this) {
    BillingPeriod.ANNUAL  -> UiText.Raw(R.string.premium_billing_period_annual)
    BillingPeriod.MONTHLY -> UiText.Raw(R.string.premium_billing_period_monthly)
}

private fun BillingPeriod.toNoteDisplay(): String = when (this) {
    BillingPeriod.ANNUAL  -> "Billed annually"
    BillingPeriod.MONTHLY -> "Cancel anytime"
}

private fun PremiumPlan.defaultPriceDisplay(): String = when (billingPeriod) {
    BillingPeriod.ANNUAL  -> "$3.99"
    BillingPeriod.MONTHLY -> "$6.99"
}