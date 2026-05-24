package io.synapse.ai.features.premium.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import io.synapse.ai.R
import io.synapse.ai.core.theme.LocalGradientTokens
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.asp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.components.PrimaryGradientButton
import io.synapse.ai.features.premium.presentation.state.PremiumPlanUiModel
import io.synapse.ai.features.premium.presentation.state.ProFeatureUiModel
import io.synapse.ai.features.premium.presentation.state.SocialProofData
import io.synapse.ai.features.premium.presentation.state.iconKeyToDrawableRes
import io.synapse.ai.features.premium.presentation.state.toColor

@Composable
fun HeroSection(
    modifier: Modifier = Modifier,
) {
    val gradients = LocalGradientTokens.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        AppIconDisplay()

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
        ) {
            Text(
                text  = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    brush      = gradients.accent,
                    fontWeight = FontWeight.ExtraBold,
                ),
                textAlign = TextAlign.Center,
            )
            ProBadgeChip()
        }
    }
}


@Composable
fun ProBadgeChip(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s4),
        modifier = modifier
            .padding(top = MaterialTheme.synapse.spacing.s12)
            .clip(MaterialTheme.synapse.radius.pill)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                    )
                )
            )
            .border(
                width = 1.5.adp,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.38f),
                shape = MaterialTheme.synapse.radius.pill,
            )
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s12,
                vertical   = MaterialTheme.synapse.spacing.s6,
            ),
    ) {
            Icon(
                painter            = painterResource(R.drawable.ic_crown),
                contentDescription = stringResource(R.string.pro_badge_label),
                tint               = MaterialTheme.colorScheme.tertiary,
                modifier           = Modifier.size(MaterialTheme.synapse.spacing.icon_sm),
            )
        Text(
            text     = stringResource(R.string.premium_pro_label),
            style    = MaterialTheme.typography.titleSmall,
            color    = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.wrapContentSize(Alignment.Center).alignByBaseline(),
        )
    }
}


@Composable
internal fun PremiumFeaturesCard(
    features: List<ProFeatureUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape  = MaterialTheme.shapes.large,
                shadow = MaterialTheme.synapse.shadows.medium.toShadow(),
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        features.forEachIndexed { index, feature ->
            PremiumFeatureRow(feature = feature)
            if (index < features.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.adp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    Color.Transparent,
                                )
                            )
                        )
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PremiumFeatureRow(
    feature  : ProFeatureUiModel,
    modifier : Modifier = Modifier,
) {
    val featureColor = feature.colorRole.toColor()
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s14),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.adp)
            .padding(start = 18.adp, end = 12.adp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.adp)
                .clip(MaterialShapes.Gem.toShape())
                .background(featureColor.copy(alpha = 0.15f)),
        ) {
            Icon(
                painter            = painterResource(iconKeyToDrawableRes(feature.iconKey)),
                contentDescription = stringResource(R.string.a11y_feature_icon, feature.label),
                tint               = featureColor,
                modifier           = Modifier.size(MaterialTheme.synapse.spacing.icon_md),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = feature.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text     = feature.sublabel,
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MaterialTheme.synapse.spacing.s2),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Same CheckIndicator as PlanCard for visual consistency
        CheckIndicator(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.80f),
            borderColor     = MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
            iconTint        = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun PlansRow(
    plans          : List<PremiumPlanUiModel>,
    selectedPlanId : String,
    onPlanSelected : (String) -> Unit,
    modifier       : Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.adp),
        modifier = modifier.fillMaxWidth(),
    ) {
        plans.forEach { plan ->
            PlanCard(
                plan       = plan,
                isSelected = selectedPlanId == plan.skuId,
                onSelect   = { onPlanSelected(plan.skuId) },
                modifier   = Modifier.weight(1f),
            )
        }
    }
}


