package io.synapse.ai.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.synapse.ai.MainActivity
import io.synapse.ai.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID      = "synapse_daily_reminder"
        const val CHANNEL_NAME    = "Daily Study Reminder"
        const val NOTIFICATION_ID = 1_001
    }

    private val manager = NotificationManagerCompat.from(context)

    /**
     * Show a smart daily reminder.
     *
     * @param dueCount Cards due today — drives the notification copy.
     *                 Pass 0 to show a "you're all caught up" message.
     */
    fun show(dueCount: Int) {
        ensureChannel()
        if (!manager.areNotificationsEnabled()) return

        val title = when {
            dueCount == 0  -> context.getString(R.string.reminder_title_caught_up)
            dueCount <= 10 -> context.getString(R.string.reminder_title_quick_win)
            dueCount <= 50 -> context.getString(R.string.reminder_title_daily_review)
            else           -> context.getString(R.string.reminder_title_pile_up)
        }

        val message = when {
            dueCount == 0  -> context.getString(R.string.reminder_message_caught_up)
            dueCount == 1  -> context.getString(R.string.reminder_message_quick_win_one)
            dueCount <= 10 -> context.getString(R.string.reminder_message_quick_win_plural, dueCount)
            dueCount <= 50 -> context.getString(R.string.reminder_message_daily_review, dueCount)
            else           -> context.getString(R.string.reminder_message_pile_up, dueCount)
        }

        val tapPi = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("destination", "review")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val actionPi = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("destination", "review")
                putExtra("auto_start", true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Decode app icon as a large icon for rich presentation
        val appIconBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.mipmap.ic_launcher_foreground
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_logo) // Use launcher foreground silhouette as small icon
            .setLargeIcon(appIconBitmap)                   // Use full-color launcher icon as large icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(tapPi)
            .addAction(R.drawable.ic_zap, context.getString(R.string.reminder_action_start_review), actionPi)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.reminder_channel_description)
                enableVibration(true)
            }
        )
    }
}
