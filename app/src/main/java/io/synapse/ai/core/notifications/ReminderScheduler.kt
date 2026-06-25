package io.synapse.ai.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.core.receiver.ReminderReceiver
import io.synapse.ai.domains.study.reminder.ReminderSettings
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG          = "ReminderScheduler"
        private const val REQUEST_CODE = 100_001
    }

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ── Public API ────────────────────────────────────────────────────────────

    fun schedule(settings: ReminderSettings) {
        if (!settings.isEnabled) {
            Log.d(TAG, "schedule() skipped — reminder disabled.")
            return
        }
        cancel() // always cancel first — guarantees a single alarm
        val triggerMs = nextTriggerMillis(settings.hour, settings.minute)
        setAlarm(triggerMs)
        Log.d(TAG, "Scheduled for ${Date(triggerMs)}")
    }

    fun cancel() {
        alarmManager.cancel(buildPendingIntent())
        Log.d(TAG, "Alarm cancelled.")
    }

    fun reschedule(settings: ReminderSettings) {
        if (settings.isEnabled) schedule(settings) else cancel()
    }

    // ── Timing ────────────────────────────────────────────────────────────────

    /**
     * Returns epoch-ms for the next occurrence of [hour]:[minute].
     * → If the time hasn't passed today → fires today.
     * → If the time has already passed  → fires tomorrow.
     */
    internal fun nextTriggerMillis(hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE,      minute)
            set(Calendar.SECOND,      0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun setAlarm(triggerMs: Long) {
        val pi = buildPendingIntent()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
                    Log.d(TAG, "Exact alarm set (API 31+, permission granted).")
                } else {
                    // User denied SCHEDULE_EXACT_ALARM — inexact (~few min drift in Doze).
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
                    Log.w(TAG, "SCHEDULE_EXACT_ALARM not granted — using inexact alarm.")
                }
            }
            else -> {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
                Log.d(TAG, "Exact alarm set (pre-API 31).")
            }
        }
    }

    private fun buildPendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
