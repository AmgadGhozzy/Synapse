package com.venom.synapse.features.session.presentation.components

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.Amber400
import com.venom.synapse.core.theme.Amber600
import com.venom.synapse.core.theme.Emerald400
import com.venom.synapse.core.theme.Emerald500
import com.venom.synapse.core.theme.Emerald600
import com.venom.synapse.core.theme.Rose400
import com.venom.synapse.core.theme.Rose500
import com.venom.synapse.core.theme.Rose600
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

// ─────────────────────────────────────────────────────────────────────────────
// HINT REVEAL  (shown above options when user taps "Hint")
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun HintReveal(
    hint    : String,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.synapse.typographyTokens

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(Amber400.copy(alpha = 0.10f))
            .border(1.adp, Amber400.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
            .padding(Spacing.Spacing16),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
            verticalAlignment     = Alignment.Top,
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_lightbulb), // lucide.dev/icons/lightbulb
                contentDescription = null,
                tint               = Amber600,
                modifier           = Modifier.size(15.adp).padding(top = 1.adp),
            )
            Text(
                text       = hint,
                style      = typo.bodySmallRegular,
                color      = Amber600,
                lineHeight = 20.asp,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEECH ALERT BANNER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun LeechAlertBanner(
    onDismiss: () -> Unit,
    modifier : Modifier = Modifier,
) {
    val typo = MaterialTheme.synapse.typographyTokens

    // Shake animation on the icon box
    val infiniteTransition = rememberInfiniteTransition(label = "leech_shake")
    val shake by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shake",
    )
    // Converts 0→1 cycle into a -10→10→-6→0 rotation (damped wiggle)
    val rotation = when {
        shake < 0.15f -> shake / 0.15f * -10f
        shake < 0.35f -> (shake - 0.15f) / 0.20f * 20f - 10f
        shake < 0.55f -> (shake - 0.35f) / 0.20f * -12f + 10f
        shake < 0.70f -> (shake - 0.55f) / 0.15f * 6f  - 2f
        else          -> 0f
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.large,
        color    = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier
                .border(1.5.adp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f), MaterialTheme.shapes.large)
                .padding(Spacing.Spacing14),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
        ) {
            // Shaking alert icon box
            Box(
                modifier = Modifier
                    .size(36.adp)
                    .graphicsLayer { rotationZ = rotation }
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter            = painterResource(R.drawable.ic_alert_triangle), // lucide.dev/icons/alert-triangle
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(16.adp),
                )
            }

            // Text block
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.quiz_leech_title),
                    style      = typo.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.error,
                )
                Text(
                    text  = stringResource(R.string.quiz_leech_body),
                    style = typo.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            // Dismiss
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.adp)) {
                Icon(
                    painter            = painterResource(R.drawable.ic_x), // lucide.dev/icons/x
                    contentDescription = stringResource(R.string.quiz_dismiss),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(14.adp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANSWER FEEDBACK CARD  (MCQ & True/False)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun AnswerFeedbackCard(
    isCorrect   : Boolean,
    explanation : String?,
    correctLabel: String? = null, // e.g. "Option B" or "True" — shown on incorrect
    modifier    : Modifier = Modifier,
) {
    val typo        = MaterialTheme.synapse.typographyTokens
    val bgColor     = if (isCorrect) Emerald400.copy(alpha = 0.10f) else Rose400.copy(alpha = 0.10f)
    val borderColor = if (isCorrect) Emerald500.copy(alpha = 0.40f) else Rose500.copy(alpha = 0.40f)
    val iconTint    = if (isCorrect) Emerald600 else Rose600
    val iconRes     = if (isCorrect) R.drawable.ic_check_circle_2 else R.drawable.ic_x_circle
    // lucide.dev/icons/check-circle-2 | lucide.dev/icons/x-circle
    val title       = if (isCorrect) "Correct! Well done." else "Not quite right."

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = bgColor,
    ) {
        Row(
            modifier = Modifier
                .border(1.adp, borderColor, MaterialTheme.shapes.medium)
                .padding(Spacing.Spacing14),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
            verticalAlignment     = Alignment.Top,
        ) {
            Icon(
                painter            = painterResource(iconRes),
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(18.adp).padding(top = 1.adp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Spacing4)) {
                // Title
                Text(
                    text       = title,
                    style      = typo.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = iconTint,
                )
                // "Correct answer: X" — only on incorrect
                if (!isCorrect && !correctLabel.isNullOrBlank()) {
                    Text(
                        text = buildAnnotatedString {
                            append("Correct answer: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(correctLabel) }
                        },
                        style = typo.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Explanation with 💡 prefix
                if (!explanation.isNullOrBlank()) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("💡 ") }
                            append(explanation)
                        },
                        style      = typo.labelMedium,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.asp,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUESTION TYPE CHIP
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun QuestionTypeChip(
    label     : String,
    chipColor : Color =
        MaterialTheme.colorScheme.primary,
    modifier  : Modifier = Modifier,
) {
    val typo = MaterialTheme.synapse.typographyTokens

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(chipColor.copy(alpha = 0.12f))
            .border(1.adp, chipColor.copy(alpha = 0.28f), MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.Spacing8, vertical = Spacing.Spacing4),
    ) {
        Text(
            text          = label,
            style         = typo.labelXSmall,
            fontWeight    = FontWeight.ExtraBold,
            letterSpacing = 0.8.asp,
            color         = chipColor,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LOADING STATE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun QuizLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ERROR STATE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun QuizErrorState(
    message : String,
    onBack  : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.synapse.typographyTokens

    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(Spacing.Spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter            = painterResource(R.drawable.ic_alert_triangle),
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.error,
            modifier           = Modifier.size(48.adp),
        )
        Spacer(Modifier.height(Spacing.Spacing12))
        Text(
            text      = message,
            style     = typo.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.Spacing24))
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onBack)
                .padding(horizontal = Spacing.Spacing24, vertical = Spacing.Spacing12),
        ) {
            Text(
                text       = stringResource(R.string.quiz_go_back),
                style      = typo.bodySmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Hint Reveal — Light", showBackground = true)
@Preview(name = "Hint Reveal — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun HintRevealPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HintReveal(
                hint     = "Think about the event that triggered the Roman Civil War.",
                modifier = Modifier.padding(Spacing.Spacing16),
            )
        }
    }
}

@Preview(name = "Leech Alert — Light", showBackground = true)
@Preview(name = "Leech Alert — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LeechAlertPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LeechAlertBanner(
                onDismiss = {},
                modifier  = Modifier.padding(Spacing.Spacing16),
            )
        }
    }
}

@Preview(name = "Feedback Correct — Light", showBackground = true)
@Preview(name = "Feedback Correct — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FeedbackCorrectPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AnswerFeedbackCard(
                isCorrect    = true,
                explanation  = "Caesar crossed the Rubicon in 49 BC, triggering the civil war.",
                correctLabel = null,
                modifier     = Modifier.padding(Spacing.Spacing16),
            )
        }
    }
}

@Preview(name = "Feedback Incorrect — Light", showBackground = true)
@Preview(name = "Feedback Incorrect — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FeedbackIncorrectPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AnswerFeedbackCard(
                isCorrect    = false,
                explanation  = "The correct answer is 49 BC. This was a pivotal moment in Roman history.",
                correctLabel = "Option B",
                modifier     = Modifier.padding(Spacing.Spacing16),
            )
        }
    }
}

@Preview(name = "Error State — Light", showBackground = true)
@Preview(name = "Error State — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ErrorStatePreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizErrorState(
                message = "Failed to load session. Please try again.",
                onBack  = {},
            )
        }
    }
}