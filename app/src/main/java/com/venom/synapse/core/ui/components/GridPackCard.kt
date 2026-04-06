package com.venom.synapse.core.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.synapse.core.ui.state.LastStudiedLabel
import com.venom.synapse.core.ui.state.PackDisplayItem
import com.venom.synapse.core.ui.state.displayString
import com.venom.ui.components.common.adp
import kotlinx.coroutines.delay

/**
 * Grid-cell pack card — 2-column grid layout for LibraryScreen.
 *
 * @param pack              Data for this card.
 * @param animDelayMs       Stagger delay in ms before the progress bar animates in.
 * @param onClick           Invoked when the card body or the Continue button is tapped.
 * @param actions           Swipe-revealed actions (edit / export / delete).
 * @param isSwiped          Drives the swipe-open state from outside.
 * @param onSwipeOpen       Called when the card is fully swiped open.
 * @param onSwipeClose      Called when the card snaps back closed.
 * @param enableSwipeActions Whether swipe-to-reveal is active on this card.
 */
@Composable
fun GridPackCard(
    pack: PackDisplayItem,
    animDelayMs: Int,
    onClick: () -> Unit,
    actions: List<SwipeAction> = emptyList(),
    isSwiped: Boolean = false,
    onSwipeOpen: () -> Unit = {},
    onSwipeClose: () -> Unit = {},
    enableSwipeActions: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colorSet = remember(pack.colorHex) { buildCardColorSet(pack.colorHex) }
    val semantic = MaterialTheme.synapse.semantic
    val hasDue   = pack.cardsToReview > 0

    val learnedPct = remember(pack.masteredCards, pack.totalCards) {
        if (pack.totalCards > 0) (pack.masteredCards * 100f / pack.totalCards).toInt() else 0
    }

    // Staggered progress bar entrance
    var progressVisible by remember { mutableStateOf(false) }
    LaunchedEffect(pack.id) {
        delay(animDelayMs.toLong())
        progressVisible = true
    }
    val animatedProgress by animateFloatAsState(
        targetValue   = if (progressVisible) pack.progress else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    )

    val shadowModifier = modifier.dropShadow(
        shape  = MaterialTheme.shapes.extraLarge,
        shadow = MaterialTheme.synapse.shadows.subtle.toShadow(customColor = colorSet.accent),
    )

    if (enableSwipeActions && actions.isNotEmpty()) {
        SwipeableCardContainer(
            actions         = actions,
            isSwiped        = isSwiped,
            onSwipeOpen     = onSwipeOpen,
            onSwipeClose    = onSwipeClose,
            onTap           = onClick,
            verticalActions = true,
            modifier        = shadowModifier,
        ) {
            GridPackCardSurface(
                pack             = pack,
                colorSet         = colorSet,
                semanticGold     = semantic.gold,
                hasDue           = hasDue,
                learnedPct       = learnedPct,
                animatedProgress = animatedProgress,
                onClick          = onClick,
                isClickable      = false,
            )
        }
    } else {
        GridPackCardSurface(
            pack             = pack,
            colorSet         = colorSet,
            semanticGold     = semantic.gold,
            hasDue           = hasDue,
            learnedPct       = learnedPct,
            animatedProgress = animatedProgress,
            onClick          = onClick,
            isClickable      = true,
            modifier         = shadowModifier,
        )
    }
}

@Composable
private fun GridPackCardSurface(
    pack: PackDisplayItem,
    colorSet: CardColorSet,
    semanticGold: Color,
    hasDue: Boolean,
    learnedPct: Int,
    animatedProgress: Float,
    onClick: () -> Unit,
    isClickable: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.then(
            if (isClickable) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
    ) {
        GridPackCardContent(
            pack             = pack,
            colorSet         = colorSet,
            semanticGold     = semanticGold,
            hasDue           = hasDue,
            learnedPct       = learnedPct,
            animatedProgress = animatedProgress,
            onClick          = onClick,
        )
    }
}

@Composable
private fun GridPackCardContent(
    pack: PackDisplayItem,
    colorSet: CardColorSet,
    semanticGold: Color,
    hasDue: Boolean,
    learnedPct: Int,
    animatedProgress: Float,
    onClick: () -> Unit,
) {
    val sp = MaterialTheme.synapse.spacing

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(
                horizontal = sp.s14,
                vertical   = sp.s14,
            ),
    ) {

        // ── Row 1: Emoji icon + category pill ─────────────────────────
        IconAndCategoryRow(
            emoji    = pack.emoji,
            category = pack.category,
            colorSet = colorSet,
        )

        Spacer(Modifier.height(sp.s10))

        // ── Title (2 lines reserved) ──────────────────────────────────
        Text(
            text     = pack.title,
            style    = MaterialTheme.typography.bodyMedium.copy(
                fontWeight     = FontWeight.Bold,
                platformStyle  = PlatformTextStyle(includeFontPadding = false),
            ),
            color    = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(sp.s10))

        // ── Progress section: label row + bar ─────────────────────────
        ProgressSection(
            mastered         = pack.masteredCards,
            total            = pack.totalCards,
            learnedPct       = learnedPct,
            animatedProgress = animatedProgress,
            accent           = colorSet.accent,
        )

        Spacer(Modifier.height(sp.s8))

        // ── Meta row: last studied | due badge + streak ───────────────
        MetaRow(
            lastStudied = pack.lastStudiedLabel,
            dueCards    = pack.cardsToReview,
            streakDays  = pack.streakDays,
            goldColor   = semanticGold,
            hasDue      = hasDue,
        )

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.height(sp.s10))

        // ── Continue / Done CTA ───────────────────────────────────────
        ContinueButton(
            hasDue   = hasDue,
            colorSet = colorSet,
            onClick  = onClick,
        )
    }
}

