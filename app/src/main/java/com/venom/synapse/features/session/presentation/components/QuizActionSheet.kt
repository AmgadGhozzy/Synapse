package com.venom.synapse.features.session.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.unit.dp
import com.venom.synapse.R
import com.venom.synapse.core.theme.StatusColors
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.theme.tokens.toShadow
import com.venom.ui.components.common.adp


@Composable
internal fun QuizActionSheet(
    isFlashcard      : Boolean,
    isFlipped        : Boolean,
    isAnswered       : Boolean,
    isLastQuestion   : Boolean,
    hasHint          : Boolean,
    questionId       : Long,
    lastAnswerCorrect: Boolean?,
    lastExplanation  : String?,
    correctLabel     : String?,
    showLeechAlert   : Boolean,
    onDismissLeech   : () -> Unit,
    onShowHint       : () -> Unit,
    onFlip           : () -> Unit,
    onSrsRating      : (Long, Int) -> Unit,
    modifier         : Modifier = Modifier,
) {
    val sheetState = when {
        isFlashcard && !isFlipped   -> ActionSheetState.FLIP
        !isFlashcard && !isAnswered -> ActionSheetState.PRE_ANSWER
        else                        -> ActionSheetState.SRS
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .dropShadow(
                shape  = MaterialTheme.shapes.extraLarge,
                shadow = MaterialTheme.synapse.shadows.medium.toShadow(
                    offset = DpOffset(0.dp, (-4).dp),
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
            AnimatedContent(
                targetState    = sheetState,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 4 }) togetherWith
                            (fadeOut(tween(150)) + slideOutVertically(tween(160)) { -it / 4 })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start   = MaterialTheme.synapse.spacing.s20,
                        end     = MaterialTheme.synapse.spacing.s20,
                        top     = MaterialTheme.synapse.spacing.s12,
                        bottom  = MaterialTheme.synapse.spacing.s20,
                    ),
            ) { state ->
                when (state) {
                    ActionSheetState.FLIP        -> FlipCardButton(onClick = onFlip)
                    ActionSheetState.PRE_ANSWER  -> PreAnswerRow(
                        hasHint    = hasHint,
                        onShowHint = onShowHint,
                    )
                    ActionSheetState.SRS         -> SrsAnsweredContent(
                        isFlashcard       = isFlashcard,
                        isLastQuestion    = isLastQuestion,
                        showLeechAlert    = showLeechAlert,
                        onDismissLeech    = onDismissLeech,
                        lastAnswerCorrect = lastAnswerCorrect,
                        lastExplanation   = lastExplanation,
                        correctLabel      = correctLabel,
                        onRatingSelected  = { rating -> onSrsRating(questionId, rating) },
                    )
                }
            }
        }
    }
}

private enum class ActionSheetState { FLIP, PRE_ANSWER, SRS }


@Composable
private fun SrsAnsweredContent(
    isFlashcard      : Boolean,
    isLastQuestion   : Boolean,
    showLeechAlert   : Boolean,
    onDismissLeech   : () -> Unit,
    lastAnswerCorrect: Boolean?,
    lastExplanation  : String?,
    correctLabel     : String?,
    onRatingSelected : (Int) -> Unit,
    modifier         : Modifier = Modifier,
) {
    Column(
        modifier              = modifier.fillMaxWidth(),
        verticalArrangement   = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
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

        SrsRatingSection(
            isLastQuestion   = isLastQuestion,
            onRatingSelected = onRatingSelected,
        )
    }
}


