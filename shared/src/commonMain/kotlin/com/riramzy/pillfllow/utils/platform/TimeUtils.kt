package com.riramzy.pillfllow.utils.platform

import com.riramzy.pillfllow.domain.compliance.DoseStateMachine

expect fun currentTimeMillis(): Long
expect fun formatTime(millis: Long): String
expect fun formatMonthYear(millis: Long): String
expect fun getDayOfMonth(millis: Long): Int
expect fun getTodayTimeInMillis(hour: Int, minute: Int): Long
expect fun isSameMonthAndYear(millis1: Long, millis2: Long): Boolean
expect fun isSameDay(millis1: Long, millis2: Long): Boolean
fun formatRelativeNextDose(
    scheduledTime: Long?,
    now: Long
): String {
    if (scheduledTime == null) return "No upcoming doses"
    val diffMillis = scheduledTime - now
    return when {
        diffMillis < -DoseStateMachine.ON_TIME_WINDOW_MILLIS -> "Overdue"
        diffMillis <= 0 -> "Due now"
        else -> {
            val minutes = (diffMillis / (1000 * 60)).toInt()
            val hours = minutes / 60
            when {
                minutes < 60 -> "Next dose in $minutes mins"
                hours < 24 -> "Next dose in $hours ${if (hours == 1) "hour" else "hours"}"
                else -> "Next dose at ${formatTime(scheduledTime)}"
            }
        }
    }
}

fun parseTimeStringToMillis(timeStr: String): Long? {
    return runCatching {
        val parts = timeStr.trim().split(" ")
        if (parts.size != 2) return null
        val timeParts = parts[0].split(":")
        var hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        val amPm = parts[1].uppercase()
        if (amPm == "PM" && hour < 12) hour += 12
        if (amPm == "AM" && hour == 12) hour = 0
        getTodayTimeInMillis(hour, minute)
    }.getOrNull()
}

fun parseScheduleTimeToMillis(scheduleText: String?): List<Long> {
    if (scheduleText.isNullOrBlank()) return emptyList()
    return scheduleText.split(",").mapNotNull { parseTimeStringToMillis(it.trim()) }
}