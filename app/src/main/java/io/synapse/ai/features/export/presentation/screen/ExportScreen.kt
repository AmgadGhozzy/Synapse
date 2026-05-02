package io.synapse.ai.features.export.presentation.screen

import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.synapse.ai.R
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.synapse
import io.synapse.ai.core.theme.tokens.adp
import io.synapse.ai.core.ui.components.CloseButton
import io.synapse.ai.core.ui.components.LoadingIndicator
import io.synapse.ai.core.ui.components.SnackbarHost
import io.synapse.ai.core.ui.components.StepIndicator
import io.synapse.ai.core.ui.components.SynapseSwitch
import io.synapse.ai.core.ui.components.rememberSnackbarController
import io.synapse.ai.features.export.domain.ExportTemplate
import io.synapse.ai.features.export.domain.InstitutionHeader
import io.synapse.ai.features.export.presentation.state.ExportEffect
import io.synapse.ai.features.export.presentation.state.ExportEvent
import io.synapse.ai.features.export.presentation.state.ExportStep
import io.synapse.ai.features.export.presentation.state.ExportUiState
import io.synapse.ai.features.export.presentation.state.stepLabels
import io.synapse.ai.features.export.presentation.state.stepsFor
import io.synapse.ai.features.export.presentation.viewmodel.ExportViewModel

