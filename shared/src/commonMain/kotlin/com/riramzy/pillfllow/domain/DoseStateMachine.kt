package com.riramzy.pillfllow.domain

import com.riramzy.pillfllow.utils.DoseComplianceStatus
import com.riramzy.pillfllow.utils.currentTimeMillis

object DoseStateMachine {
    const val GRACE_WINDOW_MILLIS = 30 * 60 * 1000L

    fun evaluateCompliance(
        scheduledTimeMillis: Long,
        actionTimeMillis: Long = currentTimeMillis()
    ): DoseComplianceStatus {
        val timeDifferenceMillis = actionTimeMillis - scheduledTimeMillis

        return when {
            timeDifferenceMillis <= GRACE_WINDOW_MILLIS -> DoseComplianceStatus.ON_TIME
            else -> DoseComplianceStatus.LATE
        }
    }

    fun isOverdue(
        scheduledTimeMillis: Long,
        currentTimeMillis: Long = currentTimeMillis()
    ): Boolean {
        return (currentTimeMillis - scheduledTimeMillis) > GRACE_WINDOW_MILLIS
    }
}