package io.synapse.ai.features.add_pdf.presentation.screen

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.components.ConfettiAnimationType
import io.synapse.ai.core.ui.components.ConfettiView
import io.synapse.ai.core.ui.components.ErrorBanner
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.features.add_pdf.presentation.components.AddPdfHeader
import io.synapse.ai.features.add_pdf.presentation.components.ConfigureStep
import io.synapse.ai.features.add_pdf.presentation.components.DoneStep
import io.synapse.ai.features.add_pdf.presentation.components.GeneratingStep
import io.synapse.ai.features.add_pdf.presentation.components.LANGUAGES
import io.synapse.ai.features.add_pdf.presentation.components.LanguageBottomSheet
import io.synapse.ai.features.add_pdf.presentation.components.StepIndicator
import io.synapse.ai.features.add_pdf.presentation.components.UploadStep
import io.synapse.ai.features.add_pdf.presentation.components.shortLabel
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfStep
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiEvent
import io.synapse.ai.features.add_pdf.presentation.state.toIndicatorIndex
import io.synapse.ai.features.add_pdf.presentation.viewmodel.AddPdfViewModel

@Composable
fun AddPdfScreen(
    onNavigateBack     : () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel          : AddPdfViewModel = hiltViewModel(),
) {
    val uiState            by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val scrollState        = rememberScrollState()
    var showLanguagePicker by remember { mutableStateOf(false) }
    val context            = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        var fileSizeMb = 0f
        val fileName = context.contentResolver
            .query(uri, null, null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeCol = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeCol != -1) {
                        fileSizeMb = cursor.getLong(sizeCol) / (1024f * 1024f)
                    }
                    val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (col != -1) cursor.getString(col) else "document.pdf"
                } else "document.pdf"
            } ?: "document.pdf"
        viewModel.onEvent(AddPdfUiEvent.FileSelected(uri.toString(), fileName, fileSizeMb))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate     -> onNavigateToSession(uiState.packId)
                is UiEffect.NavigateBack -> onNavigateBack()
                is UiEffect.ShowToast    -> snackbarController.success(effect.text.asString(context))

                is UiEffect.ShowUpgradePrompt -> {
                    val feature = effect.feature.asString(context)
                    when {
                        // Hard-navigate to Premium for pack limit or web/YouTube feature.
                        uiState.isPackLimitReached -> onNavigateToPremium()
                        feature == context.getString(R.string.feature_web_youtube_import) ||
                        feature == context.getString(R.string.feature_pro_ocr) ->
                            onNavigateToPremium()
                        // Soft prompt (snackbar) for OCR, AI quota, etc.
                        else -> snackbarController.info(
                            context.getString(R.string.add_pdf_upgrade_prompt, feature)
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(uiState.step) {
        scrollState.animateScrollTo(0)
    }

    Scaffold(
        topBar = {
            AddPdfHeader(
                onBack   = { viewModel.onEvent(AddPdfUiEvent.GoBack) },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = MaterialTheme.synapse.spacing.screen, vertical = MaterialTheme.synapse.spacing.s12),
            )
        },
        snackbarHost        = { snackbarController.SnackbarHost() },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->

        AnimatedVisibility(
            visible = uiState.step == AddPdfStep.DONE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ConfettiView(
                animationType = ConfettiAnimationType.KONFETTI,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaterialTheme.synapse.spacing.screen),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.sectionGap),
        ) {
            StepIndicator(
                currentIndex = uiState.step.toIndicatorIndex(),
                modifier     = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.adp),
            )

            AnimatedVisibility(
                visible = uiState.error != null,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut(),
            ) {
                uiState.error?.let {
                    ErrorBanner(
                        message   = it.resolve(),
                        onDismiss = { viewModel.onEvent(AddPdfUiEvent.DismissError) },
                    )
                }
            }

            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = {
                    val targetIdx  = targetState.toIndicatorIndex()
                    val initialIdx = initialState.toIndicatorIndex()
                    if (targetIdx > initialIdx) {
                        slideInHorizontally  { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally  { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "wizard_step",
            ) { step ->
                when (step) {
                    AddPdfStep.SELECT_PDF -> UploadStep(
                        uiState           = uiState,
                        onTabSelect       = { viewModel.onEvent(AddPdfUiEvent.SourceTabSelected(it)) },
                        onPickFile        = { filePicker.launch(arrayOf("application/pdf")) },
                        onClearFile       = { viewModel.onEvent(AddPdfUiEvent.ClearFile) },
                        onOcrToggle       = { viewModel.onEvent(AddPdfUiEvent.OcrToggled) },
                        onPasteTextChange = { viewModel.onEvent(AddPdfUiEvent.PasteTextChanged(it)) },
                        onWebUrlChange    = { viewModel.onEvent(AddPdfUiEvent.WebUrlChanged(it)) },
                        onWebTabLockedClick = { viewModel.onEvent(AddPdfUiEvent.WebTabLockedClicked) },
                        onContinue        = { viewModel.onEvent(AddPdfUiEvent.ContinueToConfigure) },
                    )

                    AddPdfStep.CONFIGURE -> ConfigureStep(
                        uiState               = uiState,
                        onThinkingToggle     = { viewModel.onEvent(AddPdfUiEvent.ThinkingToggled) },
                        onQuestionCountChange = { viewModel.onEvent(AddPdfUiEvent.QuestionCountChanged(it)) },
                        onTypeToggle          = { viewModel.onEvent(AddPdfUiEvent.QuestionTypeToggled(it)) },
                        onFocusNotesChange    = { viewModel.onEvent(AddPdfUiEvent.FocusNotesChanged(it)) },
                        onLanguageClick       = { showLanguagePicker = true },
                        onGenerate            = { viewModel.onEvent(AddPdfUiEvent.GeneratePack) },
                    )

                    AddPdfStep.GENERATING -> GeneratingStep(
                        progress         = uiState.generationProgress,
                        questionCount    = uiState.questionCount,
                        language         = LANGUAGES.find { it.code == uiState.language }?.label
                            ?: stringResource(R.string.language_english),
                        focusNotesActive = uiState.focusNotes.isNotBlank(),
                    )

                    AddPdfStep.DONE -> DoneStep(
                        packName           = uiState.packTitle,
                        questionCount      = uiState.questionCount,
                        language           = LANGUAGES.find { it.code == uiState.language }?.label
                            ?: stringResource(R.string.language_english),
                        languageFlag       = LANGUAGES.find { it.code == uiState.language }?.flag
                            ?: "🇺🇸",
                        languageShort      = LANGUAGES.find { it.code == uiState.language }?.shortLabel()
                            ?: stringResource(R.string.language_english_short),
                        generatedQuestions = uiState.generatedQuestions,
                        onStartStudying    = { onNavigateToSession(uiState.packId) },
                        onBackToDashboard  = onNavigateBack,
                    )
                }
            }

            Spacer(Modifier.height(40.adp))
        }

        if (showLanguagePicker) {
            LanguageBottomSheet(
                selectedCode = uiState.language,
                onSelect     = {
                    viewModel.onEvent(AddPdfUiEvent.LanguageSelected(it))
                    showLanguagePicker = false
                },
                onDismiss    = { showLanguagePicker = false },
            )
        }
    }
}
