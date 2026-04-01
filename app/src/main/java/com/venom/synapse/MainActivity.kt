package com.venom.synapse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.synapse.features.profile.presentation.viewmodel.ProfileViewModel
import com.venom.synapse.ui.SynapseApp
import com.venom.synapse.ui.viewmodel.RootViewModel
import com.venom.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.uiState
                    .map { it.appLanguage.code }
                    .distinctUntilChanged()
                    .collect { code ->
                        val localeList = if (code.isEmpty()) {
                            LocaleListCompat.getEmptyLocaleList()
                        } else {
                            LocaleListCompat.forLanguageTags(code)
                        }
                        AppCompatDelegate.setApplicationLocales(localeList)
                    }
            }
        }

        setContent {
            val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val appTheme = settings.themePrefs.appTheme

            SynapseTheme(appTheme = appTheme) {
                SynapseApp(rootViewModel = rootViewModel, profileViewModel = profileViewModel)
            }
        }
    }

    companion object {
        const val EXTRA_SHOW_ONBOARDING = "show_onboarding"
    }
}