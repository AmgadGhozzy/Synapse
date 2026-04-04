package com.venom.synapse.features.premium.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.venom.synapse.R
import com.venom.synapse.core.theme.LocalGradientTokens
import com.venom.synapse.core.theme.White
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.synapse.features.premium.presentation.state.PremiumPlanUiModel
import com.venom.synapse.features.premium.presentation.state.ProFeatureUiModel
import com.venom.synapse.features.premium.presentation.state.SocialProofData
import com.venom.synapse.features.premium.presentation.state.iconKeyToDrawableRes
import com.venom.synapse.features.premium.presentation.state.toColor
import com.venom.ui.components.common.adp

@Composable
fun HeroSection(
    trialDays: Int,
    modifier: Modifier = Modifier,
) {
    val gradients = LocalGradientTokens.current
    val pulseTransition = rememberInfiniteTransition()
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = MaterialTheme.synapse.spacing.s4,
                bottom = MaterialTheme.synapse.spacing.s20
            ),
    ) {

        AppIconDisplay()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(brush = gradients.accent, fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center,
            )
            ProBadgeChip()
        }

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s8))

        Text(
            text = stringResource(R.string.premium_tagline),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s32),
        )

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))

        Row(
            verticalAlignment = Alignment.CenterVertically,
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
                    vertical = MaterialTheme.synapse.spacing.s6
                ),
        ) {
            Box(
                modifier = Modifier
                    .size(6.adp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = pulseAlpha)),
            )
            Text(
                text = stringResource(R.string.premium_trial_badge, trialDays),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
fun ProBadgeChip(modifier: Modifier = Modifier) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s4),
        modifier = modifier
            .padding(top = MaterialTheme.synapse.spacing.s12)
            .clip(MaterialTheme.synapse.radius.pill)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
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
                vertical = MaterialTheme.synapse.spacing.s2
            ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_crown),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.adp),
        )
        Text(
            text = stringResource(R.string.premium_pro_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.wrapContentSize(Alignment.Center).alignByBaseline()
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
                shape = MaterialTheme.shapes.large,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow()
            )
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.background)
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
                                    Color.Transparent
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
    feature: ProFeatureUiModel,
    modifier: Modifier = Modifier,
) {
    val featureColor = feature.colorRole.toColor()
    Row(
        verticalAlignment = Alignment.CenterVertically,
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
                painter = painterResource(iconKeyToDrawableRes(feature.iconKey)),
                contentDescription = null,
                tint = featureColor,
                modifier = Modifier.size(22.adp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = feature.sublabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MaterialTheme.synapse.spacing.s2),
            )
        }

        CheckIndicator(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            iconTint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PlansRow(
    plans: List<PremiumPlanUiModel>,
    selectedPlanId: String,
    onPlanSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
        modifier = modifier.fillMaxWidth(),
    ) {
        plans.forEach { plan ->
            PlanCard(
                plan = plan,
                isSelected = plan.id == selectedPlanId,
                onSelect = { onPlanSelected(plan.id) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CheckIndicator(
    backgroundColor: Color,
    borderColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(20.adp)
            .size(24.adp)
            .clip(MaterialShapes.Cookie7Sided.toShape())
            .background(backgroundColor)
            .border(
                width = 1.adp,
                color = borderColor,
                shape = MaterialShapes.Cookie7Sided.toShape(),
            ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(12.adp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlanCard(
    plan: PremiumPlanUiModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradients = MaterialTheme.synapse.gradients
    val isGradient = isSelected && plan.isHighlighted

    val cardBg: Brush = if (isGradient) {
        gradients.accent
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background.copy(alpha = if (isSelected) 0.95f else 0.6f),
                MaterialTheme.colorScheme.surface.copy(alpha = if (isSelected) 0.95f else 0.6f),
            )
        )
    }

    val borderColor = when {
        isGradient -> Color.White.copy(alpha = 0.28f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }
    val borderWidth = if (isSelected) 2.adp else 1.adp

    val priceColor = if (isGradient) Color.White else MaterialTheme.colorScheme.onSurface
    val labelColor = if (isGradient) Color.White.copy(alpha = 0.75f)
    else if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
    else MaterialTheme.colorScheme.onSurfaceVariant
    val periodColor =
        if (isGradient) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
    val noteColor =
        if (isGradient) Color.White.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
            alpha = 0.75f
        )

    val shadow = if (isGradient) MaterialTheme.synapse.shadows.strong.toShadow() else MaterialTheme.synapse.shadows.subtle.toShadow()
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = modifier
            .dropShadow(shape = shape, shadow = shadow)
            .clip(shape)
            .background(cardBg)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .clickable(onClick = onSelect),
    ) {
        if (isGradient) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clipToBounds()
            ) {
                ShimmerSweep(durationMs = 2_800, delayMs = 1_600)
            }
        }

        Column(modifier = Modifier.padding(20.adp)) {
            val labelText = if (plan.isHighlighted) {
                stringResource(R.string.premium_plan_label_best, plan.label.resolve())
            } else {
                plan.label.resolve().uppercase()
            }
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
            )

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s6))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = plan.priceDisplay,
                    style = MaterialTheme.typography.headlineLarge,
                    color = priceColor,
                )
                Text(
                    text = plan.periodDisplay.resolve(),
                    style = MaterialTheme.typography.labelLarge,
                    color = periodColor,
                    modifier = Modifier.padding(
                        bottom = MaterialTheme.synapse.spacing.s3,
                        start = MaterialTheme.synapse.spacing.s2
                    ),
                )
            }

            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s2))
            Text(
                text = plan.noteDisplay,
                style = MaterialTheme.typography.labelMedium,
                color = noteColor
            )
        }

        if (plan.badgeLabel != null) {
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.adp)
                    .clip(MaterialTheme.synapse.radius.pill)
                    .background(MaterialTheme.synapse.gradients.gold)
                    .padding(
                        horizontal = 8.adp,
                        vertical = 2.adp,
                    ),
            ) {
                Text(
                    text = plan.badgeLabel.resolve(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

        }
        if (isSelected) {
            CheckIndicator(
                backgroundColor = if (isGradient) Color.White.copy(alpha = 0.26f)
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                borderColor = if (isGradient) Color.White.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                iconTint = if (isGradient) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun CtaSection(
    trialDays: Int,
    isPurchasing: Boolean,
    socialProof: SocialProofData?,
    onStartTrial: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        PremiumCtaButton(
            trialDays = trialDays,
            isPurchasing = isPurchasing,
            onClick = onStartTrial,
        )

        socialProof?.let {
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))
            SocialProofRow(socialProof = it)
        }

        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s8))

        TrustLine()

        Text(
            text = stringResource(R.string.premium_cta_skip),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(
                    vertical = MaterialTheme.synapse.spacing.s10,
                    horizontal = MaterialTheme.synapse.spacing.s16
                ),
        )
    }
}

