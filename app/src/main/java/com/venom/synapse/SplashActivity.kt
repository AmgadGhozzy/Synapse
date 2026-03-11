package com.venom.synapse

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.venom.data.repo.SettingsRepository
import com.venom.synapse.core.theme.SynapseTheme
import com.venom.ui.components.common.adp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Launcher activity — exists only to:
 *   1. Show the splash screen (system API on API 31+, custom Compose on older)
 *   2. Read [SettingsRepository.isFirstLaunch] once
 *   3. Forward the result to [MainActivity] via an Intent extra
 *
 * No ViewModel required — this is a one-shot, fire-and-forget coordinator.
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    // Holds the system splash visible until our async work is done (API 31+)
    private var splashReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen().setKeepOnScreenCondition { !splashReady }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val isFirstLaunch = settingsRepository.isFirstLaunch()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Let the system splash show while we do async work; release it here.
                splashReady = true
                navigateTo(isFirstLaunch)
            } else {
                // API < 31: show a simple branded Compose splash, then navigate
                setContent {
                    SynapseTheme {
                        LegacySplashScreen(onReady = { navigateTo(isFirstLaunch) })
                    }
                }
            }
        }
    }

    private fun navigateTo(isFirstLaunch: Boolean) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_SHOW_ONBOARDING, isFirstLaunch)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}

// ─── Legacy splash (API < 31 only) ───────────────────────────────────────────

private const val LEGACY_SPLASH_DURATION_MS = 900L

@Composable
private fun LegacySplashScreen(onReady: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(LEGACY_SPLASH_DURATION_MS)
        onReady()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter            = painterResource(R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier           = Modifier.size(120.adp),
                )
                Spacer(Modifier.height(20.adp))
                Text(
                    text       = stringResource(R.string.app_name),
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}