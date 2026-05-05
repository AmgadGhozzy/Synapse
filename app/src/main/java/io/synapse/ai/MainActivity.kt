package io.synapse.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.synapse.ai.core.framework.audio.SoundManager
import io.synapse.ai.core.theme.LimitedFontScale
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.data.repo.PremiumManager
import io.synapse.ai.features.premium.presentation.viewmodel.EntitlementViewModel
import io.synapse.ai.features.profile.presentation.viewmodel.ProfileViewModel
import io.synapse.ai.features.profile.presentation.viewmodel.StudySettingsViewModel
import io.synapse.ai.ui.SynapseApp
import io.synapse.ai.ui.viewmodel.RootViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var soundManager: SoundManager
    @Inject lateinit var premiumManager: PremiumManager

    private val rootViewModel: RootViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val studySettingsViewModel: StudySettingsViewModel by viewModels()
    private val entitlementViewModel: EntitlementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            rootViewModel.isLoadingOnboardingState || !premiumManager.isReady.value
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

        setContent {
            if (rootViewModel.isLoadingOnboardingState || !premiumManager.isReady.value) return@setContent

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
        }
    }

    override fun onResume() {
        super.onResume()
        entitlementViewModel.onResume()
    }
}