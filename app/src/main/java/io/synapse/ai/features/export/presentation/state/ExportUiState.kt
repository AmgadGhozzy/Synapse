package io.synapse.ai.features.export.presentation.state

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import io.synapse.ai.R
import io.synapse.ai.features.export.domain.ExportOptions
import io.synapse.ai.features.export.domain.ExportTemplate
import io.synapse.ai.features.export.domain.InstitutionHeader

// ── Step model ────────────────────────────────────────────────────────────────

enum class ExportStep { TEMPLATE, OPTIONS, HEADER, EXPORT }

/** Returns the ordered steps for the selected template. */
fun stepsFor(template: ExportTemplate): List<ExportStep> = when (template) {
    ExportTemplate.STUDY, ExportTemplate.TEACHER -> listOf(ExportStep.TEMPLATE, ExportStep.OPTIONS, ExportStep.EXPORT)
    ExportTemplate.EXAM -> listOf(ExportStep.TEMPLATE, ExportStep.OPTIONS, ExportStep.HEADER, ExportStep.EXPORT)
}

/** Returns localized labels for the steps. */
@Composable
fun List<ExportStep>.stepLabels(): List<String> {
    return this.map { step ->
        when (step) {
            ExportStep.TEMPLATE -> stringResource(R.string.step_template)
            ExportStep.OPTIONS -> stringResource(R.string.step_options)
            ExportStep.HEADER -> stringResource(R.string.step_header)
            ExportStep.EXPORT -> stringResource(R.string.step_export)
        }
    }
}

// ── UI State ──────────────────────────────────────────────────────────────────

@Immutable
data class ExportUiState(
    val packTitle: String = "",
    val questionCount: Int = 0,

    // ── Step wizard ──
    val options: ExportOptions = ExportOptions(),
    val steps: List<ExportStep> = stepsFor(ExportOptions().template),
    val currentStepIndex: Int = 0,

    // ── Export execution ──
    val isExporting: Boolean = false,
    val exportedUri: Uri? = null,
    val exportedFileName: String = "",
    val error: String? = null,

    // ── Monetisation ──
    val isPro: Boolean = false,
    val freeTierExportsUsed: Int = 0,
    val freeTierExportLimit: Int = 3,
) {
    val currentStep: ExportStep get() = steps[currentStepIndex]
    val isFirstStep: Boolean get() = currentStepIndex == 0
    val isLastStep: Boolean get() = currentStepIndex == steps.lastIndex
    val canGoNext: Boolean get() = !isLastStep && !isExporting
    val exportsRemaining: Int get() = (freeTierExportLimit - freeTierExportsUsed).coerceAtLeast(0)
}

// ── UI Events (user → VM) ─────────────────────────────────────────────────────

sealed interface ExportEvent {
    data class TemplateSelected(val template: ExportTemplate) : ExportEvent
    data class OptionsChanged(val options: ExportOptions) : ExportEvent
    data class HeaderChanged(val header: InstitutionHeader) : ExportEvent
    data object NextStep : ExportEvent
    data object PreviousStep : ExportEvent
    data object StartExport : ExportEvent
    data object ShareExport : ExportEvent
    data object DismissError : ExportEvent
}

// ── UI Effects (VM → Screen) ──────────────────────────────────────────────────

sealed interface ExportEffect {
    data class ShareFile(val uri: Uri, val mimeType: String = "application/pdf") : ExportEffect
    data object NavigateToPremium : ExportEffect
    data object NavigateBack : ExportEffect
}