@Composable
fun ExportScreen(
    viewModel: ExportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarController = rememberSnackbarController()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ExportEffect.NavigateBack -> onNavigateBack()
                ExportEffect.NavigateToPremium -> onNavigateToPremium()
                is ExportEffect.ShareFile -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = effect.mimeType
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            context.getString(R.string.share_pdf)
                        )
                    )
                }
            }
        }
    }

    BackHandler(enabled = !state.isExporting) {
        viewModel.onEvent(ExportEvent.PreviousStep)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { snackbarController.SnackbarHost() },
        containerColor = Color.Transparent,
        topBar = {
            ExportTopBar(
                packTitle = state.packTitle,
                currentStep = state.currentStepIndex + 1,
                totalSteps = state.steps.size,
                onBack = { viewModel.onEvent(ExportEvent.PreviousStep) },
            )
        },
        bottomBar = {
            if (!state.isLastStep && !state.isExporting) {
                ExportBottomBar(
                    isFirstStep = state.isFirstStep,
                    isPenultimateStep = state.currentStepIndex == state.steps.lastIndex - 1,
                    onBack = { viewModel.onEvent(ExportEvent.PreviousStep) },
                    onNext = { viewModel.onEvent(ExportEvent.NextStep) },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            StepIndicator(
                currentStep = state.currentStepIndex,
                steps = stepsFor(state.options.template).stepLabels(),
            )

            AnimatedContent(
                targetState = state.currentStep
            ) { step ->
                StepContent(
                    step = step,
                    state = state,
                    viewModel = viewModel,
                )
            }
        }

        // Error dialog
        if (state.error != null) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(ExportEvent.DismissError) },
                title = { Text(stringResource(R.string.export_error)) },
                text = { Text(state.error!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(ExportEvent.DismissError) }) {
                        Text(stringResource(R.string.ok))
                    }
                },
            )
        }

        // Success snackbar
        if (state.exportedUri != null && !state.isExporting) {
            val successMessage =
                stringResource(R.string.export_saved_to_downloads, state.exportedFileName)
            val actionLabel = stringResource(R.string.view_pdf)
            LaunchedEffect(state.exportedUri) {
                snackbarController.show(
                    message = successMessage,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Long,
                    action = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(state.exportedUri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    context.getString(R.string.open_pdf_with)
                                )
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ExportTopBar(
    packTitle: String,
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.synapse.spacing.screen,
                vertical = MaterialTheme.synapse.spacing.s16,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {
        CloseButton(onClick = onBack)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.export_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = packTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = stringResource(R.string.export_step_progress, currentStep, totalSteps),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExportBottomBar(
    isFirstStep: Boolean,
    isPenultimateStep: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = MaterialTheme.synapse.spacing.s2 / 2,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(MaterialTheme.synapse.spacing.s16),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.synapse.radius.lg,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                contentPadding = PaddingValues(vertical = MaterialTheme.synapse.spacing.s16),
            ) {
                Text(
                    text = if (isFirstStep) stringResource(R.string.cancel)
                    else stringResource(R.string.back),
                )
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f),
                shape = MaterialTheme.synapse.radius.lg,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
                contentPadding = PaddingValues(vertical = MaterialTheme.synapse.spacing.s16),
            ) {
                Text(
                    text = if (isPenultimateStep) stringResource(R.string.continue_to_export)
                    else stringResource(R.string.next),
                )
                if (!isPenultimateStep) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = MaterialTheme.synapse.spacing.s8)
                            .size(16.adp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepContent(
    step: ExportStep,
    state: ExportUiState,
    viewModel: ExportViewModel,
    modifier: Modifier = Modifier,
) {
    val titleRes = when (step) {
        ExportStep.TEMPLATE -> R.string.choose_template
        ExportStep.OPTIONS -> R.string.export_options
        ExportStep.HEADER -> R.string.institution_header
        ExportStep.EXPORT -> R.string.export_ready
    }
    val descRes = when (step) {
        ExportStep.TEMPLATE -> R.string.choose_template_desc
        ExportStep.OPTIONS -> R.string.export_options_desc
        ExportStep.HEADER -> R.string.institution_header_desc
        ExportStep.EXPORT -> R.string.export_ready_desc
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.synapse.spacing.screen)
            .padding(bottom = MaterialTheme.synapse.spacing.xl),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.s4),
        )
        Text(
            text = stringResource(descRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = MaterialTheme.synapse.spacing.lg),
        )

        when (step) {
            ExportStep.TEMPLATE -> Step1Template(state, viewModel)
            ExportStep.OPTIONS -> Step2Options(state, viewModel)
            ExportStep.HEADER -> Step3Header(state, viewModel)
            ExportStep.EXPORT -> Step4Export(state, viewModel)
        }
    }
}

// ── Step 1: Template ──────────────────────────────────────────────────────────

@Composable
private fun Step1Template(
    state: ExportUiState,
    viewModel: ExportViewModel,
    modifier: Modifier = Modifier,
) {
    val templates = listOf(
        TemplateUiModel(
            template = ExportTemplate.STUDY,
            labelRes = R.string.template_study,
            descriptionRes = R.string.template_study_desc,
            tagRes = R.string.template_tag_study,
            iconRes = R.drawable.ic_book_open,
            accentColor = MaterialTheme.colorScheme.primary,
        ),
        TemplateUiModel(
            template = ExportTemplate.EXAM,
            labelRes = R.string.template_exam,
            descriptionRes = R.string.template_exam_desc,
            tagRes = R.string.template_tag_exam,
            iconRes = R.drawable.ic_file_text,
            accentColor = MaterialTheme.synapse.semantic.success,
        ),
        TemplateUiModel(
            template = ExportTemplate.TEACHER,
            labelRes = R.string.template_teacher,
            descriptionRes = R.string.template_teacher_desc,
            tagRes = R.string.template_tag_teacher,
            iconRes = R.drawable.ic_graduation_cap,
            accentColor = MaterialTheme.synapse.semantic.accent,
        ),
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {
        templates.forEach { model ->
            TemplateCard(
                model = model,
                isSelected = state.options.template == model.template,
                onClick = { viewModel.onEvent(ExportEvent.TemplateSelected(model.template)) },
            )
        }
    }
}


// ── Step 2: Options ───────────────────────────────────────────────────────────

@Composable
private fun Step2Options(
    state: ExportUiState,
    viewModel: ExportViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
    ) {
        if (state.options.template == ExportTemplate.STUDY) {
            OptionToggleRow(
                iconRes = R.drawable.ic_book_open,
                labelRes = R.string.include_answers,
                descriptionRes = R.string.include_answers_desc,
                checked = state.options.includeAnswers,
                onCheckedChange = {
                    viewModel.onEvent(ExportEvent.OptionsChanged(state.options.copy(includeAnswers = it)))
                },
            )
        } else {
            OptionToggleRow(
                iconRes = R.drawable.ic_bookmark,
                labelRes = R.string.include_answer_key,
                descriptionRes = R.string.include_answer_key_desc,
                checked = state.options.includeAnswerKey,
                onCheckedChange = {
                    viewModel.onEvent(ExportEvent.OptionsChanged(state.options.copy(includeAnswerKey = it)))
                },
            )
            if (state.options.template == ExportTemplate.EXAM) {
                OptionToggleRow(
                    iconRes = R.drawable.ic_shuffle,
                    labelRes = R.string.shuffle_questions,
                    descriptionRes = R.string.shuffle_questions_desc,
                    checked = state.options.shuffleQuestions,
                    onCheckedChange = {
                        viewModel.onEvent(
                            ExportEvent.OptionsChanged(
                                state.options.copy(
                                    shuffleQuestions = it
                                )
                            )
                        )
                    },
                )
            }
            OptionToggleRow(
                iconRes = R.drawable.ic_star,
                labelRes = R.string.show_marks,
                descriptionRes = R.string.show_marks_desc,
                checked = state.options.showMarks,
                onCheckedChange = {
                    viewModel.onEvent(ExportEvent.OptionsChanged(state.options.copy(showMarks = it)))
                },
            )
        }
    }
}

@Composable
private fun OptionToggleRow(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    @StringRes descriptionRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.synapse.radius.lg,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            MaterialTheme.synapse.spacing.s2 / 2,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.synapse.spacing.s16),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.adp)
                    .clip(MaterialTheme.synapse.radius.md)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.adp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SynapseSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

// ── Step 3: Header ────────────────────────────────────────────────────────────

@Composable
private fun Step3Header(
    state: ExportUiState,
    viewModel: ExportViewModel,
    modifier: Modifier = Modifier,
) {
    val h = state.options.institutionHeader
    val onChange: (InstitutionHeader) -> Unit = { viewModel.onEvent(ExportEvent.HeaderChanged(it)) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s16),
    ) {
        // Tip banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.synapse.radius.md,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(MaterialTheme.synapse.spacing.s12),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.adp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = stringResource(R.string.header_tip),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // TEACHER template — minimal header (course + professor only)
        if (state.options.template == ExportTemplate.TEACHER) {
            TeacherHeaderFields(h = h, onChange = onChange)
        } else {
            // EXAM template — full institutional header
            ExamHeaderFields(h = h, onChange = onChange)
        }
    }
}

