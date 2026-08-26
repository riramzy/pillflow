package com.riramzy.pillfllow.utils

expect fun currentTimeMillis(): Long
expect fun formatTime(millis: Long): String
expect fun getDayOfMonth(millis: Long): Int
expect fun getTodayTimeInMillis(hour: Int, minute: Int): Long