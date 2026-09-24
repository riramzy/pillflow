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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject

@Suppress("DEPRECATION")
class CaregiverEscalationReceiver: BroadcastReceiver() {
    private val sessionManager: SessionManager by inject(SessionManager::class.java)
    private val medicationDao: MedicationDao by inject(MedicationDao::class.java)

    @SuppressLint("FullScreenIntentPolicy")
    override fun onReceive(context: Context, intent: Intent) {
        val activeUser = sessionManager.currentUser.value ?: return
        if (activeUser.userType.lowercase() != "caregiver") return

        if (intent.action == "com.riramzy.pillfllow.CAREGIVER_DOSE_ESCALATION") {
            val doseId = intent.getStringExtra("DOSE_ID") ?: ""
            val pillName = intent.getStringExtra("PILL_NAME") ?: "Medication"
            val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Your patient"

            val dose = runBlocking(Dispatchers.IO) {
                medicationDao.getScheduledDoseById(doseId)
            }

            if (dose == null || dose.isTaken) {
                return
            }

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "PillFlow:EscalationWakeLock"
            )
            wakeLock.acquire(5000L)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "pillflow_escalation_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Caregiver Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent alerts when a patient misses a medication dose"
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

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Urgent: Missed Dose Alert")
                .setContentText("$patientName has missed their scheduled dose of $pillName!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(doseId.hashCode(), notification)
        }
    }
}