package com.riramzy.pillfllow.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.usecase.medication.LogDoseTakenUseCase
import com.riramzy.pillfllow.utils.medication.DoseReminderStage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class DoseActionReceiver: BroadcastReceiver() {
    private val logDoseTakenUseCase: LogDoseTakenUseCase by inject(LogDoseTakenUseCase::class.java)
    private val platformNotifier: PlatformNotifier by inject(PlatformNotifier::class.java)

    companion object {
        const val ACTION_TAKE_DOSE = "com.riramzy.pillfllow.ACTION_TAKE_DOSE"
        const val ACTION_SNOOZE_DOSE = "com.riramzy.pillfllow.ACTION_SNOOZE_DOSE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val doseId = intent?.getStringExtra("DOSE_ID") ?: return

        when(intent.action) {
            ACTION_TAKE_DOSE -> {
                platformNotifier.cancelReminder(context, doseId)
                val pendingResult = goAsync()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        logDoseTakenUseCase(doseId = doseId)
                        showActionFeedback(context, "Dose Recorded", "Marked as taken.")
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_SNOOZE_DOSE -> {
                val pillName = intent.getStringExtra("PILL_NAME") ?: "Medication"
                platformNotifier.cancelReminder(context, doseId)

                val snoozeTargetTime = System.currentTimeMillis() + 15 * 60 * 1000L

                platformNotifier.scheduleDoseReminder(
                    context = context,
                    doseId = doseId,
                    pillName = pillName,
                    triggerTimeMillis = snoozeTargetTime,
                    stage = DoseReminderStage.DUE_NOW
                )
                showActionFeedback(context, "Dose Snoozed", "$pillName reminder set for 15 minutes.")
            }
        }
    }
}

private fun showActionFeedback(context: Context?, title: String, message: String) {
    val ctx = context ?: return
    val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager ?: return
    val channelId = "pillflow_feedback_channel"

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = android.app.NotificationChannel(
            channelId,
            "Action Confirmations",
            android.app.NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Brief confirmations for medication actions taken"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = androidx.core.app.NotificationCompat.Builder(ctx, channelId)
        .setSmallIcon(com.riramzy.pillfllow.R.drawable.ic_notification)
        .setColor(0xFF835414.toInt())
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
        .setAutoCancel(true)
        .setTimeoutAfter(10000L)
        .build()

    notificationManager.notify(99999, notification)
}