@Composable
private fun TeacherHeaderFields(
    h: InstitutionHeader,
    onChange: (InstitutionHeader) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s16),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12)) {
            InstitutionField(
                labelRes = R.string.course_name,
                value = h.courseName,
                placeholderRes = R.string.placeholder_course_name,
                onValueChange = { onChange(h.copy(courseName = it)) },
                modifier = Modifier.weight(1f),
            )
            InstitutionField(
                labelRes = R.string.course_code,
                value = h.courseCode,
                placeholderRes = R.string.placeholder_course_code,
                onValueChange = { onChange(h.copy(courseCode = it)) },
                modifier = Modifier.weight(1f),
            )
        }
        InstitutionField(
            labelRes = R.string.professor,
            value = h.professorName,
            placeholderRes = R.string.placeholder_professor,
            onValueChange = { onChange(h.copy(professorName = it)) },
        )
    }
}

@Composable
private fun ExamHeaderFields(
    h: InstitutionHeader,
    onChange: (InstitutionHeader) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s16),
    ) {
        // Institution block
        InstitutionField(
            labelRes = R.string.university,
            value = h.university,
            placeholderRes = R.string.placeholder_university,
            onValueChange = { onChange(h.copy(university = it)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12)) {
            InstitutionField(
                labelRes = R.string.faculty,
                value = h.faculty,
                placeholderRes = R.string.placeholder_faculty,
                onValueChange = { onChange(h.copy(faculty = it)) },
                modifier = Modifier.weight(1f),
            )
            InstitutionField(
                labelRes = R.string.department,
                value = h.department,
                placeholderRes = R.string.placeholder_department,
                onValueChange = { onChange(h.copy(department = it)) },
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.synapse.spacing.s4))

        // Course block
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12)) {
            InstitutionField(
                labelRes = R.string.course_name,
                value = h.courseName,
                placeholderRes = R.string.placeholder_course_name,
                onValueChange = { onChange(h.copy(courseName = it)) },
                modifier = Modifier.weight(1f),
            )
            InstitutionField(
                labelRes = R.string.course_code,
                value = h.courseCode,
                placeholderRes = R.string.placeholder_course_code,
                onValueChange = { onChange(h.copy(courseCode = it)) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12)) {
            InstitutionField(
                labelRes = R.string.professor,
                value = h.professorName,
                placeholderRes = R.string.placeholder_professor,
                onValueChange = { onChange(h.copy(professorName = it)) },
                modifier = Modifier.weight(1f),
            )
            InstitutionField(
                labelRes = R.string.academic_year,
                value = h.academicYear,
                placeholderRes = R.string.placeholder_academic_year,
                onValueChange = { onChange(h.copy(academicYear = it)) },
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.synapse.spacing.s4))

        // Exam-specific block
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12)) {
            InstitutionField(
                labelRes = R.string.exam_type,
                value = h.examType,
                placeholderRes = R.string.placeholder_exam_type,
                onValueChange = { onChange(h.copy(examType = it)) },
                modifier = Modifier.weight(1f),
            )
            InstitutionField(
                labelRes = R.string.program,
                value = h.program,
                placeholderRes = R.string.placeholder_program,
                onValueChange = { onChange(h.copy(program = it)) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12)) {
            InstitutionField(
                labelRes = R.string.level,
                value = h.level,
                placeholderRes = R.string.placeholder_level,
                onValueChange = { onChange(h.copy(level = it)) },
                modifier = Modifier.weight(1f),
            )
            InstitutionField(
                labelRes = R.string.section,
                value = h.section,
                placeholderRes = R.string.placeholder_section,
                onValueChange = { onChange(h.copy(section = it)) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.s12)) {
            InstitutionField(
                labelRes = R.string.semester,
                value = h.semester,
                placeholderRes = R.string.placeholder_semester,
                onValueChange = { onChange(h.copy(semester = it)) },
                modifier = Modifier.weight(1f),
            )
            InstitutionField(
                labelRes = R.string.exam_date,
                value = h.examDate,
                placeholderRes = R.string.placeholder_exam_date,
                onValueChange = { onChange(h.copy(examDate = it)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun InstitutionField(
    @StringRes labelRes: Int,
    value: String,
    @StringRes placeholderRes: Int,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(labelRes)) },
        placeholder = { Text(stringResource(placeholderRes)) },
        singleLine = true,
        shape = MaterialTheme.synapse.radius.md,
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

// ── Step 4: Export ────────────────────────────────────────────────────────────

@Composable
private fun Step4Export(
    state: ExportUiState,
    viewModel: ExportViewModel,
    modifier: Modifier = Modifier,
) {
    val templateColor = when (state.options.template) {
        ExportTemplate.STUDY -> MaterialTheme.colorScheme.primary
        ExportTemplate.EXAM -> MaterialTheme.synapse.semantic.success
        ExportTemplate.TEACHER -> MaterialTheme.synapse.semantic.accent
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.synapse.spacing.lg),
    ) {
        // Summary card
        ExportSummaryCard(
            state = state,
            templateColor = templateColor,
        )

        // Free tier notice
        if (!state.isPro) {
            FreeTierNotice(exportsRemaining = state.exportsRemaining)
        }

        // Actions
        when {
            state.isExporting -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.synapse.spacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }

            state.exportedUri != null -> {
                val context = LocalContext.current
                ExportActionButton(
                    iconRes = R.drawable.ic_share,
                    titleRes = R.string.share_pdf,
                    subtitle = state.exportedFileName,
                    isPrimary = true,
                    onClick = { viewModel.onEvent(ExportEvent.ShareExport) },
                )
                Spacer(Modifier.height(MaterialTheme.synapse.spacing.s12))
                ExportActionButton(
                    iconRes = R.drawable.ic_printer,
                    titleRes = R.string.print_pdf,
                    subtitleRes = R.string.print_pdf_desc,
                    isPrimary = false,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, state.exportedUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            component = ComponentName(
                                "com.android.bips",
                                "com.android.bips.PdfPrintActivity",
                            )
                        }
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    context.getString(R.string.open_pdf_with)
                                )
                            )
                        }.onFailure {
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(state.exportedUri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching {
                                context.startActivity(
                                    Intent.createChooser(
                                        viewIntent,
                                        context.getString(R.string.open_pdf_with)
                                    )
                                )
                            }
                        }
                    },
                )
            }

            else -> {
                ExportActionButton(
                    iconRes = R.drawable.ic_download,
                    titleRes = R.string.save_pdf,
                    subtitleRes = R.string.save_pdf_desc,
                    isPrimary = true,
                    enabled = state.isPro || state.exportsRemaining > 0,
                    onClick = { viewModel.onEvent(ExportEvent.StartExport) },
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Export Top Bar — Light", showBackground = true)
@Preview(
    name = "Export Top Bar — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ExportTopBarPreview() {
    SynapseTheme {
        ExportTopBar(
            packTitle = "Machine Learning Basics",
            currentStep = 2,
            totalSteps = 4,
            onBack = {},
        )
    }
}

@Preview(name = "Template Card — Light", showBackground = true)
@Preview(
    name = "Template Card — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun TemplateCardPreview() {
    SynapseTheme {
        Column(
            modifier = Modifier.padding(16.adp),
            verticalArrangement = Arrangement.spacedBy(12.adp),
        ) {
            TemplateCard(
                model = TemplateUiModel(
                    template = ExportTemplate.EXAM,
                    labelRes = R.string.template_exam,
                    descriptionRes = R.string.template_exam_desc,
                    tagRes = R.string.template_tag_exam,
                    iconRes = R.drawable.ic_file_text,
                    accentColor = Color(0xFF059669),
                ),
                isSelected = true,
                onClick = {},
            )
        }
    }
}

@Preview(name = "Option Toggle Row — Light", showBackground = true)
@Preview(
    name = "Option Toggle Row — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun OptionToggleRowPreview() {
    SynapseTheme {
        Column(modifier = Modifier.padding(16.adp)) {
            OptionToggleRow(
                iconRes = R.drawable.ic_book_open,
                labelRes = R.string.include_answers,
                descriptionRes = R.string.include_answers_desc,
                checked = true,
                onCheckedChange = {},
            )
        }
    }
}

@Preview(name = "Export Bottom Bar — Light", showBackground = true)
@Preview(
    name = "Export Bottom Bar — Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ExportBottomBarPreview() {
    SynapseTheme {
        ExportBottomBar(
            isFirstStep = false,
            isPenultimateStep = false,
            onBack = {},
            onNext = {},
        )
    }
}