package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.NudgePatientUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetDoseHistoryForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.RecentActivityUiModel
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.pill.PillColorMapper
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.isSameDay
import com.riramzy.pillfllow.utils.platform.openPhoneDialer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CaregiverDashboardViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val getDoseHistoryForUserUseCase: GetDoseHistoryForUserUseCase,
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
                    combine(
                        getPendingDosesForUserUseCase(patientId),
                        getDoseHistoryForUserUseCase(patientId)
                    ) { pendingDoses, historyDoses ->
                        Pair(pendingDoses, historyDoses)
                    }
                }
                .collectLatest { (pendingDoses, historyDoses) ->
                    val now = currentTimeMillis()
                    val patientName = _state.value.activePatient?.name ?: "Patient"

                    val todayPendingDoses = pendingDoses.filter { isSameDay(it.scheduledTime, now) }
                    val earliestDose = todayPendingDoses.minByOrNull { it.scheduledTime }
                    val (dailyStatus, alertText) = DoseComplianceEvaluator.evaluateDailyStatus(earliestDose, now)
                    val (weeklyDays, weeklyRate) = DoseComplianceEvaluator.evaluateWeeklyCompliance(todayPendingDoses, now)

                    val mappedUiDoses = todayPendingDoses.map { dose ->
                        val evalDose = DoseComplianceEvaluator.evaluateDoseCard(dose.scheduledTime, now, isTaken = false)
                        val pillColor = PillColorMapper.fromRaw(dose.colorHex)

                        ScheduledDoseUiModel(
                            id = dose.id,
                            name = dose.name,
                            dosage = dose.dosage,
                            timeFormatted = formatTime(dose.scheduledTime),
                            color = pillColor,
                            status = evalDose.status,
                            badgeText = evalDose.badgeText,
                            scheduledTime = dose.scheduledTime
                        )
                    }

                    val todayTakenDoses = historyDoses.filter {
                        it.isTaken && isSameDay(it.takenTime ?: it.scheduledTime, now)
                    }

                    val takenActivities = todayTakenDoses.map { dose ->
                        val takenTime = dose.takenTime ?: dose.scheduledTime
                        val status = when (dose.complianceStatus) {
                            "ON_TIME" -> ComplianceStatus.ON_TIME
                            "LATE" -> ComplianceStatus.LATE
                            else -> ComplianceStatus.ON_TIME
                        }

                        RecentActivityUiModel(
                            id = dose.id,
                            patientName = patientName,
                            actionDescription = "took ${dose.name} ${dose.dosage}",
                            timestampText = "at ${formatTime(takenTime)}",
                            status = status
                        ) to takenTime
                    }

                    val overduePendingDoses = todayPendingDoses.filter { dose ->
                        (now - dose.scheduledTime) > DoseStateMachine.ON_TIME_WINDOW_MILLIS
                    }

                    val alertActivities = overduePendingDoses.map { dose ->
                        val evalDose = DoseComplianceEvaluator.evaluateLiveActivity(
                            dose.name,
                            dose.dosage,
                            dose.scheduledTime, now
                        )

                        RecentActivityUiModel(
                            id = dose.id,
                            patientName = patientName,
                            actionDescription = evalDose.actionDescription,
                            timestampText = "at ${formatTime(dose.scheduledTime)}",
                            status = evalDose.status
                        ) to dose.scheduledTime
                    }

                    val activities = (takenActivities + alertActivities)
                        .sortedByDescending { it.second }
                        .map { it.first }
                        .take(4)

                    _state.update {
                        it.copy(
                            selectedPatientDoses = mappedUiDoses,
                            selectedPatientDailyStatus = dailyStatus,
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

    fun onAction(action: CaregiverDashboardAction) {
        when (action) {
            is CaregiverDashboardAction.SelectPatient -> selectPatient(action.patientId)
            is CaregiverDashboardAction.CallPatient -> callPatient(action.patientId)
            is CaregiverDashboardAction.NudgePatient -> nudgePatient(action.patientId)
        }
    }
}