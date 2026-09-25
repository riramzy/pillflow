package com.riramzy.pillfllow.domain.compliance

import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceDayUiModel
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.getDayOfMonth

data class DoseCardEvaluation(
    val status: ComplianceStatus,
    val badgeText: String,
    val isDishEligible: Boolean
)

data class DailyStatusEvaluation(
    val status: ComplianceStatus,
    val alertText: String
)

data class LiveActivityEvaluation(
    val status: ComplianceStatus,
    val actionDescription: String
)

data class WeeklyComplianceEvaluation(
    val days: List<ComplianceDayUiModel>,
    val weeklyRate: Int
)

object DoseComplianceEvaluator {
    fun evaluateDoseCard(
        scheduledTime: Long,
        now: Long,
        isTaken: Boolean
    ): DoseCardEvaluation {
        val formattedTime = formatTime(scheduledTime)
        val elapsed = now - scheduledTime

        val isOverdue = elapsed > DoseStateMachine.LATE_WINDOW_MILLIS
        val isLate = elapsed in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
        val isDueNow = now >= scheduledTime && elapsed <= DoseStateMachine.ON_TIME_WINDOW_MILLIS
        val isTomorrow = getDayOfMonth(scheduledTime) != getDayOfMonth(now)

        val (status, badge) = when {
            isTaken -> ComplianceStatus.ON_TIME to "Taken: $formattedTime"
            isOverdue -> ComplianceStatus.MISSED to "Missed: Was Due $formattedTime"
            isLate -> ComplianceStatus.LATE to "Late: Was Due $formattedTime"
            isDueNow -> ComplianceStatus.DEFAULT to "Due Now: $formattedTime"
            isTomorrow -> ComplianceStatus.DEFAULT to "Tomorrow: $formattedTime"
            else -> ComplianceStatus.DEFAULT to "Upcoming: $formattedTime"
        }

        return DoseCardEvaluation(
            status = status,
            badgeText = badge,
            isDishEligible = !isTaken && now in (scheduledTime - DoseStateMachine.ON_TIME_WINDOW_MILLIS)..(scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS)
        )
    }

    fun evaluateDailyStatus(
        earliestDose: PendingDoseWithMedication?,
        now: Long
    ): DailyStatusEvaluation {
        if (earliestDose == null) {
            return DailyStatusEvaluation(ComplianceStatus.ON_TIME, "All Set For Today!")
        }

        val elapsed = now - earliestDose.scheduledTime
        val formattedTime = formatTime(earliestDose.scheduledTime)


        return when {
            elapsed > DoseStateMachine.LATE_WINDOW_MILLIS -> DailyStatusEvaluation(
                ComplianceStatus.MISSED,
                "ALERT: ${earliestDose.name} ${earliestDose.dosage} was due at $formattedTime"
            )

            elapsed > DoseStateMachine.ON_TIME_WINDOW_MILLIS -> DailyStatusEvaluation(
                ComplianceStatus.LATE,
                "LATE: ${earliestDose.name} ${earliestDose.dosage} was due at $formattedTime"
            )

            now >= earliestDose.scheduledTime -> DailyStatusEvaluation(
                ComplianceStatus.DEFAULT,
                "DUE NOW: ${earliestDose.name} ${earliestDose.dosage} is scheduled for $formattedTime"
            )

            else -> DailyStatusEvaluation(
                ComplianceStatus.DEFAULT,
                "Next: ${earliestDose.name} ${earliestDose.dosage} is scheduled for $formattedTime"
            )
        }
    }

    fun evaluateLiveActivity(
        doseName: String,
        dosage: String,
        scheduledTime: Long,
        now: Long
    ): LiveActivityEvaluation {
        val elapsed = now - scheduledTime
        val isOverdue = elapsed > DoseStateMachine.LATE_WINDOW_MILLIS
        val isLate = elapsed in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
        val isDueNow = now >= scheduledTime && elapsed <= DoseStateMachine.ON_TIME_WINDOW_MILLIS

        val (status, action) = when {
            isOverdue -> ComplianceStatus.MISSED to "missed $doseName $dosage"
            isLate -> ComplianceStatus.LATE to "is late taking $doseName $dosage"
            isDueNow -> ComplianceStatus.DEFAULT to "has $doseName $dosage due now"
            else -> ComplianceStatus.DEFAULT to "scheduled for $doseName $dosage"
        }

        return LiveActivityEvaluation(status, action)
    }

    fun evaluateWeeklyCompliance(
        pendingDoses: List<PendingDoseWithMedication>,
        now: Long
    ): WeeklyComplianceEvaluation {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val currentDayIndex = (((now / 86400000L) + 3) % 7).toInt()

        val overdueCount = pendingDoses.count { (now - it.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS }
        val lateCount = pendingDoses.count { (now - it.scheduledTime) in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS }

        val todayStatus = when {
            overdueCount > 0 -> ComplianceStatus.MISSED
            lateCount > 0 -> ComplianceStatus.LATE
            else -> ComplianceStatus.ON_TIME
        }

        val days = dayNames.mapIndexed { index, name ->
            when {
                index < currentDayIndex -> ComplianceDayUiModel(name, ComplianceStatus.ON_TIME)
                index == currentDayIndex -> ComplianceDayUiModel(name, todayStatus)
                else -> ComplianceDayUiModel(name, ComplianceStatus.DEFAULT)
            }
        }

        val totalRecorded = currentDayIndex + 1
        val missedDays = if (todayStatus == ComplianceStatus.MISSED) 1 else 0
        val rate = ((totalRecorded - missedDays) * 100) / totalRecorded.coerceAtLeast(1)

        return WeeklyComplianceEvaluation(days, rate)
    }
}