@Composable
private fun IconAndCategoryRow(
    emoji: String,
    category: String,
    colorSet: CardColorSet,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Emoji container — tinted bg + subtle accent border
        Box(
            modifier = Modifier
                .size(38.adp)
                .clip(MaterialTheme.synapse.radius.md)
                .background(colorSet.bg)
                .border(
                    width = 1.dp,
                    color = colorSet.border,
                    shape = MaterialTheme.synapse.radius.md,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = emoji,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(Modifier.weight(1f))

        // Category pill — true pill shape, uppercase micro label
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(colorSet.bg)
                .border(1.dp, colorSet.border, CircleShape)
                .padding(
                    horizontal = MaterialTheme.synapse.spacing.s8,
                    vertical   = MaterialTheme.synapse.spacing.s4,
                ),
        ) {
            Text(
                text     = category.uppercase(),
                style    = MaterialTheme.typography.labelSmall.copy(
                    fontWeight    = FontWeight.ExtraBold,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                color    = colorSet.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProgressSection(
    mastered: Int,
    total: Int,
    learnedPct: Int,
    animatedProgress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.synapse.spacing

    Column(modifier = modifier) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = stringResource(R.string.grid_card_learned, mastered, total),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight    = FontWeight.Medium,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text  = stringResource(R.string.grid_card_pct, learnedPct),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight    = FontWeight.ExtraBold,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                color = accent,
            )
        }

        Spacer(Modifier.height(sp.s6))

        ProgressBar(
            progress = animatedProgress,
            accent   = accent,
        )
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val fraction = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(7.adp)
            .clip(MaterialTheme.synapse.radius.xxxl)
            .background(accent.copy(alpha = 0.13f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .dropShadow(
                    shape  = MaterialTheme.synapse.radius.xxxl,
                    shadow = Shadow(radius = 6.dp, color = accent, alpha = 0.45f),
                )
                .clip(MaterialTheme.synapse.radius.xxxl)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(accent.copy(alpha = 0.70f), accent),
                    )
                ),
        )
    }
}

@Composable
private fun MetaRow(
    lastStudied: LastStudiedLabel,
    dueCards: Int,
    streakDays: Int,
    goldColor: Color,
    hasDue: Boolean,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.synapse.spacing

    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.s4),
        ) {
            Icon(
                painter           = painterResource(R.drawable.ic_clock),
                contentDescription = null,
                tint              = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier          = Modifier.size(10.adp),
            )
            Text(
                text  = lastStudied.displayString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.s6),
        ) {
            if (hasDue) {
                DueBadge(dueCards = dueCards, goldColor = goldColor)
            }
            if (streakDays > 0) {
                StreakChip(streakDays = streakDays, goldColor = goldColor)
            }
        }
    }
}

@Composable
private fun DueBadge(
    dueCards: Int,
    goldColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(goldColor.copy(alpha = 0.13f))
            .border(1.dp, goldColor.copy(alpha = 0.28f), CircleShape)
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s8,
                vertical   = MaterialTheme.synapse.spacing.s2,
            ),
    ) {
        Text(
            text  = stringResource(R.string.due_badge, dueCards),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.Bold,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            color = goldColor,
        )
    }
}

@Composable
private fun StreakChip(
    streakDays: Int,
    goldColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s2),
    ) {
        Icon(
            painter           = painterResource(R.drawable.ic_zap),
            contentDescription = null,
            tint              = goldColor,
            modifier          = Modifier.size(10.adp),
        )
        Text(
            text  = stringResource(R.string.grid_card_streak, streakDays),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.Bold,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            color = goldColor,
        )
    }
}

@Composable
private fun ContinueButton(
    hasDue: Boolean,
    colorSet: CardColorSet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctaGradient = MaterialTheme.synapse.gradients.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.adp)
            .clip(MaterialTheme.synapse.radius.lg)
            .then(
                if (hasDue) {
                    Modifier.background(ctaGradient)
                } else {
                    Modifier.border(
                        width = 1.5.dp,
                        color = colorSet.accent.copy(alpha = 0.30f),
                        shape = MaterialTheme.synapse.radius.lg,
                    )
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s4),
        ) {
            Text(
                text  = stringResource(if (hasDue) R.string.grid_card_continue else R.string.grid_card_done),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight    = FontWeight.Bold,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                color = if (hasDue) Color.White.copy(alpha = 0.92f) else colorSet.accent,
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint               = if (hasDue) Color.White.copy(alpha = 0.92f) else colorSet.accent,
                modifier           = Modifier.size(10.adp),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Light — Due", showBackground = true)
@Preview(name = "Dark  — Due", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GridPackCardDuePreview() {
    SynapseTheme {
        GridPackCard(
            pack        = PackDisplayItem.Mocks.first(),
            animDelayMs = 0,
            onClick     = {},
            modifier    = Modifier
                .padding(16.dp)
                .width(180.dp),
        )
    }
}

@Preview(name = "Light — No Due", showBackground = true)
@Preview(name = "Dark  — No Due", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GridPackCardNoDuePreview() {
    SynapseTheme {
        GridPackCard(
            pack        = PackDisplayItem.Mocks.last(),
            animDelayMs = 0,
            onClick     = {},
            modifier    = Modifier
                .padding(16.dp)
                .width(180.dp),
        )
    }
}