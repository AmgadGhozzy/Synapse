package io.synapse.ai.core.analytics.model

/**
 * Typed, sealed event taxonomy for the Synapse analytics system.
 *
 * **Privacy contract:**
 * - NO study text, PDF content, prompts, or generated answers.
 * - NO emails, user IDs, or personally identifiable information.
 * - Raw timing values are included only for performance debugging
 *   (generation_duration_ms) — not for individual profiling.
 *
 * Events are bucketed by analytical purpose:
 *   - **Lifecycle**      → app open, session bounds
 *   - **Onboarding**     → funnel entry
 *   - **Auth**           → login/signup method
 *   - **Generation**     → pack creation funnel + performance
 *   - **Content**        → pack/question management
 *   - **Export**         → export funnel + share
 *   - **Monetisation**   → paywall + purchase funnel
 *   - **Study**          → learning session behaviour
 *   - **Performance**    → slow operations
 *   - **Errors**         → non-fatal diagnostics
 *   - **Privacy**        → consent changes
 *   - **Navigation**     → screen views
 */
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
) {

    data object AppOpened : AnalyticsEvent(name = "app_opened")

    data object OnboardingStarted : AnalyticsEvent(name = "onboarding_started")

    data object OnboardingCompleted : AnalyticsEvent(name = "onboarding_completed")

    data class LoginCompleted(val method: String) : AnalyticsEvent(
        name = "login_completed",
        params = mapOf("method" to method),
    )

    data class SignupCompleted(val method: String) : AnalyticsEvent(
        name = "signup_completed",
        params = mapOf("method" to method),
    )

    data object Logout : AnalyticsEvent(name = "logout")

    data class SourceSelected(val sourceType: String) : AnalyticsEvent(
        name = "source_selected",
        params = mapOf("source_type" to sourceType),
    )

    data class GenerationStarted(
        val sourceType: String,
        val language: String,
        val questionCount: Int,
        val thinkingEnabled: Boolean,
    ) : AnalyticsEvent(
        name = "generation_started",
        params = mapOf(
            "source_type" to sourceType,
            "language" to language,
            "question_count" to questionCount,
            "thinking_enabled" to thinkingEnabled,
        ),
    )

    data class GenerationSuccess(
        val sourceType: String,
        val language: String,
        val questionCount: Int,
        val durationMs: Long,
    ) : AnalyticsEvent(
        name = "generation_success",
        params = mapOf(
            "source_type" to sourceType,
            "language" to language,
            "question_count" to questionCount,
            "duration_ms" to durationMs,
        ),
    )

    data class GenerationFailed(
        val sourceType: String,
        val reason: String,   // error class name — NO message text
    ) : AnalyticsEvent(
        name = "generation_failed",
        params = mapOf(
            "source_type" to sourceType,
            "reason" to reason,
        ),
    )

    data class GenerationCancelled(
        val sourceType: String,
        val questionsCompleted: Int,
    ) : AnalyticsEvent(
        name = "generation_cancelled",
        params = mapOf(
            "source_type" to sourceType,
            "questions_completed" to questionsCompleted,
        ),
    )

    data class EmptyGeneration(val sourceType: String) : AnalyticsEvent(
        name = "empty_generation",
        params = mapOf("source_type" to sourceType),
    )

    data class PackCreated(
        val sourceType: String,
        val questionCount: Int,
    ) : AnalyticsEvent(
        name = "pack_created",
        params = mapOf(
            "source_type" to sourceType,
            "question_count" to questionCount,
        ),
    )

    data class PackOpened(val questionCount: Int) : AnalyticsEvent(
        name = "pack_opened",
        params = mapOf("question_count" to questionCount),
    )

    data object PackEdited : AnalyticsEvent(name = "pack_edited")

    data class PackDeleted(
        val questionCount: Int,
        val masteryPct: Int,
    ) : AnalyticsEvent(
        name = "pack_deleted",
        params = mapOf(
            "question_count" to questionCount,
            "mastery_pct" to masteryPct,
        ),
    )

    data object QuestionEdited : AnalyticsEvent(name = "question_edited")
    data object QuestionDeleted : AnalyticsEvent(name = "question_deleted")
    data object QuestionRestored : AnalyticsEvent(name = "question_restored")
    data object QuestionReordered : AnalyticsEvent(name = "question_reordered")

    data class ExportOpened(val questionCount: Int) : AnalyticsEvent(
        name = "export_opened",
        params = mapOf("question_count" to questionCount),
    )

    data class TemplateSelected(val templateType: String) : AnalyticsEvent(
        name = "template_selected",
        params = mapOf("template_type" to templateType),
    )

    data class PdfExportStarted(
        val templateType: String,
        val includeAnswers: Boolean,
    ) : AnalyticsEvent(
        name = "pdf_export_started",
        params = mapOf(
            "template_type" to templateType,
            "include_answers" to includeAnswers,
        ),
    )

    data class PdfExportSuccess(
        val templateType: String,
        val questionCount: Int,
        val durationMs: Long,
    ) : AnalyticsEvent(
        name = "pdf_export_success",
        params = mapOf(
            "template_type" to templateType,
            "question_count" to questionCount,
            "duration_ms" to durationMs,
        ),
    )

    data class PdfExportFailed(
        val templateType: String,
        val reason: String,
    ) : AnalyticsEvent(
        name = "pdf_export_failed",
        params = mapOf(
            "template_type" to templateType,
            "reason" to reason,
        ),
    )

    data class ShareClicked(val exportType: String) : AnalyticsEvent(
        name = "share_clicked",
        params = mapOf("export_type" to exportType),
    )

    data class PaywallViewed(val trigger: String) : AnalyticsEvent(
        name = "paywall_viewed",
        params = mapOf("trigger" to trigger),
    )

    data class PurchaseStarted(val planId: String) : AnalyticsEvent(
        name = "purchase_started",
        params = mapOf("plan_id" to planId),
    )

    data class PurchaseCompleted(val planId: String) : AnalyticsEvent(
        name = "purchase_completed",
        params = mapOf("plan_id" to planId),
    )

    data class PurchaseFailed(val planId: String, val reason: String) : AnalyticsEvent(
        name = "purchase_failed",
        params = mapOf(
            "plan_id" to planId,
            "reason" to reason,
        ),
    )

    data object SubscriptionRestored : AnalyticsEvent(name = "subscription_restored")

    data class SessionStarted(
        val packId: Long,
        val mode: String,
        val questionCount: Int,
    ) : AnalyticsEvent(
        name = "session_started",
        params = mapOf(
            "pack_id" to packId.toString(),
            "mode" to mode,
            "question_count" to questionCount,
        ),
    )

    data class SessionCompleted(
        val durationSec: Long,
        val accuracy: Float,
        val itemsCount: Int,
        val mode: String,
    ) : AnalyticsEvent(
        name = "session_completed",
        params = mapOf(
            "duration_sec" to durationSec,
            "accuracy_pct" to (accuracy * 100).toInt(),
            "items_count" to itemsCount,
            "mode" to mode,
        ),
    )

    data class SessionAbandoned(
        val durationSec: Long,
        val progressPct: Int,
        val questionsAnswered: Int,
    ) : AnalyticsEvent(
        name = "session_abandoned",
        params = mapOf(
            "duration_sec" to durationSec,
            "progress_pct" to progressPct,
            "questions_answered" to questionsAnswered,
        ),
    )

    /**
     * Fired after each card is reviewed.
     * Response time is bucketed (fast/normal/slow) — not raw ms.
     */
    data class WordPracticed(
        val difficultyBucket: String,
        val responseTimeBucket: String,
        val isCorrect: Boolean,
    ) : AnalyticsEvent(
        name = "word_practiced",
        params = mapOf(
            "difficulty" to difficultyBucket,
            "response_time" to responseTimeBucket,
            "is_correct" to isCorrect,
        ),
    )

    data class MasteryChanged(
        val fromLevel: Int,
        val toLevel: Int,
        val direction: String, // "up" | "down"
    ) : AnalyticsEvent(
        name = "mastery_changed",
        params = mapOf(
            "from_level" to fromLevel,
            "to_level" to toLevel,
            "direction" to direction,
        ),
    )

    data class LeechDetected(val packId: Long) : AnalyticsEvent(
        name = "leech_detected",
        params = mapOf("pack_id" to packId.toString()),
    )

    data class SlowGenerationDetected(
        val durationMs: Long,
        val sourceType: String,
    ) : AnalyticsEvent(
        name = "slow_generation_detected",
        params = mapOf(
            "duration_ms" to durationMs,
            "source_type" to sourceType,
        ),
    )

    data class PdfRenderSlow(val durationMs: Long) : AnalyticsEvent(
        name = "pdf_render_slow",
        params = mapOf("duration_ms" to durationMs),
    )

    data class UnexpectedError(
        val location: String,
        val errorType: String,
    ) : AnalyticsEvent(
        name = "unexpected_error",
        params = mapOf(
            "location" to location,
            "error_type" to errorType,
        ),
    )

    data class InvalidPdf(val reason: String) : AnalyticsEvent(
        name = "invalid_pdf",
        params = mapOf("reason" to reason),
    )

    data class AnalyticsOptInChanged(val enabled: Boolean) : AnalyticsEvent(
        name = "analytics_opt_in_changed",
        params = mapOf("enabled" to enabled),
    )

    data class ScreenViewed(val screenName: String) : AnalyticsEvent(
        name = "screen_view",
        params = mapOf("screen_name" to screenName),
    )

    companion object {
        const val SLOW_GENERATION_THRESHOLD_MS = 45_000L

        const val SLOW_PDF_RENDER_THRESHOLD_MS = 8_000L

        fun bucketResponseTime(ms: Long): String = when {
            ms < 2_000 -> "fast"
            ms < 5_000 -> "normal"
            else -> "slow"
        }

        fun bucketDifficulty(easeFactor: Float): String = when {
            easeFactor < 1.8f -> "hard"
            easeFactor < 2.5f -> "medium"
            else -> "easy"
        }

        fun routeToScreenName(route: String): String = when {
            route.startsWith("dashboard") -> "dashboard"
            route.startsWith("library") -> "library"
            route.startsWith("marketplace") -> "marketplace"
            route.startsWith("stats") -> "stats"
            route.startsWith("profile") -> "profile"
            route.startsWith("quiz/content") -> "study_session"
            route.startsWith("quiz/summary") -> "session_summary"
            route.startsWith("add_pdf") -> "pack_creation"
            route.startsWith("overview") -> "pack_overview"
            route.startsWith("export") -> "export"
            route.startsWith("premium") -> "paywall"
            route.startsWith("onboarding") -> "onboarding"
            route.startsWith("about") -> "about"
            else -> route.substringBefore("/")
        }
    }
}
