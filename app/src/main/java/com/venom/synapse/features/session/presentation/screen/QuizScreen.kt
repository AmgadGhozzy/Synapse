package com.venom.synapse.features.session.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.R
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.core.theme.synapse
import com.venom.synapse.core.ui.components.SnackbarHost
import com.venom.synapse.core.ui.components.rememberSnackbarController
import com.venom.synapse.core.ui.state.UiEffect
import com.venom.synapse.domain.model.QuestionType
import com.venom.synapse.domain.srs.AnswerPayload
import com.venom.synapse.features.session.presentation.components.McqPanel
import com.venom.synapse.features.session.presentation.components.QuizActionSheet
import com.venom.synapse.features.session.presentation.components.QuizErrorState
import com.venom.synapse.features.session.presentation.components.QuizLoadingState
import com.venom.synapse.features.session.presentation.components.QuizTopBar
import com.venom.synapse.features.session.presentation.components.SwipeableFlashcard
import com.venom.synapse.features.session.presentation.components.TrueFalsePanel
import com.venom.synapse.features.session.presentation.state.QuestionUiContent
import com.venom.synapse.features.session.presentation.state.QuestionUiModel
import com.venom.synapse.features.session.presentation.state.SessionUiState
import com.venom.synapse.features.session.presentation.viewmodel.SessionViewModel

