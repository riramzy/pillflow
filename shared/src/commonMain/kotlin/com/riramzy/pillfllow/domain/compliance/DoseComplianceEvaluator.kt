package com.riramzy.pillfllow.domain.compliance

import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.ui.components.history.MonthDaysCompliance
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceCardUiModel
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceDayUiModel
import com.riramzy.pillfllow.ui.state.history.HistoryLogRecordUiModel
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.getDayOfMonth
import com.riramzy.pillfllow.utils.platform.isSameDay
import com.riramzy.pillfllow.utils.platform.isSameMonthAndYear

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

data class HistoryAnalyticsEvaluation(
    val scorePercentage: Int,
    val onTimeCount: Int,
    val lateCount: Int,
    val missedCount: Int,
    val monthlyHeatmap: List<MonthDaysCompliance>,
    val logRecords: List<HistoryLogRecordUiModel>
)

data class PatientSummaryEvaluation(
    val status: ComplianceStatus,
    val lateDosesCount: Int,
    val missedDosesCount: Int,
    val compliancePercentage: Int
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

        val isDishEligible = !isTaken &&
                isSameDay(scheduledTime, now) &&
                now in (scheduledTime - DoseStateMachine.ON_TIME_WINDOW_MILLIS)..(scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS)

        val (status, badge) = when {
            isTaken -> ComplianceStatus.ON_TIME to "Taken: $formattedTime"
            isOverdue -> ComplianceStatus.MISSED to "Missed: Was Due $formattedTime"
            isLate -> ComplianceStatus.LATE to "Late: Was Due $formattedTime"
            isDueNow -> ComplianceStatus.DEFAULT to "Due Now: $formattedTime"
            else -> ComplianceStatus.DEFAULT to "Upcoming: $formattedTime"
        }

        return DoseCardEvaluation(
            status = status,
            badgeText = badge,
            isDishEligible = isDishEligible
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

    fun evaluatePatientComplianceCard(
        pendingDoses: List<PendingDoseWithMedication>,
        now: Long
    ): ComplianceCardUiModel {
        val earliestDose = pendingDoses.minByOrNull { it.scheduledTime }

        if (earliestDose == null) {
            return ComplianceCardUiModel(
                status = ComplianceStatus.ON_TIME,
                title = "All Set For Today!",
                subtitle = "All scheduled doses completed",
                badgeText = "100% On-Time"
            )
        }

        val elapsed = now - earliestDose.scheduledTime
        val formattedTime = formatTime(earliestDose.scheduledTime)

        return when {
            elapsed > DoseStateMachine.LATE_WINDOW_MILLIS -> ComplianceCardUiModel(
                status = ComplianceStatus.MISSED,
                title = "Overdue: ${earliestDose.name}",
                subtitle = "Scheduled time window expired",
                badgeText = "Missed: Was Due $formattedTime"
            )

            elapsed > DoseStateMachine.ON_TIME_WINDOW_MILLIS -> ComplianceCardUiModel(
                status = ComplianceStatus.LATE,
                title = "Late: ${earliestDose.name} ${earliestDose.dosage}",
                subtitle = "Take as soon as possible",
                badgeText = "Late: Was Due $formattedTime"
            )

            now >= earliestDose.scheduledTime -> ComplianceCardUiModel(
                status = ComplianceStatus.DEFAULT, // Correct neutral status for Due Now
                title = "Due Now: ${earliestDose.name} ${earliestDose.dosage}",
                subtitle = "Scheduled for today",
                badgeText = "Due Now: $formattedTime"
            )

            else -> {
                ComplianceCardUiModel(
                    status = ComplianceStatus.DEFAULT,
                    title = "Next: ${earliestDose.name} ${earliestDose.dosage}",
                    subtitle = "Scheduled for today",
                    badgeText = "Upcoming: $formattedTime"
                )
            }
        }
    }

    fun evaluateHistoryAnalytics(
        historyDoses: List<DoseHistoryEntity>,
        now: Long
    ): HistoryAnalyticsEvaluation {
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val rollingDoses = historyDoses.filter { it.scheduledTime >= sevenDaysAgo }

        var onTime = 0
        var late = 0
        var missed = 0

        rollingDoses.forEach { record ->
            when {
                record.isTaken && record.complianceStatus == "ON_TIME" -> onTime++
                record.isTaken && record.complianceStatus == "LATE" -> late++
                !record.isTaken && now > (record.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS) -> missed++
                record.complianceStatus == "MISSED" -> missed++
            }
        }

        val total = onTime + late + missed
        val score = if (total > 0) ((onTime * 100) / total) else 100

        val currentMonthDoses = historyDoses.filter { isSameMonthAndYear(it.scheduledTime, now) }

        val heatmapDays = (1..31).map { day ->
            val dayDoses = currentMonthDoses.filter { getDayOfMonth(it.scheduledTime) == day }

            val dayStatus = when {
                dayDoses.isEmpty() -> ComplianceStatus.DEFAULT

                dayDoses.any {
                    (!it.isTaken && now > (it.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS)) ||
                            it.complianceStatus == "MISSED"
                } -> ComplianceStatus.MISSED

                dayDoses.any { it.complianceStatus == "LATE" } -> ComplianceStatus.LATE

                dayDoses.all { it.isTaken && it.complianceStatus == "ON_TIME" } -> ComplianceStatus.ON_TIME

                else -> ComplianceStatus.DEFAULT
            }

            MonthDaysCompliance(dayNumber = day.toString(), status = dayStatus)
        }

        val pastOrTakenDoses = historyDoses.filter { record ->
            record.isTaken || (now - record.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS
        }

        val logRecords = pastOrTakenDoses.map { record ->
            val recordStatus = when {
                record.isTaken && record.complianceStatus == "ON_TIME" -> ComplianceStatus.ON_TIME
                record.isTaken && record.complianceStatus == "LATE" -> ComplianceStatus.LATE
                !record.isTaken && (now - record.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS -> ComplianceStatus.MISSED
                else -> ComplianceStatus.DEFAULT
            }

            val recordTimestampText = if (record.isTaken && record.takenTime != null) {
                "Logged ${formatTime(record.takenTime)}"
            } else {
                "Dose Missed"
            }

            HistoryLogRecordUiModel(
                id = record.id,
                patientName = "",
                actionTitle = "${record.name} ${record.dosage}",
                actionDescription = "Scheduled ${formatTime(record.scheduledTime)}",
                timestampText = recordTimestampText,
                status = recordStatus,
            )
        }

        return HistoryAnalyticsEvaluation(
            scorePercentage = score,
            onTimeCount = onTime,
            lateCount = late,
            missedCount = missed,
            monthlyHeatmap = heatmapDays,
            logRecords = logRecords
        )
    }

    fun evaluatePatientSummary(
        patientDoses: List<PendingDoseWithMedication>,
        now: Long
    ): PatientSummaryEvaluation {
        val missedCount = patientDoses.count { (now - it.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS }

        val lateCount = patientDoses.count {
            val diff = now - it.scheduledTime
            diff in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
        }

        val status = when {
            missedCount > 0 -> ComplianceStatus.MISSED
            lateCount > 0 -> ComplianceStatus.LATE
            else -> ComplianceStatus.ON_TIME
        }

        val score = if (patientDoses.isEmpty()) 100 else (100 - ((missedCount * 100) / patientDoses.size))

        return PatientSummaryEvaluation(
            status = status,
            lateDosesCount = lateCount,
            missedDosesCount = missedCount,
            compliancePercentage = score
        )
    }
}