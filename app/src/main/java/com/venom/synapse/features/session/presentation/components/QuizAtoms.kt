package com.venom.synapse.features.session.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.dropShadow
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
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.ui.components.common.adp

@Composable
internal fun HintReveal(
    hint: String,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(semantic.goldContainer)
            .padding(MaterialTheme.synapse.spacing.s16),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_lightbulb),
                contentDescription = null,
                tint = semantic.gold,
                modifier = Modifier
                    .size(24.adp)
                    .padding(top = 2.adp)
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = semantic.gold,
            )
        }
    }
}

@Composable
internal fun LeechAlertBanner(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography
    val error = MaterialTheme.colorScheme.error
    val shape = MaterialTheme.shapes.large

    val infiniteTransition = rememberInfiniteTransition(label = "leech_shake")
    val shake by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        )
    )
    val rotation = when {
        shake < 0.15f -> shake / 0.15f * -10f
        shake < 0.35f -> (shake - 0.15f) / 0.20f * 20f - 10f
        shake < 0.55f -> (shake - 0.35f) / 0.20f * -12f + 10f
        shake < 0.70f -> (shake - 0.55f) / 0.15f * 6f - 2f
        else -> 0f
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = shape,
        color = error.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s14),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
        ) {
            Box(
                modifier = Modifier
                    .size(36.adp)
                    .graphicsLayer { rotationZ = rotation }
                    .clip(MaterialTheme.shapes.small)
                    .background(error.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_alert_triangle),
                    contentDescription = null,
                    tint = error,
                    modifier = Modifier.size(16.adp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.quiz_leech_title),
                    style = typo.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = error,
                )
                Text(
                    text = stringResource(R.string.quiz_leech_body),
                    style = typo.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.adp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = stringResource(R.string.quiz_dismiss),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.adp),
                )
            }
        }
    }
}

@Composable
internal fun AnswerFeedbackCard(
    isCorrect: Boolean,
    explanation: String?,
    correctLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography
    val semantic = MaterialTheme.synapse.semantic
    val shape = MaterialTheme.shapes.medium

    val bgColor  = if (isCorrect) semantic.successContainer else semantic.errorContainer
    val iconTint = if (isCorrect) semantic.success else semantic.error
    val iconRes  = if (isCorrect) R.drawable.ic_check_circle_2 else R.drawable.ic_x_circle
    val title    = stringResource(
        if (isCorrect) R.string.quiz_feedback_correct_title else R.string.quiz_feedback_incorrect_title
    )
    val correctAnswerPrefix = stringResource(R.string.quiz_feedback_correct_answer_prefix)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape  = shape,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(customColor = iconTint),
            ),
        shape = shape,
        color = bgColor,
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s14),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(24.adp)
                    .padding(top = 2.adp)

            )
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s4)) {
                Text(
                    text = title,
                    style = typo.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                )
                if (!isCorrect && !correctLabel.isNullOrBlank()) {
                    Text(
                        text = buildAnnotatedString {
                            append(correctAnswerPrefix)
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(correctLabel) }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!explanation.isNullOrBlank()) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("💡 ") }
                            append(explanation)
                        },
                        style = typo.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun QuestionTypeChip(
    label: String,
    chipColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(chipColor.copy(alpha = 0.12f))
            .padding(horizontal = MaterialTheme.synapse.spacing.s8, vertical = MaterialTheme.synapse.spacing.s4),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = chipColor,
        )
    }
}

@Composable
internal fun QuizLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
internal fun QuizErrorState(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.synapse.spacing.s24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_alert_triangle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.adp),
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s24))
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onBack)
                .padding(horizontal = MaterialTheme.synapse.spacing.s24, vertical = MaterialTheme.synapse.spacing.s12),
        ) {
            Text(
                text = stringResource(R.string.quiz_go_back),
                style = typo.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(name = "Hint Reveal — Light", showBackground = true)
@Preview(
    name = "Hint Reveal — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun HintRevealPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HintReveal(
                hint = "Think about the event that triggered the Roman Civil War.",
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            )
        }
    }
}

@Preview(name = "Leech Alert — Light", showBackground = true)
@Preview(
    name = "Leech Alert — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun LeechAlertPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LeechAlertBanner(
                onDismiss = {},
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            )
        }
    }
}

@Preview(name = "Feedback Correct — Light", showBackground = true)
@Preview(
    name = "Feedback Correct — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun FeedbackCorrectPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AnswerFeedbackCard(
                isCorrect = true,
                explanation = "Caesar crossed the Rubicon in 49 BC, triggering the civil war.",
                correctLabel = null,
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            )
        }
    }
}

@Preview(name = "Feedback Incorrect — Light", showBackground = true)
@Preview(
    name = "Feedback Incorrect — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun FeedbackIncorrectPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AnswerFeedbackCard(
                isCorrect = false,
                explanation = "The correct answer is 49 BC.",
                correctLabel = "Option B",
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            )
        }
    }
}