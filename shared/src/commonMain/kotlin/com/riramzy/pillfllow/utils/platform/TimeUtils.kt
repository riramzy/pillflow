package com.riramzy.pillfllow.utils.platform

import com.riramzy.pillfllow.domain.compliance.DoseStateMachine

expect fun currentTimeMillis(): Long
expect fun formatTime(millis: Long): String
expect fun formatMonthYear(millis: Long): String
expect fun getDayOfMonth(millis: Long): Int
expect fun getTodayTimeInMillis(hour: Int, minute: Int): Long
expect fun isSameMonthAndYear(millis1: Long, millis2: Long): Boolean
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