package io.synapse.ai.features.add_pdf.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.core.ui.state.QuestionUiModel
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.domain.model.QuestionType

enum class AddPdfStep {
    SELECT_PDF,
    //EXTRACTING,
    CONFIGURE,
    GENERATING,
    DONE
}

/** Source tabs shown on the first screen of the creation flow. */
enum class SourceTab { FILE, TEXT, WEB, YOUTUBE }

fun AddPdfStep.toIndicatorIndex() = when (this) {
    AddPdfStep.SELECT_PDF -> 0
    AddPdfStep.CONFIGURE  -> 1
    AddPdfStep.GENERATING -> 2
    AddPdfStep.DONE       -> 3
}

@Immutable
data class AddPdfUiState(
    val step: AddPdfStep = AddPdfStep.SELECT_PDF,
    val sourceTab: SourceTab = SourceTab.FILE,

    // ── File (PDF) tab ────────────────────────────────────────────
    val fileUri: String? = null,
    val fileName: String? = null,
    val fileSizeMb: Float = 0f,
    val ocrEnabled: Boolean = false,
    val extractedText: String? = null,
    val extractionProgress: Float = 0f,

    // ── Text tab ──────────────────────────────────────────────────
    val pasteText: String = "",

    // ── Web / YouTube tab ─────────────────────────────────────────
    val webUrl: String = "",

    // ── Configure step ────────────────────────────────────────────
    val packTitle: String = "",
    val questionCount: Int = 20,
    val selectedTypes: Set<QuestionType> = setOf(
        QuestionType.MCQ,
        QuestionType.TRUE_FALSE,
        QuestionType.FLASHCARD,
    ),
    val language: String = "en",
    val difficulty: String = "medium",          // "easy" | "medium" | "hard"
    val focusNotes: String = "",

    // ── Deep Thinking (Pro only) ──────────────────────────────────
    val thinkingEnabled: Boolean = false,
    val isThinkingLocked: Boolean = false,

    // ── Generation / Done ─────────────────────────────────────────
    val generationProgress: Float = 0f,
    val packId: Long = 0L,
    val generatedQuestions: List<QuestionUiModel> = emptyList(),

    // ── UI meta ───────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val isPackLimitReached: Boolean = false,
    val isOcrFeatureLocked: Boolean = false,

    /** True when the signed-in user has an active pro / premium status. */
    val isPro: Boolean = false,

    val maxPages: Int = 20,
    val maxFileSizeMb: Int = 5
)