@Composable
fun QuizScreen(
    onBack             : () -> Unit,
    onNavigateToSummary: () -> Unit,
    modifier           : Modifier = Modifier,
    viewModel          : SessionViewModel = hiltViewModel(),
) {
    val uiState            by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val context            = LocalContext.current
    val optionFormat       = stringResource(R.string.session_correct_option_label)
    val trueLabel          = stringResource(R.string.quiz_true_label)
    val falseLabel         = stringResource(R.string.quiz_false_label)

    LaunchedEffect(Unit) { viewModel.initSession() }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.NavigateBack -> onBack()
                is UiEffect.Navigate     -> onNavigateToSummary()
                is UiEffect.ShowToast    -> snackbarController.success(effect.text.asString(context))
                is UiEffect.ShowError    -> snackbarController.error(effect.text.asString(context))
                else                     -> Unit
            }
        }
    }

    Scaffold(
        modifier            = modifier.fillMaxSize().systemBarsPadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost        = { snackbarController.SnackbarHost() },
        containerColor       = Color.Transparent,
    ) { innerPadding ->
        when {
            uiState.isLoading     -> QuizLoadingState(Modifier.padding(innerPadding))
            uiState.error != null -> QuizErrorState(
                message  = uiState.error!!.asString(context),
                onBack   = onBack,
                modifier = Modifier.padding(innerPadding),
            )
            else -> QuizContent(
                uiState              = uiState,
                optionFormat         = optionFormat,
                trueLabel            = trueLabel,
                falseLabel           = falseLabel,
                onBack               = { viewModel.cancelSession() },
                onMcqAnswer          = { qId, idx -> viewModel.submitAnswer(qId, AnswerPayload.McqAnswer(idx)) },
                onTfAnswer           = { qId, v -> viewModel.submitAnswer(qId, AnswerPayload.TfAnswer(v)) },
                onFlashcardSrsAnswer = { qId, rating ->
                    viewModel.submitAnswer(qId, AnswerPayload.FlashcardSelfRate(rating))
                    viewModel.nextQuestion()
                },
                onMcqTfSrsAdvance    = { viewModel.nextQuestion() },
                onDismissLeech       = { viewModel.dismissLeechAlert() },
                onShowHint           = { /* TODO: open PremiumBottomSheet */ },
                modifier             = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuizContent(
    uiState             : SessionUiState,
    optionFormat        : String,
    trueLabel           : String,
    falseLabel          : String,
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

    var isFlipped by remember(question.id) { mutableStateOf(false) }

    val isAnswered = uiState.lastAnswerCorrect != null

    var showHint by remember(question.id) { mutableStateOf(false) }

    val hasHint by remember(question.id) {
        derivedStateOf { !question.hint.isNullOrBlank() }
    }

    val isLastQuestion by remember {
        derivedStateOf { uiState.questionIndex + 1 >= uiState.totalQuestions }
    }

    val correctLabel: String? by remember(
        uiState.lastAnswerCorrect, question.id, optionFormat, trueLabel, falseLabel,
    ) {
        derivedStateOf {
            if (uiState.lastAnswerCorrect == false) {
                when (val c = question.content) {
                    is QuestionUiContent.Mcq       -> optionFormat.format('A' + c.correctIndex)
                    is QuestionUiContent.TrueFalse -> if (c.correctAnswer) trueLabel else falseLabel
                    else                           -> null
                }
            } else null
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor       = Color.Transparent,
        topBar = {
            QuizTopBar(
                title          = uiState.packTitle,
                questionIndex  = uiState.questionIndex + 1,
                totalQuestions = uiState.totalQuestions,
                progress       = uiState.progressPercent,
                accuracy       = uiState.accuracy,
                showAccuracy   = !isFlashcard && uiState.answeredCount > 0,
                onClose        = onBack,
                scrollBehavior = scrollBehavior,
                modifier       = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.screen),
            )
        },
        bottomBar = {
            QuizActionSheet(
                isFlashcard       = isFlashcard,
                isFlipped         = isFlipped,
                isAnswered        = isAnswered,
                isLastQuestion    = isLastQuestion,
                hasHint           = hasHint,
                questionId        = question.id,
                lastAnswerCorrect = uiState.lastAnswerCorrect,
                lastExplanation   = uiState.lastExplanation,
                correctLabel      = correctLabel,
                showLeechAlert    = uiState.showLeechAlert,
                onDismissLeech    = onDismissLeech,
                onShowHint        = {
                    val willShow = !showHint
                    showHint = willShow
                    if (willShow) onShowHint()
                },
                onFlip      = { isFlipped = true },
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
                .padding(innerPadding)
                .padding(MaterialTheme.synapse.spacing.screen),
        ) {
            AnimatedContent(
                targetState    = question.id,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInHorizontally(tween(220)) { it / 4 }) togetherWith
                            (fadeOut(tween(160)) + slideOutHorizontally(tween(180)) { -it / 4 })
                },
                modifier = Modifier.weight(1f),
            ) {
                if (isFlashcard) {
                    FlashcardContentArea(
                        question     = question,
                        isFlipped    = isFlipped,
                        onFlip       = { isFlipped = true },
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
        SwipeableFlashcard(
            content      = content,
            isAnswered   = isFlipped,
            onFlip       = onFlip,
            onSwipeLeft  = onSwipeLeft,
            onSwipeRight = onSwipeRight,
        )
    }
}

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
    }
}

@Preview(name = "Quiz MCQ — Light", showBackground = true)
@Preview(name = "Quiz MCQ — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizMcqPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState, onBack = {},
                optionFormat = "Option %s", trueLabel = "True", falseLabel = "False",
                onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

@Preview(name = "Quiz Flashcard — Light", showBackground = true)
@Preview(name = "Quiz Flashcard — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizFlashcardPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState.copy(currentQuestion = previewFlashcardQuestion),
                optionFormat = "Option %s", trueLabel = "True", falseLabel = "False",
                onBack = {}, onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

@Preview(name = "Quiz Leech — Light", showBackground = true)
@Preview(name = "Quiz Leech — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuizLeechPreview() {
    SynapseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizContent(
                uiState = previewSessionUiState.copy(
                    showLeechAlert    = true,
                    lastAnswerCorrect = false,
                ),
                optionFormat = "Option %s", trueLabel = "True", falseLabel = "False",
                onBack = {}, onMcqAnswer = { _, _ -> }, onTfAnswer = { _, _ -> },
                onFlashcardSrsAnswer = { _, _ -> }, onMcqTfSrsAdvance = {},
                onDismissLeech = {}, onShowHint = {},
            )
        }
    }
}

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
    hint         = null,
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