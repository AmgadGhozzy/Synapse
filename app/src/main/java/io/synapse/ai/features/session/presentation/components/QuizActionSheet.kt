package io.synapse.ai.features.session.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
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
import androidx.compose.ui.unit.DpOffset
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.domain.srs.ReviewRating

@Composable
internal fun QuizActionSheet(
    isFlashcard      : Boolean,
    isFlipped        : Boolean,
    isAnswered       : Boolean,
    isLastQuestion   : Boolean,
    hasHint          : Boolean,
    hasDiagram       : Boolean,
    questionId       : Long,
    lastAnswerCorrect: Boolean?,
    lastExplanation  : String?,
    correctLabel     : String?,
    showLeechAlert   : Boolean,
    onDismissLeech   : () -> Unit,
    onShowHint       : () -> Unit,
    onShowDiagram    : () -> Unit,
    onFlip           : () -> Unit,
    onSrsRating      : (questionId: Long, rating: ReviewRating) -> Unit,
    modifier         : Modifier = Modifier,
    showHint         : Boolean = false,
    hint             : String? = null,
) {
    val sheetState = when {
        isFlashcard && !isFlipped   -> ActionSheetState.PRE_ANSWER
        !isFlashcard && !isAnswered -> ActionSheetState.PRE_ANSWER
        else                        -> ActionSheetState.SRS
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .dropShadow(
                shape  = MaterialTheme.shapes.extraLarge,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(
                    customOffset = DpOffset(0.adp, (-4).adp),
                ),
            ),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            SheetHandle()

            AnimatedVisibility(
                visible = showHint && !hint.isNullOrBlank(),
                enter   = fadeIn(tween(200)),
                exit    = fadeOut(tween(160)),
            ) {
                HintReveal(
                    hint     = hint ?: "",
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.synapse.spacing.s20)
                        .padding(bottom     = MaterialTheme.synapse.spacing.s12),
                )
            }

            val isPostAnswer = sheetState == ActionSheetState.SRS

            AnimatedVisibility(visible = isPostAnswer) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.synapse.spacing.s20)
                        .padding(bottom = MaterialTheme.synapse.spacing.s12),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12)
                ) {
                    AnimatedVisibility(
                        visible = showLeechAlert,
                        enter   = fadeIn(tween(200)) + slideInVertically(tween(220)) { -it / 3 },
                        exit    = fadeOut(tween(160)) + slideOutVertically(tween(180)) { -it / 3 },
                    ) {
                        LeechAlertBanner(onDismiss = onDismissLeech)
                    }

                    if (!isFlashcard && lastAnswerCorrect != null) {
                        AnswerFeedbackCard(
                            isCorrect    = lastAnswerCorrect,
                            explanation  = lastExplanation,
                            correctLabel = correctLabel,
                        )
                    }

                    Text(
                        text      = stringResource(R.string.quiz_srs_prompt),
                        style     = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            ActionRow(
                isFlashcard = isFlashcard,
                isPostAnswer = isPostAnswer,
                isLastQuestion = isLastQuestion,
                hasHint = hasHint,
                hasDiagram = hasDiagram,
                onShowHint = onShowHint,
                onShowDiagram = onShowDiagram,
                onFlip = onFlip,
                onRatingSelected = { rating -> onSrsRating(questionId, rating) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start   = MaterialTheme.synapse.spacing.s20,
                        end     = MaterialTheme.synapse.spacing.s20,
                        top     = MaterialTheme.synapse.spacing.s12,
                        bottom  = MaterialTheme.synapse.spacing.s20,
                    )
            )
        }
    }
}

private enum class ActionSheetState { PRE_ANSWER, SRS }


