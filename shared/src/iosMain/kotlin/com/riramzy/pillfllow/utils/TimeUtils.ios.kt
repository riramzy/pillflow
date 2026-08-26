package com.riramzy.pillfllow.utils

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeInterval
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun formatTime(millis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "h:mm a"
    }
    return formatter.stringFromDate(date)
}

actual fun getDayOfMonth(millis: Long): Int {
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
    return NSCalendar.currentCalendar.component(NSCalendarUnitDay, fromDate = date).toInt()
}

actual fun getTodayTimeInMillis(hour: Int, minute: Int): Long {
    val cal = NSCalendar.currentCalendar
    val now = NSDate()

    val components = cal.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        fromDate = now
    ).apply {
        this.hour = hour.toLong()
        this.minute = minute.toLong()
        this.second = 0
    }

    var date = cal.dateFromComponents(components) ?: now

    if (date.timeIntervalSince1970 <= now.timeIntervalSince1970) {
        date = NSDate.dateWithTimeInterval(86400.0, sinceDate = date)
    }

    return (date.timeIntervalSince1970 * 1000).toLong()
}