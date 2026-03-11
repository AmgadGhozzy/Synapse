package com.venom.synapse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.features.profile.presentation.viewmodel.ProfileViewModel
import com.venom.synapse.ui.SynapseApp
import com.venom.synapse.ui.viewmodel.RootViewModel
import com.venom.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

/**
 * Single-Activity host.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val rootViewModel: RootViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val showOnboarding = intent.getBooleanExtra(EXTRA_SHOW_ONBOARDING, false)
        rootViewModel.setOnboardingState(showOnboarding)

        setContent {
            val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val appTheme = settings.themePrefs.appTheme
            val languageCode = settings.appLanguage.code

            ApplySelectedLanguage("")

            SynapseTheme(appTheme = appTheme) {
                SynapseApp(rootViewModel = rootViewModel, profileViewModel = profileViewModel)
            }
        }
    }

    @Composable
    private fun ApplySelectedLanguage(languageCode: String) {
        val locale = if (languageCode.isEmpty()) Locale.getDefault() else Locale(languageCode)
        val config = LocalConfiguration.current

        config.setLocale(locale)
        val context = LocalContext.current
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    companion object {
        const val EXTRA_SHOW_ONBOARDING = "show_onboarding"
    }
}
