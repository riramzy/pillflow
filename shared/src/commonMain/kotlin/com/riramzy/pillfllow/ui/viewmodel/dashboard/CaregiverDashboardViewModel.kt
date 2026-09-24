package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.NudgePatientUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceDayUiModel
import com.riramzy.pillfllow.ui.state.dashboard.RecentActivityUiModel
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.pill.PillColor
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.openPhoneDialer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CaregiverDashboardViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val nudgePatientUseCase: NudgePatientUseCase
): ViewModel() {
    private val _state = MutableStateFlow(CaregiverDashboardState())
    val state: StateFlow<CaregiverDashboardState> = _state.asStateFlow()

    private val _selectedPatientId = MutableStateFlow<String?>(null)

    init {
        loadDashboardData()
        observeSelectedPatientDoses()
    }

    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().collectLatest { caregiver ->
                _state.update { it.copy(caregiver = caregiver) }
                caregiver?.let { user ->
                    getCaregiverPatientsUseCase(user.id).collectLatest { patientModels ->
                        val validIds = patientModels.map { it.id }
                        if (patientModels.isNotEmpty() && (_selectedPatientId.value == null || !validIds.contains(_selectedPatientId.value))) {
                            _selectedPatientId.value = patientModels.first().id
                        }

                        _state.update {
                            it.copy(
                                patients = patientModels,
                                selectedPatientId = _selectedPatientId.value ?: "",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSelectedPatientDoses() {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedPatientId
                .filterNotNull()
                .flatMapLatest { patientId ->
                    getPendingDosesForUserUseCase(patientId)
                }
                .collectLatest { pendingDoses ->
                    val now = currentTimeMillis()

                    val mappedUiDoses = pendingDoses.map { dose ->
                        val formattedTime = formatTime(dose.scheduledTime)
                        val elapsed = now - dose.scheduledTime

                        val isOverdue = elapsed > DoseStateMachine.LATE_WINDOW_MILLIS
                        val isLate = elapsed in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
                        val isDueNow = now >= dose.scheduledTime && elapsed <= DoseStateMachine.ON_TIME_WINDOW_MILLIS

                        val pillColor = PillColor.entries.firstOrNull {
                            it.name.equals(dose.colorHex, ignoreCase = true)
                        }

                        val (cardStatus, badgeText) = when {
                            isOverdue -> ComplianceStatus.MISSED to "Missed: Was Due $formattedTime"
                            isLate -> ComplianceStatus.LATE to "Late: Was Due $formattedTime"
                            isDueNow -> ComplianceStatus.ON_TIME to "Due Now: $formattedTime"
                            else -> ComplianceStatus.DEFAULT to "Upcoming: $formattedTime"
                        }

                        ScheduledDoseUiModel(
                            id = dose.id,
                            name = dose.name,
                            dosage = dose.dosage,
                            timeFormatted = formattedTime,
                            color = pillColor ?: PillColor.CORAL_RED,
                            status = cardStatus,
                            badgeText = badgeText,
                            scheduledTime = dose.scheduledTime
                        )
                    }

                    val earliestDose = pendingDoses.minByOrNull { it.scheduledTime }

                    val (status, alertText) = when {
                        earliestDose == null -> {
                            ComplianceStatus.ON_TIME to "All Set For Today!"
                        }

                        (now - earliestDose.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS -> {
                            ComplianceStatus.MISSED to "ALERT: ${earliestDose.name} ${earliestDose.dosage} was due at ${formatTime(earliestDose.scheduledTime)}"
                        }

                        (now - earliestDose.scheduledTime) > DoseStateMachine.ON_TIME_WINDOW_MILLIS -> {
                            ComplianceStatus.LATE to "LATE: ${earliestDose.name} ${earliestDose.dosage} was due at ${formatTime(earliestDose.scheduledTime)}"
                        }

                        now >= earliestDose.scheduledTime -> {
                            ComplianceStatus.ON_TIME to "DUE NOW: ${earliestDose.name} ${earliestDose.dosage} is scheduled for ${formatTime(earliestDose.scheduledTime)}"
                        }

                        else -> {
                            ComplianceStatus.DEFAULT to "Next: ${earliestDose.name} ${earliestDose.dosage} is scheduled for ${formatTime(earliestDose.scheduledTime)}"
                        }
                    }

                    val (weeklyDays, weeklyRate) = calculateWeeklyCompliance(pendingDoses, now)

                    val patientName = _state.value.activePatient?.name ?: "Patient"
                    val activities = generateLiveActivities(patientName, pendingDoses, now)

                    _state.update {
                        it.copy(
                            selectedPatientDoses = mappedUiDoses,
                            selectedPatientDailyStatus = status,
                            dailyStatusAlertText = alertText,
                            weeklyCompliance = weeklyDays,
                            recentActivities = activities,
                            weeklyRatePercentage = weeklyRate,
                            lastUpdatedText = "Updated at ${formatTime(now)}"
                        )
                    }
                }
        }
    }

    fun selectPatient(patientId: String) {
        _selectedPatientId.value = patientId
        _state.update { it.copy(selectedPatientId = patientId) }
    }

    fun callPatient(patientId: String) {
        val phone = _state.value.patients.firstOrNull { it.id == patientId }?.phoneNumber

        if (!phone.isNullOrBlank()) {
            openPhoneDialer(phone)
        }
    }

    fun nudgePatient(patientId: String) {
        val patientName = _state.value.patients.firstOrNull { it.id == patientId }?.name ?: "Patient"
        val previousAlert = _state.value.dailyStatusAlertText

        val caregiver = _state.value.caregiver

        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    dailyStatusAlertText = "Nudge reminder sent to $patientName!",
                    lastUpdatedText = "Updated just now"
                )
            }

            nudgePatientUseCase(
                patientId = patientId,
                caregiverId = caregiver?.id ?: "",
                caregiverName = caregiver?.firstName ?: "Your Caregiver"
            )

            delay(3000L.milliseconds)
            _state.update { it.copy(dailyStatusAlertText = previousAlert) }
        }
    }

    private fun calculateWeeklyCompliance(
        pendingDoses: List<PendingDoseWithMedication>,
        now: Long
    ): Pair<List<ComplianceDayUiModel>, Int> {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val graceWindowMillis = 30 * 60 * 1000L

        val overdueCount = pendingDoses.count { (now - it.scheduledTime) > graceWindowMillis }
        val lateCount = pendingDoses.count { now >= it.scheduledTime && (now - it.scheduledTime) <= graceWindowMillis }

        val todayStatus = when {
            overdueCount > 0 -> ComplianceStatus.MISSED
            lateCount > 0 -> ComplianceStatus.LATE
            else -> ComplianceStatus.ON_TIME
        }

        val weeklyDays = dayNames.mapIndexed { index, dayName ->
            when {
                index < 4 -> ComplianceDayUiModel(dayName, ComplianceStatus.ON_TIME)
                index == 4 -> ComplianceDayUiModel(dayName, todayStatus)
                else -> ComplianceDayUiModel(dayName, ComplianceStatus.DEFAULT)
            }
        }

        val totalRecorded = 5
        val missedDays = if (todayStatus == ComplianceStatus.MISSED) 1 else 0
        val rate = (((totalRecorded - missedDays) * 100) / totalRecorded)

        return weeklyDays to rate
    }

    private fun generateLiveActivities(
        patientName: String,
        doses: List<PendingDoseWithMedication>,
        now: Long
    ): List<RecentActivityUiModel> {
        val graceWindowMillis = 30 * 60 * 1000L

        return doses.take(3).map { dose ->
            val isOverdue = (now - dose.scheduledTime) > graceWindowMillis
            val isDueNow = now >= dose.scheduledTime && !isOverdue

            val (status, action) = when {
                isOverdue -> ComplianceStatus.MISSED to "missed ${dose.name} ${dose.dosage}"
                isDueNow -> ComplianceStatus.LATE to "has ${dose.name} ${dose.dosage} due now"
                else -> ComplianceStatus.DEFAULT to "scheduled for ${dose.name} ${dose.dosage}"
            }

            RecentActivityUiModel(
                id = dose.id,
                patientName = patientName,
                actionDescription = action,
                timestampText = "at ${formatTime(dose.scheduledTime)}",
                status = status
            )
        }
    }

    fun onAction(action: CaregiverDashboardAction) {
        when (action) {
            is CaregiverDashboardAction.SelectPatient -> selectPatient(action.patientId)
            is CaregiverDashboardAction.CallPatient -> callPatient(action.patientId)
            is CaregiverDashboardAction.NudgePatient -> nudgePatient(action.patientId)
        }
    }
}