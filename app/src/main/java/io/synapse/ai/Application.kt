package io.synapse.ai

import android.app.Application
import android.os.StrictMode
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import io.synapse.ai.core.analytics.TrackingManager
import io.synapse.ai.core.analytics.data.ConsentRepository
import io.synapse.ai.core.analytics.model.AnalyticsEvent
import io.synapse.ai.data.repo.AppConfigProvider
import io.synapse.ai.di.NetworkEntryPoint
import io.synapse.ai.domain.repo.IAuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject lateinit var trackingManager: TrackingManager
    @Inject lateinit var consentRepository: ConsentRepository
    @Inject lateinit var appConfigProvider: AppConfigProvider
    @Inject lateinit var authRepo: IAuthRepository

    override fun onCreate() {
        super.onCreate()
        initializeTracking()
        bootstrapAuth()
        preWarmNetworkClients()
        
        applicationScope.launch {
            appConfigProvider.fetchAndActivate()
        }

        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
    }

    /**
     * Consent-first initialization flow:
     * 1. Load consent from DataStore (synchronous first emission)
     * 2. Initialize TrackingManager with resolved consent
     * 3. Observe future consent changes for runtime toggling
     *
     * Firebase SDKs are NEVER touched before consent is resolved.
     * Manifest flags ensure zero auto-collection.
     */
    private fun initializeTracking() {
        applicationScope.launch {
            try {
                // Step 1: Resolve consent before any SDK interaction
                val consent = consentRepository.consent.first()

                // Step 2: Initialize providers based on consent state
                trackingManager.initialize(consent)

                // Step 3: Set non-PII crash context
                if (consent.crashEnabled) {
                    trackingManager.setCrashKey("app_version", BuildConfig.VERSION_NAME)
                    trackingManager.setCrashKey("build_type", BuildConfig.BUILD_TYPE)
                }

                // Step 4: Log app open (only if analytics consented)
                if (consent.analyticsEnabled) {
                    trackingManager.logEvent(AnalyticsEvent.AppOpened)
                }

                Log.d(TAG, "Tracking initialized: analytics=${consent.analyticsEnabled}, " +
                        "crash=${consent.crashEnabled}, push=${consent.pushEnabled}")

                // Step 5: Observe runtime consent changes
                observeConsentChanges()

            } catch (e: Exception) {
                Log.e(TAG, "Tracking init failed", e)
            }
        }
    }

    /**
     * Continuously observes consent changes from settings UI.
     * When the user toggles a consent flag, the corresponding
     * Firebase SDK is enabled/disabled in real time.
     */
    private fun observeConsentChanges() {
        applicationScope.launch {
            consentRepository.consent.collect { consent ->
                trackingManager.updateConsent(consent)
            }
        }
    }

    private fun preWarmNetworkClients() {
        applicationScope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    this@Application,
                    NetworkEntryPoint::class.java,
                )
                entryPoint.getRegularClient().connectionPool
                entryPoint.getAiClient().connectionPool
                Log.d(TAG, "Network clients warmed")
            } catch (e: Exception) {
                Log.e(TAG, "Network client warm-up failed", e)
                trackingManager.logException(e, "Network client warm-up failed")
            }
        }
    }

    private fun bootstrapAuth() {
        applicationScope.launch {
            authRepo.ensureSignedIn().onFailure { e ->
                Log.e(TAG, "Auth bootstrap failed", e)
                trackingManager.logException(e, "Auth bootstrap failed")
            }
        }
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
        Log.d(TAG, "StrictMode enabled")
    }

    companion object {
        private const val TAG = "SynapseApp"
    }
}