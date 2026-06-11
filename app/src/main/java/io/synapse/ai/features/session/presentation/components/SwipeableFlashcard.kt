package io.synapse.ai.features.session.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.asp
import io.synapse.ai.core.tts.TtsState
import io.synapse.ai.core.ui.components.TtsButton
import io.synapse.ai.features.session.presentation.screen.previewFlashcardQuestion
import io.synapse.ai.features.session.presentation.state.QuestionUiContent
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

// Flip animation phases
private const val BOUNCE_BACK_SCALE = 0.92f
private const val BOUNCE_BACK_DURATION = 120
private const val FLIP_SPIN_DURATION = 380

@Composable
internal fun SwipeableFlashcard(
    content: QuestionUiContent.Flashcard,
    isAnswered: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier,
    ttsState: TtsState = TtsState.Idle,
    onSpeak: ((String) -> Unit)? = null,
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val swipeRotation = remember { Animatable(0f) }
    val flipAngle = remember { Animatable(0f) }
    // Extra animatables for the enhanced flip feel
    val flipScale = remember { Animatable(1f) }
    val flipTranslationZ = remember { Animatable(0f) }

    val swipeThresholdPx = remember(density) { with(density) { SWIPE_THRESHOLD_DP.dp.toPx() } }
    val buttonAreaSizePx = with(density) { 80.adp.toPx() }

    // Enhanced flip: bounce back → spin → spring overshoot landing
    suspend fun animateFlip(targetAngle: Float) {
        coroutineScope {
            // Phase 1: quick bounce-back scale + slight push away
            launch { flipScale.animateTo(BOUNCE_BACK_SCALE, tween(BOUNCE_BACK_DURATION)) }
            launch { flipTranslationZ.animateTo(-20f, tween(BOUNCE_BACK_DURATION)) }
        }
        coroutineScope {
            // Phase 2: spin to target with momentum
            launch {
                flipAngle.animateTo(
                    targetAngle,
                    tween(FLIP_SPIN_DURATION)
                )
            }
            // Scale back up mid-spin
            launch { flipScale.animateTo(1f, tween(FLIP_SPIN_DURATION / 2)) }
            launch { flipTranslationZ.animateTo(0f, tween(FLIP_SPIN_DURATION / 2)) }
        }
        // Phase 3: spring overshoot on landing — the "pat" sensation
        coroutineScope {
            launch {
                flipScale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    LaunchedEffect(isAnswered) {
        if (isAnswered && flipAngle.value < 90f) {
            animateFlip(180f)
        } else if (!isAnswered) {
            flipAngle.snapTo(0f)
            flipScale.snapTo(1f)
            flipTranslationZ.snapTo(0f)
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
            absProgress < PROGRESS_THRESHOLD -> Color(0x22808080)
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
                    translationY = offsetY.value + flipTranslationZ.value
                    rotationZ = if (!showFront) -swipeRotation.value else swipeRotation.value
                    rotationY = flipAngle.value
                    val dist = sqrt(offsetX.value.pow(2) + offsetY.value.pow(2))
                    val swipeScale = (1f - dist * 0.0003f).coerceIn(0.80f, 1f)
                    scaleX = swipeScale * flipScale.value
                    scaleY = swipeScale * flipScale.value
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
                    detectTapGestures(onTap = { offset ->
                        val isInsideTtsButton = offset.x >= (size.width - buttonAreaSizePx) && offset.y <= buttonAreaSizePx

                        if (!isInsideTtsButton) {
                            if (!isAnswered) {
                                onFlip()
                            } else {
                                scope.launch {
                                    if (flipAngle.value > 90f) {
                                        animateFlip(0f)
                                    } else {
                                        animateFlip(180f)
                                    }
                                }
                            }
                        }
                    })
                }
                .pointerInput(isAnswered) {
                    if (isAnswered) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    val backFaceVisible =
                                        flipAngle.value > 90f && flipAngle.value < 270f
                                    val dx = if (backFaceVisible) -dragAmount.x else dragAmount.x
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
                                            launch { offsetY.animateTo(THROW_TARGET_Y, tween(300)) }
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
                .semantics {
                    customActions = listOf(
                        CustomAccessibilityAction("Flip card") {
                            if (!isAnswered) {
                                onFlip()
                            } else {
                                scope.launch {
                                    if (flipAngle.value > 90f) animateFlip(0f) else animateFlip(180f)
                                }
                            }
                            true
                        }
                    )
                },
        ) {
            FlashcardFace(
                text = content.front,
                isFront = true,
                visible = showFront,
                cardShape = cornerRadius,
                ttsState = ttsState,
                onSpeak = onSpeak,
            )
            FlashcardFace(
                text = content.back,
                isFront = false,
                visible = !showFront,
                cardShape = cornerRadius,
                ttsState = ttsState,
                onSpeak = onSpeak,
            )
        }

        if (isAnswered) {
            FlashcardSwipeIndicators(
                offsetXProvider = { offsetX.value },
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
    ttsState: TtsState = TtsState.Idle,
    onSpeak: ((String) -> Unit)? = null,
) {
    val backgroundModifier = if (isFront) {
        Modifier.background(
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        )
    } else {
        Modifier.background(
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
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
            .then(backgroundModifier),
    ) {
        DecorativeCircles(isFront = isFront)

        if (onSpeak != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.adp, end = 16.adp)
            ) {
                TtsButton(
                    text = text,
                    ttsState = ttsState,
                    onSpeak = onSpeak,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.adp, vertical = 20.adp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Badge pill at top
            CardBadge(isFront = isFront)

            Spacer(modifier = Modifier.weight(1f))

            // Main text centered
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = if (isFront) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
                    lineHeight = 38.asp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom action hint
            BottomHint(isFront = isFront)
        }
    }
}

@Composable
private fun DecorativeCircles(isFront: Boolean) {
    val circleColor = if (isFront) MaterialTheme.colorScheme.onSurface.copy(0.05f) else
        MaterialTheme.colorScheme.onBackground.copy(0.05f)

    // Top-right circle
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size(80.adp)
                .offset(x = 20.adp, y = (-20).adp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(circleColor)
        )
        // Bottom-left circle
        Box(
            modifier = Modifier
                .size(64.adp)
                .offset(x = (-16).adp, y = 16.adp)
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .background(circleColor)
        )
    }
}

@Composable
private fun CardBadge(isFront: Boolean) {
    val badgeBackground = if (isFront) MaterialTheme.colorScheme.onSurface.copy(0.05f) else
        MaterialTheme.colorScheme.onBackground.copy(0.05f)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(badgeBackground)
            .padding(horizontal = 16.adp, vertical = 8.adp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.adp),
    ) {
        Icon(
            painter = painterResource(if (isFront) R.drawable.ic_help_circle else R.drawable.ic_sparkles),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(14.adp),
        )
        Text(
            text = stringResource(if (isFront) R.string.quiz_question_label else R.string.quiz_answer_label),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.asp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BottomHint(isFront: Boolean) {
    val hintBackground = if (isFront) MaterialTheme.colorScheme.onSurface.copy(0.05f) else
        MaterialTheme.colorScheme.onBackground.copy(0.05f)
    val iconRes = if (isFront) R.drawable.ic_hand_tap else R.drawable.ic_check_circle_2
    val textRes = if (isFront) R.string.quiz_tap_to_flip else R.string.quiz_did_you_know

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(hintBackground)
            .padding(horizontal = 20.adp, vertical = 10.adp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.adp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.adp),
        )
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FlashcardSwipeIndicators(
    offsetXProvider: () -> Float,
    swipeThresholdPx: Float,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic
    Box(modifier = modifier) {
        SwipeIndicatorBubble(
            icon = painterResource(R.drawable.ic_alert_circle),
            tint = semantic.error,
            background = semantic.error,
            alphaProvider = {
                val leftAbs = abs((offsetXProvider() / swipeThresholdPx).coerceIn(-1f, 0f))
                if (leftAbs > 0.18f) ((leftAbs - 0.18f) / 0.82f).coerceIn(0f, 1f) else 0f
            },
            scaleProvider = {
                val leftAbs = abs((offsetXProvider() / swipeThresholdPx).coerceIn(-1f, 0f))
                0.5f + leftAbs * 0.5f
            },
            label = stringResource(R.string.quiz_srs_hard),
            modifier = Modifier
                .align(AbsoluteAlignment.CenterLeft)
                .padding(start = MaterialTheme.synapse.spacing.s16),
        )
        SwipeIndicatorBubble(
            icon = painterResource(R.drawable.ic_check),
            tint = semantic.success,
            background = semantic.success,
            alphaProvider = {
                val rightProg = (offsetXProvider() / swipeThresholdPx).coerceIn(0f, 1f)
                if (rightProg > 0.18f) ((rightProg - 0.18f) / 0.82f).coerceIn(0f, 1f) else 0f
            },
            scaleProvider = {
                val rightProg = (offsetXProvider() / swipeThresholdPx).coerceIn(0f, 1f)
                0.5f + rightProg * 0.5f
            },
            label = stringResource(R.string.quiz_srs_easy),
            modifier = Modifier
                .align(AbsoluteAlignment.CenterRight)
                .padding(end = MaterialTheme.synapse.spacing.s16),
        )
    }
}

@Composable
private fun SwipeIndicatorBubble(
    icon: Painter,
    tint: Color,
    background: Color,
    alphaProvider: () -> Float,
    scaleProvider: () -> Float,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.graphicsLayer {
            this.alpha = alphaProvider()
            this.scaleX = scaleProvider()
            this.scaleY = scaleProvider()
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {
        Box(
            modifier = Modifier
                .size(56.adp)
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