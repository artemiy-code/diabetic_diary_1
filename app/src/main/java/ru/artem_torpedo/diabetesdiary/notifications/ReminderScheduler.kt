package ru.artem_torpedo.diabetesdiary.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ru.artem_torpedo.diabetesdiary.data.local.entity.ReminderEntity
import java.util.Calendar

object ReminderScheduler {

    fun schedule(context: Context, reminder: ReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("profile_id", reminder.profileId)
            putExtra("title", reminder.title)
            putExtra("note", reminder.note)
            putExtra("trigger_at", reminder.triggerAtMillis)
            putExtra("repeat_daily", reminder.repeatDaily)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = computeNextTrigger(reminder.triggerAtMillis, reminder.repeatDaily)

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                } else {
                    // Фолбэк: если точные напоминания недоступны, ставим неточное
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Дополнительная защита на случай ограничений системы
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun computeNextTrigger(triggerAtMillis: Long, repeatDaily: Boolean): Long {
        if (!repeatDaily) return triggerAtMillis

        val now = System.currentTimeMillis()
        var next = triggerAtMillis

        while (next <= now) {
            val calendar = Calendar.getInstance().apply { timeInMillis = next }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            next = calendar.timeInMillis
        }

        return next
    }
}