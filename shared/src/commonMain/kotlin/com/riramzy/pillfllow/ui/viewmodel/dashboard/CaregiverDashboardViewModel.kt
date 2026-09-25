package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.NudgePatientUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.RecentActivityUiModel
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
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

                    val patientName = _state.value.activePatient?.name ?: "Patient"


                    val todayDoses = pendingDoses.filter { isSameDay(it.scheduledTime, now) }
                    val earliestDose = todayDoses.minByOrNull { it.scheduledTime }
                    val (dailyStatus, alertText) = DoseComplianceEvaluator.evaluateDailyStatus(earliestDose, now)

                    val (weeklyDays, weeklyRate) = DoseComplianceEvaluator.evaluateWeeklyCompliance(todayDoses, now)

                    val mappedUiDoses = todayDoses.map { dose ->
                        val evalDose = DoseComplianceEvaluator.evaluateDoseCard(
                            dose.scheduledTime,
                            now,
                            isTaken = false
                        )

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

                    val activities = todayDoses.take(3).map { dose ->
                        val evalDose = DoseComplianceEvaluator.evaluateLiveActivity(
                            dose.name,
                            dose.dosage,
                            dose.scheduledTime,
                            now
                        )

                        RecentActivityUiModel(
                            id = dose.id,
                            patientName = patientName,
                            actionDescription = evalDose.actionDescription,
                            timestampText = "at ${formatTime(dose.scheduledTime)}",
                            status = evalDose.status
                        )
                    }

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