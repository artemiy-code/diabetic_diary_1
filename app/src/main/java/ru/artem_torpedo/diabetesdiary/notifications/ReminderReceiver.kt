package ru.artem_torpedo.diabetesdiary.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import ru.artem_torpedo.diabetesdiary.R
import ru.artem_torpedo.diabetesdiary.ui.MainActivity
import ru.artem_torpedo.diabetesdiary.ui.measurement.MeasurementsActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Напоминание"
        val note = intent.getStringExtra(EXTRA_NOTE)
        val hour = intent.getIntExtra(EXTRA_HOUR, 0)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
        val repeatDaily = intent.getBooleanExtra(EXTRA_REPEAT_DAILY, true)

        showNotification(context, reminderId, profileId, title, note)

        if (repeatDaily && reminderId > 0 && profileId > 0) {
            ReminderScheduler.schedule(
                context = context,
                reminderId = reminderId,
                profileId = profileId,
                title = title,
                note = note,
                hour = hour,
                minute = minute,
                repeatDaily = true
            )
        }

    }

    private fun showNotification(
        context: Context,
        reminderId: Long,
        profileId: Long,
        title: String,
        note: String?
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(nm)

        val measurementsIntent = Intent(context, MeasurementsActivity::class.java).apply {
            putExtra("profile_id", profileId)
        }

        val pi: PendingIntent = TaskStackBuilder.create(context).run {
            addNextIntent(Intent(context, MainActivity::class.java))
            addNextIntent(measurementsIntent)
            getPendingIntent(
                reminderId.toInt(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val text = note?.takeIf { it.isNotBlank() } ?: "Пора выполнить действие"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(reminderId.toInt(), notification)
    }

    private fun ensureChannel(nm: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Уведомления о приёме препаратов и самоконтроле"
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "diabetes_reminders"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_PROFILE_ID = "extra_profile_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
        const val EXTRA_REPEAT_DAILY = "extra_repeat_daily"
    }
}