package io.synapse.ai.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import java.text.NumberFormat
import java.util.Locale

@Composable
fun Int.localized(): String {
    val locale = ConfigurationCompat
        .getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()
    return remember(this, locale) {
        NumberFormat.getInstance(locale).format(this)
    }
}
