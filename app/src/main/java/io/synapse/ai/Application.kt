package io.synapse.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import io.synapse.ai.core.analytics.TrackingManager
import io.synapse.ai.core.analytics.data.ConsentRepository
import io.synapse.ai.core.analytics.model.AnalyticsEvent
import io.synapse.ai.core.framework.audio.SoundManager
import io.synapse.ai.data.repo.AppConfigProvider
import io.synapse.ai.data.repo.PremiumManager
import io.synapse.ai.data.sync.SyncScheduler
import io.synapse.ai.di.NetworkEntryPoint
import io.synapse.ai.domain.repo.IAuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
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
    @Inject lateinit var premiumManager: PremiumManager
    @Inject lateinit var soundManager: SoundManager
    @Inject lateinit var syncScheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initializeTracking()
        initializePremium()
        bootstrapAuth()
        preWarmNetworkClients()

        applicationScope.launch {
            appConfigProvider.fetchAndActivate()
            premiumManager.reEvaluateReviewerMode()
        }

        if (BuildConfig.DEBUG) enableStrictMode()
    }

    private fun initializePremium() {
        premiumManager.initialize()
    }

    private fun bootstrapAuth() {
        applicationScope.launch {
            authRepo.ensureSignedIn()
                .onSuccess {
                    premiumManager.verifyWithServer(force = true)
                    if (authRepo.isAuthenticated()) {
                        syncScheduler.schedule()
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Auth bootstrap failed", e)
                    trackingManager.logException(e, "Auth bootstrap failed")
                    premiumManager.verifyWithServer()
                }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        soundManager.release()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Study Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Get notified about study reminders and daily goals"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

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

    private fun observeConsentChanges() {
        applicationScope.launch {
            consentRepository.consent.drop(1).collect { consent ->
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
        const val CHANNEL_REMINDERS = "study_reminders"
    }
}