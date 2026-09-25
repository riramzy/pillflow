package com.riramzy.pillfllow.ui.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetDoseHistoryForUserUseCase
import com.riramzy.pillfllow.ui.components.history.MonthDaysCompliance
import com.riramzy.pillfllow.ui.state.history.HistoryAction
import com.riramzy.pillfllow.ui.state.history.HistoryLogRecordUiModel
import com.riramzy.pillfllow.ui.state.history.HistoryState
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatMonthYear
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.getDayOfMonth
import com.riramzy.pillfllow.utils.platform.isSameMonthAndYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val getDoseHistoryForUserUseCase: GetDoseHistoryForUserUseCase
): ViewModel() {
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    private val _selectedPatientId = MutableStateFlow<String?>(null)

    init {
        observeUserAndRole()
        observeDoseHistory()
    }

    private fun observeUserAndRole() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().collectLatest { user ->
                if (user?.userType?.equals("CAREGIVER", ignoreCase = true) == true) {
                    getCaregiverPatientsUseCase(user.id).collectLatest { patientModels ->
                        val validIds = patientModels.map { it.id }

                        if (patientModels.isNotEmpty() && (_selectedPatientId.value == null || !validIds.contains(_selectedPatientId.value))) {
                            _selectedPatientId.value = patientModels.first().id
                        }

                        _state.update {
                            it.copy(
                                isCaregiver = true,
                                pairedPatients = patientModels,
                                selectedPatientId = _selectedPatientId.value ?: ""
                            )
                        }
                    }
                } else if (user != null) {
                    _selectedPatientId.value = user.id
                    _state.update { it.copy(selectedPatientId = user.id, isCaregiver = false) }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDoseHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedPatientId.filterNotNull().flatMapLatest { patientId ->
                getDoseHistoryForUserUseCase(patientId)
            }.collectLatest { historyDoses ->
                val now = currentTimeMillis()

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

                val currentMonthTitle = formatMonthYear(now)

                _state.update {
                    it.copy(
                        monthYearTitle = currentMonthTitle,
                        scorePercentage = score,
                        onTimeCount = onTime,
                        lateCount = late,
                        missedCount = missed,
                        monthlyComplianceDays = heatmapDays,
                        logRecords = logRecords,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectPatient(patientId: String) {
        _selectedPatientId.value = patientId
        _state.update { it.copy(selectedPatientId = patientId) }
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.SelectPatient -> selectPatient(action.patientId)
        }
    }
}