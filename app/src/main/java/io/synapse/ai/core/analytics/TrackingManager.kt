package io.synapse.ai.core.analytics

import io.synapse.ai.core.analytics.model.AnalyticsEvent
import io.synapse.ai.core.analytics.model.UserConsent

/**
 * Central abstraction for the Synapse tracking system.
 *
 * All tracking calls flow through this interface. The implementation
 * gates every operation behind [UserConsent] — no Firebase call is
 * made unless the relevant consent flag is `true`.
 *
 * **Architecture flow:** UI → UseCase → TrackingManager → Providers
 *
 * This is injected into UseCases, NEVER into ViewModels directly.
 */
interface TrackingManager {

    fun initialize(consent: UserConsent)

    fun updateConsent(consent: UserConsent)

    fun logEvent(event: AnalyticsEvent)

    fun setUserProperty(key: String, value: String)

    fun logException(throwable: Throwable, message: String? = null)

    fun logCrashBreadcrumb(message: String)

    fun setCrashKey(key: String, value: String)

    suspend fun subscribeTopic(topic: String)

    suspend fun unsubscribeTopic(topic: String)
}
