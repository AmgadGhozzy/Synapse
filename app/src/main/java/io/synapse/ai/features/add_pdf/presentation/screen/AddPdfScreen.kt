package io.synapse.ai.features.add_pdf.presentation.screen

import android.app.Activity
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import io.synapse.ai.R
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.theme.tokens.toShadow
import io.synapse.ai.core.ui.components.ErrorBanner
import io.synapse.ai.core.ui.components.PrimaryGradientButton
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.StepIndicator
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.core.ui.state.UiEffect
import io.synapse.ai.features.add_pdf.presentation.components.AddPdfHeader
import io.synapse.ai.features.add_pdf.presentation.components.ConfigureStep
import io.synapse.ai.features.add_pdf.presentation.components.DoneStep
import io.synapse.ai.features.add_pdf.presentation.components.GeneratingStep
import io.synapse.ai.features.add_pdf.presentation.components.LANGUAGES
import io.synapse.ai.features.add_pdf.presentation.components.LanguageBottomSheet
import io.synapse.ai.features.add_pdf.presentation.components.UploadStep
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfStep
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiEvent
import io.synapse.ai.features.add_pdf.presentation.state.SourceTab
import io.synapse.ai.features.add_pdf.presentation.state.toIndicatorIndex
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.style.TextOverflow
import io.synapse.ai.features.add_pdf.presentation.viewmodel.AddPdfViewModel
import io.synapse.ai.features.summary.presentation.screen.SummaryConfigContent
import io.synapse.ai.features.summary.presentation.state.SummaryConfig
import io.synapse.ai.features.add_pdf.presentation.components.GenerationOptionsSelector

