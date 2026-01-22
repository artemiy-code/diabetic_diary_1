package ru.artem_torpedo.diabetesdiary.receiver

import android.Manifest
import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.artem_torpedo.diabetesdiary.notifications.ReminderNotification

class ReminderReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {

        val title =
            intent.getStringExtra("title") ?: "Напоминание"

        val notification = NotificationCompat.Builder(
            context,
            ReminderNotification.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Дневник диабетика")
            .setContentText(title)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
