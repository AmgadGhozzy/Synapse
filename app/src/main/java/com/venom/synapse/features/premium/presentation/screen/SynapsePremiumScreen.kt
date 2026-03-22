package com.venom.synapse.features.premium.presentation.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.LocalGradientTokens
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.White
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.AvatarStackTokens
import com.venom.synapse.core.theme.tokens.FeatureRowTokens
import com.venom.synapse.core.theme.tokens.Gradients
import com.venom.synapse.core.theme.tokens.PricingCardTokens
import com.venom.synapse.core.theme.tokens.PrimaryButtonTokens
import com.venom.synapse.core.theme.tokens.Radius
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.theme.tokens.TypeScale
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.features.premium.presentation.state.PremiumEvent
import com.venom.synapse.features.premium.presentation.state.PremiumPlanUiModel
import com.venom.synapse.features.premium.presentation.state.PremiumUiState
import com.venom.synapse.features.premium.presentation.state.ProFeatureUiModel
import com.venom.synapse.features.premium.presentation.state.SocialProofData
import com.venom.synapse.features.premium.presentation.state.iconKeyToDrawableRes
import com.venom.synapse.features.premium.presentation.state.toColor
import com.venom.synapse.features.premium.presentation.viewmodel.PremiumViewModel
import com.venom.ui.components.common.adp