@Composable
fun PlanCard(
    plan       : PremiumPlanUiModel,
    isSelected : Boolean,
    onSelect   : () -> Unit,
    modifier   : Modifier = Modifier,
) {
    val gradients  = MaterialTheme.synapse.gradients
    val spacing    = MaterialTheme.synapse.spacing
    val isGradient = isSelected && plan.isHighlighted

    // ── Card surface brush ────────────────────────────────────────────────────
    val cardBg: Brush = if (isGradient) gradients.accent
    else Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = if (isSelected) 0.95f else 0.60f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.95f else 0.60f),
        )
    )

    // ── Border ────────────────────────────────────────────────────────────────
    val borderBrush: Brush = when {
        isGradient -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.08f))
        )
        isSelected -> Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            )
        )
        else       -> Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f),
            )
        )
    }
    val borderWidth = if (isSelected) 1.5.adp else 1.adp

    // ── Text color roles ──────────────────────────────────────────────────────
    val onCard      = if (isGradient) Color.White else MaterialTheme.colorScheme.onSurface
    val onCardMid   = when {
        isGradient -> Color.White.copy(alpha = 0.9f)
        isSelected -> MaterialTheme.colorScheme.onSurface
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val shape  = MaterialTheme.shapes.extraLarge
    val shadow = if (isGradient) MaterialTheme.synapse.shadows.strong.toShadow()
    else MaterialTheme.synapse.shadows.medium.toShadow()

    // Outer Box is NOT clipped — badge overflows upward
    Box(modifier = modifier) {

        // ── Clipped card surface ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(158.adp)
                .padding(top = 11.adp)
                .dropShadow(shape = shape, shadow = shadow)
                .clip(shape)
                .background(cardBg)
                .border(borderWidth, brush = borderBrush, shape = shape)
                .clickable(onClick = onSelect),
        ) {
            // Diagonal shimmer — only on gradient-selected card
            if (isGradient) {
                Box(modifier = Modifier.matchParentSize().clipToBounds()) {
                    ShimmerSweep(durationMs = 2_800, delayMs = 1_600)
                }
            }

            // Content column
            Column(
                modifier = Modifier.padding(spacing.s16)
            ) {
                Spacer(Modifier.height(spacing.s10))
                // Period label — "YEARLY · Best Value" or "Monthly"
                Text(
                    text  = if (plan.isHighlighted) {
                        stringResource(R.string.premium_plan_label_best, plan.title.resolve().uppercase())
                    } else {
                        plan.title.resolve()
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = onCardMid,
                )

                Spacer(Modifier.height(spacing.s4))

                // Main price + abbreviated period — baseline aligned
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text       = plan.formattedPrice,
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = onCard,
                    )
                    Text(
                        text     = plan.billingPeriodStr.resolve(),  // "/YR" or "/MO" from strings.xml
                        style    = MaterialTheme.typography.labelMedium,
                        color    = onCardMid,
                        modifier = Modifier.padding(start = spacing.s2, bottom = spacing.s4),
                    )
                }

                // Monthly equivalent for annual plan ("$4.99/mo")
                plan.monthlyEquivalent?.let { sub ->
                    Text(
                        text  = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = onCardMid,
                    )
                }
            }

            // ── Selection indicator — TOP-END ─────────────────────────────────
            if (isSelected) {
                CheckIndicator(
                    backgroundColor = if (isGradient) Color.White.copy(alpha = 0.26f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                    borderColor     = if (isGradient) Color.White.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
                    iconTint        = if (isGradient) Color.White
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }

        // Savings badge — top corner (dynamically calculated)
        plan.savingsPercentage?.let { pct ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(MaterialTheme.synapse.radius.pill)
                    .background(gradients.premium)
                    .padding(horizontal = spacing.s12, vertical = 3.adp),
            ) {
                Text(
                    text  = stringResource(R.string.plan_badge_save, pct),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 0.04.asp,
                    ),
                    color = Color.White,
                )
            }
        }
    }
}


@Composable
fun CtaSection(
    isPurchasing  : Boolean,
    socialProof   : SocialProofData?,
    selectedSkuId : String,
    products      : List<PremiumPlanUiModel>,
    onStartTrial  : () -> Unit,
    onDismiss     : () -> Unit,
    modifier      : Modifier = Modifier,
) {
    val spacing         = MaterialTheme.synapse.spacing
    val selectedProduct = remember(selectedSkuId, products) { products.find { it.skuId == selectedSkuId } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = modifier.fillMaxWidth(),
    ) {
        val ctaText = selectedProduct?.freeTrialStr?.let { trial ->
            if (trial.isNotBlank()) stringResource(R.string.premium_cta_trial_dynamic, trial) else null
        } ?: stringResource(R.string.premium_cta_subscribe)
        PrimaryGradientButton(
            text      = ctaText,
            enabled   = !isPurchasing,
            isLoading = isPurchasing,
            onClick   = onStartTrial,
        )

        // billing disclosure note
        Spacer(Modifier.height(spacing.s8))
        Text(
            text      = selectedProduct?.noteDisplay?.resolve() ?: "",
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = spacing.s16),
        )

//        socialProof?.let { proof ->
//            Spacer(Modifier.height(spacing.s12))
//            SocialProofRow(socialProof = proof)
//        }

        Spacer(Modifier.height(spacing.s8))
        TrustLine()
        Spacer(Modifier.height(spacing.s12))
//        Text(
//            text      = stringResource(R.string.premium_cta_skip),
//            style     = MaterialTheme.typography.bodyMedium,
//            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
//            textAlign = TextAlign.Center,
//            modifier  = Modifier
//                .clickable(onClick = onDismiss)
//                .padding(top = spacing.s10, bottom = spacing.s6),
//        )
    }
}


