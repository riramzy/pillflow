package com.riramzy.pillfllow.domain.scheduler

import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.isSameDay

object MedicationScheduler {
    const val ROLLING_DAYS = 7
    const val DAY_MILLIS = 86_400_000L

    fun generateRollingDoses(
        medicationId: String,
        baseTimesToday: List<Long>,
        existingTimes: Set<Long> = emptySet(),
        daysAhead: Int = ROLLING_DAYS
    ): List<ScheduledDoseEntity> {
        if (baseTimesToday.isEmpty()) return emptyList()

        return (0 until daysAhead).flatMap { dayOffset ->
            val isToday = dayOffset == 0

            val takenTodayCount = if (isToday) {
                existingTimes.count { takenTime ->
                    baseTimesToday.any { baseTime ->
                        isSameDay(takenTime, baseTime)
                    }
                }
            } else 0

            if (isToday && takenTodayCount >= baseTimesToday.size) {
                emptyList()
            } else {
                baseTimesToday.mapNotNull { baseTime ->
                    val targetTime = baseTime + (dayOffset * DAY_MILLIS)
                    if (existingTimes.contains(targetTime)) {
                        null
                    } else {
                        ScheduledDoseEntity(
                            id = "${medicationId}_${targetTime}",
                            medicationId = medicationId,
                            scheduledTime = targetTime,
                            complianceStatus = "PENDING",
                            isTaken = false,
                            isSynced = false
                        )
                    }
                }
            }
        }
    }

    fun extendRollingWindow(
        medicationId: String,
        existingDoses: List<ScheduledDoseEntity>,
        now: Long = currentTimeMillis()
    ): List<ScheduledDoseEntity> {
        if (existingDoses.isEmpty()) return emptyList()

        val latestDoseTime = existingDoses.maxOf { it.scheduledTime }
        if (latestDoseTime > now + (6 * DAY_MILLIS)) return emptyList()

        val existingTimes = existingDoses.map { it.scheduledTime }.toSet()
        val distinctOffsets = existingDoses.map { it.scheduledTime % DAY_MILLIS }.distinct()

        val todayStart = now - (now % DAY_MILLIS)
        val baseTimesToday = distinctOffsets.map { todayStart + it }

        return generateRollingDoses(
            medicationId = medicationId,
            baseTimesToday = baseTimesToday,
            existingTimes = existingTimes,
            daysAhead = ROLLING_DAYS
        )
    }
}