@Composable
private fun SrsRatingSection(
    isLastQuestion  : Boolean,
    onRatingSelected: (Int) -> Unit,
    modifier        : Modifier = Modifier,
) {
    Column(
        modifier              = modifier.fillMaxWidth(),
        verticalArrangement   = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {
        Text(
            text      = stringResource(R.string.quiz_srs_prompt),
            style     = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Text(
            text      = stringResource(
                if (isLastQuestion) R.string.quiz_srs_finish_hint else R.string.quiz_srs_advance_hint,
            ),
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier  = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MaterialTheme.synapse.spacing.s6))
        SrsRatingRow(
            isLastQuestion   = isLastQuestion,
            onRatingSelected = onRatingSelected,
        )
    }
}


@Composable
internal fun SrsRatingRow(
    isLastQuestion  : Boolean,
    onRatingSelected: (Int) -> Unit,
    modifier        : Modifier = Modifier,
) {
    data class SrsOption(
        val labelRes  : Int,
        val rating    : Int,
        val tone      : SrsActionTone,
        val iconRes   : Int,
        val intervalRes: Int,
    )

    val options = listOf(
        SrsOption(R.string.quiz_srs_hard, 1, SrsActionTone.Hard, R.drawable.ic_alert_circle, R.string.quiz_srs_hard_interval),
        SrsOption(R.string.quiz_srs_good, 3, SrsActionTone.Good, R.drawable.ic_thumbs_up,    R.string.quiz_srs_good_interval),
        SrsOption(R.string.quiz_srs_easy, 4, SrsActionTone.Easy, R.drawable.ic_check,         R.string.quiz_srs_easy_interval),
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
                statusColors = srsActionColors(opt.tone),
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
    statusColors: StatusColors,
    iconRes     : Int,
    onClick     : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    val typo  = MaterialTheme.typography
    val shape = MaterialTheme.shapes.large
    val style = srsButtonVisualStyle(statusColors)

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 84.adp)
            .clip(shape)
            .background(style.background)
            .border(1.dp, style.borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.adp, horizontal = 8.adp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {

            Icon(
                painter           = painterResource(iconRes),
                contentDescription = label,
                tint              = style.accentColor,
            modifier = Modifier.size(18.adp),
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
private fun PreAnswerRow(
    hasHint   : Boolean,
    onShowHint: () -> Unit,
    modifier  : Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s10),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        if (hasHint) {
            HintButton(onClick = onShowHint)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(58.adp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = stringResource(R.string.quiz_choose_answer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
private fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier
            .fillMaxWidth()
            .padding(top = MaterialTheme.synapse.spacing.s10),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(
                    width  = MaterialTheme.synapse.spacing.s32,
                    height = 4.adp,
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        )
    }
}


@Composable
internal fun FlipCardButton(
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.adp)
            .clip(shape)
            .dropShadow(
                shape  = shape,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(
                    customColor = MaterialTheme.colorScheme.primary,
                    offset      = DpOffset(0.dp, 4.dp),
                ),
            )
            .background(MaterialTheme.synapse.gradients.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
        ) {
            Icon(
                painter           = painterResource(R.drawable.ic_hand_tap),
                contentDescription = null,
                tint              = Color.White.copy(0.9f),
                modifier          = Modifier.size(18.adp),
            )
            Text(
                text       = stringResource(R.string.quiz_tap_to_flip),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = Color.White.copy(0.9f),
            )
        }
    }
}


@Composable
internal fun HintButton(
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.synapse.levelColors.gold
    val shape = MaterialTheme.shapes.large

    Row(
        modifier = modifier
            .height(58.adp)
            .clip(shape)
            .dropShadow(
                shape  = shape,
                shadow = MaterialTheme.synapse.shadows.subtle.toShadow(
                    customColor = color.accentColor,
                ),
            )
            .background(color.bgColor)
            .border(1.dp, color.borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.synapse.spacing.s14),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s6),
    ) {
        Icon(
            painter           = painterResource(R.drawable.ic_lightbulb),
            contentDescription = stringResource(R.string.quiz_hint_label),
            tint              = color.accentColor,
            modifier          = Modifier.size(15.adp),
        )
        Text(
            text       = stringResource(R.string.quiz_hint_label),
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = color.accentColor,
        )
    }
}


enum class SrsActionTone { Hard, Good, Easy }

@Composable
@ReadOnlyComposable
fun srsActionColors(tone: SrsActionTone): StatusColors {
    val level = MaterialTheme.synapse.levelColors
    return when (tone) {
        SrsActionTone.Hard -> level.error
        SrsActionTone.Good -> level.accent
        SrsActionTone.Easy -> level.success
    }
}

@Immutable
data class SrsButtonStyle(
    val accentColor      : Color,
    val background       : Color,
    val borderColor      : Color,
    val secondaryTextColor: Color,
)

fun srsButtonVisualStyle(colors: StatusColors) = SrsButtonStyle(
    accentColor = colors.accentColor,
    background = colors.bgColor,
    borderColor = colors.borderColor,
    secondaryTextColor = colors.accentColor.copy(alpha = 0.5f),
)


@Preview(name = "Sheet FLIP — Light", showBackground = true)
@Preview(name = "Sheet FLIP — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SheetFlipPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizActionSheet(
                isFlashcard = true, isFlipped = false, isAnswered = false,
                isLastQuestion = false, hasHint = false, questionId = 1L,
                lastAnswerCorrect = null, lastExplanation = null, correctLabel = null,
                showLeechAlert = false, onDismissLeech = {},
                onShowHint = {}, onFlip = {}, onSrsRating = { _, _ -> },
            )
        }
    }
}