@Composable
fun SynapsePremiumScreen(
    onDismiss: () -> Unit,
    onPurchaseSuccess: (planId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PremiumEvent.PurchaseSuccess -> onPurchaseSuccess(event.planId)
                is PremiumEvent.Dismissed       -> onDismiss()
                is PremiumEvent.PurchaseFailed  -> snackbarController.error(event.reason)
                is PremiumEvent.ShowSnackbar    -> snackbarController.success(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost        = { snackbarController.SnackbarHost() },
        modifier            = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor      = Color.Transparent,
    ) { innerPadding ->
        when (val state = uiState) {
            is PremiumUiState.Loading -> PremiumLoadingContent(Modifier.padding(innerPadding))
            is PremiumUiState.Error   -> PremiumErrorContent(
                message  = state.message,
                onRetry  = viewModel::loadConfig,
                modifier = Modifier.padding(innerPadding),
            )
            is PremiumUiState.Ready   -> PremiumReadyContent(
                state          = state,
                onPlanSelected = viewModel::selectPlan,
                onStartTrial   = viewModel::startPurchase,
                onDismiss      = viewModel::dismiss,
                modifier       = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun PremiumReadyContent(
    state: PremiumUiState.Ready,
    onPlanSelected: (String) -> Unit,
    onStartTrial: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.synapse.gradients.page),
    ) {
        AmbientOrb(
            color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            size     = 280.adp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.adp, y = (-80).adp),
        )
        AmbientOrb(
            color    = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
            size     = 240.adp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).adp, y = 120.adp),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(end = Spacing.Spacing16, top = Spacing.Spacing12),
                contentAlignment = Alignment.CenterEnd,
            ) {
                PremiumCloseButton(onClick = onDismiss)
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = Spacing.Spacing32),
                modifier       = Modifier.fillMaxSize(),
            ) {
                item { HeroSection(trialDays = state.trialDays) }

                item {
                    SectionLabel(
                        text     = stringResource(R.string.premium_section_features),
                        modifier = Modifier.padding(
                            start = Spacing.Spacing20, end = Spacing.Spacing20,
                            top   = Spacing.Spacing4,  bottom = Spacing.Spacing10,
                        ),
                    )
                }

                item { PremiumFeaturesCard(features = state.features) }

                item {
                    SectionLabel(
                        text     = stringResource(R.string.premium_section_plans),
                        modifier = Modifier.padding(
                            start = Spacing.Spacing20, end = Spacing.Spacing20,
                            top   = Spacing.Spacing20, bottom = Spacing.Spacing10,
                        ),
                    )
                }

                item {
                    PlansRow(
                        plans          = state.plans,
                        selectedPlanId = state.selectedPlanId,
                        onPlanSelected = onPlanSelected,
                        modifier       = Modifier.padding(horizontal = Spacing.Spacing20),
                    )
                }

                item {
                    Spacer(Modifier.height(Spacing.Spacing20))
                    CtaSection(
                        trialDays    = state.trialDays,
                        isPurchasing = state.isPurchasing,
                        socialProof  = state.socialProof,
                        onStartTrial = onStartTrial,
                        onDismiss    = onDismiss,
                        modifier     = Modifier.padding(horizontal = Spacing.Spacing20),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    trialDays: Int,
    modifier: Modifier = Modifier,
) {
    val gradients       = LocalGradientTokens.current
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label         = "pulseAlpha",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.Spacing4, bottom = Spacing.Spacing20),
    ) {
        Text(
            text      = stringResource(R.string.premium_tagline),
            style     = TypeScale.BodySmallRegular,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = Spacing.Spacing32),
        )

        Spacer(Modifier.height(Spacing.Spacing12))

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing10),
        ) {
            Text(
                text  = stringResource(R.string.app_name),
                style = TypeScale.DisplayLarge.copy(brush = gradients.title),
            )
            ProBadgeChip()
        }

        Spacer(Modifier.height(Spacing.Spacing12))

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing6),
            modifier = Modifier
                .clip(Radius.ShapePill)
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f))
                .border(
                    width = 1.adp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.32f),
                    shape = Radius.ShapePill,
                )
                .padding(horizontal = Spacing.Spacing16, vertical = Spacing.Spacing6),
        ) {
            Box(
                modifier = Modifier
                    .size(6.adp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = pulseAlpha)),
            )
            Text(
                text  = stringResource(R.string.premium_trial_badge, trialDays),
                style = TypeScale.LabelXSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun ProBadgeChip(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing4),
        modifier = modifier
            .clip(Radius.ShapePill)
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
                shape = Radius.ShapePill,
            )
            .padding(horizontal = Spacing.Spacing8, vertical = Spacing.Spacing4),
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_crown),
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.tertiary,
            modifier           = Modifier.size(10.adp),
        )
        Text(
            text  = stringResource(R.string.premium_pro_label),
            style = TypeScale.LabelMicro,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
internal fun PremiumFeaturesCard(
    features: List<ProFeatureUiModel>,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape          = Radius.ShapeXL,
        color          = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 0.adp,
        modifier       = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Spacing20)
            .border(
                width = FeatureRowTokens.ContainerBorderWidth,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = Radius.ShapeXL,
            ),
    ) {
        Column {
            features.forEachIndexed { index, feature ->
                PremiumFeatureRow(feature = feature)
                if (index < features.lastIndex) {
                    HorizontalDivider(
                        color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                        modifier = Modifier.padding(
                            start = (FeatureRowTokens.HorizontalPadding.value +
                                    FeatureRowTokens.IconContainerSize.value +
                                    FeatureRowTokens.InternalGap.value).adp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun PremiumFeatureRow(
    feature: ProFeatureUiModel,
    modifier: Modifier = Modifier,
) {
    val featureColor = feature.colorRole.toColor()

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FeatureRowTokens.InternalGap),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = FeatureRowTokens.HorizontalPadding,
                vertical   = FeatureRowTokens.VerticalPadding,
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(FeatureRowTokens.IconContainerSize)
                .clip(FeatureRowTokens.IconContainerShape)
                .background(featureColor.copy(alpha = 0.14f)),
        ) {
            Icon(
                painter            = painterResource(iconKeyToDrawableRes(feature.iconKey)),
                contentDescription = null,
                tint               = featureColor,
                modifier           = Modifier.size(FeatureRowTokens.IconSize),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = feature.label,
                style = TypeScale.LabelXLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text     = feature.sublabel,
                style    = TypeScale.LabelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.Spacing2),
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(FeatureRowTokens.CheckBadgeSize)
                .clip(FeatureRowTokens.CheckBadgeShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                .border(
                    width = FeatureRowTokens.CheckBadgeBorderWidth,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                    shape = FeatureRowTokens.CheckBadgeShape,
                ),
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(FeatureRowTokens.CheckIconSize),
            )
        }
    }
}

@Composable
private fun PlansRow(
    plans: List<PremiumPlanUiModel>,
    selectedPlanId: String,
    onPlanSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
        modifier              = modifier.fillMaxWidth(),
    ) {
        plans.forEach { plan ->
            PlanCard(
                plan       = plan,
                isSelected = plan.id == selectedPlanId,
                onSelect   = { onPlanSelected(plan.id) },
                modifier   = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: PremiumPlanUiModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradients  = LocalGradientTokens.current
    val isGradient = isSelected && plan.isHighlighted
    val onPrimary  = MaterialTheme.colorScheme.onPrimary

    val cardBg: Brush = if (isGradient) {
        gradients.annual
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.9f else 0.55f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.9f else 0.55f),
            )
        )
    }

    val borderColor = when {
        isGradient -> MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.45f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
        else       -> MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
    }
    val borderWidth = if (isSelected) PricingCardTokens.BorderWidthSelected else PricingCardTokens.BorderWidth

    val priceColor  = if (isGradient) onPrimary else MaterialTheme.colorScheme.onSurface
    val labelColor  = if (isGradient) onPrimary.copy(alpha = 0.70f) else MaterialTheme.colorScheme.onSurfaceVariant
    val periodColor = if (isGradient) onPrimary.copy(alpha = 0.60f) else MaterialTheme.colorScheme.onSurfaceVariant
    val noteColor   = if (isGradient) onPrimary.copy(alpha = 0.52f) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(PricingCardTokens.Shape)
            .background(cardBg)
            .border(width = borderWidth, color = borderColor, shape = PricingCardTokens.Shape)
            .clickable(onClick = onSelect)
            .padding(PricingCardTokens.Padding),
    ) {
        Column {
            // Resolve UiText label to a locale-correct String for this Composable scope.
            val labelText = if (plan.isHighlighted) {
                stringResource(R.string.premium_plan_label_best, plan.label.resolve())
            } else {
                plan.label.resolve().uppercase()
            }
            Text(
                text  = labelText,
                style = TypeScale.LabelXSmall,
                color = labelColor,
            )

            Spacer(Modifier.height(Spacing.Spacing4))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = plan.priceDisplay,           style = TypeScale.HeadlineLarge, color = priceColor)
                Text(
                    text     = plan.periodDisplay.resolve(),
                    style    = TypeScale.LabelLarge,
                    color    = periodColor,
                    modifier = Modifier.padding(bottom = Spacing.Spacing3, start = Spacing.Spacing2),
                )
            }

            Text(text = plan.noteDisplay, style = TypeScale.LabelMedium, color = noteColor)
        }

        if (plan.badgeLabel != null) {
            Text(
                text     = plan.badgeLabel.resolve(),
                style    = TypeScale.LabelMicro,
                color    = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(PricingCardTokens.BadgeShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(
                        horizontal = PricingCardTokens.BadgeHorizontalPadding,
                        vertical   = PricingCardTokens.BadgeVerticalPadding,
                    ),
            )
        }

        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(PricingCardTokens.CheckBadgeInset)
                    .size(PricingCardTokens.CheckBadgeSize)
                    .clip(PricingCardTokens.CheckBadgeShape)
                    .background(
                        if (isGradient) onPrimary.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    )
                    .border(
                        width = 1.adp,
                        color = if (isGradient) onPrimary.copy(alpha = 0.40f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                        shape = PricingCardTokens.CheckBadgeShape,
                    ),
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint               = if (isGradient) onPrimary else MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(10.adp),
                )
            }
        }

        if (isGradient) ShimmerSweep(durationMs = 2_800, delayMs = 1_600)
    }
}

