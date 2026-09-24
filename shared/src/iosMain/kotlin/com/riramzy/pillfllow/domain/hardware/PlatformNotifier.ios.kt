package com.riramzy.pillfllow.domain.hardware

import com.riramzy.pillfllow.utils.medication.DoseReminderStage
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

actual class PlatformNotifier {
    actual fun scheduleDoseReminder(
        context: Any?,
        doseId: String,
        pillName: String,
        triggerTimeMillis: Long,
        stage: DoseReminderStage
    ) {
        val nowSecs = NSDate().timeIntervalSince1970
        val targetSecs = triggerTimeMillis / 1000.0
        val delaySeconds = (targetSecs - nowSecs).coerceAtLeast(1.0)

        val center = UNUserNotificationCenter.currentNotificationCenter()

        val content = UNMutableNotificationContent().apply {
            setTitle("Medication Reminder")
            setBody("Time to take your $pillName")
            setSound(UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(delaySeconds, false)
        val request = UNNotificationRequest.requestWithIdentifier("$doseId:${stage.name}", content, trigger)

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("PillFlow Error: Failed to schedule notification for $pillName: ${error.localizedDescription}")
            } else {
                println("PillFlow: Successfully scheduled local reminder for $pillName (ID: $doseId)")
            }
        }
    }

    actual fun scheduleCaregiverEscalation(
        context: Any?,
        doseId: String,
        pillName: String,
        patientName: String,
        triggerTimeMillis: Long
    ) {
        val center = UNUserNotificationCenter.currentNotificationCenter()

        val content = UNMutableNotificationContent().apply {
            setTitle("Urgent: Missed Dose Alert")
            setBody("$patientName has missed their scheduled dose of $pillName!")
            setSound(UNNotificationSound.defaultSound())
        }

        val nowSecs = NSDate().timeIntervalSince1970
        val targetSecs = triggerTimeMillis / 1000.0
        val delaySeconds = (targetSecs - nowSecs).coerceAtLeast(1.0)

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(delaySeconds, false)
        val request = UNNotificationRequest.requestWithIdentifier("escalation_$doseId", content, trigger)

        center.addNotificationRequest(request) { _ -> }
    }

    actual fun cancelReminder(context: Any?, doseId: String) {
        val identifiers = DoseReminderStage.entries.map { "$doseId:${it.name}" } + "escalation_$doseId"
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
    }

    actual fun cancelAllReminders(context: Any?)  {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
    }

    actual fun sendInstantNudge(context: Any?, title: String, message: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
            setSound(UNNotificationSound.defaultSound())
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            "nudge_${NSDate().timeIntervalSince1970}",
            content,
            null
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("PillFlow Error: Failed to dispatch instant nudge: ${error.localizedDescription}")
            }
        }
    }
}