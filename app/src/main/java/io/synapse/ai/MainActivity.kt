package io.synapse.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.synapse.ai.core.framework.audio.SoundManager
import io.synapse.ai.core.theme.LimitedFontScale
import io.synapse.ai.core.theme.SynapseTheme
import io.synapse.ai.features.profile.presentation.viewmodel.ProfileViewModel
import io.synapse.ai.features.profile.presentation.viewmodel.StudySettingsViewModel
import io.synapse.ai.ui.SynapseApp
import io.synapse.ai.ui.viewmodel.RootViewModel
import javax.inject.Inject

/**
 * Single-Activity host.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundManager: SoundManager

    private val rootViewModel: RootViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val studySettingsViewModel: StudySettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val showOnboarding = intent.getBooleanExtra(EXTRA_SHOW_ONBOARDING, false)
        rootViewModel.setOnboardingState(showOnboarding)

        setContent {
            val studySettings by studySettingsViewModel.uiState.collectAsStateWithLifecycle()
            val appTheme = studySettings.appTheme

            SynapseTheme(appTheme = appTheme) {
                LimitedFontScale {
                    SynapseApp(
                        soundManager = soundManager,
                        rootViewModel = rootViewModel,
                        profileViewModel = profileViewModel,
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_SHOW_ONBOARDING = "show_onboarding"
    }
}
