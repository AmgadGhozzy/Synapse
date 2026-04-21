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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import io.synapse.ai.R
import io.synapse.ai.core.theme.LocalGradientTokens
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.asp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.features.premium.presentation.state.PremiumPlanUiModel
import io.synapse.ai.features.premium.presentation.state.ProFeatureUiModel
import io.synapse.ai.features.premium.presentation.state.SocialProofData
import io.synapse.ai.features.premium.presentation.state.iconKeyToDrawableRes
import io.synapse.ai.features.premium.presentation.state.toColor

@Composable
fun HeroSection(
    trialDays: Int,
    modifier: Modifier = Modifier,
) {
    val gradients = LocalGradientTokens.current
    val pulseTransition = rememberInfiniteTransition()
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = MaterialTheme.synapse.spacing.s4, bottom = MaterialTheme.synapse.spacing.s20),
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

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s8))

        Text(
            text      = stringResource(R.string.premium_tagline),
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s32),
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))

        // Pulsing trial badge pill
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
            modifier = Modifier
                .clip(MaterialTheme.synapse.radius.pill)
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f))
                .border(
                    width = 1.adp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.38f),
                    shape = MaterialTheme.synapse.radius.pill,
                )
                .padding(
                    horizontal = MaterialTheme.synapse.spacing.s16,
                    vertical   = MaterialTheme.synapse.spacing.s6,
                ),
        ) {
            Box(
                modifier = Modifier
                    .size(6.adp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = pulseAlpha)),
            )
            Text(
                text  = stringResource(R.string.premium_trial_badge, trialDays),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
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
                vertical   = MaterialTheme.synapse.spacing.s8,
            ),
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_crown),
            contentDescription = null,
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
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(),
            )
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.background),
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
            .padding(start = 18.adp, end = 8.adp),
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
                contentDescription = null,
                tint               = featureColor,
                modifier           = Modifier.size(MaterialTheme.synapse.spacing.icon_md),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = feature.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text     = feature.sublabel,
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MaterialTheme.synapse.spacing.s2),
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
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(end = 2.adp, bottom = 12.adp),
    )
}