@Composable
fun SocialProofRow(
    socialProof : SocialProofData,
    modifier    : Modifier = Modifier,
) {
    val spacing    = MaterialTheme.synapse.spacing
    val initials   = socialProof.avatarInitials
    val avatarSize = 28.adp
    val overlap    = 14.adp

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.s12)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(
                width = 1.adp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.large,
            )
            .padding(horizontal = spacing.s16, vertical = spacing.s12),
    ) {
        Box(
            modifier = Modifier
                .width(avatarSize + overlap * (initials.size - 1))
                .height(avatarSize),
        ) {
            initials.forEachIndexed { index, initial ->
                val hue = 240f + index * 22f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = overlap * index)
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.hsl(hue, 0.60f, 0.50f),
                                    Color.hsl(hue + 20f, 0.70f, 0.60f),
                                )
                            )
                        )
                        .border(width = 2.adp, color = MaterialTheme.colorScheme.surface, shape = CircleShape),
                ) {
                    Text(
                        text  = initial.take(1).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
            }
        }

        Spacer(Modifier.width(spacing.s12))

        Column(verticalArrangement = Arrangement.spacedBy(spacing.s2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.s2),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                repeat(5) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_star_fill),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.tertiary,
                        modifier           = Modifier.size(10.adp),
                    )
                }
            }
            Text(
                text  = stringResource(R.string.premium_social_proof_community),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
fun TrustLine(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
        modifier              = modifier,
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier           = Modifier.size(12.adp),
        )
        Text(
            text      = stringResource(R.string.premium_trust_line),
            style     = MaterialTheme.typography.labelMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
internal fun CheckIndicator(
    backgroundColor : Color,
    borderColor     : Color,
    iconTint        : Color,
    modifier        : Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(MaterialTheme.synapse.spacing.s14)
            .size(24.adp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(width = 1.adp, color = borderColor, shape = CircleShape),
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_check),
            contentDescription = stringResource(R.string.a11y_included),
            tint               = iconTint,
            modifier           = Modifier.size(12.adp),
        )
    }
}


@Composable
internal fun ShimmerSweep(
    durationMs : Int = 2_600,
    delayMs    : Int = 1_200,
) {
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue  = -0.5f,
        targetValue   = 1.5f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMs, delayMs, LinearEasing),
            repeatMode = RepeatMode.Restart,
    )

    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val shimmerBandWidth = remember(maxWidth) { maxWidth / 3 }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(shimmerBandWidth)
                .offset(x = maxWidth * progress)
                .background(
                    remember {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.20f),
                                Color.Transparent,
                            )
                        )
                    }
                ),
        )
    }
}


@Composable
internal fun AmbientOrb(
    color    : Color,
    size     : Dp,
    modifier : Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = center,
                        radius = this.size.minDimension / 2f,
                    ),
                )
            },
    )
}

@Composable
fun PremiumErrorContent(
    message  : String,
    onRetry  : () -> Unit,
    modifier : Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.synapse.spacing.s24),
    ) {
        Text(
            text      = message,
            style     = MaterialTheme.typography.titleSmall,
            color     = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.premium_retry)) }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun AppIconDisplay(modifier: Modifier = Modifier) {
    val glowTransition = rememberInfiniteTransition()
    val glowAlpha by glowTransition.animateFloat(
        initialValue  = 0.30f,
        targetValue   = 0.70f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        )
    )

    val shape = MaterialShapes.Cookie9Sided.toShape()

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(148.adp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.45f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = glowAlpha * 0.25f),
                            Color.Transparent,
                        )
                    )
                ),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(124.adp)
                .dropShadow(shape = shape, shadow = MaterialTheme.synapse.shadows.strong.toShadow())
                .clip(shape)
                .border(width = 3.adp, brush = MaterialTheme.synapse.gradients.premium, shape = shape)
        ) {
            Image(
                painter          = painterResource(R.drawable.ic_launcher),
                contentDescription = null,
                contentScale     = ContentScale.Crop,
                modifier         = Modifier.fillMaxSize(),
            )
            Box(modifier = Modifier.matchParentSize().clipToBounds()) {
                ShimmerSweep(durationMs = 3_000, delayMs = 0)
            }
        }
    }
}