@Composable
private fun CtaSection(
    trialDays: Int,
    isPurchasing: Boolean,
    socialProof: SocialProofData?,
    onStartTrial: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = modifier.fillMaxWidth(),
    ) {
        PremiumCtaButton(
            trialDays    = trialDays,
            isPurchasing = isPurchasing,
            onClick      = onStartTrial,
        )

        Spacer(Modifier.height(Spacing.Spacing12))

        SocialProofRow(socialProof = socialProof)

        Spacer(Modifier.height(Spacing.Spacing8))

        TrustLine()

        Text(
            text      = stringResource(R.string.premium_cta_skip),
            style     = TypeScale.BodySmallRegular,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .clickable(onClick = onDismiss)
                .padding(vertical = Spacing.Spacing10, horizontal = Spacing.Spacing16),
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
    val gradients = LocalGradientTokens.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(PrimaryButtonTokens.Height)
            .clip(PrimaryButtonTokens.Shape)
            .background(gradients.cta)
            .clickable(enabled = !isPurchasing, onClick = onClick)
            .semantics { contentDescription = "Start trial" },
    ) {
        ShimmerSweep(durationMs = 2_600, delayMs = 1_200)

        if (isPurchasing) {
            CircularProgressIndicator(
                color       = White,
                strokeWidth = 2.adp,
                modifier    = Modifier.size(22.adp),
            )
        } else {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PrimaryButtonTokens.IconTextGap),
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_gem),
                    contentDescription = null,
                    tint               = White,
                    modifier           = Modifier.size(PrimaryButtonTokens.IconSize),
                )
                Text(
                    text  = stringResource(R.string.premium_cta_start_trial, trialDays),
                    style = TypeScale.BodyXLarge,
                    color = White,
                )
                Icon(
                    imageVector        = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint               = White,
                    modifier           = Modifier.size(PrimaryButtonTokens.IconSize),
                )
            }
        }
    }
}