@Composable
internal fun SrsRatingRow(
    isLastQuestion  : Boolean,
    onRatingSelected: (ReviewRating) -> Unit,
    modifier        : Modifier = Modifier,
) {
    data class SrsOption(
        val labelRes  : Int,
        val rating    : ReviewRating,
        val tone      : SrsActionTone,
        val iconRes   : Int,
        val intervalRes: Int,
    )

    val options = listOf(
        SrsOption(R.string.quiz_srs_hard, ReviewRating.HARD, SrsActionTone.Hard, R.drawable.ic_alert_circle, R.string.quiz_srs_hard_interval),
        SrsOption(R.string.quiz_srs_good, ReviewRating.GOOD, SrsActionTone.Good, R.drawable.ic_thumbs_up,    R.string.quiz_srs_good_interval),
        SrsOption(R.string.quiz_srs_easy, ReviewRating.EASY, SrsActionTone.Easy, R.drawable.ic_check,         R.string.quiz_srs_easy_interval),
    )

    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
    ) {
        options.forEach { opt ->
            SrsButton(
                label        = stringResource(opt.labelRes),
                interval     = if (isLastQuestion) stringResource(R.string.quiz_srs_finish)
                else stringResource(opt.intervalRes),
                style = srsButtonVisualStyle(opt.tone),
                iconRes      = opt.iconRes,
                onClick      = { onRatingSelected(opt.rating) },
                modifier     = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SrsButton(
    label       : String,
    interval    : String,
    style: SrsButtonStyle,
    iconRes     : Int,
    onClick     : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    val typo  = MaterialTheme.typography
    val shape = MaterialTheme.shapes.large

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 84.adp)
            .clip(shape)
            .background(style.background)
            .clickable(onClick = onClick)
            .padding(vertical = 14.adp, horizontal = 8.adp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {

            Icon(
                painter           = painterResource(iconRes),
                contentDescription = label,
                tint              = style.accentColor,
            modifier = Modifier.size(MaterialTheme.synapse.spacing.icon_xs),
            )


        Text(
            text      = label,
            style     = typo.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color     = style.accentColor,
            textAlign = TextAlign.Center,
        )

        Text(
            text      = interval,
            style     = typo.labelSmall,
            color     = style.secondaryTextColor,
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
private fun ActionRow(
    isFlashcard  : Boolean,
    isPostAnswer : Boolean,
    isLastQuestion: Boolean,
    hasHint      : Boolean,
    hasDiagram   : Boolean,
    onShowHint   : () -> Unit,
    onShowDiagram: () -> Unit,
    onFlip       : () -> Unit,
    onRatingSelected: (ReviewRating) -> Unit,
    modifier     : Modifier = Modifier,
) {
    AnimatedContent(
        targetState = isPostAnswer,
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            (fadeIn(tween(250)) + slideInVertically(tween(300)) { it / 2 }) togetherWith
                    (fadeOut(tween(200)) + slideOutVertically(tween(250)) { it / 2 })
        }
    ) { postAnswer ->
        if (postAnswer) {
            SrsRatingRow(
                isLastQuestion = isLastQuestion,
                onRatingSelected = onRatingSelected,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasHint) {
                    HintButton(onClick = onShowHint)
                }
                if (hasDiagram) {
                    DiagramButton(onClick = onShowDiagram)
                }
                QuizPrimaryActionButton(
                    isFlashcard = isFlashcard,
                    onClick     = onFlip,
                    modifier    = Modifier.weight(1f),
                )
            }
        }
    }
}


@Composable
private fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.synapse.spacing.s12),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(
                    width  = MaterialTheme.synapse.spacing.s32,
                    height = 4.adp,
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.outline),
        )
    }
}


