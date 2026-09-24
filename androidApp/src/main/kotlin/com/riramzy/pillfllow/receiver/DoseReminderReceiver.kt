package com.riramzy.pillfllow.receiver

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.riramzy.pillfllow.MainActivity
import com.riramzy.pillfllow.data.local.dao.MedicationDao
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.medication.DoseReminderStage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject

@Suppress("DEPRECATION")
class DoseReminderReceiver: BroadcastReceiver() {
    private val sessionManager: SessionManager by inject(SessionManager::class.java)
    private val medicationDao: MedicationDao by inject(MedicationDao::class.java)

    @SuppressLint("FullScreenIntentPolicy")
    override fun onReceive(context: Context, intent: Intent) {
        sessionManager.currentUser.value ?: return

        if (intent.action == "com.riramzy.pillfllow.DOSE_REMINDER") {
            val pillName = intent.getStringExtra("PILL_NAME") ?: "Medication"
            val doseId = intent.getStringExtra("DOSE_ID") ?: ""
            val stageName = intent.getStringExtra("REMINDER_STAGE") ?: DoseReminderStage.ADVANCE_30MIN.name

            val dose = runBlocking(Dispatchers.IO) {
                medicationDao.getScheduledDoseById(doseId)
            }

            if (dose == null || dose.isTaken) {
                return
            }

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "PillFlow:DoseWakeLock"
            )
            wakeLock.acquire(5000L)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "pillflow_dose_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Timely medication dose reminders"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val (title, message) = when (stageName) {
                DoseReminderStage.DUE_NOW.name ->
                    "Time for your medication!" to "Your $pillName is ready in the dish to dispense."
                DoseReminderStage.PRE_EXPIRY_15MIN.name ->
                    "Urgent: Grace Window Closing!" to "Please take your $pillName. Grace period ends in 15 minutes."
                else ->
                    "Upcoming Medication" to "Your $pillName is scheduled in 30 minutes."
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify("$doseId:$stageName".hashCode(), notification)
        }
    }
}