@Composable
private fun SocialProofRow(
    socialProof: SocialProofData?,
    modifier: Modifier = Modifier,
) {
    val initials   = socialProof?.avatarInitials ?: listOf("A", "J", "M", "S", "K")
    val countLabel = socialProof?.userCountLabel ?: "50,000+"

    val avatarStep  = AvatarStackTokens.AvatarSize - AvatarStackTokens.Overlap
    val stackWidth  = remember(initials.size) {
        AvatarStackTokens.AvatarSize + avatarStep * (initials.size - 1)
    }

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(Radius.ShapeXXXL)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(
                width = 1.adp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = Radius.ShapeXXXL,
            )
            .padding(horizontal = Spacing.Spacing16, vertical = Spacing.Spacing12),
    ) {
        // ── Avatar stack ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(stackWidth)
                .height(AvatarStackTokens.AvatarSize),
        ) {
            initials.forEachIndexed { index, initial ->
                val hue = 240f + index * 22f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = avatarStep * index)
                        .size(AvatarStackTokens.AvatarSize)
                        .clip(AvatarStackTokens.AvatarShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.hsl(hue, 0.6f, 0.50f),
                                    Color.hsl(hue + 20f, 0.7f, 0.60f),
                                )
                            )
                        )
                        .border(
                            width = AvatarStackTokens.AvatarBorderWidth,
                            color = MaterialTheme.colorScheme.surface,
                            shape = AvatarStackTokens.AvatarShape,
                        ),
                ) {
                    Text(
                        text  = initial,
                        style = TypeScale.LabelXSmall.copy(
                            fontSize   = AvatarStackTokens.InitialFontSize,
                            fontWeight = AvatarStackTokens.InitialFontWeight,
                        ),
                        color = White,
                    )
                }
            }
        }

        Spacer(Modifier.width(Spacing.Spacing12))

        // ── Stars + copy ──────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Spacing2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AvatarStackTokens.StarGap),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                repeat(5) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_star),
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.tertiary,
                        modifier           = Modifier.size(AvatarStackTokens.StarSize),
                    )
                }
            }
            Text(
                text  = stringResource(R.string.premium_social_proof, countLabel),
                style = TypeScale.LabelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TrustLine(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing6),
        modifier              = modifier,
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier           = Modifier.size(10.adp),
        )
        Text(
            text      = stringResource(R.string.premium_trust_line),
            style     = TypeScale.LabelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
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
        text     = text.uppercase(),
        style    = TypeScale.LabelSmall,
        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = modifier.padding(start = Spacing.Spacing2),
    )
}

@Composable
internal fun PremiumCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick  = onClick,
        modifier = modifier
            .size(34.adp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(1.adp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), CircleShape),
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_x),
            contentDescription = stringResource(R.string.premium_close),
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(15.adp),
        )
    }
}

@Composable
internal fun ShimmerSweep(
    durationMs: Int = 2_600,
    delayMs: Int = 1_200,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue  = -1f,
        targetValue   = 2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMs, easing = FastOutSlowInEasing, delayMillis = delayMs),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerOffset",
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { translationX = offset * size.width }
            .background(Gradients.GradientShimmer),
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
private fun PremiumLoadingContent(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PremiumErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier            = modifier.fillMaxSize().padding(Spacing.Spacing24),
    ) {
        Text(
            text      = message,
            style     = TypeScale.BodyMedium,
            color     = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.Spacing16))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.premium_retry)) }
    }
}

@Preview(name = "SocialProofRow · With data", showBackground = true)
@Preview(name = "SocialProofRow · Loading (null)", showBackground = true)
@Composable
private fun SocialProofRowPreview() {
    SynapseTheme {
        Column(Modifier.padding(Spacing.Spacing16), verticalArrangement = Arrangement.spacedBy(Spacing.Spacing12)) {
            SocialProofRow(socialProof = SocialProofData(userCountLabel = "50,000+"))
            SocialProofRow(socialProof = null)
        }
    }
}