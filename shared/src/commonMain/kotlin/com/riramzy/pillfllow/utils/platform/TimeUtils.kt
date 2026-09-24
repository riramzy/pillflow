package com.riramzy.pillfllow.utils.platform

expect fun currentTimeMillis(): Long
expect fun formatTime(millis: Long): String
expect fun getDayOfMonth(millis: Long): Int
expect fun getTodayTimeInMillis(hour: Int, minute: Int): Long
expect fun isSameMonthAndYear(millis1: Long, millis2: Long): Boolean