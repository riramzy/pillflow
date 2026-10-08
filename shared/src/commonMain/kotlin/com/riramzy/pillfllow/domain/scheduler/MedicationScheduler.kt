package com.riramzy.pillfllow.domain.scheduler

import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity

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
            baseTimesToday.mapNotNull { baseTime ->
                val targetTime = baseTime + (dayOffset * DAY_MILLIS)
                if (existingTimes.contains(targetTime)) {
                    null
                } else {
                    ScheduledDoseEntity(
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