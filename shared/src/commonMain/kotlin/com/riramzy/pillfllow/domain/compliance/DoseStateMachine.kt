package com.riramzy.pillfllow.domain.compliance

import com.riramzy.pillfllow.utils.medication.DoseComplianceStatus
import com.riramzy.pillfllow.utils.platform.currentTimeMillis

object DoseStateMachine {
    const val ON_TIME_WINDOW_MILLIS = 30 * 60 * 1000L
    const val LATE_WINDOW_MILLIS = 2 * 60 * 60 * 1000L

    fun evaluateCompliance(
        scheduledTimeMillis: Long,
        actionTimeMillis: Long = currentTimeMillis()
    ): DoseComplianceStatus {
        val diff = actionTimeMillis - scheduledTimeMillis

        return when {
            diff <= ON_TIME_WINDOW_MILLIS -> DoseComplianceStatus.ON_TIME
            diff <= LATE_WINDOW_MILLIS -> DoseComplianceStatus.LATE
            else -> DoseComplianceStatus.LATE
        }
    }

    fun isOverdue(
        scheduledTimeMillis: Long,
        currentTimeMillis: Long = currentTimeMillis()
    ): Boolean {
        return (currentTimeMillis - scheduledTimeMillis) > LATE_WINDOW_MILLIS
    }
}