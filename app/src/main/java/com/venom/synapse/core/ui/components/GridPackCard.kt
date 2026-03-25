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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.PackCardTokens
import com.venom.synapse.core.theme.tokens.Radius
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.synapse.core.ui.state.LastStudiedLabel
import com.venom.synapse.core.ui.state.PackDisplayItem
import com.venom.synapse.core.ui.state.displayString
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp
import kotlinx.coroutines.delay

/**
 * Grid-cell pack card — the 2-column grid design from LibraryScreen.
 * @param pack          Data for this card.
 * @param animDelayMs   Stagger delay in ms before the progress bar animates in.
 * @param onClick       Invoked when the card or the Continue button is tapped.
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
    val colorSet  = remember(pack.colorHex) { buildCardColorSet(pack.colorHex) }
    val semantic  = MaterialTheme.synapse.semantic
    val hasDue    = pack.cardsToReview > 0

    val learnedPct = remember(pack.masteredCards, pack.totalCards) {
        if (pack.totalCards > 0) (pack.masteredCards * 100f / pack.totalCards).toInt() else 0
    }

    // Progress bar animates in after the stagger delay
    var progressVisible by remember { mutableStateOf(false) }
    LaunchedEffect(pack.id) {
        delay(animDelayMs.toLong())
        progressVisible = true
    }
    val animatedProgress by animateFloatAsState(
        targetValue    = if (progressVisible) pack.progress else 0f,
        animationSpec  = tween(durationMillis = 900, easing = FastOutSlowInEasing)
    )

    if (enableSwipeActions && actions.isNotEmpty()) {
        SwipeableCardContainer(
            actions         = actions,
            isSwiped        = isSwiped,
            onSwipeOpen     = onSwipeOpen,
            onSwipeClose    = onSwipeClose,
            onTap           = onClick,
            verticalActions = true,
            modifier = modifier.dropShadow(
                shape = PackCardTokens.Shape,
                shadow = PackCardTokens.Shadow.toShadow(customColor = colorSet.accent)
            ),
        ) {
            GridPackCardSurface(
                pack = pack,
                colorSet = colorSet,
                semanticGold = semantic.gold,
                hasDue = hasDue,
                learnedPct = learnedPct,
                animatedProgress = animatedProgress,
                surfaceOnClick = null,
                onClick = onClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else {
        GridPackCardSurface(
            pack = pack,
            colorSet = colorSet,
            semanticGold = semantic.gold,
            hasDue = hasDue,
            learnedPct = learnedPct,
            animatedProgress = animatedProgress,
            surfaceOnClick = onClick,
            onClick = onClick,
            modifier = modifier.dropShadow(
                shape = PackCardTokens.Shape,
                shadow = PackCardTokens.Shadow.toShadow(customColor = colorSet.accent)
            ),
        )
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun GridPackCardSurface(
    pack: PackDisplayItem,
    colorSet: CardColorSet,
    semanticGold: Color,
    hasDue: Boolean,
    learnedPct: Int,
    animatedProgress: Float,
    surfaceOnClick: (() -> Unit)?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (surfaceOnClick != null) {
        Card(
            onClick  = surfaceOnClick,
            modifier = modifier,
            shape    = PackCardTokens.Shape,
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            GridPackCardContent(
                pack = pack,
                colorSet = colorSet,
                semanticGold = semanticGold,
                hasDue = hasDue,
                learnedPct = learnedPct,
                animatedProgress = animatedProgress,
                onClick = onClick,
            )
        }
    } else {
        Surface(
            modifier = modifier,
            shape    = PackCardTokens.Shape,
            color    = MaterialTheme.colorScheme.surface,
        ) {
            GridPackCardContent(
                pack = pack,
                colorSet = colorSet,
                semanticGold = semanticGold,
                hasDue = hasDue,
                learnedPct = learnedPct,
                animatedProgress = animatedProgress,
                onClick = onClick,
            )
        }
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
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(
                start  = Spacing.Spacing12,
                end    = Spacing.Spacing12,
                top    = Spacing.Spacing14,
                bottom = Spacing.Spacing12,
            ),
    ) {
        // ── Row 1: Icon + Category pill ───────────────────────────────
        IconAndCategoryRow(
            emoji    = pack.emoji,
            category = pack.category,
            colorSet = colorSet,
        )

        Spacer(Modifier.height(Spacing.Spacing12))

        // ── Title ─────────────────────────────────────────────────────
        Text(
            text       = pack.title,
            fontSize   = 13.asp,
            fontWeight = FontWeight.Bold,
            lineHeight = 17.5.asp,
            color      = MaterialTheme.colorScheme.onSurface,
            maxLines   = 2,
            minLines   = 2,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.padding(bottom = 10.adp),
        )

        // ── Progress bar ──────────────────────────────────────────────
        ProgressBar(
            progress = animatedProgress,
            accent   = colorSet.accent,
        )

        Spacer(Modifier.height(Spacing.Spacing6))

        // ── Learned / total + pct ─────────────────────────────────────
        LearnedRow(
            mastered = pack.masteredCards,
            total    = pack.totalCards,
            pct      = learnedPct,
            accent   = colorSet.accent,
        )

        Spacer(Modifier.height(Spacing.Spacing4))

        // ── Last studied + Due badge / streak ─────────────────────────
        DueAndLastStudiedRow(
            lastStudied = pack.lastStudiedLabel,
            dueCards    = pack.cardsToReview,
            streakDays  = pack.streakDays,
            goldColor   = semanticGold,
            hasDue      = hasDue,
        )

        Spacer(Modifier.height(Spacing.Spacing4))

        // ── Continue CTA ──────────────────────────────────────────────
        ContinueButton(
            hasDue   = hasDue,
            colorSet = colorSet,
            onClick  = onClick,
        )
    }
}

@Composable
private fun IconAndCategoryRow(
    emoji:    String,
    category: String,
    colorSet: CardColorSet,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top,
    ) {
        // Emoji icon container
        Box(
            modifier         = Modifier
                .size(38.adp)
                .clip(Radius.ShapeMedium)
                .background(colorSet.bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 18.asp)
        }

        // Category pill — uppercase, tiny bold
        Box(
            modifier = Modifier
                .clip(Radius.ShapePill)
                .background(colorSet.bg)
                .padding(horizontal = Spacing.Spacing6),
        ) {
            Text(
                text          = category.uppercase(),
                fontSize      = 10.asp,
                fontWeight    = FontWeight.ExtraBold,
                color      = colorSet.accent,
                letterSpacing = 0.06.asp,
            )
        }
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    accent:   Color,
    modifier: Modifier = Modifier,
) {
    val fraction = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.adp)
            .clip(Radius.ShapePill)
            .background(accent.copy(alpha = 0.13f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .dropShadow(
                    shape  = Radius.ShapePill,
                    shadow = Shadow(radius = 8.dp, color = accent, alpha = 0.55f),
                )
                .clip(Radius.ShapePill)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(accent.copy(alpha = 0.7f), accent),
                    )
                ),
        )
    }
}
@Composable
private fun LearnedRow(
    mastered: Int,
    total:    Int,
    pct:      Int,
    accent:   Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = stringResource(R.string.grid_card_learned, mastered, total),
            fontSize   = 10.asp,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text       = stringResource(R.string.grid_card_pct, pct),
            fontSize   = 10.asp,
            fontWeight = FontWeight.Bold,
            color      = accent,
        )
    }
}

@Composable
private fun DueAndLastStudiedRow(
    lastStudied: LastStudiedLabel,
    dueCards:    Int,
    streakDays:  Int,
    goldColor:   Color,
    hasDue:      Boolean,
    modifier:    Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        // Clock + last studied
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing4),
        ) {
            Icon(
                painter           = painterResource(R.drawable.ic_clock),
                contentDescription = null,
                tint              = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier          = Modifier.size(9.adp),
            )
            Text(
                text     = lastStudied.displayString(),
                fontSize = 9.asp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing4),
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
    dueCards:  Int,
    goldColor: Color,
    modifier:  Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(Radius.ShapePill)
            .background(goldColor.copy(alpha = 0.14f))
            .padding(horizontal = Spacing.Spacing6),
    ) {
        Text(
            text       = stringResource(R.string.due_badge, dueCards),
            fontSize   = 9.asp,
            fontWeight = FontWeight.Bold,
            color      = goldColor,
        )
    }
}

@Composable
private fun StreakChip(
    streakDays: Int,
    goldColor:  Color,
    modifier:   Modifier = Modifier,
) {
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing2),
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_zap),
            contentDescription = null,
            tint               = goldColor,
            modifier           = Modifier.size(9.adp),
        )
        Text(
            text       = stringResource(R.string.grid_card_streak, streakDays),
            fontSize   = 9.asp,
            fontWeight = FontWeight.Bold,
            color      = goldColor,
        )
    }
}

@Composable
private fun ContinueButton(
    hasDue:   Boolean,
    colorSet: CardColorSet,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctaGradient = MaterialTheme.synapse.gradients.cta

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.adp)
            .clip(Radius.ShapeMedium)
            .then(
                if (hasDue) Modifier.background(ctaGradient)
                else Modifier.border(1.5.dp, colorSet.accent.copy(alpha = 0.33f), Radius.ShapeMedium)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                enabled           = hasDue,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing4),
        ) {
            Text(
                text       = stringResource(R.string.grid_card_continue),
                fontSize   = 12.asp,
                fontWeight = FontWeight.Bold,
                color      = if (hasDue) Color.White else colorSet.accent,
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint               = if (hasDue) Color.White else colorSet.accent,
                modifier           = Modifier.size(11.adp),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Light — Due", showBackground = true)
@Preview(name = "Dark — Due", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GridPackCardDuePreview() {
    SynapseTheme {
        GridPackCard(
            pack = PackDisplayItem.Mocks.first(),
            animDelayMs = 0,
            onClick = {},
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.48f),
        )
    }
}

@Preview(name = "Light — No Due", showBackground = true)
@Preview(name = "Dark — No Due", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GridPackCardNoDuePreview() {
    SynapseTheme {
        GridPackCard(
            pack = PackDisplayItem.Mocks.last(),
            animDelayMs = 0,
            onClick = {},
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.48f),
        )
    }
}