@Composable
internal fun QuizPrimaryActionButton(
    isFlashcard: Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    val contentColor = if (isFlashcard) Color.White.copy(0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
    val text         = if (isFlashcard) R.string.quiz_tap_to_flip else R.string.quiz_choose_answer
    val icon         = if (isFlashcard) R.drawable.ic_hand_tap else null
    val fontWeight   = if (isFlashcard) FontWeight.Bold else FontWeight.Normal

    val mod = if (isFlashcard) {
        modifier
            .height(58.adp)
            .clip(shape)
            .dropShadow(
                shape  = shape,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(
                    customColor = MaterialTheme.colorScheme.primary,
                    customOffset = DpOffset(0.adp, 4.adp),
                ),
            )
            .background(MaterialTheme.synapse.gradients.primary, shape)
            .clickable(onClick = onClick)
    } else {
        modifier
            .height(58.adp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
    }

    Box(
        modifier         = mod,
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
        ) {
            if (icon != null) {
                Icon(
                    painter           = painterResource(icon),
                    contentDescription = null,
                    tint              = contentColor,
                    modifier          = Modifier.size(MaterialTheme.synapse.spacing.icon_xs),
                )
            }
            Text(
                text       = stringResource(text),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = fontWeight,
                color      = contentColor,
            )
        }
    }
}


@Composable
internal fun HintButton(
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic
    val shape = MaterialTheme.shapes.medium

    Row(
        modifier = modifier
            .height(58.adp)
            .clip(shape)
            .background(semantic.goldBg)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.synapse.spacing.s14),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {
        Icon(
            painter           = painterResource(R.drawable.ic_lightbulb),
            contentDescription = stringResource(R.string.quiz_hint_label),
            tint              = semantic.gold,
            modifier          = Modifier.size(16.adp),
        )
    }
}

@Composable
internal fun DiagramButton(
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val semantic = MaterialTheme.synapse.semantic
    val shape = MaterialTheme.shapes.medium

    Row(
        modifier = modifier
            .height(58.adp)
            .clip(shape)
            .background(semantic.primaryBg)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.synapse.spacing.s14),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {
        Icon(
            painter           = painterResource(R.drawable.ic_tree_structure),
            contentDescription = "Diagram",
            tint              = semantic.primary,
            modifier          = Modifier.size(16.adp),
        )
    }
}


enum class SrsActionTone { Hard, Good, Easy }

@Composable
@ReadOnlyComposable
private fun srsButtonVisualStyle(tone: SrsActionTone): SrsButtonStyle {
    val semantic = MaterialTheme.synapse.semantic
    return when (tone) {
        SrsActionTone.Hard -> SrsButtonStyle(
            accentColor = semantic.gold,
            background = semantic.goldBg,
            borderColor = semantic.goldBorder,
            secondaryTextColor = semantic.gold.copy(alpha = 0.55f),
        )
        SrsActionTone.Good -> SrsButtonStyle(
            accentColor = semantic.primary,
            background = semantic.primaryBg,
            borderColor = semantic.primaryBorder,
            secondaryTextColor = semantic.primary.copy(alpha = 0.55f),
        )
        SrsActionTone.Easy -> SrsButtonStyle(
            accentColor = semantic.success,
            background = semantic.successBg,
            borderColor = semantic.successBorder,
            secondaryTextColor = semantic.success.copy(alpha = 0.55f),
        )
    }
}

data class SrsButtonStyle(
    val accentColor      : Color,
    val background       : Color,
    val borderColor      : Color,
    val secondaryTextColor: Color,
)


@Preview(name = "Action Sheet — Flashcard (Pre-Flip)", showBackground = true)
@Composable
private fun QuizActionSheetFlashcardPreview() {
    SynapseTheme {
        QuizActionSheet(
            isFlashcard       = true,
            isFlipped         = false,
            isAnswered        = false,
            isLastQuestion    = false,
            hasHint           = true,
            hasDiagram        = true,
            questionId        = 1L,
            lastAnswerCorrect = null,
            lastExplanation   = null,
            correctLabel      = null,
            showLeechAlert    = false,
            onDismissLeech    = {},
            onShowHint        = {},
            onShowDiagram     = {},
            onFlip            = {},
            onSrsRating       = { _, _ -> },
        )
    }
}

@Preview(name = "Action Sheet — MCQ (Pre-Answer)", showBackground = true)
@Composable
private fun QuizActionSheetMcqPreview() {
    SynapseTheme {
        QuizActionSheet(
            isFlashcard       = false,
            isFlipped         = false,
            isAnswered        = false,
            isLastQuestion    = false,
            hasHint           = true,
            hasDiagram        = true,
            questionId        = 1L,
            lastAnswerCorrect = null,
            lastExplanation   = null,
            correctLabel      = null,
            showLeechAlert    = false,
            onDismissLeech    = {},
            onShowHint        = {},
            onShowDiagram     = {},
            onFlip            = {},
            onSrsRating       = { _, _ -> },
        )
    }
}

@Preview(name = "Action Sheet — SRS", showBackground = true)
@Composable
private fun QuizActionSheetSrsPreview() {
    SynapseTheme {
        QuizActionSheet(
            isFlashcard       = false,
            isFlipped         = false,
            isAnswered        = true,
            isLastQuestion    = false,
            hasHint           = true,
            hasDiagram        = false,
            questionId        = 1L,
            lastAnswerCorrect = true,
            lastExplanation   = "The Rubicon was a river in northeastern Italy.",
            correctLabel      = "Rubicon",
            showLeechAlert    = true,
            onDismissLeech    = {},
            onShowHint        = {},
            onShowDiagram     = {},
            onFlip            = {},
            onSrsRating       = { _, _ -> },
        )
    }
}
