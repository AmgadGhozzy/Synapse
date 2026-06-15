package io.synapse.ai.features.add_pdf.presentation.state

import androidx.compose.runtime.Immutable
import io.synapse.ai.core.ui.model.QuestionUiModel
import io.synapse.ai.core.ui.state.UiText
import io.synapse.ai.domains.study.model.QuestionType

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
    val filePageCount: Int? = null,
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

    // ── Streaming generation state ────────────────────────────────
    /** Server-provided stage message (e.g. "Extracting key ideas…") */
    val streamStage: String = "",
    /** Index for local checklist animation during the Preparing phase */
    val progressMessageIndex: Int = 0,
    /** Number of distinct concepts the AI found in the source */
    val conceptsFound: Int = 0,
    /** Questions completed so far (live counter) */
    val questionsCompleted: Int = 0,
    /** Total questions expected from the server */
    val questionsExpected: Int = 0,
    /** Pack title from server (available after pack_meta event) */
    val streamPackTitle: String = "",
    /** Pack emoji from server */
    val streamPackEmoji: String = "",
    /** Pack color from server */
    val streamPackColor: String = "",
    /** True once enough questions exist for early start (≥40%) */
    val canStartEarly: Boolean = false,
    /** Whether generation is still running in background after early start */
    val generatingInBackground: Boolean = false,

    // ── Generation / Done ─────────────────────────────────────────
    val sourceDescription: String = "",
    val generationProgress: Float = 0f,
    val packId: Long = 0L,
    /** Supabase packs.id (UUID) — available after successful generation. */
    val packUuid: String? = null,
    val generatedQuestions: List<QuestionUiModel> = emptyList(),

    // ── UI meta ───────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float? = null,
    val error: UiText? = null,
    val isPackLimitReached: Boolean = false,
    val isOcrFeatureLocked: Boolean = false,

    /** True when the signed-in user has an active pro / gold status. */
    val isPro: Boolean = false,

    val maxPages: Int = 20,
    val maxFileSizeMb: Int = 5
)

