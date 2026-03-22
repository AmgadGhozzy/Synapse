package com.venom.synapse.features.session.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.domain.model.QuestionType
import com.venom.synapse.domain.srs.AnswerPayload
import com.venom.synapse.features.session.presentation.components.LeechAlertBanner
import com.venom.synapse.features.session.presentation.components.McqPanel
import com.venom.synapse.features.session.presentation.components.QuizActionSheet
import com.venom.synapse.features.session.presentation.components.QuizErrorState
import com.venom.synapse.features.session.presentation.components.QuizLoadingState
import com.venom.synapse.features.session.presentation.components.QuizTopBar
import com.venom.synapse.features.session.presentation.components.SessionSwipeableFlashcard
import com.venom.synapse.features.session.presentation.components.TrueFalsePanel
import com.venom.synapse.features.session.presentation.state.QuestionUiContent
import com.venom.synapse.features.session.presentation.state.QuestionUiModel
import com.venom.synapse.features.session.presentation.state.SessionUiState
import com.venom.synapse.features.session.presentation.viewmodel.SessionViewModel

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QuizScreen(
    onBack             : () -> Unit,
    onNavigateToSummary: () -> Unit,
    modifier           : Modifier = Modifier,
    viewModel          : SessionViewModel = hiltViewModel(),
) {
    val uiState            by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()

    LaunchedEffect(Unit) { viewModel.initSession() }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.NavigateBack -> onBack()
                is UiEffect.Navigate     -> onNavigateToSummary()
                is UiEffect.ShowToast    -> snackbarController.success(effect.message)
                is UiEffect.ShowError    -> snackbarController.error(effect.message)
                else                     -> Unit
            }
        }
    }

    Scaffold(
        modifier            = modifier.fillMaxSize().systemBarsPadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost        = { snackbarController.SnackbarHost() },
        containerColor      = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            uiState.isLoading     -> QuizLoadingState(Modifier.padding(innerPadding))
            uiState.error != null -> QuizErrorState(
                message  = uiState.error!!,
                onBack   = onBack,
                modifier = Modifier.padding(innerPadding),
            )
            else -> QuizContent(
                uiState    = uiState,
                onBack     = { viewModel.cancelSession() },
                onMcqAnswer = { qId, idx ->
                    viewModel.submitAnswer(qId, AnswerPayload.McqAnswer(idx))
                },
                onTfAnswer = { qId, v ->
                    viewModel.submitAnswer(qId, AnswerPayload.TfAnswer(v))
                },
                // Flashcard only: submits the self-rating and advances.
                // Called by SRS buttons and by swipe gestures.
                onFlashcardSrsAnswer = { qId, rating ->
                    viewModel.submitAnswer(qId, AnswerPayload.FlashcardSelfRate(rating))
                    viewModel.nextQuestion()
                },
                // MCQ/TF only: answer already submitted by onMcqAnswer/onTfAnswer.
                // SRS button just advances — it must NOT submit again, because
                // sending FlashcardSelfRate for an MCQ question makes checkAnswer()
                // return false, which records an extra wrong answer every time and
                // causes the engine to re-queue the question indefinitely.
                onMcqTfSrsAdvance = { viewModel.nextQuestion() },
                onDismissLeech    = { viewModel.dismissLeechAlert() },
                onShowHint        = { /* TODO: open PremiumBottomSheet */ },
                modifier          = Modifier.padding(innerPadding),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CONTENT ORCHESTRATOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun QuizContent(
    uiState             : SessionUiState,
    onBack              : () -> Unit,
    onMcqAnswer         : (Long, Int) -> Unit,
    onTfAnswer          : (Long, Boolean) -> Unit,
    onFlashcardSrsAnswer: (Long, Int) -> Unit,
    onMcqTfSrsAdvance   : () -> Unit,
    onDismissLeech      : () -> Unit,
    onShowHint          : () -> Unit,
    modifier            : Modifier = Modifier,
) {
    val question    = uiState.currentQuestion ?: return
    val isFlashcard = question.type == QuestionType.FLASHCARD

    // isFlipped: LOCAL state — tracks whether the flashcard has been tapped
    // to reveal the back face. Decoupled from isAnswered so that tapping the
    // card does NOT submit an answer; only the SRS button / swipe does.
    var isFlipped by remember(question.id) { mutableStateOf(false) }

    // isAnswered: set by the ViewModel after a real submit. For flashcards,
    // this only becomes true after the SRS rating button or swipe is used.
    val isAnswered = uiState.lastAnswerCorrect != null

    var showHint by remember(question.id) { mutableStateOf(false) }

    val hasHint by remember(question.id) {
        derivedStateOf { !question.hint.isNullOrBlank() }
    }

    val isLastQuestion by remember(uiState.questionIndex, uiState.totalQuestions) {
        derivedStateOf { uiState.questionIndex + 1 >= uiState.totalQuestions }
    }

    val correctLabel: String? by remember(uiState.lastAnswerCorrect, question.id) {
        derivedStateOf {
            if (uiState.lastAnswerCorrect == false) {
                when (val c = question.content) {
                    is QuestionUiContent.Mcq       -> "Option ${'A' + c.correctIndex}"
                    is QuestionUiContent.TrueFalse -> if (c.correctAnswer) "True" else "False"
                    else                           -> null
                }
            } else null
        }
    }

    Scaffold(
        modifier       = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar      = {
            QuizActionSheet(
                isFlashcard       = isFlashcard,
                // Sheet state for flashcards is driven by isFlipped so the SRS
                // buttons appear as soon as the card is tapped, before any submit.
                isFlipped         = isFlipped,
                // isAnswered drives the MCQ/TF feedback card and PRE_ANSWER state.
                isAnswered        = isAnswered,
                isLastQuestion    = isLastQuestion,
                hasHint           = hasHint,
                questionId        = question.id,
                lastAnswerCorrect = uiState.lastAnswerCorrect,
                lastExplanation   = uiState.lastExplanation,
                correctLabel      = correctLabel,
                showHint          = showHint,
                onShowHint        = {
                    showHint = !showHint
                    if (showHint) onShowHint()
                },
                onFlip      = { isFlipped = true }, // local only — no submit
                onSrsRating = { qId, rating ->
                    if (isFlashcard) onFlashcardSrsAnswer(qId, rating)
                    else             onMcqTfSrsAdvance()
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            QuizTopBar(
                title          = uiState.packTitle,
                questionIndex  = uiState.questionIndex + 1,
                totalQuestions = uiState.totalQuestions,
                progress       = uiState.progressPercent,
                accuracy       = uiState.accuracy,
                showAccuracy   = !isFlashcard && uiState.answeredCount > 0,
                onClose        = onBack,
                modifier       = Modifier.padding(
                    horizontal = Spacing.Spacing20,
                    vertical   = Spacing.Spacing12,
                ),
            )

            AnimatedVisibility(
                visible = uiState.showLeechAlert,
                enter   = fadeIn() + slideInVertically { -it },
                exit    = fadeOut() + slideOutVertically { -it },
            ) {
                LeechAlertBanner(
                    onDismiss = onDismissLeech,
                    modifier  = Modifier
                        .padding(horizontal = Spacing.Spacing20)
                        .padding(bottom = Spacing.Spacing8),
                )
            }

            AnimatedContent(
                targetState    = question.id,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInHorizontally(tween(220)) { it / 4 }) togetherWith
                            (fadeOut(tween(160)) + slideOutHorizontally(tween(180)) { -it / 4 })
                },
                label          = "question_transition",
                modifier       = Modifier.weight(1f),
            ) { _ ->
                if (isFlashcard) {
                    FlashcardContentArea(
                        question     = question,
                        isFlipped    = isFlipped,
                        onFlip       = { isFlipped = true },
                        // Swipe left = Hard (1 day), swipe right = Easy (7 days).
                        // Each swipe submits the rating and advances the session.
                        onSwipeLeft  = { onFlashcardSrsAnswer(question.id, 1) },
                        onSwipeRight = { onFlashcardSrsAnswer(question.id, 4) },
                    )
                } else {
                    ScrollableQuestionArea(
                        question    = question,
                        uiState     = uiState,
                        showHint    = showHint,
                        onMcqAnswer = onMcqAnswer,
                        onTfAnswer  = onTfAnswer,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FLASHCARD CONTENT AREA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FlashcardContentArea(
    question    : QuestionUiModel,
    isFlipped   : Boolean,
    onFlip      : () -> Unit,
    onSwipeLeft : () -> Unit,
    onSwipeRight: () -> Unit,
    modifier    : Modifier = Modifier,
) {
    val content = question.content as? QuestionUiContent.Flashcard ?: return
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        SessionSwipeableFlashcard(
            content      = content,
            isAnswered   = isFlipped,   // card face + swipe gate driven by isFlipped
            onFlip       = onFlip,
            onSwipeLeft  = onSwipeLeft,
            onSwipeRight = onSwipeRight,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MCQ / T-F SCROLLABLE AREA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScrollableQuestionArea(
    question    : QuestionUiModel,
    uiState     : SessionUiState,
    showHint    : Boolean,
    onMcqAnswer : (Long, Int) -> Unit,
    onTfAnswer  : (Long, Boolean) -> Unit,
    modifier    : Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.Spacing20),
    ) {
        when (val content = question.content) {
            is QuestionUiContent.Mcq -> McqPanel(
                question          = question,
                content           = content,
                isInputEnabled    = uiState.isInputEnabled,
                lastAnswerCorrect = uiState.lastAnswerCorrect,
                showHint          = showHint,
                onOptionSelected  = { idx -> onMcqAnswer(question.id, idx) },
            )
            is QuestionUiContent.TrueFalse -> TrueFalsePanel(
                question          = question,
                content           = content,
                isInputEnabled    = uiState.isInputEnabled,
                lastAnswerCorrect = uiState.lastAnswerCorrect,
                showHint          = showHint,
                onAnswer          = { v -> onTfAnswer(question.id, v) },
            )
            else -> Unit
        }
        Spacer(Modifier.height(Spacing.Spacing16))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Quiz MCQ — Light", showBackground = true)
@Preview(name = "Quiz MCQ — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizMcqPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState, onBack = {},
                onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

@Preview(name = "Quiz MCQ Answered — Light", showBackground = true)
@Preview(name = "Quiz MCQ Answered — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizMcqAnsweredPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState.copy(
                    lastAnswerCorrect = true,
                    lastExplanation   = "Caesar crossed the Rubicon in 49 BC.",
                    isInputEnabled    = false,
                ),
                onBack = {}, onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

@Preview(name = "Quiz MCQ Wrong — Light", showBackground = true)
@Preview(name = "Quiz MCQ Wrong — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizMcqWrongPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState.copy(
                    lastAnswerCorrect = false,
                    lastExplanation   = "Caesar crossed in 49 BC, triggering the civil war.",
                    isInputEnabled    = false,
                ),
                onBack = {}, onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

@Preview(name = "Quiz TF — Light", showBackground = true)
@Preview(name = "Quiz TF — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizTfPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState.copy(currentQuestion = previewTfQuestion),
                onBack = {}, onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

@Preview(name = "Quiz Flashcard — Light", showBackground = true)
@Preview(name = "Quiz Flashcard — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizFlashcardPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState.copy(currentQuestion = previewFlashcardQuestion),
                onBack = {}, onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

@Preview(name = "Quiz Leech — Light", showBackground = true)
@Preview(name = "Quiz Leech — Dark",  uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizLeechPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState.copy(showLeechAlert = true),
                onBack = {}, onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

// ── Preview fixtures ──────────────────────────────────────────────────────────

internal val previewMcqQuestion = QuestionUiModel(
    id           = 1L,
    type         = QuestionType.MCQ,
    questionText = "In what year did Julius Caesar cross the Rubicon River?",
    content      = QuestionUiContent.Mcq(
        options      = listOf("55 BC", "49 BC", "44 BC", "31 BC"),
        correctIndex = 1,
    ),
    hint        = "Think about the event that triggered the Roman Civil War.",
    explanation = "Caesar crossed the Rubicon in 49 BC, triggering the civil war.",
)

internal val previewTfQuestion = QuestionUiModel(
    id           = 2L,
    type         = QuestionType.TRUE_FALSE,
    questionText = "Julius Caesar crossed the Rubicon River in 49 BC, triggering a civil war.",
    content      = QuestionUiContent.TrueFalse(correctAnswer = true),
    hint         = null, // TF has no hint — HintButton hidden
    explanation  = "He crossed the Rubicon on January 10, 49 BC with his legion.",
)

internal val previewFlashcardQuestion = QuestionUiModel(
    id           = 3L,
    type         = QuestionType.FLASHCARD,
    questionText = "What is machine learning?",
    content      = QuestionUiContent.Flashcard(
        front = "Supervised Learning",
        back  = "A type of ML where the model is trained on labelled data to map inputs to outputs.",
    ),
)

internal val previewSessionUiState = SessionUiState(
    packTitle       = "Machine Learning Basics",
    mode            = "mcq",
    currentQuestion = previewMcqQuestion,
    questionIndex   = 2,
    totalQuestions  = 10,
    answeredCount   = 2,
    correctCount    = 2,
    progressPercent = 0.2f,
    accuracy        = 1.0f,
    isInputEnabled  = true,
)