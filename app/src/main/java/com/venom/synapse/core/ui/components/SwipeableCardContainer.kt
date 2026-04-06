package com.venom.synapse.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.util.lerp
import com.venom.synapse.core.theme.synapse
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp
import kotlin.math.roundToInt

@Composable
fun SwipeableCardContainer(
    actions: List<SwipeAction>,
    isSwiped: Boolean,
    onSwipeOpen: () -> Unit,
    onSwipeClose: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    verticalActions: Boolean = false,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val haptic  = LocalHapticFeedback.current

    val actionWidth    = 72.adp
    val panelPadding   = MaterialTheme.synapse.spacing.s4
    // Panel width = action slots + gaps between them + outer padding
    val revealWidthDp: Dp = if (verticalActions) {
        actionWidth + panelPadding
    } else {
        actionWidth * actions.size + panelPadding * (actions.size + 1)
    }
    val revealWidthPx = with(density) { revealWidthDp.toPx() }

    val dragState = remember(revealWidthPx) {
        AnchoredDraggableState(initialValue = DragValue.Closed).also { s ->
            s.updateAnchors(DraggableAnchors {
                DragValue.Closed at 0f
                DragValue.Open   at -revealWidthPx
            })
        }
    }

    LaunchedEffect(isSwiped) {
        val target = if (isSwiped) DragValue.Open else DragValue.Closed
        if (dragState.currentValue != target) dragState.animateTo(target)
    }

    LaunchedEffect(dragState.settledValue) {
        when (dragState.settledValue) {
            DragValue.Open   -> { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onSwipeOpen() }
            DragValue.Closed -> onSwipeClose()
        }
    }

    val progress by remember {
        derivedStateOf {
            val offset = if (dragState.offset.isNaN()) 0f else dragState.offset
            (-offset / revealWidthPx).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.synapse.radius.xl),
    ) {
        SwipeActionsPanel(
            actions    = actions,
            progress   = progress,
            isVertical = verticalActions,
            modifier   = Modifier
                .align(Alignment.CenterEnd)
                .width(revealWidthDp)
                .fillMaxHeight(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(dragState.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(
                    state         = dragState,
                    orientation   = Orientation.Horizontal,
                    flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                        state               = dragState,
                        positionalThreshold = { total -> total * 0.3f },
                        animationSpec       = spring(
                            stiffness    = Spring.StiffnessMedium,
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                        ),
                    ),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                ) {
                    if (dragState.currentValue == DragValue.Open) onSwipeClose() else onTap()
                },
        ) {
            content()
        }
    }
}

// ── Actions panel ─────────────────────────────────────────────────────────────
@Composable
fun SwipeActionsPanel(
    actions:    List<SwipeAction>,
    progress:   Float,
    isVertical: Boolean = false,
    modifier:   Modifier = Modifier,
) {
    val haptic      = LocalHapticFeedback.current
    val staggerStep = if (actions.size > 1) 0.30f / (actions.size - 1) else 0f

    // alpha/scale per action index
    fun actionGraphics(index: Int): Pair<Float, Float> {
        val startAt = index * staggerStep
        val alpha   = lerp(0f, 1f, ((progress - startAt) / 0.30f).coerceIn(0f, 1f))
        val scale   = lerp(0.55f, 1f, ((progress - startAt) / 0.35f).coerceIn(0f, 1f))
        return alpha to scale
    }

    val spacing  = MaterialTheme.synapse.spacing.s4
    val paddings = Modifier.padding(
        start  = spacing,
        end    = spacing,
        top    = if (isVertical) spacing else 0.adp,
        bottom = if (isVertical) spacing else 0.adp,
    )

    if (isVertical) {
        Column(
            modifier            = modifier.then(paddings),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            actions.forEachIndexed { index, action ->
                val (alpha, scale) = actionGraphics(index)
                ActionButton(
                    action   = action,
                    alpha    = alpha,
                    scale    = scale,
                    onClick  = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); action.onClick() },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }
    } else {
        Row(
            modifier             = modifier.then(paddings),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            actions.forEachIndexed { index, action ->
                val (alpha, scale) = actionGraphics(index)
                ActionButton(
                    action   = action,
                    alpha    = alpha,
                    scale    = scale,
                    onClick  = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); action.onClick() },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

// ── Action button ─────────────────────────────────────────────────────────────
@Composable
fun ActionButton(
    action:   SwipeAction,
    alpha:    Float,
    scale:    Float,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha      = alpha
                scaleX          = scale
                scaleY          = scale
                transformOrigin = TransformOrigin(1f, 0.5f)
            }
            .background(action.color, MaterialTheme.synapse.radius.md)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier            = Modifier.padding(vertical = MaterialTheme.synapse.spacing.s8, horizontal = MaterialTheme.synapse.spacing.s4),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter            = painterResource(action.iconRes),
                contentDescription = stringResource(action.labelRes),
                tint               = Color.White.copy(0.9f),
                modifier           = Modifier.size(18.adp),
            )
            Spacer(Modifier.height(MaterialTheme.synapse.spacing.s4))
            Text(
                text       = stringResource(action.labelRes).uppercase(),
                style      = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.asp,
                    fontWeight = FontWeight.Bold,
                ),
                color      = Color.White.copy(0.9f),
            )
        }
    }
}