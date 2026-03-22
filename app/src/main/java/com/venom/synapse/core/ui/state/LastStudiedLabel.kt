package com.venom.synapse.core.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import com.venom.synapse.R
import java.util.concurrent.TimeUnit

/**
 * Sealed hierarchy replacing hardcoded English strings for
 * the "last studied" label. The ViewModel produces one of these
 * from a nullable epoch-ms timestamp; the Composable resolves it
 * to a localised string via [displayString].
 */
@Immutable
sealed interface LastStudiedLabel {
    data object Never : LastStudiedLabel
    data object JustNow : LastStudiedLabel
    data object Today : LastStudiedLabel
    data object Yesterday : LastStudiedLabel
    data class DaysAgo(val days: Long) : LastStudiedLabel
    data class WeeksAgo(val weeks: Long) : LastStudiedLabel

    companion object {
        /**
         * Converts an epoch-ms timestamp into a [LastStudiedLabel].
         * Pure function — safe to call from a ViewModel.
         */
        fun from(timestampMs: Long?): LastStudiedLabel {
            if (timestampMs == null || timestampMs == 0L) return Never
            val diff = System.currentTimeMillis() - timestampMs
            return when {
                diff < TimeUnit.HOURS.toMillis(1)  -> JustNow
                diff < TimeUnit.HOURS.toMillis(24) -> Today
                diff < TimeUnit.DAYS.toMillis(2)   -> Yesterday
                diff < TimeUnit.DAYS.toMillis(7)   -> DaysAgo(TimeUnit.MILLISECONDS.toDays(diff))
                else                               -> WeeksAgo(TimeUnit.MILLISECONDS.toDays(diff) / 7)
            }
        }
    }
}

/** Resolves to a localised display string inside a Composable. */
@Composable
fun LastStudiedLabel.displayString(): String = when (this) {
    LastStudiedLabel.Never     -> stringResource(R.string.last_studied_never)
    LastStudiedLabel.JustNow   -> stringResource(R.string.last_studied_just_now)
    LastStudiedLabel.Today     -> stringResource(R.string.last_studied_today)
    LastStudiedLabel.Yesterday -> stringResource(R.string.last_studied_yesterday)
    is LastStudiedLabel.DaysAgo  -> stringResource(R.string.last_studied_days_ago, days)
    is LastStudiedLabel.WeeksAgo -> stringResource(R.string.last_studied_weeks_ago, weeks)
}
