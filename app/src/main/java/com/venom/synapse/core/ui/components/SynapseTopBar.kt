package com.venom.synapse.core.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.Gradients
import com.venom.synapse.core.theme.tokens.ShadowTokens
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.ui.components.common.adp

/**
 * Shell-level top bar for the application.
 */
@Composable
fun SynapseTopBar(
    title: String,
    subtitle: String,
    userInitial: String,
    onProfileClick: () -> Unit,
    onPremiumClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileAvatarUrl: String? = null,
    isPremium: Boolean = false
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
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        GoProButton(
            isPremium = isPremium,
            onClick = onPremiumClick,
        )
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

    Box(
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = modifier
                .size(56.adp)
                .clip(shape)
                .background(MaterialTheme.synapse.gradients.primary)
                .border(
                   2.adp,
                    if (isPremium) MaterialTheme.synapse.gradients.gold else MaterialTheme.synapse.gradients.primary,
                     shape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (profileAvatarUrl != null) {
                AsyncImage(
                    model = profileAvatarUrl,
                    contentDescription = stringResource(R.string.profile_photo_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(shape),
                )
            } else {
                Text(
                    text = initial.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(0.9f),
                    )
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
    val infiniteTransition = rememberInfiniteTransition()
    var pillWidthPx by remember { mutableFloatStateOf(0f) }

    // ── Shimmer sweep — parked off-screen for premium users ────────────────────
    val shimmerFraction by infiniteTransition.animateFloat(
        initialValue = if (isPremium) 100f else -1.4f,
        targetValue  = if (isPremium) 100f else  2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_000, delayMillis = 2_000, easing = EaseInOut),
            repeatMode = RepeatMode.Restart,
        ),
    )

    // ── Outer pulse ring — frozen at 1× scale / 0 alpha for premium users ─────
    val ringScale by infiniteTransition.animateFloat(
        initialValue = if (isPremium) 1f else 0.8f,
        targetValue  = if (isPremium) 1f else 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = if (isPremium) 0f else 0.5f,
        targetValue  = if (isPremium) 0f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    // ── Crown rock — kept for all users (status indicator) ────────────────────
    val crownRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue  = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4_000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    // ── Shake — kept for all users ────────────────────────────────────────────
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 10_000
                0f at 0
                6f at 60
                6f at 120
                5f at 180
                5f at 240
                3f at 300
                3f at 360
                0f at 420
                0f at 10_000
            },
            repeatMode = RepeatMode.Restart,
        )
    )

    val pillLabel = if (isPremium) {
        stringResource(R.string.go_pro_label_premium)
    } else {
        stringResource(R.string.go_pro_label)
    }

    Box(
        modifier = modifier
            .graphicsLayer { translationX = shakeOffset }
            .dropShadow(
                shape  = MaterialTheme.synapse.radius.pill,
                shadow = ShadowTokens.GoldGlow.toShadow(),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = ringScale
                    scaleY = ringScale
                    alpha  = ringAlpha
                }
                .border(
                    width = 1.5.adp,
                    color = MaterialTheme.synapse.semantic.gold.copy(alpha = 0.7f),
                    shape = MaterialTheme.synapse.radius.pill,
                ),
        )

        // Pill surface
        Surface(
            onClick = onClick,
            modifier = Modifier.onSizeChanged { pillWidthPx = it.width.toFloat() },
            shape = MaterialTheme.synapse.radius.pill,
            color = Color.Transparent,
            contentColor = Color.White.copy(0.9f),
        ) {
            Box(contentAlignment = Alignment.Center) {

                // Full pill gradient background
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.synapse.gradients.gold),
                )

                // Angled shimmer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            translationX = shimmerFraction * pillWidthPx
                            rotationZ    = 25f
                            scaleX       = 0.25f
                        }
                        .background(Gradients.GradientShimmer),
                )

                Row(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.synapse.spacing.s16,
                        vertical   = MaterialTheme.synapse.spacing.s8,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_crown),
                        contentDescription = null,
                        tint = Color.White.copy(0.9f),
                        modifier = Modifier
                            .size(18.adp)
                            .graphicsLayer { rotationZ = crownRotation },
                    )
                    Text(
                        text  = pillLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                        ),
                        color = Color.White.copy(0.9f),
                    )
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SynapseTopBarPreview() {
    SynapseTheme {
        SynapseTopBar(
            title = "Dashboard",
            subtitle = "Good morning",
            userInitial = "A",
            onProfileClick = {},
            onPremiumClick = {},
        )
    }
}

@Preview(name = "Premium · Light", showBackground = true)
@Preview(
    name = "Premium · Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun SynapseTopBarPremiumPreview() {
    SynapseTheme {
        SynapseTopBar(
            title = "Library",
            subtitle = "Your collection",
            userInitial = "A",
            isPremium = true,
            onProfileClick = {},
            onPremiumClick = {}
        )
    }
}