@Preview(name = "Sheet MCQ Correct — Light", showBackground = true)
@Preview(name = "Sheet MCQ Correct — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SheetMcqCorrectPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizActionSheet(
                isFlashcard = false, isFlipped = false, isAnswered = true,
                isLastQuestion = false, hasHint = true, questionId = 1L,
                lastAnswerCorrect = true,
                lastExplanation   = "Caesar crossed the Rubicon in 49 BC.",
                correctLabel      = null,
                showLeechAlert = false, onDismissLeech = {},
                onShowHint = {}, onFlip = {}, onSrsRating = { _, _ -> },
            )
        }
    }
}

@Preview(name = "Sheet MCQ Wrong — Light", showBackground = true)
@Preview(name = "Sheet MCQ Wrong — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SheetMcqWrongPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizActionSheet(
                isFlashcard = false, isFlipped = false, isAnswered = true,
                isLastQuestion = false, hasHint = true, questionId = 1L,
                lastAnswerCorrect = false,
                lastExplanation   = "He crossed in 49 BC, triggering the civil war.",
                correctLabel      = "Option B",
                showLeechAlert = false, onDismissLeech = {},
                onShowHint = {}, onFlip = {}, onSrsRating = { _, _ -> },
            )
        }
    }
}

@Preview(name = "Sheet Leech Alert — Light", showBackground = true)
@Preview(name = "Sheet Leech Alert — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SheetLeechPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizActionSheet(
                isFlashcard = false, isFlipped = false, isAnswered = true,
                isLastQuestion = false, hasHint = false, questionId = 1L,
                lastAnswerCorrect = false,
                lastExplanation   = "He crossed in 49 BC, triggering the civil war.",
                correctLabel      = "Option B",
                showLeechAlert = true, onDismissLeech = {},
                onShowHint = {}, onFlip = {}, onSrsRating = { _, _ -> },
            )
        }
    }
}

@Preview(name = "Sheet Pre-Answer with Hint — Light", showBackground = true)
@Preview(name = "Sheet Pre-Answer with Hint — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SheetPreAnswerHintPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizActionSheet(
                isFlashcard = false, isFlipped = false, isAnswered = false,
                isLastQuestion = false, hasHint = true, questionId = 1L,
                lastAnswerCorrect = null, lastExplanation = null, correctLabel = null,
                showLeechAlert = false, onDismissLeech = {},
                onShowHint = {}, onFlip = {}, onSrsRating = { _, _ -> },
            )
        }
    }
}

@Preview(name = "Sheet Last Question — Light", showBackground = true)
@Preview(name = "Sheet Last Question — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SheetLastPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizActionSheet(
                isFlashcard = false, isFlipped = false, isAnswered = true,
                isLastQuestion = true, hasHint = false, questionId = 1L,
                lastAnswerCorrect = true, lastExplanation = null, correctLabel = null,
                showLeechAlert = false, onDismissLeech = {},
                onShowHint = {}, onFlip = {}, onSrsRating = { _, _ -> },
            )
        }
    }
}