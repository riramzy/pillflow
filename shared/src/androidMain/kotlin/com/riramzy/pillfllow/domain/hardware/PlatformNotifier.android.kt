package com.riramzy.pillfllow.domain.hardware

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

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

    actual fun sendInstantNudge(context: Any?, title: String, message: String) {
        val ctx = context as? Context ?: return
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "pillflow_nudge_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Caregiver Nudges",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority caregiver medication reminders"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}