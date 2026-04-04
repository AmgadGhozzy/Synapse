package com.venom.synapse.features.session.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.synapse.features.session.presentation.screen.previewFlashcardQuestion
import com.venom.synapse.features.session.presentation.screen.previewMcqQuestion
import com.venom.synapse.features.session.presentation.screen.previewTfQuestion
import com.venom.synapse.features.session.presentation.state.QuestionUiContent
import com.venom.synapse.features.session.presentation.state.QuestionUiModel
import com.venom.ui.components.common.adp

@Composable
internal fun McqPanel(
    question: QuestionUiModel,
    content: QuestionUiContent.Mcq,
    isInputEnabled: Boolean,
    lastAnswerCorrect: Boolean?,
    showHint: Boolean,
    explanation: String? = null,
    correctLabel: String? = null,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAnswered = lastAnswerCorrect != null
    val semantic = MaterialTheme.synapse.semantic

    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var optionsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(question.id) {
        selectedIndex = null
        optionsVisible = false
        optionsVisible = true
    }

    Column(modifier = modifier) {
        QuestionCard(
            label = stringResource(R.string.quiz_question_label, question.id),
            chipColor = MaterialTheme.colorScheme.primary,
            text = question.questionText,
            modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s14),
        )

        AnimatedVisibility(
            visible = showHint && !question.hint.isNullOrBlank(),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(160)),
        ) {
            HintReveal(
                hint = question.hint ?: "",
                modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s12),
            )
        }

        content.options.forEachIndexed { index, option ->
            val letter = ('A' + index).toString()
            val isCorrect = index == content.correctIndex
            val isSelected = selectedIndex == index
            val isWrong = isAnswered && isSelected && !isCorrect

            val containerColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                !isAnswered -> MaterialTheme.colorScheme.surface
                isCorrect -> semantic.successContainer
                isWrong -> semantic.errorContainer
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
            val badgeColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                !isAnswered -> MaterialTheme.colorScheme.surfaceVariant
                isCorrect -> semantic.success.copy(alpha = 0.22f)
                isWrong -> semantic.error.copy(alpha = 0.22f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            val badgeTextColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
                !isAnswered -> MaterialTheme.colorScheme.onSurfaceVariant
                isCorrect -> semantic.success
                isWrong -> semantic.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            }
            val optionTextColor = when {
                !isAnswered -> MaterialTheme.colorScheme.onSurface
                isCorrect -> semantic.success
                isWrong -> semantic.error
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            }
            val glowColor = when {
                isAnswered && isCorrect -> semantic.success
                isAnswered && isWrong -> semantic.error
                else -> null
            }

            AnimatedVisibility(
                visible = optionsVisible,
                enter = fadeIn(tween(durationMillis = 180, delayMillis = index * 50)),
            ) {
                McqOptionRow(
                    letter = letter,
                    text = option,
                    containerColor = containerColor,
                    badgeColor = badgeColor,
                    badgeTextColor = badgeTextColor,
                    optionTextColor = optionTextColor,
                    glowColor = glowColor,
                    isCorrect = isAnswered && isCorrect,
                    isWrong = isWrong,
                    enabled = isInputEnabled && !isAnswered,
                    onClick = {
                        selectedIndex = index
                        onOptionSelected(index)
                    },
                    modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s10),
                )
            }
        }
    }
}

