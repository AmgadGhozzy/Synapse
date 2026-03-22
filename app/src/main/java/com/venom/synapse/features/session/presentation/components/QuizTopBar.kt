package com.venom.synapse.features.session.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.venom.synapse.R
import com.venom.synapse.core.theme.Emerald400
import com.venom.synapse.core.theme.Rose400
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

// ─────────────────────────────────────────────────────────────────────────────
// QUIZ TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun QuizTopBar(
    title         : String,
    questionIndex : Int,   // 1-based
    totalQuestions: Int,
    progress      : Float, // 0f..1f
    accuracy      : Float, // 0f..1f
    showAccuracy  : Boolean,
    onClose       : () -> Unit,
    modifier      : Modifier = Modifier,
) {
    val typo    = MaterialTheme.synapse.typographyTokens
    val primary = MaterialTheme.colorScheme.primary

    // FIX: capture the display mode once at first composition.
    // Previously totalQuestions <= 16 was evaluated on every recomposition.
    // If the SRS engine re-queued wrong answers and totalQuestions grew past 16,
    // the track switched from dots to the percentage bar mid-session.
    // Locking with remember { } ensures the choice is stable for the session lifetime.
    val useDotsProgress = remember { totalQuestions <= 16 }

    Column(modifier = modifier.fillMaxWidth()) {

        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
        ) {
            Text(
                text       = title,
                style      = typo.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                modifier   = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.10f))
                    .border(1.adp, primary.copy(alpha = 0.22f), CircleShape)
                    .padding(horizontal = Spacing.Spacing12, vertical = Spacing.Spacing4),
            ) {
                Text(
                    text          = "$questionIndex / $totalQuestions",
                    style         = typo.labelSmall,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 0.4.asp,
                    color         = primary,
                )
            }

            AnimatedVisibility(
                visible = showAccuracy,
                enter   = scaleIn(tween(220)),
                exit    = scaleOut(tween(160)),
            ) {
                val accInt     = (accuracy * 100).toInt()
                val isGood     = accuracy >= 0.70f
                val badgeColor = if (isGood) Emerald400 else Rose400
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.13f))
                        .border(1.adp, badgeColor.copy(alpha = 0.35f), CircleShape)
                        .padding(horizontal = Spacing.Spacing12, vertical = Spacing.Spacing4),
                ) {
                    Text(
                        text       = "$accInt%",
                        style      = typo.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = badgeColor,
                    )
                }
            }

            Spacer(Modifier.width(Spacing.Spacing4))

            Box(
                modifier = Modifier
                    .size(34.adp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.adp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_x),
                    contentDescription = stringResource(R.string.quiz_close),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(15.adp),
                )
            }
        }

        Spacer(Modifier.height(Spacing.Spacing10))

        if (useDotsProgress) {
            SegmentedDotTrack(
                questionIndex  = questionIndex,
                totalQuestions = totalQuestions,
            )
        } else {
            SmoothProgressBar(
                progress       = progress,
                questionIndex  = questionIndex,
                totalQuestions = totalQuestions,
            )
        }
    }
}

// ── Segmented dot track ───────────────────────────────────────────────────────

@Composable
private fun SegmentedDotTrack(
    questionIndex : Int,
    totalQuestions: Int,
    modifier      : Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.adp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        for (i in 0 until totalQuestions) {
            val isDone   = i < questionIndex - 1
            val isActive = i == questionIndex - 1

            val dotColor  = if (isDone || isActive) primary else primary.copy(alpha = 0.20f)
            val dotAlpha  = if (!isDone && !isActive) 0.32f else 1f
            val dotHeight = if (isActive) 7.adp else 5.adp

            Box(
                modifier = Modifier
                    .weight(if (isActive) 1.6f else 1f)
                    .height(dotHeight)
                    .drawBehind {
                        if (isActive) {
                            drawRoundRect(
                                color        = primary.copy(alpha = 0.22f),
                                cornerRadius = CornerRadius(size.height * 2),
                                size         = size.copy(
                                    width  = size.width  + 6.dp.toPx(),
                                    height = size.height + 6.dp.toPx(),
                                ),
                                topLeft = Offset(-3.dp.toPx(), -3.dp.toPx()),
                            )
                        }
                    }
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = dotAlpha)),
            )
        }
    }
}

// ── Smooth fill bar (>16 questions) ──────────────────────────────────────────

@Composable
private fun SmoothProgressBar(
    progress      : Float,
    questionIndex : Int,
    totalQuestions: Int,
    modifier      : Modifier = Modifier,
) {
    val typo    = MaterialTheme.synapse.typographyTokens
    val primary = MaterialTheme.colorScheme.primary

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label         = "quiz_progress",
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.adp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(7.adp)
                    .clip(CircleShape)
                    .background(primary),
            )
        }
        Row(
            modifier              = Modifier.fillMaxWidth().padding(top = Spacing.Spacing4),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text  = stringResource(R.string.quiz_progress_label),
                style = typo.labelXSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text       = "${((questionIndex - 1).toFloat() / totalQuestions * 100).toInt()}%",
                style      = typo.labelXSmall,
                fontWeight = FontWeight.Bold,
                color      = primary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "TopBar Dots — Light", showBackground = true)
@Preview(name = "TopBar Dots — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizTopBarDotsPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizTopBar(
                title = "Machine Learning Basics", questionIndex = 3, totalQuestions = 10,
                progress = 0.28f, accuracy = 0.85f, showAccuracy = true, onClose = {},
                modifier = Modifier.padding(Spacing.Spacing20),
            )
        }
    }
}

@Preview(name = "TopBar Bar — Light", showBackground = true)
@Preview(name = "TopBar Bar — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizTopBarBarPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizTopBar(
                title = "Advanced Data Structures & Algorithms", questionIndex = 8,
                totalQuestions = 25, progress = 0.28f, accuracy = 0.55f,
                showAccuracy = true, onClose = {},
                modifier = Modifier.padding(Spacing.Spacing20),
            )
        }
    }
}