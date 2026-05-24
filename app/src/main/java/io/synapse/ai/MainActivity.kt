package io.synapse.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.synapse.ai.core.framework.audio.SoundManager
import io.synapse.ai.core.theme.LimitedFontScale
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.core.theme.tokens.Cairo
import io.synapse.ai.core.theme.tokens.InterBold
import io.synapse.ai.data.repo.PremiumManager
import io.synapse.ai.features.premium.presentation.viewmodel.EntitlementViewModel
import io.synapse.ai.features.profile.presentation.viewmodel.ProfileViewModel
import io.synapse.ai.features.profile.presentation.viewmodel.StudySettingsViewModel
import io.synapse.ai.ui.SynapseApp
import io.synapse.ai.ui.viewmodel.RootViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var soundManager: SoundManager
    @Inject lateinit var premiumManager: PremiumManager

    private val rootViewModel: RootViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val studySettingsViewModel: StudySettingsViewModel by viewModels()
    private val entitlementViewModel: EntitlementViewModel by viewModels()

    private val fullyDrawnReported = AtomicBoolean(false)

    private var isFontLoaded by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            rootViewModel.isLoadingOnboardingState || !premiumManager.isReady.value || !isFontLoaded
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        handleIntent(intent)

        lifecycleScope.launch {
            delay(FULLY_DRAWN_TIMEOUT_MS)
            safeReportFullyDrawn()
        }

        setContent {
            val resolver = LocalFontFamilyResolver.current
            LaunchedEffect(Unit) {
                try {
                    resolver.preload(InterBold)
                    resolver.preload(Cairo)
                } catch (_: Exception) {
                } finally {
                    isFontLoaded = true
                }
            }

            if (rootViewModel.isLoadingOnboardingState || !premiumManager.isReady.value || !isFontLoaded) return@setContent

            val studySettings by studySettingsViewModel.uiState.collectAsStateWithLifecycle()
            val appTheme = studySettings.appTheme

            SynapseTheme(appTheme = appTheme) {
                LimitedFontScale {
                    SynapseApp(
                        soundManager     = soundManager,
                        rootViewModel    = rootViewModel,
                        profileViewModel = profileViewModel,
                    )
                }
            }

            LaunchedEffect(Unit) {
                safeReportFullyDrawn()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        entitlementViewModel.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            uri?.let {
                rootViewModel.setSharedUri(it.toString())
            }
        }
    }

    private fun safeReportFullyDrawn() {
        if (fullyDrawnReported.compareAndSet(false, true)) {
            try {
                reportFullyDrawn()
            } catch (_: Exception) {
            }
        }
    }

    private companion object {
        const val FULLY_DRAWN_TIMEOUT_MS = 10_000L
    }
}
