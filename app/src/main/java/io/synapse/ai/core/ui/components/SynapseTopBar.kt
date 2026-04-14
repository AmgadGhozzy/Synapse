package io.synapse.ai.core.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.asp
import io.synapse.ai.core.theme.tokens.toShadow

@Composable
fun SynapseTopBar(
    title: String,
    subtitle: String,
    userInitial: String,
    onProfileClick: () -> Unit,
    onPremiumClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileAvatarUrl: String? = null,
    isPremium: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s24,
                vertical = MaterialTheme.synapse.spacing.s8,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarButton(
            initial = userInitial,
            onClick = onProfileClick,
            profileAvatarUrl = profileAvatarUrl,
            isPremium = isPremium,
        )

        Spacer(Modifier.width(MaterialTheme.synapse.spacing.listItemGap))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge.copy(
                    lineHeight = 15.asp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = 20.asp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        GoProButton(isPremium = isPremium, onClick = onPremiumClick)
    }
}

// ── Avatar Button ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AvatarButton(
    initial: String,
    onClick: () -> Unit,
    profileAvatarUrl: String? = null,
    isPremium: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialShapes.Cookie9Sided.toShape()

    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(60.adp)
                .clip(shape)
                .background(MaterialTheme.synapse.gradients.primary)
                .border(
                    width = 2.adp,
                    brush = if (isPremium) MaterialTheme.synapse.gradients.gold
                    else MaterialTheme.synapse.gradients.primary,
                    shape = shape,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (profileAvatarUrl != null) {
                AsyncImage(
                    model = profileAvatarUrl,
                    contentDescription = stringResource(R.string.profile_photo_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape),
                )
            } else {
                Text(
                    text = initial.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.9f),
                    ),
                )
            }
        }
    }
}

// ── Go Pro Button ─────────────────────────────────────────────────────────────
@Composable
private fun GoProButton(
    isPremium: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition()
    val density = LocalDensity.current

    // Measured from the pill's inner content Box — shadow-free, accurate
    var pillSize by remember { mutableStateOf(IntSize.Zero) }
    val pillW = with(density) { pillSize.width.toDp() }
    val pillH = with(density) { pillSize.height.toDp() }

    val shimmerFraction by transition.animateFloat(
        initialValue = -1.4f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            tween(3_000, delayMillis = 2_000, easing = EaseInOut),
            RepeatMode.Restart,
        ),
    )
    val ringScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
    )
    val crownRotation by transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            tween(4_000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
    )
    val shakeOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 10_000
                0f at 0; 6f at 60; 6f at 120; 5f at 180
                5f at 240; 3f at 300; 3f at 360; 0f at 420; 0f at 10_000
            },
            RepeatMode.Restart,
        ),
    )

    val pillLabel = if (isPremium) stringResource(R.string.go_pro_label_premium)
    else stringResource(R.string.go_pro_label)

    Box(
        modifier = modifier.graphicsLayer { translationX = shakeOffset },
        contentAlignment = Alignment.Center,
    ) {
        if (!isPremium) {
            Box(
                modifier = Modifier
                    .size(width = pillW, height = pillH)
                    .graphicsLayer { scaleX = ringScale; scaleY = ringScale; alpha = ringAlpha }
                    .border(
                        width = 1.5.adp,
                        color = MaterialTheme.synapse.semantic.gold.copy(alpha = 0.80f),
                        shape = CircleShape,
                    ),
            )
        }

        Box(
            modifier = Modifier.dropShadow(
                shape = MaterialTheme.synapse.radius.pill,
                shadow = MaterialTheme.synapse.shadows.goldGlow.toShadow(),
            ),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                onClick = onClick,
                shape = CircleShape,
                color = Color.Transparent,
                contentColor = Color.White.copy(alpha = 0.9f),
            ) {
                Box(
                    modifier = Modifier.onSizeChanged { pillSize = it },
                    contentAlignment = Alignment.Center,
                ) {
                    // Gold gradient background
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.synapse.gradients.gold),
                    )
                    // Shimmer sweep
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                translationX = shimmerFraction * pillSize.width
                                rotationZ = 25f
                                scaleX = 0.25f
                            }
                            .background(MaterialTheme.synapse.gradients.shimmer),
                    )
                    // Label row
                    Row(
                        modifier = Modifier.padding(bottom = 1.adp)
                            .padding(
                            horizontal = MaterialTheme.synapse.spacing.s16,
                            vertical = MaterialTheme.synapse.spacing.s10,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_crown),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .size(18.adp)
                                .graphicsLayer { rotationZ = crownRotation },
                        )
                        Text(
                            text = pillLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                lineHeight = 24.asp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
            }
        }
    }
}


@Preview(name = "Light", showBackground = true, locale = "ar")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SynapseTopBarPreview() {
    SynapseTheme {
        SynapseTopBar(
            title = "Dashboard", subtitle = "Good morning",
            userInitial = "A", onProfileClick = {}, onPremiumClick = {},
        )
    }
}

@Preview(name = "Premium · Light", showBackground = true)
@Preview(name = "Premium · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SynapseTopBarPremiumPreview() {
    SynapseTheme {
        SynapseTopBar(
            title = "Library", subtitle = "Your collection",
            userInitial = "A", isPremium = true, onProfileClick = {}, onPremiumClick = {},
        )
    }
}