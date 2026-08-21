package com.riramzy.pillfllow.domain.hardware

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
        triggerTimeMillis: Long
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

        val request = UNNotificationRequest.requestWithIdentifier(doseId, content, trigger)

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("PillFlow Error: Failed to schedule notification for $pillName: ${error.localizedDescription}")
            } else {
                println("PillFlow: Successfully scheduled local reminder for $pillName (ID: $doseId)")
            }
        }
    }

    actual fun cancelReminder(context: Any?, doseId: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(doseId))
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