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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.Emerald400
import com.venom.synapse.core.theme.Emerald500
import com.venom.synapse.core.theme.Emerald600
import com.venom.synapse.core.theme.Rose400
import com.venom.synapse.core.theme.Rose500
import com.venom.synapse.core.theme.Rose600
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.features.session.presentation.screen.previewFlashcardQuestion
import com.venom.synapse.features.session.presentation.screen.previewMcqQuestion
import com.venom.synapse.features.session.presentation.screen.previewTfQuestion
import com.venom.synapse.features.session.presentation.state.QuestionUiContent
import com.venom.synapse.features.session.presentation.state.QuestionUiModel
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp

// ─────────────────────────────────────────────────────────────────────────────
// MCQ PANEL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun McqPanel(
    question         : QuestionUiModel,
    content          : QuestionUiContent.Mcq,
    isInputEnabled   : Boolean,
    lastAnswerCorrect: Boolean?,
    showHint         : Boolean,
    explanation      : String? = null,
    correctLabel     : String? = null,
    onOptionSelected : (Int) -> Unit,
    modifier         : Modifier = Modifier,
) {
    val isAnswered = lastAnswerCorrect != null

    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var optionsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(question.id) {
        selectedIndex  = null
        optionsVisible = false
        optionsVisible = true
    }

    Column(modifier = modifier) {
        QuestionCard(
            label     = stringResource(R.string.quiz_question_label, question.id),
            chipColor = MaterialTheme.colorScheme.primary,
            text      = question.questionText,
            modifier  = Modifier.padding(bottom = Spacing.Spacing14),
        )

        AnimatedVisibility(
            visible = showHint && !question.hint.isNullOrBlank(),
            enter   = fadeIn(tween(200)),
            exit    = fadeOut(tween(160)),
        ) {
            HintReveal(
                hint     = question.hint ?: "",
                modifier = Modifier.padding(bottom = Spacing.Spacing12),
            )
        }

        content.options.forEachIndexed { index, option ->
            val letter     = ('A' + index).toString()
            val isCorrect  = index == content.correctIndex
            val isSelected = selectedIndex == index
            val isWrong    = isAnswered && isSelected && !isCorrect

            val containerColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                !isAnswered              -> MaterialTheme.colorScheme.surface
                isCorrect                -> Emerald400.copy(alpha = 0.12f)
                isWrong                  -> Rose400.copy(alpha = 0.12f)
                else                     -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
            val borderColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                !isAnswered              -> MaterialTheme.colorScheme.outlineVariant
                isCorrect                -> Emerald500.copy(alpha = 0.65f)
                isWrong                  -> Rose500.copy(alpha = 0.55f)
                else                     -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
            }
            val badgeColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                !isAnswered              -> MaterialTheme.colorScheme.surfaceVariant
                isCorrect                -> Emerald500.copy(alpha = 0.22f)
                isWrong                  -> Rose500.copy(alpha = 0.22f)
                else                     -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            val badgeTextColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
                !isAnswered              -> MaterialTheme.colorScheme.onSurfaceVariant
                isCorrect                -> Emerald600
                isWrong                  -> Rose600
                else                     -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            }
            val optionTextColor = when {
                !isAnswered -> MaterialTheme.colorScheme.onSurface
                isCorrect   -> Emerald600
                isWrong     -> Rose600
                else        -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            }

            AnimatedVisibility(
                visible = optionsVisible,
                enter   = fadeIn(tween(durationMillis = 180, delayMillis = index * 50)),
            ) {
                McqOptionRow(
                    letter          = letter,
                    text            = option,
                    containerColor  = containerColor,
                    borderColor     = borderColor,
                    badgeColor      = badgeColor,
                    badgeTextColor  = badgeTextColor,
                    optionTextColor = optionTextColor,
                    isCorrect       = isAnswered && isCorrect,
                    isWrong         = isWrong,
                    enabled         = isInputEnabled && !isAnswered,
                    onClick         = {
                        selectedIndex = index
                        onOptionSelected(index)
                    },
                    modifier = Modifier.padding(bottom = Spacing.Spacing10),
                )
            }
        }
    }
}