@Composable
private fun McqOptionRow(
    letter: String,
    text: String,
    containerColor: Color,
    badgeColor: Color,
    badgeTextColor: Color,
    optionTextColor: Color,
    glowColor: Color?,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium

    val rowModifier = if (glowColor != null) {
        modifier.dropShadow(
            shape = shape,
            shadow = MaterialTheme.synapse.shadows.subtle.toShadow(customColor = glowColor)
        )
    } else modifier

    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = MaterialTheme.synapse.spacing.s18,
                vertical = MaterialTheme.synapse.spacing.s16
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s14),
    ) {
        val typo = MaterialTheme.typography
        val semantic = MaterialTheme.synapse.semantic
        Box(
            modifier = Modifier
                .size(36.adp)
                .clip(MaterialTheme.shapes.small)
                .background(badgeColor),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isCorrect -> Text(
                    text = "✓",
                    style = typo.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = semantic.success,
                )

                isWrong -> Text(
                    text = "✗",
                    style = typo.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = semantic.error,
                )

                else -> Text(
                    text = letter,
                    style = typo.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor,
                )
            }
        }

        Text(
            text = text,
            style = typo.bodyLarge,
            color = optionTextColor,
            fontWeight = if (isCorrect || isWrong) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )

        AnimatedVisibility(
            visible = isCorrect,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120)),
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(semantic.success.copy(alpha = 0.18f))
                    .padding(horizontal = MaterialTheme.synapse.spacing.s8)
                    .padding(vertical = MaterialTheme.synapse.spacing.s2),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.quiz_correct_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = semantic.success,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun TrueFalsePanel(
    question: QuestionUiModel,
    content: QuestionUiContent.TrueFalse,
    isInputEnabled: Boolean,
    lastAnswerCorrect: Boolean?,
    showHint: Boolean,
    explanation: String? = null,
    correctLabel: String? = null,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAnswered = lastAnswerCorrect != null
    var selectedAnswer by rememberSaveable { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(question.id) { selectedAnswer = null }

    Column(modifier = modifier) {
        QuestionCard(
            label = stringResource(R.string.quiz_statement_label),
            chipColor = MaterialTheme.colorScheme.secondary,
            text = question.questionText,
            modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s14),
        )

        AnimatedVisibility(
            visible = showHint && !question.hint.isNullOrBlank(),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(160)),
        ) {
            HintReveal(
                hint = question.hint ?: "",
                modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s12),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
        ) {
            TfButton(
                isTrue = true,
                isSelected = selectedAnswer == true,
                isAnswered = isAnswered,
                correctAnswer = content.correctAnswer,
                enabled = isInputEnabled && !isAnswered,
                onClick = { selectedAnswer = true; onAnswer(true) },
                modifier = Modifier.weight(1f),
            )
            TfButton(
                isTrue = false,
                isSelected = selectedAnswer == false,
                isAnswered = isAnswered,
                correctAnswer = content.correctAnswer,
                enabled = isInputEnabled && !isAnswered,
                onClick = { selectedAnswer = false; onAnswer(false) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TfButton(
    isTrue: Boolean,
    isSelected: Boolean,
    isAnswered: Boolean,
    correctAnswer: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography
    val semantic = MaterialTheme.synapse.semantic
    val isThisCorrect = isTrue == correctAnswer
    val shape = MaterialTheme.shapes.large

    val containerColor = when {
        !isAnswered && isSelected && isTrue -> semantic.successContainer
        !isAnswered && isSelected && !isTrue -> semantic.errorContainer
        !isAnswered -> MaterialTheme.colorScheme.surface
        isThisCorrect -> semantic.successContainer
        isSelected -> semantic.errorContainer
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
    }
    val borderColor = when {
        !isAnswered && isSelected && isTrue -> semantic.success.copy(alpha = 0.70f)
        !isAnswered && isSelected && !isTrue -> semantic.error.copy(alpha = 0.60f)
        !isAnswered -> MaterialTheme.colorScheme.outlineVariant
        isThisCorrect -> semantic.success.copy(alpha = 0.70f)
        isSelected -> semantic.error.copy(alpha = 0.50f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
    }
    val iconTint = when {
        !isAnswered && isSelected && isTrue -> semantic.success
        !isAnswered && isSelected && !isTrue -> semantic.error
        !isAnswered && isTrue -> MaterialTheme.colorScheme.primary
        !isAnswered -> MaterialTheme.colorScheme.error
        isThisCorrect -> semantic.success
        isSelected -> semantic.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
    }
    val glowColor = when {
        isAnswered && isThisCorrect -> semantic.success
        isAnswered && isSelected -> semantic.error
        else -> null
    }

    val iconRes = if (isTrue) R.drawable.ic_check else R.drawable.ic_x
    val label = stringResource(if (isTrue) R.string.quiz_true_label else R.string.quiz_false_label)

    val btnModifier = if (glowColor != null) {
        modifier.dropShadow(
            shape = shape,
            shadow = MaterialTheme.synapse.shadows.subtle.toShadow(customColor = glowColor)
        )
    } else modifier

    Column(
        modifier = btnModifier
            .clip(shape)
            .background(containerColor)
            .border(if (isAnswered && isThisCorrect) 2.adp else 1.5.adp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 28.adp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(32.adp),
        )
        Text(
            text = label,
            style = typo.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = iconTint,
        )
    }
}

@Composable
internal fun FlashcardPanel(
    content: QuestionUiContent.Flashcard,
    isAnswered: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SwipeableFlashcard(
        content = content,
        isAnswered = isAnswered,
        onFlip = onFlip,
        modifier = modifier,
    )
}

@Composable
internal fun QuestionCard(
    label: String,
    text: String,
    chipColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.typography
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow(
                shape = shape,
                shadow = MaterialTheme.synapse.shadows.medium.toShadow()
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(MaterialTheme.synapse.spacing.s28),
        ) {
            Column {
                QuestionTypeChip(label = label, chipColor = chipColor)
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s16))
                Text(
                    text = text,
                    style = typo.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview(name = "MCQ Panel — Light", showBackground = true)
@Preview(name = "MCQ Panel — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun McqPanelPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            McqPanel(
                question = previewMcqQuestion,
                content = previewMcqQuestion.content as QuestionUiContent.Mcq,
                isInputEnabled = true,
                lastAnswerCorrect = null,
                showHint = false,
                onOptionSelected = {},
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            )
        }
    }
}

@Preview(name = "TF Panel — Light", showBackground = true)
@Preview(name = "TF Panel — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TrueFalsePanelPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TrueFalsePanel(
                question = previewTfQuestion,
                content = previewTfQuestion.content as QuestionUiContent.TrueFalse,
                isInputEnabled = true,
                lastAnswerCorrect = null,
                showHint = false,
                onAnswer = {},
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            )
        }
    }
}

@Preview(name = "Flashcard Panel — Light", showBackground = true)
@Preview(
    name = "Flashcard Panel — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun FlashcardPanelPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.synapse.spacing.s20),
                contentAlignment = Alignment.Center,
            ) {
                FlashcardPanel(
                    content = previewFlashcardQuestion.content as QuestionUiContent.Flashcard,
                    isAnswered = false,
                    onFlip = {},
                )
            }
        }
    }
}