package com.riramzy.pillfllow.utils

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
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