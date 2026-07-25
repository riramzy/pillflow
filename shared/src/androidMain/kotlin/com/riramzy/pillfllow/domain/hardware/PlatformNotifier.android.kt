package com.riramzy.pillfllow.domain.hardware

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

actual class PlatformNotifier {
    @SuppressLint("ScheduleExactAlarm")
    actual fun scheduleDoseReminder(
        context: Any?,
        doseId: String,
        pillName: String,
        triggerTimeMillis: Long
    ) {
        val androidContext = context as? Context ?: return
        val alarmManager = androidContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent().apply {
            action = "com.riramzy.pillfllow.DOSE_REMINDER"
            putExtra("DOSE_ID", doseId)
            putExtra("PILL_NAME", pillName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            androidContext,
            doseId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    actual fun cancelReminder(context: Any?, doseId: String) {
        val androidContext = context as? Context ?: return
        val alarmManager = androidContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent().apply {
            action = "com.riramzy.pillfllow.DOSE_REMINDER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            androidContext,
            doseId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}