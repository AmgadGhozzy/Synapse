package com.venom.synapse

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.venom.analytics.AnalyticsManager
import com.venom.analytics.CrashlyticsManager
import com.venom.di.NetworkEntryPoint
import com.venom.domain.provider.AppConfigProvider
import com.venom.synapse.domain.repo.IAuthRepository
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var crashlyticsManager: CrashlyticsManager
    @Inject lateinit var appConfigProvider: AppConfigProvider
    @Inject lateinit var authRepo: IAuthRepository

    override fun onCreate() {
        super.onCreate()
        initializeAnalytics()
        bootstrapAuth()
        preWarmNetworkClients()

        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
    }

    private fun initializeAnalytics() {
        try {
            val enableAnalytics    = appConfigProvider.isAnalyticsEnabled
            val enableCrashlytics  = appConfigProvider.isCrashlyticsEnabled

            analyticsManager.initialize(enableAnalytics)
            crashlyticsManager.initialize(enableCrashlytics)

            if (enableCrashlytics) {
                crashlyticsManager.setCustomKey("app_version", BuildConfig.VERSION_NAME)
                crashlyticsManager.setCustomKey("build_type",  BuildConfig.BUILD_TYPE)
            }

            if (enableAnalytics) {
                analyticsManager.logEvent("app_open")
            }

            Log.d(TAG, "Analytics=$enableAnalytics  Crashlytics=$enableCrashlytics")
        } catch (e: Exception) {
            Log.e(TAG, "Analytics init failed", e)
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
                crashlyticsManager.logNonFatalException(e, "Network client warm-up failed")
            }
        }
    }

    private fun bootstrapAuth() {
        applicationScope.launch {
            authRepo.ensureSignedIn().onFailure { e ->
                Log.e(TAG, "Auth bootstrap failed", e)
                crashlyticsManager.logNonFatalException(e, "Auth bootstrap failed")
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