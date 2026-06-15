package io.synapse.ai.features.add_pdf.presentation.analytics

import io.synapse.ai.core.analytics.TrackingManager
import io.synapse.ai.core.analytics.model.AnalyticsEvent
import javax.inject.Inject

class AddPdfAnalyticsTracker @Inject constructor(
    private val trackingManager: TrackingManager
) {
    fun sourceSelected(sourceTypeStr: String) {
        trackingManager.logEvent(AnalyticsEvent.SourceSelected(sourceTypeStr))
    }

    fun generationStarted(
        sourceTypeStr: String,
        language: String,
        questionCount: Int,
        thinkingEnabled: Boolean
    ) {
        trackingManager.logEvent(
            AnalyticsEvent.GenerationStarted(
                sourceType = sourceTypeStr,
                language = language,
                questionCount = questionCount,
                thinkingEnabled = thinkingEnabled
            )
        )
        trackingManager.setCrashKey("generation_state", "started")
        trackingManager.setCrashKey("source_type", sourceTypeStr)
    }

    fun generationSucceeded(
        sourceTypeStr: String,
        language: String,
        questionCount: Int,
        durationMs: Long
    ) {
        trackingManager.logEvent(
            AnalyticsEvent.GenerationSuccess(
                sourceType = sourceTypeStr,
                language = language,
                questionCount = questionCount,
                durationMs = durationMs
            )
        )
        trackingManager.logEvent(
            AnalyticsEvent.PackCreated(
                sourceType = sourceTypeStr,
                questionCount = questionCount
            )
        )
        if (durationMs > AnalyticsEvent.SLOW_GENERATION_THRESHOLD_MS) {
            trackingManager.logEvent(
                AnalyticsEvent.SlowGenerationDetected(durationMs, sourceTypeStr)
            )
        }
        trackingManager.setCrashKey("generation_state", "completed")
        if (questionCount == 0) {
            trackingManager.logEvent(AnalyticsEvent.EmptyGeneration(sourceTypeStr))
        }
    }

    fun generationCancelled(sourceTypeStr: String, questionsCompleted: Int) {
        trackingManager.logEvent(
            AnalyticsEvent.GenerationCancelled(
                sourceType = sourceTypeStr,
                questionsCompleted = questionsCompleted
            )
        )
    }
}
