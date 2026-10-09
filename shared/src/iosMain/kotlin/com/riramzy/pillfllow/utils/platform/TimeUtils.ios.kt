package com.riramzy.pillfllow.utils.platform

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
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

actual fun formatDate(millis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "MMM d"
    }

    return formatter.stringFromDate(date)
}

actual fun formatMonthYear(millis: Long): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "MMMM yyyy"
    }

    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)

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

actual fun isSameMonthAndYear(millis1: Long, millis2: Long): Boolean {
    val cal = NSCalendar.currentCalendar
    val date1 = NSDate.dateWithTimeIntervalSince1970(millis1 / 1000.0)
    val date2 = NSDate.dateWithTimeIntervalSince1970(millis2 / 1000.0)
    val comp1 = cal.components(NSCalendarUnitYear or NSCalendarUnitMonth, fromDate = date1)
    val comp2 = cal.components(NSCalendarUnitYear or NSCalendarUnitMonth, fromDate = date2)

    return comp1.year == comp2.year && comp1.month == comp2.month
}

actual fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal = NSCalendar.currentCalendar
    val d1 = NSDate.dateWithTimeIntervalSince1970(millis1 / 1000.0)
    val d2 = NSDate.dateWithTimeIntervalSince1970(millis2 / 1000.0)
    return cal.isDate(d1, inSameDayAsDate = d2)
}

actual fun shiftMonth(millis: Long, amount: Int): Long {
    val cal = NSCalendar.currentCalendar
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
    val components = NSDateComponents().apply {
        month = amount.toLong()
    }
    val shiftedDate = cal.dateByAddingComponents(components, toDate = date, options = 0u) ?: date

    return (shiftedDate.timeIntervalSince1970 * 1000).toLong()
}

actual fun getDaysInMonth(millis: Long): Int {
    val cal = NSCalendar.currentCalendar
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)

    val components = cal.components(
        NSCalendarUnitYear or NSCalendarUnitMonth,
        fromDate = date
    ).apply {
        day = 1
    }

    val startOfMonth = cal.dateFromComponents(components) ?: date

    val nextMonthComp = NSDateComponents().apply { month = 1 }
    val startOfNextMonth = cal.dateByAddingComponents(nextMonthComp, toDate = startOfMonth, options = 0u) ?: date

    val dayComponents = cal.components(
        NSCalendarUnitDay,
        fromDate = startOfMonth,
        toDate = startOfNextMonth,
        options = 0u
    )

    return dayComponents.day.toInt()
}

actual fun getFirstDayOfWeekOfMonth(millis: Long): Int {
    val cal = NSCalendar.currentCalendar
    val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
    val comp = cal.components(NSCalendarUnitYear or NSCalendarUnitMonth, fromDate = date).apply {
        day = 1
    }

    val firstDay = cal.dateFromComponents(comp) ?: date
    val weekday = cal.component(NSCalendarUnitWeekday, fromDate = firstDay).toInt()

    return (weekday + 5) % 7
}