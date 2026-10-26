package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.medication.PrescriptionEvaluator
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetPendingDosesForPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.NudgePatientUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetDoseHistoryForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetMedicationsForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SyncAndMaintainScheduleUseCase
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.CombinedDoseUiData
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.openPhoneDialer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CaregiverDashboardViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val getDoseHistoryForUserUseCase: GetDoseHistoryForUserUseCase,
    private val nudgePatientUseCase: NudgePatientUseCase,
    private val syncAndMaintainScheduleUseCase: SyncAndMaintainScheduleUseCase,
    private val getPendingDosesForPatientsUseCase: GetPendingDosesForPatientsUseCase,
    private val sessionManager: SessionManager,
    private val getMedicationsForUserUseCase: GetMedicationsForUserUseCase
): ViewModel() {
    private val _state = MutableStateFlow(CaregiverDashboardState())
    val state: StateFlow<CaregiverDashboardState> = _state.asStateFlow()

    val selectedPatientId: StateFlow<String?> = sessionManager.selectedPatientId
    private var nudgeJob: Job? = null

    init {
        loadDashboardData()
        observeSelectedPatientDoses()
        observeCaregiverEscalations()
    }

    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().collectLatest { caregiver ->
                _state.update { it.copy(caregiver = caregiver) }
                caregiver?.let { user ->
                    getCaregiverPatientsUseCase(user.id).collectLatest { patientModels ->
                        val validIds = patientModels.map { it.id }
                        if (patientModels.isNotEmpty() && (selectedPatientId.value == null || !validIds.contains(selectedPatientId.value))) {
                            sessionManager.setSelectedPatientId(patientModels.first().id)
                        }

                        _state.update {
                            it.copy(
                                patients = patientModels,
                                selectedPatientId = selectedPatientId.value ?: "",
                                isLoading = patientModels.isNotEmpty() && it.isLoading
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
            selectedPatientId
                .filterNotNull()
                .flatMapLatest { patientId ->
                    val patientNameFlow = _state
                        .map { state -> state.patients.find { it.id == patientId }?.name }
                        .distinctUntilChanged()

                    combine(
                        getPendingDosesForUserUseCase(patientId),
                        getDoseHistoryForUserUseCase(patientId),
                        getMedicationsForUserUseCase(patientId),
                        patientNameFlow
                    ) { pendingDoses, historyDoses, medications, resolvedName ->
                        CombinedDoseUiData(
                            patientId = patientId,
                            pendingDoses = pendingDoses,
                            historyDoses = historyDoses,
                            medications = medications,
                            patientName = resolvedName ?: "Patient"
                        )
                    }
                }.collectLatest { data ->
                    val (patientId, pendingDoses, historyDoses, medications, patientName) = data
                    val now = currentTimeMillis()
                    val today = DoseComplianceEvaluator.filterTodayDoses(pendingDoses, historyDoses, now)
                    val (dailyStatus, alertText) = DoseComplianceEvaluator.evaluateDailyStatus(
                        todayPendingDoses = today.todayPendingDoses,
                        todayHistoryDoses = today.todayScheduledHistoryDoses,
                        now = now
                    )

                    val (weeklyDays, weeklyRate) = DoseComplianceEvaluator.evaluateWeeklyCompliance(
                        historyDoses = historyDoses,
                        pendingDoses = pendingDoses,
                        now = now
                    )

                    val timeline = DoseComplianceEvaluator.evaluateScheduledTimeline(
                        todayPendingDoses = today.todayPendingDoses,
                        todayHistoryDoses = today.todayScheduledHistoryDoses,
                        now = now
                    )

                    val activities = DoseComplianceEvaluator.evaluateRecentActivities(
                        todayPendingDoses = today.todayPendingDoses,
                        todayTakenDoses = today.todayTakenHistoryDoses,
                        patientName = patientName,
                        now = now
                    )

                    val summary = DoseComplianceEvaluator.evaluatePatientSummary(
                        todayPendingDoses = today.todayPendingDoses,
                        todayHistoryDoses = today.todayScheduledHistoryDoses,
                        now = now
                    )

                    val cabinet = PrescriptionEvaluator.evaluateCabinet(medications)

                    _state.update { current ->
                        val updatedPatients = current.patients.map { patient ->
                            if (patient.id == patientId) {
                                patient.copy(
                                    status = summary.status,
                                    lateDosesCount = summary.lateDosesCount,
                                    missedDosesCount = summary.missedDosesCount,
                                    compliancePercentage = summary.compliancePercentage
                                )
                            } else patient
                        }

                        current.copy(
                            patients = updatedPatients,
                            selectedPatientId = patientId,
                            selectedPatientDoses = timeline.scheduledDoses,
                            selectedPatientDailyStatus = if (nudgeJob?.isActive == true) current.selectedPatientDailyStatus else dailyStatus,
                            dailyStatusAlertText = if (nudgeJob?.isActive == true) current.dailyStatusAlertText else alertText,
                            weeklyCompliance = weeklyDays,
                            weeklyRatePercentage = weeklyRate,
                            recentActivities = activities,
                            trackedMedicationsCount = cabinet.trackedCount,
                            minDaysOfSupply = cabinet.minDaysOfSupply,
                            lowSupplyMedications = cabinet.lowSupplyMedications,
                            lastUpdatedText = "Updated at ${formatTime(now)}",
                            isLoading = false
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCaregiverEscalations() {
        _state
            .map { state -> state.patients.map { it.id }.sorted() }
            .distinctUntilChanged()
            .flatMapLatest { patientIds ->
                if (patientIds.isEmpty()) emptyFlow()
                else getPendingDosesForPatientsUseCase(patientIds)
            }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    fun selectPatient(patientId: String) {
        sessionManager.setSelectedPatientId(patientId)
    }

    fun callPatient(patientId: String) {
        val phone = _state.value.patients.firstOrNull { it.id == patientId }?.phoneNumber

        if (!phone.isNullOrBlank()) {
            openPhoneDialer(phone)
        }
    }

    fun nudgePatient(patientId: String) {
        if (nudgeJob?.isActive == true) return

        val patientName = _state.value.patients.firstOrNull { it.id == patientId }?.name ?: "Patient"
        val previousAlert = _state.value.dailyStatusAlertText
        val previousStatus = _state.value.selectedPatientDailyStatus

        val caregiver = _state.value.caregiver

        nudgeJob = viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    selectedPatientDailyStatus = ComplianceStatus.DEFAULT,
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

            _state.update {
                it.copy(
                    selectedPatientDailyStatus = previousStatus,
                    dailyStatusAlertText = previousAlert
                )
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isRefreshing = true) }
            try {
                syncAndMaintainScheduleUseCase()
            } finally {
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onAction(action: CaregiverDashboardAction) {
        when (action) {
            is CaregiverDashboardAction.SelectPatient -> selectPatient(action.patientId)
            is CaregiverDashboardAction.CallPatient -> callPatient(action.patientId)
            is CaregiverDashboardAction.NudgePatient -> nudgePatient(action.patientId)
            is CaregiverDashboardAction.Refresh -> refresh()
        }
    }
}