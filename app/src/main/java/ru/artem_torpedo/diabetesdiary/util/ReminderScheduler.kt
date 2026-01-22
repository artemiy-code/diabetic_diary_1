package ru.artem_torpedo.diabetesdiary.util

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import ru.artem_torpedo.diabetesdiary.receiver.ReminderReceiver

object ReminderScheduler {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun schedule(
        context: Context,
        title: String,
        timeMillis: Long
    ) {
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.putExtra("title", title)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timeMillis.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE)
                    as AlarmManager

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            timeMillis,
            pendingIntent
        )
    }
}
