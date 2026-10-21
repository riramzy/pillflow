package com.riramzy.pillfllow.domain.compliance

import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.ui.components.history.MonthDaysCompliance
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceCardUiModel
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceDayUiModel
import com.riramzy.pillfllow.ui.state.history.HistoryLogRecordUiModel
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.platform.formatDate
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.getDayOfMonth
import com.riramzy.pillfllow.utils.platform.getDaysInMonth
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
    val skippedCount: Int = 0,
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
        todayPendingDoses: List<PendingDoseWithMedication>,
        todayHistoryDoses: List<DoseHistoryEntity>,
        now: Long
    ): DailyStatusEvaluation {
        val scheduledHistory = todayHistoryDoses.filter { !it.isPrn }
        val prnHistory = todayHistoryDoses.filter { it.isPrn }

        val totalToday = todayPendingDoses.size + todayHistoryDoses.size
        if (totalToday == 0) {
            return DailyStatusEvaluation(ComplianceStatus.DEFAULT, "No Doses Scheduled Today")
        }

        val overdue = todayPendingDoses.filter { (now - it.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS }
        if (overdue.isNotEmpty()) {
            val d = overdue.minByOrNull { it.scheduledTime }!!
            return DailyStatusEvaluation(ComplianceStatus.MISSED, "MISSED: ${d.name} ${d.dosage} was due at ${formatTime(d.scheduledTime)}")
        }

        val late = todayPendingDoses.filter {
            val diff = now - it.scheduledTime
            diff in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
        }
        if (late.isNotEmpty()) {
            val d = late.minByOrNull { it.scheduledTime }!!
            return DailyStatusEvaluation(ComplianceStatus.LATE, "LATE: ${d.name} ${d.dosage} was due at ${formatTime(d.scheduledTime)}")
        }

        val dueNow = todayPendingDoses.filter { now >= it.scheduledTime && (now - it.scheduledTime) <= DoseStateMachine.ON_TIME_WINDOW_MILLIS }
        if (dueNow.isNotEmpty()) {
            val d = dueNow.minByOrNull { it.scheduledTime }!!
            return DailyStatusEvaluation(ComplianceStatus.DEFAULT, "DUE NOW: ${d.name} ${d.dosage} is scheduled for ${formatTime(d.scheduledTime)}")
        }

        val upcoming = todayPendingDoses.filter { now < it.scheduledTime }
        if (upcoming.isNotEmpty()) {
            val d = upcoming.minByOrNull { it.scheduledTime }!!
            return DailyStatusEvaluation(ComplianceStatus.DEFAULT, "Next: ${d.name} ${d.dosage} is scheduled for ${formatTime(d.scheduledTime)}")
        }

        val hasLate = scheduledHistory.any { it.complianceStatus == "LATE" }
        val allSkipped = scheduledHistory.isNotEmpty() && scheduledHistory.all { it.complianceStatus == "SKIPPED" }

        return when {
            allSkipped -> {
                val reason = scheduledHistory.firstOrNull()?.skipReason?.ifBlank { null } ?: "Doctor Hold"
                DailyStatusEvaluation(ComplianceStatus.SKIPPED, "All Set For Today (Dose Skipped: $reason)")
            }

            hasLate -> DailyStatusEvaluation(ComplianceStatus.LATE, "All Doses Taken (With Late Doses)")

            scheduledHistory.isNotEmpty() -> DailyStatusEvaluation(ComplianceStatus.ON_TIME, "All Set For Today! 100% On-Time")

            prnHistory.isNotEmpty() -> DailyStatusEvaluation(ComplianceStatus.ON_TIME, "All Good! As-Needed Dose Logged")

            else -> DailyStatusEvaluation(ComplianceStatus.DEFAULT, "No Doses Scheduled Today")
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
        historyDoses: List<DoseHistoryEntity>,
        pendingDoses: List<PendingDoseWithMedication>,
        now: Long
    ): WeeklyComplianceEvaluation {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val currentDayIndex = (((now / 86400000L) + 3) % 7).toInt()

        val days = dayNames.mapIndexed { index, name ->
            val dayOffsetMillis = (index - currentDayIndex) * 86400000L
            val targetDayMillis = now + dayOffsetMillis

            when {
                index > currentDayIndex -> ComplianceDayUiModel(name, ComplianceStatus.DEFAULT)

                index == currentDayIndex -> {
                    val todayPending = pendingDoses.filter { isSameDay(it.scheduledTime, now) }
                    val todayHistory = historyDoses.filter { isSameDay(it.scheduledTime, now) && !it.isPrn }

                    val overdueCount = todayPending.count { (now - it.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS }

                    val lateCount = todayPending.count {
                        val diff = now - it.scheduledTime
                        diff in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
                    }

                    val hasMissedHistory = todayHistory.any {
                        (!it.isTaken && it.complianceStatus != "SKIPPED") || it.complianceStatus == "MISSED"
                    }

                    val hasLateHistory = todayHistory.any { it.complianceStatus == "LATE" }

                    val isAllSkipped = todayHistory.isNotEmpty() && todayPending.isEmpty() && todayHistory.all { it.complianceStatus == "SKIPPED" }

                    val todayStatus = when {
                        overdueCount > 0 || hasMissedHistory -> ComplianceStatus.MISSED
                        lateCount > 0 || hasLateHistory -> ComplianceStatus.LATE
                        isAllSkipped -> ComplianceStatus.SKIPPED
                        todayHistory.isEmpty() && (todayPending.isEmpty() || todayPending.all { now < it.scheduledTime }) -> ComplianceStatus.DEFAULT
                        else -> ComplianceStatus.ON_TIME
                    }

                    ComplianceDayUiModel(name, todayStatus)
                }

                else -> {
                    val dayRecords = historyDoses.filter {
                        isSameDay(it.scheduledTime, targetDayMillis) && !it.isPrn
                    }

                    val dayStatus = when {
                        dayRecords.isEmpty() -> ComplianceStatus.DEFAULT

                        dayRecords.any {
                            (!it.isTaken && it.complianceStatus != "SKIPPED") || it.complianceStatus == "MISSED"
                        } -> ComplianceStatus.MISSED

                        dayRecords.any { it.complianceStatus == "LATE" } -> ComplianceStatus.LATE

                        dayRecords.all { it.complianceStatus == "SKIPPED" } -> ComplianceStatus.SKIPPED

                        else -> ComplianceStatus.ON_TIME
                    }

                    ComplianceDayUiModel(name, dayStatus)
                }
            }
        }

        val elapsedDays = days.take(currentDayIndex + 1)
        val activeDays = elapsedDays.filter { it.status != ComplianceStatus.DEFAULT }
        val scorableDays = activeDays.filter { it.status != ComplianceStatus.SKIPPED }

        val totalScore = scorableDays.sumOf { day ->
            when (day.status) {
                ComplianceStatus.ON_TIME -> 100
                ComplianceStatus.LATE -> 80
                else -> 0
            }
        }

        val rate = if (scorableDays.isEmpty()) 100 else (totalScore / scorableDays.size)

        return WeeklyComplianceEvaluation(days, rate)
    }

    fun evaluatePatientComplianceCards(
        todayPendingDoses: List<PendingDoseWithMedication>,
        todayHistoryDoses: List<DoseHistoryEntity>,
        now: Long
    ): List<ComplianceCardUiModel> {
        val totalToday = todayPendingDoses.size + todayHistoryDoses.size
        if (totalToday == 0) {
            return listOf(
                ComplianceCardUiModel(
                    status = ComplianceStatus.DEFAULT,
                    title = "No Prescriptions Today",
                    subtitle = "You have no doses scheduled for today",
                    badgeText = "No Schedule"
                )
            )
        }

        val overduePending = todayPendingDoses.filter { (now - it.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS }
        if (overduePending.isNotEmpty()) {
            val target = overduePending.minByOrNull { it.scheduledTime }!!
            return listOf(
                ComplianceCardUiModel(
                    status = ComplianceStatus.MISSED,
                    title = "Missed: ${target.name} ${target.dosage}",
                    subtitle = "Scheduled time window expired",
                    badgeText = "Missed: Was Due ${formatTime(target.scheduledTime)}"
                )
            )
        }

        val latePending = todayPendingDoses.filter {
            val diff = now - it.scheduledTime
            diff in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
        }
        if (latePending.isNotEmpty()) {
            val target = latePending.minByOrNull { it.scheduledTime }!!
            return listOf(
                ComplianceCardUiModel(
                    status = ComplianceStatus.LATE,
                    title = "Late: ${target.name} ${target.dosage}",
                    subtitle = "Take as soon as possible",
                    badgeText = "Late: Was Due ${formatTime(target.scheduledTime)}"
                )
            )
        }

        val dueNowPending = todayPendingDoses.filter {
            now >= it.scheduledTime && (now - it.scheduledTime) <= DoseStateMachine.ON_TIME_WINDOW_MILLIS
        }
        if (dueNowPending.isNotEmpty()) {
            val target = dueNowPending.minByOrNull { it.scheduledTime }!!
            return listOf(
                ComplianceCardUiModel(
                    status = ComplianceStatus.DEFAULT,
                    title = "Due Now: ${target.name} ${target.dosage}",
                    subtitle = "Scheduled for today",
                    badgeText = "Due Now: ${formatTime(target.scheduledTime)}"
                )
            )
        }

        val upcomingPending = todayPendingDoses.filter { now < it.scheduledTime }
        if (upcomingPending.isNotEmpty()) {
            val target = upcomingPending.minByOrNull { it.scheduledTime }!!
            return listOf(
                ComplianceCardUiModel(
                    status = ComplianceStatus.DEFAULT,
                    title = "Next: ${target.name} ${target.dosage}",
                    subtitle = "Scheduled for today",
                    badgeText = "Upcoming: ${formatTime(target.scheduledTime)}"
                )
            )
        }

        val scheduledHistory = todayHistoryDoses.filter { !it.isPrn }
        val hasLateHistory = scheduledHistory.any { it.complianceStatus == "LATE" }
        val allSkipped = scheduledHistory.isNotEmpty() && scheduledHistory.all { it.complianceStatus == "SKIPPED" }

        return when {
            allSkipped -> {
                val reason = scheduledHistory.firstOrNull()?.skipReason?.ifBlank { null }
                val reasonSuffix = if (reason != null) ": $reason" else ""

                listOf(
                    ComplianceCardUiModel(
                        status = ComplianceStatus.SKIPPED,
                        title = "All Scheduled Doses Skipped",
                        subtitle = "Marked as skipped$reasonSuffix",
                        badgeText = "Skipped Today"
                    )
                )
            }

            hasLateHistory -> {
                listOf(
                    ComplianceCardUiModel(
                        status = ComplianceStatus.LATE,
                        title = "All Doses Taken For Today",
                        subtitle = "Completed with late doses",
                        badgeText = "Completed Late"
                    )
                )
            }

            scheduledHistory.isNotEmpty() -> {
                listOf(
                    ComplianceCardUiModel(
                        status = ComplianceStatus.ON_TIME,
                        title = "All Set For Today!",
                        subtitle = "All ${scheduledHistory.size} scheduled doses completed on time",
                        badgeText = "100% On-Time"
                    )
                )
            }

            todayHistoryDoses.any { it.isPrn } -> {
                listOf(
                    ComplianceCardUiModel(
                        status = ComplianceStatus.DEFAULT,
                        title = "As-Needed Dose Logged",
                        subtitle = "PRN medication taken today",
                        badgeText = "PRN Logged"
                    )
                )
            }

            else -> {
                listOf(
                    ComplianceCardUiModel(
                        status = ComplianceStatus.DEFAULT,
                        title = "No Prescriptions Today",
                        subtitle = "You have no doses scheduled for today",
                        badgeText = "No Schedule"
                    )
                )
            }
        }
    }

    fun evaluateHistoryAnalytics(
        historyDoses: List<DoseHistoryEntity>,
        targetMonthMillis: Long,
        now: Long
    ): HistoryAnalyticsEvaluation {
        val monthDoses = historyDoses.filter { isSameMonthAndYear(it.scheduledTime, targetMonthMillis) }

        var onTime = 0
        var late = 0
        var missed = 0
        var skipped = 0

        val pastOrTakenRecords = monthDoses.filter { record ->
            record.isTaken ||
                    now > (record.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS) ||
                    record.complianceStatus == "MISSED"
        }

        monthDoses.forEach { record ->
            when {
                record.complianceStatus == "SKIPPED" -> skipped++
                record.isPrn -> { /* Neutral: PRN does not affect scheduled adherence */ }
                record.isTaken && record.complianceStatus == "ON_TIME" -> onTime++
                record.isTaken && record.complianceStatus == "LATE" -> late++
                !record.isTaken && now > (record.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS) -> missed++
                record.complianceStatus == "MISSED" -> missed++
            }
        }

        val totalEvaluated = onTime + late + missed
        val score = if (totalEvaluated > 0) {
            ((onTime * 100) + (late * 80)) / totalEvaluated
        } else 100

        val daysInMonth = getDaysInMonth(targetMonthMillis)

        val heatmap = (1..daysInMonth).map { day ->
            val dayDoses = monthDoses.filter { getDayOfMonth(it.scheduledTime) == day && !it.isPrn }

            val dayStatus = when {
                dayDoses.isEmpty() -> ComplianceStatus.DEFAULT
                dayDoses.any {
                    (!it.isTaken && now > (it.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS)) ||
                            it.complianceStatus == "MISSED"
                } -> ComplianceStatus.MISSED
                dayDoses.any { it.complianceStatus == "LATE" } -> ComplianceStatus.LATE
                dayDoses.all { it.complianceStatus == "SKIPPED" } -> ComplianceStatus.SKIPPED
                dayDoses.all { it.isTaken && (it.complianceStatus == "ON_TIME" || it.complianceStatus == "SKIPPED") } -> ComplianceStatus.ON_TIME
                else -> ComplianceStatus.DEFAULT
            }

            MonthDaysCompliance(dayNumber = day.toString(), status = dayStatus)
        }

        val logRecords = pastOrTakenRecords.map { record ->
            val isSkipped = record.complianceStatus == "SKIPPED"

            val isPrn = record.isPrn

            val isMissed = !isSkipped && !isPrn && (
                    (!record.isTaken && now > (record.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS)) ||
                            record.complianceStatus == "MISSED"
                    )

            val recordStatus = when {
                isSkipped -> ComplianceStatus.SKIPPED
                isPrn -> ComplianceStatus.ON_TIME
                record.isTaken && record.complianceStatus == "ON_TIME" -> ComplianceStatus.ON_TIME
                record.isTaken && record.complianceStatus == "LATE" -> ComplianceStatus.LATE
                isMissed -> ComplianceStatus.MISSED
                else -> ComplianceStatus.DEFAULT
            }

            val recordTimestampText = when {
                isSkipped -> {
                    val reason = record.skipReason?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
                    "Skipped$reason"
                }

                isPrn -> "Taken ${formatTime(record.takenTime ?: record.scheduledTime)}"

                record.isTaken && record.takenTime != null -> "Logged ${formatTime(record.takenTime)}"

                isMissed -> "Dose Missed"

                else -> "Scheduled ${formatTime(record.scheduledTime)}"
            }

            HistoryLogRecordUiModel(
                id = record.id,
                patientName = "",
                actionTitle = "${formatDate(record.scheduledTime)}, ${record.name} ${record.dosage}",
                actionDescription = if (isPrn) "As Needed (PRN)" else "Scheduled ${formatTime(record.scheduledTime)}",
                timestampText = recordTimestampText,
                status = recordStatus,
            )
        }

        return HistoryAnalyticsEvaluation(
            scorePercentage = score,
            onTimeCount = onTime,
            lateCount = late,
            missedCount = missed,
            skippedCount = skipped,
            monthlyHeatmap = heatmap,
            logRecords = logRecords
        )
    }

    fun evaluatePatientSummary(
        todayPendingDoses: List<PendingDoseWithMedication>,
        todayHistoryDoses: List<DoseHistoryEntity>,
        now: Long
    ): PatientSummaryEvaluation {
        val totalDoses = todayPendingDoses.size + todayHistoryDoses.size
        if (totalDoses == 0) {
            return PatientSummaryEvaluation(ComplianceStatus.DEFAULT, 0, 0, 100)
        }

        val missedCount = todayPendingDoses.count {
            (now - it.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS
        }

        val pendingLate = todayPendingDoses.count {
            val diff = now - it.scheduledTime
            diff in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
        }
        val historyLate = todayHistoryDoses.count { it.complianceStatus == "LATE" }
        val lateCount = pendingLate + historyLate

        val onTimeCount = todayHistoryDoses.count { it.isTaken && it.complianceStatus == "ON_TIME" }

        val scheduledHistory = todayHistoryDoses.filter { !it.isPrn }
        val allSkipped = scheduledHistory.isNotEmpty() && todayPendingDoses.isEmpty() && scheduledHistory.all { it.complianceStatus == "SKIPPED" }

        val status = when {
            allSkipped -> ComplianceStatus.SKIPPED
            missedCount > 0 -> ComplianceStatus.MISSED
            lateCount > 0 -> ComplianceStatus.LATE
            todayPendingDoses.any { now >= it.scheduledTime } -> ComplianceStatus.DEFAULT
            todayPendingDoses.isNotEmpty() && onTimeCount == 0 -> ComplianceStatus.DEFAULT
            else -> ComplianceStatus.ON_TIME
        }

        val evaluatedCount = onTimeCount + lateCount + missedCount

        val score = if (evaluatedCount > 0) {
            ((onTimeCount * 100) + (lateCount * 80)) / evaluatedCount
        } else 100

        return PatientSummaryEvaluation(
            status = status,
            lateDosesCount = lateCount,
            missedDosesCount = missedCount,
            compliancePercentage = score
        )
    }
}