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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.Gradients
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.theme.tokens.TopAppBarTokens
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

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
    isPremium: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(
                horizontal = TopAppBarTokens.HorizontalPadding,
                vertical = Spacing.Spacing12,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarButton(
            initial = userInitial,
            onClick = onProfileClick,
            profileAvatarUrl = profileAvatarUrl,
            isPremium = isPremium
        )

        Spacer(Modifier.width(Spacing.ListItemVerticalGap))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = TopAppBarTokens.TitleFontStyle,
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

@Composable
private fun AvatarButton(
    initial: String,
    onClick: () -> Unit,
    profileAvatarUrl: String? = null,
    isPremium: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(TopAppBarTokens.AvatarSize),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = Modifier
                .size(TopAppBarTokens.AvatarSize)
                .clip(TopAppBarTokens.AvatarShape)
                .background(MaterialTheme.synapse.gradients.primary)
                .border(
                    width = TopAppBarTokens.AvatarBorderWidth,
                    brush = if (isPremium) MaterialTheme.synapse.gradients.gold else MaterialTheme.synapse.gradients.primary,
                    shape = TopAppBarTokens.AvatarShape,
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
                        .size(80.adp)
                        .clip(CircleShape),
                )
            } else {
                Text(
                    text = initial.take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.asp,
                        color = Color.White
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
    val infiniteTransition = rememberInfiniteTransition(label = "goPro")

    // ── Shimmer sweep ──────────────────────────────────────────────────────────
    val shimmerFraction by infiniteTransition.animateFloat(
        initialValue = -1.4f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_000, delayMillis = 2_000, easing = EaseInOut),
            repeatMode = RepeatMode.Restart,
        )
    )

    // ── Outer pulse ring ───────────────────────────────────────────────────────
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        )
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        )
    )

    // ── Crown rock ─────────────────────────────────────────────────────────────
    val crownRotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4_000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        )
    )

    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
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
                // silent until end of cycle
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

    BoxWithConstraints(
        modifier = modifier.graphicsLayer { translationX = shakeOffset },
        contentAlignment = Alignment.Center,
    ) {
        val pillWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

        // Outer pulse ring (drawn behind pill)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = ringScale
                    scaleY = ringScale
                    alpha = ringAlpha
                }
                .border(
                    width = 1.5.adp,
                    color = MaterialTheme.synapse.semantic.gold.copy(alpha = 0.70f),
                    shape = TopAppBarTokens.GoProShape,
                ),
        )

        // Pill surface
        Surface(
            onClick = onClick,
            shape = TopAppBarTokens.GoProShape,
            color = Color.Transparent,
            contentColor = Color.White,
        ) {
            Box(contentAlignment = Alignment.Center) {

                // Full pill gradient background
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.synapse.gradients.goPro),
                )

                // Angled shimmer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            translationX = shimmerFraction * pillWidthPx
                            rotationZ = 25f          // diagonal angle
                            scaleX = 0.25f        // narrow beam width
                        }
                        .background(Gradients.GradientShimmer),
                )

                // Content row (owns the padding)
                Row(
                    modifier = Modifier.padding(
                        horizontal = TopAppBarTokens.GoProHorizontalPadding,
                        vertical = TopAppBarTokens.GoProVerticalPadding,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing6),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_crown),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(TopAppBarTokens.GoProIconSize)
                            .graphicsLayer { rotationZ = crownRotation },
                    )
                    Text(
                        text = pillLabel,
                        fontSize = TopAppBarTokens.GoProFontSize,
                        fontWeight = TopAppBarTokens.GoProFontWeight,
                        letterSpacing = TopAppBarTokens.GoProLetterSpacing,
                        color = Color.White,
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
    showBackground = true
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
            onPremiumClick = {},
        )
    }
}
