package com.venom.synapse.features.session.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.synapse.features.session.presentation.screen.previewFlashcardQuestion
import com.venom.synapse.features.session.presentation.state.QuestionUiContent
import com.venom.ui.components.common.adp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

private const val SWIPE_THRESHOLD_DP = 150
private const val MAX_ROTATION_DEG = 15f
private const val THROW_TARGET_BASE = 1400f
private const val THROW_TARGET_Y = -80f
private const val PROGRESS_THRESHOLD = 0.30f

@Composable
internal fun SwipeableFlashcard(
    content: QuestionUiContent.Flashcard,
    isAnswered: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val swipeRotation = remember { Animatable(0f) }
    val flipAngle = remember { Animatable(0f) }

    val swipeThresholdPx = remember(density) {
        with(density) { SWIPE_THRESHOLD_DP.dp.toPx() }
    }

    LaunchedEffect(isAnswered) {
        if (isAnswered && flipAngle.value < 90f) {
            flipAngle.animateTo(180f, tween(420))
        } else if (!isAnswered) {
            flipAngle.snapTo(0f)
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
            swipeRotation.snapTo(0f)
        }
    }

    val showFront by remember(flipAngle) {
        derivedStateOf {
            val raw = flipAngle.value % 360f
            val eff = if (raw < 0f) raw + 360f else raw
            eff <= 90f || eff >= 270f
        }
    }

    val (cardWidth, cardHeight) = remember(configuration, density) {
        val screenWPx = with(density) { configuration.screenWidthDp.dp.toPx() }
        val padPx = with(density) { 40.dp.toPx() }
        val limitPx = with(density) { 420.dp.toPx() }
        val widthPx = min(screenWPx - padPx, limitPx)
        val widthDp = with(density) { widthPx.toDp() }
        Pair(widthDp, widthDp * 1.48f)
    }

    val semantic = MaterialTheme.synapse.semantic
    val cameraDistance = remember(density) { 12f * density.density }
    val cornerRadius = 28.adp
    val borderCornerPx = remember(cornerRadius, density) { with(density) { cornerRadius.toPx() } }
    val borderThinPx = remember(density) { with(density) { 1.dp.toPx() } }
    val borderThickPx = remember(density) { with(density) { 2.dp.toPx() } }

    fun borderColor(): Color {
        val progress = (offsetX.value / swipeThresholdPx).coerceIn(-1f, 1f)
        val absProgress = abs(progress)
        return when {
            absProgress < PROGRESS_THRESHOLD -> Color(0x33808080)
            progress > 0 -> lerp(
                semantic.success.copy(alpha = 0.30f),
                semantic.success.copy(alpha = 0.70f),
                ((absProgress - PROGRESS_THRESHOLD) / (1f - PROGRESS_THRESHOLD)).coerceIn(0f, 1f),
            )

            else -> lerp(
                semantic.error.copy(alpha = 0.30f),
                semantic.error.copy(alpha = 0.70f),
                ((absProgress - PROGRESS_THRESHOLD) / (1f - PROGRESS_THRESHOLD)).coerceIn(0f, 1f),
            )
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(cardWidth, cardHeight)
                .graphicsLayer {
                    translationX = offsetX.value
                    translationY = offsetY.value
                    rotationZ = if (!showFront) -swipeRotation.value else swipeRotation.value
                    rotationY = flipAngle.value
                    val dist = sqrt(offsetX.value.pow(2) + offsetY.value.pow(2))
                    val s = (1f - dist * 0.0003f).coerceIn(0.80f, 1f)
                    scaleX = s; scaleY = s
                    this.cameraDistance = cameraDistance
                    clip = false
                }
                .drawWithContent {
                    drawContent()
                    val progress = (offsetX.value / swipeThresholdPx).coerceIn(-1f, 1f)
                    val strokeWidth =
                        if (abs(progress) > PROGRESS_THRESHOLD) borderThickPx else borderThinPx
                    drawRoundRect(
                        color = borderColor(),
                        cornerRadius = CornerRadius(borderCornerPx),
                        style = Stroke(width = strokeWidth),
                    )
                }
                .pointerInput(isAnswered) {
                    if (!isAnswered) {
                        detectTapGestures(onTap = { onFlip() })
                    } else {
                        coroutineScope {
                            val scope = this
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    scope.launch {
                                        val backFaceVisible =
                                            flipAngle.value > 90f && flipAngle.value < 270f
                                        val dx =
                                            if (backFaceVisible) -dragAmount.x else dragAmount.x
                                        offsetX.snapTo(offsetX.value + dx)
                                        offsetY.snapTo(offsetY.value + dragAmount.y)
                                        swipeRotation.snapTo(
                                            (offsetX.value / swipeThresholdPx).coerceIn(
                                                -1f,
                                                1f
                                            ) * MAX_ROTATION_DEG
                                        )
                                    }
                                },
                                onDragEnd = {
                                    val currentX = offsetX.value
                                    if (abs(currentX) >= swipeThresholdPx) {
                                        val dir = if (currentX > 0f) 1f else -1f
                                        scope.launch {
                                            coroutineScope {
                                                launch {
                                                    offsetX.animateTo(
                                                        dir * THROW_TARGET_BASE,
                                                        tween(300)
                                                    )
                                                }
                                                launch {
                                                    offsetY.animateTo(
                                                        THROW_TARGET_Y,
                                                        tween(300)
                                                    )
                                                }
                                            }
                                            if (dir > 0f) onSwipeRight() else onSwipeLeft()
                                        }
                                    } else {
                                        scope.launch { offsetX.animateTo(0f, tween(300)) }
                                        scope.launch { offsetY.animateTo(0f, tween(300)) }
                                        scope.launch { swipeRotation.animateTo(0f, tween(300)) }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch { offsetX.animateTo(0f, tween(300)) }
                                    scope.launch { offsetY.animateTo(0f, tween(300)) }
                                    scope.launch { swipeRotation.animateTo(0f, tween(300)) }
                                },
                            )
                        }
                    }
                },
        ) {
            FlashcardFace(
                text = content.front,
                isFront = true,
                visible = showFront,
                cardShape = cornerRadius,
            )
            FlashcardFace(
                text = content.back,
                isFront = false,
                visible = !showFront,
                cardShape = cornerRadius,
            )
        }

        if (isAnswered) {
            FlashcardSwipeIndicators(
                offsetX = offsetX.value,
                swipeThresholdPx = swipeThresholdPx,
                modifier = Modifier
                    .size(cardWidth, cardHeight)
                    .align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun FlashcardFace(
    text: String,
    isFront: Boolean,
    visible: Boolean,
    cardShape: Dp,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary

    val gradient = if (isFront) {
        Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    } else {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.surfaceVariant
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(if (visible) 1f else 0f)
            .graphicsLayer {
                if (!isFront) scaleX = -1f
                this.alpha = if (visible) 1f else 0f
            }
            .clip(RoundedCornerShape(cardShape))
            .dropShadow(
                shape = RoundedCornerShape(cardShape),
                shadow = MaterialTheme.synapse.shadows.strong.toShadow(
                    customColor = primary,
                    offset = if (isFront) DpOffset(0.dp, 8.dp) else DpOffset(0.dp, 10.dp)
                ),
            )
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.synapse.spacing.s24)
                .padding(bottom = if (isFront) 36.adp else 0.adp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                style = if (isFront) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                fontWeight = if (isFront) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        if (isFront) {
            TapToFlipHint(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = MaterialTheme.synapse.spacing.s24),
            )
        }
    }
}

@Composable
private fun TapToFlipHint(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.alpha(0.35f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s4),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_hand_tap),
            contentDescription = null,
            modifier = Modifier.size(16.adp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.quiz_tap_to_flip).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FlashcardSwipeIndicators(
    offsetX: Float,
    swipeThresholdPx: Float,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic

    Box(modifier = modifier) {
        val leftAbs = abs((offsetX / swipeThresholdPx).coerceIn(-1f, 0f))
        if (leftAbs > 0.18f) {
            SwipeIndicatorBubble(
                icon = painterResource(R.drawable.ic_alert_circle),
                tint = semantic.error,
                background = semantic.error,
                alpha = ((leftAbs - 0.18f) / 0.82f).coerceIn(0f, 1f),
                scale = 0.5f + leftAbs * 0.5f,
                label = stringResource(R.string.quiz_srs_hard),
                modifier = Modifier
                    .align(AbsoluteAlignment.CenterLeft)
                    .padding(start = MaterialTheme.synapse.spacing.s16),
            )
        }
        val rightProg = (offsetX / swipeThresholdPx).coerceIn(0f, 1f)
        if (rightProg > 0.18f) {
            SwipeIndicatorBubble(
                icon = painterResource(R.drawable.ic_check),
                tint = semantic.success,
                background = semantic.success,
                alpha = ((rightProg - 0.18f) / 0.82f).coerceIn(0f, 1f),
                scale = 0.5f + rightProg * 0.5f,
                label = stringResource(R.string.quiz_srs_easy),
                modifier = Modifier
                    .align(AbsoluteAlignment.CenterRight)
                    .padding(end = MaterialTheme.synapse.spacing.s16),
            )
        }
    }
}

@Composable
private fun SwipeIndicatorBubble(
    icon: Painter,
    tint: Color,
    background: Color,
    alpha: Float,
    scale: Float,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.scaleX = scale
            this.scaleY = scale
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {
        Box(
            modifier = Modifier
                .size(52.adp)
                .clip(CircleShape)
                .background(background.copy(alpha = 0.90f))
                .padding(12.adp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color.White.copy(0.9f),
                modifier = Modifier.size(28.adp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = tint,
        )
    }
}

@Preview(name = "Flashcard Front — Light", showBackground = true)
@Preview(
    name = "Flashcard Front — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun FlashcardFrontPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.synapse.spacing.s20),
                Alignment.Center
            ) {
                SwipeableFlashcard(
                    content = previewFlashcardQuestion.content as QuestionUiContent.Flashcard,
                    isAnswered = false,
                    onFlip = {},
                )
            }
        }
    }
}

@Preview(name = "Flashcard Back — Light", showBackground = true)
@Preview(
    name = "Flashcard Back — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun FlashcardBackPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.synapse.spacing.s20),
                Alignment.Center
            ) {
                SwipeableFlashcard(
                    content = previewFlashcardQuestion.content as QuestionUiContent.Flashcard,
                    isAnswered = true,
                    onFlip = {},
                )
            }
        }
    }
}