@Composable
fun AddPdfScreen(
    onNavigateBack      : () -> Unit,
    onNavigateToSession : (Long) -> Unit,
    onNavigateToExport  : (Long) -> Unit,
    onNavigateToPremium : () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSummaryConfig: (String, String, String, SummaryConfig) -> Unit,
    viewModel           : AddPdfViewModel = hiltViewModel(),
) {
    val uiState            by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = rememberSnackbarController()
    val scrollState        = rememberScrollState()
    var showLanguagePicker by remember { mutableStateOf(false) }
    val context            = LocalContext.current
    val selectedLanguage   = LANGUAGES.find { it.code == uiState.language } ?: LANGUAGES[0]
    val stepLabels = listOf(
        stringResource(R.string.step_upload),
        stringResource(R.string.step_configure),
        stringResource(R.string.step_generate),
        stringResource(R.string.step_done),
    )
    
    var isSummarySelected by remember { mutableStateOf(false) }
    var summaryConfig by remember { mutableStateOf(SummaryConfig()) }

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

    // ── ML Kit Document Scanner ───────────────────────────────────────────────
    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setResultFormats(RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pdf?.let { pdf ->
                val pdfUri   = pdf.uri
                val pdfName  = "scanned_document.pdf"

                var fileSizeMb = 0f

                // 1. Try file:// path first (most reliable for scanned PDFs)
                if (pdfUri.scheme == "file") {
                    pdfUri.path?.let { path ->
                        val file = java.io.File(path)
                        if (file.exists()) fileSizeMb = file.length() / (1024f * 1024f)
                    }
                }

                // 2. Try ContentResolver SIZE column
                if (fileSizeMb == 0f) {
                    context.contentResolver.query(pdfUri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val sizeCol = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeCol != -1) {
                                val raw = cursor.getLong(sizeCol)
                                if (raw > 0) fileSizeMb = raw / (1024f * 1024f)
                            }
                        }
                    }
                }

                // 3. Fall back: count bytes from InputStream (accurate but reads the file)
                if (fileSizeMb == 0f) {
                    try {
                        context.contentResolver.openInputStream(pdfUri)?.use { stream ->
                            var count = 0L
                            val buf   = ByteArray(8192)
                            var read: Int
                            while (stream.read(buf).also { read = it } != -1) count += read
                            if (count > 0) fileSizeMb = count / (1024f * 1024f)
                        }
                    } catch (_: Exception) { /* ignore — fileSizeMb stays 0 */ }
                }

                val pageCount = pdf.pageCount.takeIf { it > 0 } ?: scanResult.pages?.size ?: 0
                viewModel.onEvent(AddPdfUiEvent.ScannedPdfReady(pdfUri.toString(), pdfName, fileSizeMb, pageCount))
            }
        }
    }

    BackHandler {
        viewModel.onEvent(AddPdfUiEvent.GoBack)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is UiEffect.Navigate     -> onNavigateToSession(uiState.packId)
                is UiEffect.NavigateBack -> onNavigateBack()
                is UiEffect.ShowToast    -> snackbarController.showToast(effect.type, effect.text.asString(context))

                is UiEffect.ShowPaywall -> {
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
                    .padding(horizontal = MaterialTheme.synapse.spacing.screen, vertical = MaterialTheme.synapse.spacing.s12),
            )
        },
        snackbarHost        = { snackbarController.SnackbarHost() },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { step ->
                if (step == AddPdfStep.SELECT_PDF || step == AddPdfStep.CONFIGURE) {
                    val canProceed by remember(
                        uiState.sourceTab, uiState.fileName,
                        uiState.pasteText, uiState.webUrl,
                        uiState.isPro, uiState.isLoading,
                    ) {
                        derivedStateOf {
                            when (uiState.sourceTab) {
                                SourceTab.FILE    -> uiState.fileName != null && !uiState.isLoading
                                SourceTab.TEXT    -> uiState.pasteText.length >= 10
                                SourceTab.WEB     -> uiState.isPro && uiState.webUrl.isNotBlank()
                                SourceTab.YOUTUBE -> uiState.isPro && uiState.webUrl.isNotBlank()
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .dropShadow(
                                shape = RoundedCornerShape(topStart = 24.adp, topEnd = 24.adp),
                                shadow = MaterialTheme.synapse.shadows.medium.toShadow()
                            ),
                        shape = RoundedCornerShape(topStart = 24.adp, topEnd = 24.adp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.synapse.spacing.screen)
                                .padding(top = 16.adp, bottom = MaterialTheme.synapse.spacing.screen)
                                .navigationBarsPadding()
                        ) {
                            if (step == AddPdfStep.SELECT_PDF) {
                                PrimaryGradientButton(
                                    text = stringResource(R.string.add_pdf_continue),
                                    iconRes = R.drawable.ic_chevron_right,
                                    enabled = canProceed,
                                    onClick = { viewModel.onEvent(AddPdfUiEvent.ContinueToConfigure) },
                                )
                            } else {
                                val canGenerate = uiState.canGenerate && (!uiState.generateSummary || summaryConfig.focus.isNotEmpty())
                                PrimaryGradientButton(
                                    text = if (uiState.generatePack && uiState.generateSummary) stringResource(R.string.generate_pack_and_summary)
                                           else if (uiState.generateSummary) stringResource(R.string.summary_config_generate)
                                           else stringResource(R.string.configure_generate),
                                    iconRes = R.drawable.ic_sparkles,
                                    enabled = canGenerate,
                                    onClick = {
                                        if (uiState.generatePack && !uiState.generateSummary) {
                                            viewModel.onEvent(AddPdfUiEvent.GeneratePack)
                                        } else if (uiState.generateSummary && !uiState.generatePack) {
                                            val type = when (uiState.sourceTab) {
                                                SourceTab.FILE -> "pdf"
                                                SourceTab.TEXT -> "text"
                                                SourceTab.YOUTUBE -> "youtube"
                                                SourceTab.WEB -> "url"
                                            }
                                            val name = when (uiState.sourceTab) {
                                                SourceTab.FILE -> uiState.fileName ?: "document"
                                                SourceTab.TEXT -> "Pasted Text"
                                                SourceTab.WEB, SourceTab.YOUTUBE -> uiState.webUrl
                                            }
                                            val content = when (uiState.sourceTab) {
                                                SourceTab.FILE -> uiState.fileUri ?: ""
                                                SourceTab.TEXT -> uiState.pasteText
                                                SourceTab.WEB, SourceTab.YOUTUBE -> uiState.webUrl
                                            }
                                            onNavigateToSummaryConfig(type, name, content, summaryConfig)
                                        } else {
                                            // BOTH selected: For now, we can launch the pack generation. The Edge function can be updated to generate both.
                                            // The user explicitly requested to update the edge function to handle both.
                                            viewModel.onEvent(AddPdfUiEvent.GeneratePack)
                                        }
                                    },
                                )
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(0.adp))
                }
            }
        }
    ) { innerPadding ->

//        AnimatedVisibility(
//            visible = uiState.step == AddPdfStep.DONE,
//            enter = fadeIn(),
//            exit = fadeOut()
//        ) {
//            ConfettiView(
//                modifier = Modifier.fillMaxSize()
//            )
//        }

        val columnModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .then(if (uiState.step != AddPdfStep.DONE) Modifier.verticalScroll(scrollState) else Modifier)
            .imePadding()
            .padding(horizontal = if (uiState.step != AddPdfStep.DONE) MaterialTheme.synapse.spacing.screen else 0.adp)

        Column(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.sectionGap),
        ) {
            if (uiState.step != AddPdfStep.DONE) {
                StepIndicator(
                    currentStep = uiState.step.toIndicatorIndex(),
                    steps = stepLabels,
                    modifier     = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.adp),
                )
            }

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
                }
            ) { step ->
                when (step) {
                    AddPdfStep.SELECT_PDF -> UploadStep(
                        uiState           = uiState,
                        onTabSelect       = { viewModel.onEvent(AddPdfUiEvent.SourceTabSelected(it)) },
                        onPickFile        = { filePicker.launch(arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) },
                        onClearFile       = { viewModel.onEvent(AddPdfUiEvent.ClearFile) },
                        onOcrToggle       = { viewModel.onEvent(AddPdfUiEvent.OcrToggled) },
                        onPasteTextChange = { viewModel.onEvent(AddPdfUiEvent.PasteTextChanged(it)) },
                        onWebUrlChange    = { viewModel.onEvent(AddPdfUiEvent.WebUrlChanged(it)) },
                        onWebTabLockedClick = { viewModel.onEvent(AddPdfUiEvent.WebTabLockedClicked) },
                        onContinue        = { viewModel.onEvent(AddPdfUiEvent.ContinueToConfigure) },
                        onSavePdf         = { viewModel.onEvent(AddPdfUiEvent.SavePdfClicked) },
                        onScanDocument    = {
                            scanner.getStartScanIntent(context as Activity)
                                .addOnSuccessListener { intentSender ->
                                    scannerLauncher.launch(
                                        IntentSenderRequest.Builder(intentSender).build()
                                    )
                                }
                                .addOnFailureListener { e ->
                                    snackbarController.info(e.localizedMessage ?: context.getString(R.string.error_scanner_unavailable))
                                }
                        },
                    )

                    AddPdfStep.CONFIGURE -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.adp)) {
                            GenerationOptionsSelector(
                                generatePack = uiState.generatePack,
                                generateSummary = uiState.generateSummary,
                                isPro = uiState.isPro,
                                onPackToggled = { viewModel.onEvent(AddPdfUiEvent.GeneratePackToggled) },
                                onSummaryToggled = { viewModel.onEvent(AddPdfUiEvent.GenerateSummaryToggled) },
                                onSummaryLockedClicked = { viewModel.onEvent(AddPdfUiEvent.ShowSummaryPaywall) },
                                modifier = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.screen)
                            )

                            if (uiState.generatePack) {
                                ConfigureStep(
                                    uiState               = uiState,
                                    onThinkingToggle     = { viewModel.onEvent(AddPdfUiEvent.ThinkingToggled) },
                                    onQuestionCountChange = { viewModel.onEvent(AddPdfUiEvent.QuestionCountChanged(it)) },
                                    onTypeToggle          = { viewModel.onEvent(AddPdfUiEvent.QuestionTypeToggled(it)) },
                                    onFocusNotesChange    = { viewModel.onEvent(AddPdfUiEvent.FocusNotesChanged(it)) },
                                    onLanguageClick       = { showLanguagePicker = true },
                                    onNavigateToPremium    = onNavigateToPremium,
                                    onGenerate            = { viewModel.onEvent(AddPdfUiEvent.GeneratePack) },
                                    onSavePdf             = { viewModel.onEvent(AddPdfUiEvent.SavePdfClicked) },
                                )
                            }
                            
                            if (uiState.generateSummary) {
                                SummaryConfigContent(
                                    config = summaryConfig,
                                    onConfigChange = { summaryConfig = it },
                                    onLanguageClick = { showLanguagePicker = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 140.adp),
                                )
                            }
                        }
                    }

                    AddPdfStep.GENERATING -> GeneratingStep(
                        uiState          = uiState,
                        onStartEarly     = { viewModel.onEvent(AddPdfUiEvent.StartStudyEarly) },
                    )

                    AddPdfStep.DONE -> DoneStep(
                        packName            = uiState.packTitle,
                        sourceDescription  = uiState.sourceDescription,
                        language          = selectedLanguage,
                        generatedQuestions = uiState.generatedQuestions,
                        filePageCount      = uiState.filePageCount,
                        onStartStudying   = { onNavigateToSession(uiState.packId) },
                        onExport          = { onNavigateToExport(uiState.packId) },
                        onBackToDashboard = onNavigateToDashboard,
                    )
                }
            }

            Spacer(Modifier.height(40.adp))
        }

        if (showLanguagePicker) {
            LanguageBottomSheet(
                selectedCode = if (isSummarySelected) summaryConfig.language else uiState.language,
                isPro        = uiState.isPro,
                onSelect     = {
                    if (isSummarySelected) {
                        summaryConfig = summaryConfig.copy(language = it)
                    } else {
                        viewModel.onEvent(AddPdfUiEvent.LanguageSelected(it))
                    }
                    showLanguagePicker = false
                },
                onDismiss    = { showLanguagePicker = false },
                onUpgrade  = { showLanguagePicker = false; onNavigateToPremium() },
            )
        }
    }
}
