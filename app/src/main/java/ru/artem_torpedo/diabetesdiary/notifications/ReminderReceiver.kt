package ru.artem_torpedo.diabetesdiary.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ru.artem_torpedo.diabetesdiary.R
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val profileId = intent.getLongExtra("profile_id", -1L)
        val title = intent.getStringExtra("title") ?: "Напоминание"
        val note = intent.getStringExtra("note")
        val triggerAt = intent.getLongExtra("trigger_at", System.currentTimeMillis())
        val repeatDaily = intent.getBooleanExtra("repeat_daily", false)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminders_channel"
        val channel = NotificationChannel(
            channelId,
            "Напоминания",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(note ?: "Время выполнить действие")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminderId.toInt(), notification)

        if (repeatDaily) {
            val calendar = Calendar.getInstance().apply { timeInMillis = triggerAt }
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            val nextReminder = ReminderEntity(
                id = reminderId,
                profileId = profileId,
                title = title,
                note = note,
                triggerAtMillis = calendar.timeInMillis,
                repeatDaily = true,
                enabled = true
            )

            ReminderScheduler.schedule(context, nextReminder)
        }
    }
}