@Composable
private fun McqOptionRow(
    letter         : String,
    text           : String,
    containerColor : Color,
    borderColor    : Color,
    badgeColor     : Color,
    badgeTextColor : Color,
    optionTextColor: Color,
    isCorrect      : Boolean,
    isWrong        : Boolean,
    enabled        : Boolean,
    onClick        : () -> Unit,
    modifier       : Modifier = Modifier,
) {
    val typo = MaterialTheme.synapse.typographyTokens

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .border(1.5.adp, borderColor, MaterialTheme.shapes.large)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Spacing.Spacing16, vertical = Spacing.Spacing14),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing14),
    ) {
        Box(
            modifier = Modifier
                .size(34.adp)
                .clip(MaterialTheme.shapes.small)
                .background(badgeColor),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isCorrect -> Icon(
                    painter            = painterResource(R.drawable.ic_check_circle_2),
                    contentDescription = null,
                    tint               = Emerald600,
                    modifier           = Modifier.size(16.adp),
                )
                isWrong -> Icon(
                    painter            = painterResource(R.drawable.ic_x_circle),
                    contentDescription = null,
                    tint               = Rose600,
                    modifier           = Modifier.size(16.adp),
                )
                else -> Text(
                    text       = letter,
                    style      = typo.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color      = badgeTextColor,
                )
            }
        }

        Text(
            text       = text,
            style      = typo.bodyMedium,
            color      = optionTextColor,
            fontWeight = if (isCorrect || isWrong) FontWeight.SemiBold else FontWeight.Normal,
            modifier   = Modifier.weight(1f),
        )

        AnimatedVisibility(
            visible = isCorrect,
            enter   = fadeIn(tween(180)),
            exit    = fadeOut(tween(120)),
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Emerald500.copy(alpha = 0.18f))
                    .padding(horizontal = Spacing.Spacing8, vertical = 2.adp),
            ) {
                Text(
                    text       = stringResource(R.string.quiz_correct_label),
                    style      = MaterialTheme.synapse.typographyTokens.labelMicro,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Emerald600,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TRUE / FALSE PANEL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TrueFalsePanel(
    question         : QuestionUiModel,
    content          : QuestionUiContent.TrueFalse,
    isInputEnabled   : Boolean,
    lastAnswerCorrect: Boolean?,
    showHint         : Boolean,
    explanation      : String? = null,
    correctLabel     : String? = null,
    onAnswer         : (Boolean) -> Unit,
    modifier         : Modifier = Modifier,
) {
    val isAnswered = lastAnswerCorrect != null

    var selectedAnswer by rememberSaveable { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(question.id) { selectedAnswer = null }

    Column(modifier = modifier) {
        QuestionCard(
            label     = stringResource(R.string.quiz_statement_label),
            chipColor = MaterialTheme.colorScheme.secondary,
            text      = question.questionText,
            modifier  = Modifier.padding(bottom = Spacing.Spacing14),
        )

        AnimatedVisibility(
            visible = showHint && !question.hint.isNullOrBlank(),
            enter   = fadeIn(tween(200)),
            exit    = fadeOut(tween(160)),
        ) {
            HintReveal(
                hint     = question.hint ?: "",
                modifier = Modifier.padding(bottom = Spacing.Spacing12),
            )
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
        ) {
            TfButton(
                isTrue        = true,
                isSelected    = selectedAnswer == true,
                isAnswered    = isAnswered,
                correctAnswer = content.correctAnswer,
                enabled       = isInputEnabled && !isAnswered,
                onClick       = { selectedAnswer = true; onAnswer(true) },
                modifier      = Modifier.weight(1f),
            )
            TfButton(
                isTrue        = false,
                isSelected    = selectedAnswer == false,
                isAnswered    = isAnswered,
                correctAnswer = content.correctAnswer,
                enabled       = isInputEnabled && !isAnswered,
                onClick       = { selectedAnswer = false; onAnswer(false) },
                modifier      = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TfButton(
    isTrue       : Boolean,
    isSelected   : Boolean,
    isAnswered   : Boolean,
    correctAnswer: Boolean,
    enabled      : Boolean,
    onClick      : () -> Unit,
    modifier     : Modifier = Modifier,
) {
    val typo          = MaterialTheme.synapse.typographyTokens
    val isThisCorrect = isTrue == correctAnswer

    val containerColor = when {
        !isAnswered && isSelected && isTrue  -> Emerald400.copy(alpha = 0.14f)
        !isAnswered && isSelected && !isTrue -> Rose400.copy(alpha = 0.12f)
        !isAnswered                          -> MaterialTheme.colorScheme.surface
        isThisCorrect                        -> Emerald400.copy(alpha = 0.16f)
        isSelected                           -> Rose400.copy(alpha = 0.12f)
        else                                 -> MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
    }
    val borderWidth = if (isAnswered && isThisCorrect) 2.adp else 1.5.adp
    val borderColor = when {
        !isAnswered && isSelected && isTrue  -> Emerald500.copy(alpha = 0.70f)
        !isAnswered && isSelected && !isTrue -> Rose500.copy(alpha = 0.60f)
        !isAnswered                          -> MaterialTheme.colorScheme.outlineVariant
        isThisCorrect                        -> Emerald500.copy(alpha = 0.70f)
        isSelected                           -> Rose500.copy(alpha = 0.50f)
        else                                 -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
    }
    val iconTint = when {
        !isAnswered && isSelected && isTrue  -> Emerald600
        !isAnswered && isSelected && !isTrue -> Rose600
        !isAnswered && isTrue                -> MaterialTheme.colorScheme.primary
        !isAnswered                          -> MaterialTheme.colorScheme.error
        isThisCorrect                        -> Emerald600
        isSelected                           -> Rose600
        else                                 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
    }

    val iconRes = if (isTrue) R.drawable.ic_check else R.drawable.ic_x
    val label   = stringResource(if (isTrue) R.string.quiz_true_label else R.string.quiz_false_label)

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .border(borderWidth, borderColor, MaterialTheme.shapes.large)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 28.adp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Spacing10),
    ) {
        Icon(
            painter            = painterResource(iconRes),
            contentDescription = label,
            tint               = iconTint,
            modifier           = Modifier.size(32.adp),
        )
        Text(
            text          = label,
            style         = typo.bodyMedium,
            fontWeight    = FontWeight.ExtraBold,
            letterSpacing = 0.8.asp,
            color         = iconTint,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FLASHCARD PANEL
// ─────────────────────────────────────────────────────────────────────────────
// Delegates entirely to SessionSwipeableFlashcard — no local flip state.
// The parent (QuizScreen) manages isAnswered and passes onFlip + onSrsRating.

@Composable
internal fun FlashcardPanel(
    content    : QuestionUiContent.Flashcard,
    isAnswered : Boolean,
    onFlip     : () -> Unit,
    modifier   : Modifier = Modifier,
) {
    SessionSwipeableFlashcard(
        content     = content,
        isAnswered  = isAnswered,
        onFlip      = onFlip,
        modifier    = modifier,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED — Question card (gradient surface, chip + question text)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun QuestionCard(
    label    : String,
    text     : String,
    chipColor: Color = MaterialTheme.colorScheme.primary,
    modifier : Modifier = Modifier,
) {
    val typo     = MaterialTheme.synapse.typographyTokens
    val gradient = Brush.linearGradient(
        colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface),
        start  = Offset(0f, 0f),
        end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(gradient)
            .border(
                width = 1.adp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                shape = MaterialTheme.shapes.large,
            )
            .padding(Spacing.Spacing20),
    ) {
        Column {
            QuestionTypeChip(label = label, chipColor = chipColor)
            Spacer(Modifier.height(Spacing.Spacing10))
            Text(
                text       = text,
                style      = typo.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                lineHeight = 26.asp,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "MCQ Panel — Light", showBackground = true)
@Preview(name = "MCQ Panel — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun McqPanelPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            McqPanel(
                question          = previewMcqQuestion,
                content           = previewMcqQuestion.content as QuestionUiContent.Mcq,
                isInputEnabled    = true,
                lastAnswerCorrect = null,
                showHint          = false,
                explanation       = null,
                correctLabel      = null,
                onOptionSelected  = {},
                modifier          = Modifier.padding(Spacing.Spacing16),
            )
        }
    }
}

@Preview(name = "TF Panel — Light", showBackground = true)
@Preview(name = "TF Panel — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TrueFalsePanelPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TrueFalsePanel(
                question          = previewTfQuestion,
                content           = previewTfQuestion.content as QuestionUiContent.TrueFalse,
                isInputEnabled    = true,
                lastAnswerCorrect = null,
                showHint          = false,
                explanation       = null,
                correctLabel      = null,
                onAnswer          = {},
                modifier          = Modifier.padding(Spacing.Spacing16),
            )
        }
    }
}

@Preview(name = "Flashcard Front — Light", showBackground = true)
@Preview(name = "Flashcard Front — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FlashcardPanelFrontPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier         = Modifier.fillMaxWidth().padding(Spacing.Spacing20),
                contentAlignment = Alignment.Center,
            ) {
                FlashcardPanel(
                    content     = previewFlashcardQuestion.content as QuestionUiContent.Flashcard,
                    isAnswered  = false,
                    onFlip      = {},
                )
            }
        }
    }
}

@Preview(name = "Flashcard Back — Light", showBackground = true)
@Preview(name = "Flashcard Back — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FlashcardPanelBackPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier         = Modifier.fillMaxWidth().padding(Spacing.Spacing20),
                contentAlignment = Alignment.Center,
            ) {
                FlashcardPanel(
                    content     = previewFlashcardQuestion.content as QuestionUiContent.Flashcard,
                    isAnswered  = true,
                    onFlip      = {},
                )
            }
        }
    }
}