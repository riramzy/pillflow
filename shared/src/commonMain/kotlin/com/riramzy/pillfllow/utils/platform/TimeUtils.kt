package com.riramzy.pillfllow.utils.platform

import com.riramzy.pillfllow.domain.compliance.DoseStateMachine

expect fun currentTimeMillis(): Long
expect fun formatTime(millis: Long): String
expect fun formatDate(millis: Long): String
expect fun formatMonthYear(millis: Long): String
expect fun getDayOfMonth(millis: Long): Int
expect fun getTodayTimeInMillis(hour: Int, minute: Int): Long
expect fun isSameMonthAndYear(millis1: Long, millis2: Long): Boolean
expect fun isSameDay(millis1: Long, millis2: Long): Boolean
expect fun shiftMonth(millis: Long, amount: Int): Long
expect fun getDaysInMonth(millis: Long): Int
expect fun getFirstDayOfWeekOfMonth(millis: Long): Int
fun formatRelativeNextDose(
    scheduledTime: Long?,
    now: Long
): String {
    if (scheduledTime == null) return "No upcoming doses"
    val diffMillis = scheduledTime - now
    val timeLabel = formatTime(scheduledTime)

    return when {
        diffMillis < -DoseStateMachine.ON_TIME_WINDOW_MILLIS -> "Overdue ($timeLabel)"
        diffMillis <= 0 -> "Due now ($timeLabel)"
        else -> {
            val totalMinutes = (diffMillis / (1000 * 60)).toInt().coerceAtLeast(1)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60

            when {
                totalMinutes < 60 -> "Next dose in ${totalMinutes}m ($timeLabel)"
                isSameDay(scheduledTime, now) -> {
                    if (minutes > 0) "Next dose in ${hours}h ${minutes}m ($timeLabel)"
                    else "Next dose in ${hours}h ($timeLabel)"
                }
                else -> "Next dose at $timeLabel"
            }
        }
    }
}

fun findNextActionDoseTime(
    scheduledTimes: Iterable<Long>,
    now: Long,
    includeLateWindowToday: Boolean = true
): Long? {
    return scheduledTimes.filter { time ->
        if (includeLateWindowToday) {
            time >= now - DoseStateMachine.ON_TIME_WINDOW_MILLIS ||
                    (isSameDay(time, now) &&
                            time >= now - DoseStateMachine.LATE_WINDOW_MILLIS)
        } else {
            time >= now - DoseStateMachine.ON_TIME_WINDOW_MILLIS
        }
    }.minOrNull()
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