package com.riramzy.pillfllow.domain.hardware

import com.riramzy.pillfllow.utils.medication.DoseReminderStage
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

actual class PlatformNotifier {
    actual fun scheduleDoseReminder(
        context: Any?,
        doseId: String,
        pillName: String,
        triggerTimeMillis: Long,
        stage: DoseReminderStage
    ) {
        val center = UNUserNotificationCenter.currentNotificationCenter()

        val content = UNMutableNotificationContent().apply {
            setTitle("Medication Reminder")
            setBody("Time to take your $pillName")
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound())
        }

        val date = NSDate.dateWithTimeIntervalSince1970(triggerTimeMillis / 1000.0)
        val calendar = NSCalendar.currentCalendar()

        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or NSCalendarUnitMinute,
            fromDate = date
        )

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            false
        )

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

        val date = NSDate.dateWithTimeIntervalSince1970(triggerTimeMillis / 1000.0)

        val components = NSCalendar.currentCalendar().components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or NSCalendarUnitMinute,
            fromDate = date
        )

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, false)
        val request = UNNotificationRequest.requestWithIdentifier("escalation_$doseId", content, trigger)

        center.addNotificationRequest(request) { _ -> }
    }

    actual fun cancelReminder(context: Any?, doseId: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(DoseReminderStage.entries.map { "$doseId:${it.name}" })
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