@Composable
fun PlansRow(
    plans          : List<PremiumPlanUiModel>,
    selectedPlanId : String,
    onPlanSelected : (String) -> Unit,
    modifier       : Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.adp),
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
    val hasBadge   = plan.badgeLabel != null

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
        isGradient -> Color.White.copy(alpha = 0.80f)
        isSelected -> MaterialTheme.colorScheme.onSurface
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val onCardFaint = if (isGradient) Color.White.copy(alpha = 0.52f)
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)

    val shape  = MaterialTheme.shapes.extraLarge
    val shadow = if (isGradient) MaterialTheme.synapse.shadows.strong.toShadow()
    else MaterialTheme.synapse.shadows.subtle.toShadow()

    // Outer Box is NOT clipped — badge overflows upward
    Box(modifier = modifier) {

        // ── Clipped card surface ──────────────────────────────────────────────
        Box(
            modifier = Modifier                      // intentional: fresh Modifier, not outer `modifier`
                .fillMaxWidth()
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
                modifier = Modifier.padding(
                    start  = spacing.s16,
                    end    = spacing.s16,
                    top    = spacing.s14,
                    bottom = spacing.s16,
                ),
            ) {
                Spacer(Modifier.height(spacing.s12))
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

                Spacer(Modifier.height(spacing.s6))

                // Main price + abbreviated period — baseline aligned
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text       = plan.mainPrice,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color      = onCard,
                    )
                    Text(
                        text     = plan.periodDisplay.resolve(),  // "/YR" or "/MO" from strings.xml
                        style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color    = onCardMid,
                        modifier = Modifier.padding(start = spacing.s2, bottom = spacing.s4),
                    )
                }

                // Original (pre-discount) price — strikethrough
                plan.originalPrice?.let { original ->
                    Spacer(Modifier.height(spacing.s2))
                    Text(
                        text  = original,
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                        ),
                        color = onCardFaint,
                    )
                }

                // Sub-price — monthly equivalent for annual plan ("$4.99/mo")
                plan.subPrice?.let { sub ->
                    Spacer(Modifier.height(spacing.s3))
                    Text(
                        text  = sub.resolve(),
                        style = MaterialTheme.typography.bodySmall,
                        color = onCardMid,
                    )
                }

                Spacer(Modifier.height(spacing.s10))

                // Billing note
                Text(
                    text  = plan.noteDisplay.resolve(),
                    style = MaterialTheme.typography.labelSmall,
                    color = onCardFaint,
                )

                // Space to avoid overlapping the top-end check indicator
                Spacer(Modifier.height(spacing.s24))
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

        // Savings badge — top corner
        plan.badgeLabel?.let { badge ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(MaterialTheme.synapse.radius.pill)
                    .background(gradients.premium)
                    .padding(horizontal = spacing.s12, vertical = 3.adp),
            ) {
                Text(
                    text  = badge.resolve(),
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
    trialDays     : Int,
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
        PremiumCtaButton(
            trialDays    = trialDays,
            isPurchasing = isPurchasing,
            onClick      = onStartTrial,
        )

        // trial info + auto-renew disclosure
        selectedProduct?.trialInfo?.let { trialInfo ->
            Spacer(Modifier.height(spacing.s8))
            Text(
                text      = trialInfo.resolve(),
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = spacing.s16),
            )
        }

        socialProof?.let { proof ->
            Spacer(Modifier.height(spacing.s12))
            SocialProofRow(socialProof = proof)
        }

        Spacer(Modifier.height(spacing.s10))
        TrustLine()

        Text(
            text      = stringResource(R.string.premium_cta_skip),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .clickable(onClick = onDismiss)
                .padding(vertical = spacing.s10, horizontal = spacing.s16),
        )
    }
}


@Composable
internal fun PremiumCtaButton(
    trialDays    : Int,
    isPurchasing : Boolean,
    onClick      : () -> Unit,
    modifier     : Modifier = Modifier,
) {
    val shape   = MaterialTheme.shapes.medium
    val ctaDesc = stringResource(R.string.premium_cta_start_trial, trialDays)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(shape = shape, shadow = MaterialTheme.synapse.shadows.strong.toShadow())
            .height(60.adp)
            .clip(shape)
            .background(MaterialTheme.synapse.gradients.primary)
            .clickable(enabled = !isPurchasing, onClick = onClick)
            .semantics { contentDescription = ctaDesc },
    ) {
        Box(modifier = Modifier.matchParentSize().clipToBounds()) {
            ShimmerSweep(durationMs = 2_600, delayMs = 1_200)
        }

        if (isPurchasing) {
            LottieAnimation(
                composition  = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.dot_loading)).value,
                iterations   = LottieConstants.IterateForever,
                contentScale = ContentScale.FillWidth,
                modifier     = Modifier.fillMaxWidth(0.35f).blur(0.5.adp),
                speed        = 0.8f,
            )
        } else {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_gem),
                    contentDescription = null,
                    tint               = Color.White.copy(alpha = 0.90f),
                    modifier           = Modifier.size(18.adp),
                )
                Text(
                    text  = stringResource(R.string.premium_cta_start_trial, trialDays),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.90f),
                )
                Icon(
                    imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint               = Color.White.copy(alpha = 0.90f),
                    modifier           = Modifier.size(14.adp),
                )
            }
        }
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
                text  = stringResource(R.string.premium_social_proof, socialProof.userCountLabel),
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
            contentDescription = null,
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
        val shimmerBandWidth = maxWidth / 3
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(shimmerBandWidth)
                .offset(x = maxWidth * progress)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.20f),
                            Color.Transparent,
                        )
                    )
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
    val glowTransition = rememberInfiniteTransition(label = "iconGlow")
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
                .border(width = 3.adp, brush = MaterialTheme.synapse.gradients.premium, shape = shape),
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