@Composable
internal fun PremiumCtaButton(
    trialDays: Int,
    isPurchasing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradients = MaterialTheme.synapse.gradients
    val shape = MaterialTheme.shapes.medium
    val ctaShadow = MaterialTheme.synapse.shadows.strong.toShadow()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(shape = shape, shadow = ctaShadow)
            .height(68.adp)
            .clip(shape)
            .background(gradients.primary)
            .clickable(enabled = !isPurchasing, onClick = onClick)
            .semantics { contentDescription = "Start trial" },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clipToBounds()
        ) {
            ShimmerSweep(durationMs = 2_600, delayMs = 1_200)
        }

        if (isPurchasing) {
            LottieAnimation(
                composition = rememberLottieComposition(
                    LottieCompositionSpec.RawRes(com.venom.resources.R.raw.dot_loading)
                ).value,
                iterations = LottieConstants.IterateForever,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .blur(0.5.adp),
                speed = 0.8f
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.adp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_gem),
                    contentDescription = null,
                    tint = Color.White.copy(0.9f),
                    modifier = Modifier.size(20.adp),
                )
                Text(
                    text = stringResource(R.string.premium_cta_start_trial, trialDays),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White.copy(0.9f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.White.copy(0.9f),
                    modifier = Modifier.size(20.adp),
                )
            }
        }
    }
}

@Composable
fun SocialProofRow(
    socialProof: SocialProofData,
    modifier: Modifier = Modifier,
) {
    val initials = socialProof.avatarInitials
    val countLabel = socialProof.userCountLabel

    val avatarStep = 32.adp - 12.adp
    val stackWidth = remember(initials.size) {
        32.dp + avatarStep * (initials.size - 1)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
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
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s16,
                vertical = MaterialTheme.synapse.spacing.s12
            ),
    ) {
        // ── Avatar stack ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(stackWidth)
                .height(32.adp),
        ) {
            initials.forEachIndexed { index, initial ->
                val hue = 240f + index * 22f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = avatarStep * index)
                        .size(32.adp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.hsl(hue, 0.6f, 0.50f),
                                    Color.hsl(hue + 20f, 0.7f, 0.60f),
                                )
                            )
                        )
                        .border(
                            width = 2.adp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape,
                        ),
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = White,
                    )
                }
            }
        }

        Spacer(Modifier.width(MaterialTheme.synapse.spacing.s12))

        // ── Stars + copy ──────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.adp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(5) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star_fill),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.adp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.premium_social_proof, countLabel),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun TrustLine(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(10.adp),
        )
        Text(
            text = stringResource(R.string.premium_trust_line),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = modifier.padding(start = MaterialTheme.synapse.spacing.s12),
    )
}

@Composable
internal fun ShimmerSweep(
    modifier: Modifier = Modifier,
    durationMs: Int = 2_600,
    delayMs: Int = 1_200,
) {
    val transition = rememberInfiniteTransition()
    val offset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing, delayMillis = delayMs),
            repeatMode = RepeatMode.Restart,
        )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { translationX = offset * size.width }
            .background(MaterialTheme.synapse.gradients.shimmer),
    )
}

@Composable
internal fun AmbientOrb(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
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
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.synapse.spacing.s24),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.premium_retry)) }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun AppIconDisplay(
    modifier: Modifier = Modifier,
) {
    val glowTransition = rememberInfiniteTransition()
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        )
    )

    val shape = MaterialShapes.Cookie9Sided.toShape()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        // ── Animated glow orb behind the icon ──────────────────────────
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
                )
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(124.adp)
                .dropShadow(shape = shape, shadow = MaterialTheme.synapse.shadows.strong.toShadow())
                .clip(shape)
                .border(width = 3.adp, brush = MaterialTheme.synapse.gradients.gold, shape = shape),
        ) {
            // ── App icon fills the shaped container ───────────────────
            Image(
                painter = painterResource(R.drawable.ic_launcher),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clipToBounds(),
            ) {
                ShimmerSweep(durationMs = 3000, delayMs = 0)
            }
        }
    }
}
