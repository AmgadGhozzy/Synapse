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

/** Tracks which phase of a dual-generation flow we are in. */
enum class DualGenerationPhase {
    /** Only pack is being generated, or the pack phase of a dual run. */
    PACK,
    /** The summary phase of a dual run (follows PACK). */
    SUMMARY,
}

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

    // ── Generation Selection ──────────────────────────────────────
    val generatePack: Boolean = true,
    val generateSummary: Boolean = false,
    val summaryFocus: Set<String> = emptySet(),
    val summaryDepth: String = "standard",
    val summaryLanguage: String = "en",

    // ── Deep Thinking (Pro only) ──────────────────────────────────
    val thinkingEnabled: Boolean = false,
    val isThinkingLocked: Boolean = false,

    // ── Streaming generation state (Pack) ─────────────────────────
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

    // ── Streaming generation state (Summary) ──────────────────────
    /** Summary title from server (available after summary_metadata event) */
    val streamSummaryTitle: String = "",
    /** Summary emoji from server */
    val streamSummaryEmoji: String = "",
    /** Summary color from server */
    val streamSummaryColor: String = "",
    /** Sections completed so far (live counter) */
    val sectionsCompleted: Int = 0,
    /** Total sections expected from the server */
    val sectionsExpected: Int = 0,
    /** Summary concepts found */
    val summaryConcepts: Int = 0,
    /** Summary generation progress (0..1) */
    val summaryProgress: Float = 0f,
    /** Summary stage message */
    val summaryStage: String = "",

    // ── Dual generation phase ─────────────────────────────────────
    /** Which phase we are currently in during generation. */
    val generationPhase: DualGenerationPhase = DualGenerationPhase.PACK,

    // ── Generation / Done ─────────────────────────────────────────
    val sourceDescription: String = "",
    val generationProgress: Float = 0f,
    val packId: Long = 0L,
    /** Supabase packs.id (UUID) — available after successful generation. */
    val packUuid: String? = null,
    val generatedQuestions: List<QuestionUiModel> = emptyList(),
    /** Local Room summary ID — available after summary generation. */
    val summaryId: Long = 0L,
    /** True when pack was successfully generated in this session. */
    val packWasGenerated: Boolean = false,
    /** True when summary was successfully generated in this session. */
    val summaryWasGenerated: Boolean = false,

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
) {
    val canGenerate: Boolean
        get() = generatePack || generateSummary
}

