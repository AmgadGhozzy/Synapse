package io.synapse.ai.core.analytics

import android.util.Log
import io.synapse.ai.core.analytics.model.AnalyticsEvent
import io.synapse.ai.core.analytics.model.UserConsent
import io.synapse.ai.core.analytics.providers.AnalyticsProvider
import io.synapse.ai.core.analytics.providers.CrashProvider
import io.synapse.ai.core.analytics.providers.PushProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [TrackingManager].
 *
 * **Single enforcement point:** Every tracking call is gated behind
 * the current [UserConsent]. No Firebase SDK is touched unless the
 * relevant consent flag is `true`.
 *
 * This class is injected into domain-layer UseCases — NEVER into
 * ViewModels or UI components.
 */
@Singleton
class TrackingManagerImpl @Inject constructor(
    private val analyticsProvider: AnalyticsProvider,
    private val crashProvider: CrashProvider,
    private val pushProvider: PushProvider,
) : TrackingManager {

    @Volatile
    private var currentConsent = UserConsent()

    override fun initialize(consent: UserConsent) {
        currentConsent = consent
        applyConsent(consent)
        Log.d(TAG, "Initialized: analytics=${consent.analyticsEnabled}, " +
                "crash=${consent.crashEnabled}, push=${consent.pushEnabled}")
    }

    override fun updateConsent(consent: UserConsent) {
        val previous = currentConsent
        currentConsent = consent
        applyConsent(consent)
        Log.d(TAG, "Consent updated: analytics=${consent.analyticsEnabled}, " +
                "crash=${consent.crashEnabled}, push=${consent.pushEnabled}")

        if (consent.analyticsEnabled && !previous.analyticsEnabled) {
            logEvent(AnalyticsEvent.AppOpened)
        }
    }

    private fun applyConsent(consent: UserConsent) {
        analyticsProvider.setEnabled(consent.analyticsEnabled)
        crashProvider.setEnabled(consent.crashEnabled)
        pushProvider.setEnabled(consent.pushEnabled)
    }

    override fun logEvent(event: AnalyticsEvent) {
        if (!currentConsent.analyticsEnabled) return
        analyticsProvider.logEvent(event)
    }

    override fun setUserProperty(key: String, value: String) {
        if (!currentConsent.analyticsEnabled) return
        analyticsProvider.setUserProperty(key, value)
    }

    override fun logException(throwable: Throwable, message: String?) {
        if (!currentConsent.crashEnabled) return
        crashProvider.logException(throwable, message)
    }

    override fun logCrashBreadcrumb(message: String) {
        if (!currentConsent.crashEnabled) return
        crashProvider.log(message)
    }

    override fun setCrashKey(key: String, value: String) {
        if (!currentConsent.crashEnabled) return
        crashProvider.setCustomKey(key, value)
    }

    override suspend fun subscribeTopic(topic: String) {
        if (!currentConsent.pushEnabled) return
        pushProvider.subscribeToTopic(topic)
    }

    override suspend fun unsubscribeTopic(topic: String) {
        if (!currentConsent.pushEnabled) return
        pushProvider.unsubscribeFromTopic(topic)
    }

    companion object {
        private const val TAG = "TrackingManager"
    }
}
