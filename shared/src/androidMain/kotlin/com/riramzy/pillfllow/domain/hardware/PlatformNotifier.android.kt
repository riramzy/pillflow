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
import com.riramzy.pillfllow.utils.medication.DoseReminderStage
import org.koin.mp.KoinPlatformTools

actual class PlatformNotifier {
    @SuppressLint("ScheduleExactAlarm")
    actual fun scheduleDoseReminder(
        context: Any?,
        doseId: String,
        pillName: String,
        triggerTimeMillis: Long,
        stage: DoseReminderStage
    ) {
        val androidContext = resolveContext(context) ?: return
        val alarmManager = androidContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent().apply {
            action = "com.riramzy.pillfllow.DOSE_REMINDER"
            `package` = androidContext.packageName
            putExtra("DOSE_ID", doseId)
            putExtra("PILL_NAME", pillName)
            putExtra("REMINDER_STAGE", stage.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            androidContext,
            "$doseId:${stage.name}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
            e.printStackTrace()
        }
    }

    actual fun scheduleCaregiverEscalation(
        context: Any?,
        doseId: String,
        pillName: String,
        patientName: String,
        triggerTimeMillis: Long
    ) {
        val androidContext = resolveContext(context) ?: return
        val alarmManager = androidContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent().apply {
            action = "com.riramzy.pillfllow.CAREGIVER_DOSE_ESCALATION"
            `package` = androidContext.packageName
            putExtra("DOSE_ID", doseId)
            putExtra("PILL_NAME", pillName)
            putExtra("PATIENT_NAME", patientName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            androidContext,
            "escalation_$doseId".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            pendingIntent
        )
    }

    actual fun cancelReminder(context: Any?, doseId: String) {
        val androidContext = resolveContext(context) ?: return
        val alarmManager = androidContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent().apply {
            action = "com.riramzy.pillfllow.DOSE_REMINDER"
            `package` = androidContext.packageName
        }

        DoseReminderStage.entries.forEach { s ->
            val pendingIntent = PendingIntent.getBroadcast(
                androidContext,
                "$doseId:${s.name}".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    actual fun cancelAllReminders(context: Any?) {
        val androidContext = resolveContext(context) ?: return
        val notificationManager = androidContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        notificationManager.cancelAll()
    }

    actual fun sendInstantNudge(context: Any?, title: String, message: String) {
        val ctx = resolveContext(context) ?: return
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

    private fun resolveContext(context: Any?): Context? {
        return (context as? Context) ?: runCatching {
            KoinPlatformTools.defaultContext().get().get<Context>()
        }.getOrNull()
    }
}