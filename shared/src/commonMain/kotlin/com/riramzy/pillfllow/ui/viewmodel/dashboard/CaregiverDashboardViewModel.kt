package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetPendingDosesForPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.NudgePatientUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetDoseHistoryForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SyncAndMaintainScheduleUseCase
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
    private val sessionManager: SessionManager
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
                        patientNameFlow
                    ) { pendingDoses, historyDoses, resolvedName ->
                        data class CombinedDoseData(
                            val patientId: String,
                            val pendingDoses: List<PendingDoseWithMedication>,
                            val historyDoses: List<DoseHistoryEntity>,
                            val patientName: String
                        )

                        CombinedDoseData(
                            patientId = patientId,
                            pendingDoses = pendingDoses,
                            historyDoses = historyDoses,
                            patientName = resolvedName ?: "Patient"
                        )
                    }
                }
                .collectLatest { data ->
                    val (patientId, pendingDoses, historyDoses, patientName) = data
                    val now = currentTimeMillis()

                    val todayPendingDoses = pendingDoses.filter { isSameDay(it.scheduledTime, now) }
                    val todayTakenDoses = historyDoses.filter {
                        it.isTaken && isSameDay(it.takenTime ?: it.scheduledTime, now)
                    }

                    val (dailyStatus, alertText) = DoseComplianceEvaluator.evaluateDailyStatus(
                        todayPendingDoses = todayPendingDoses,
                        todayHistoryDoses = todayTakenDoses,
                        now = now
                    )

                    val (weeklyDays, weeklyRate) = DoseComplianceEvaluator.evaluateWeeklyCompliance(
                        historyDoses = historyDoses,
                        pendingDoses = pendingDoses,
                        now = now
                    )

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

                    val skippedUiDoses = todayTakenDoses
                        .filter { it.complianceStatus == "SKIPPED" }
                        .map { dose ->
                            ScheduledDoseUiModel(
                                id = dose.id,
                                name = dose.name,
                                dosage = dose.dosage,
                                timeFormatted = formatTime(dose.scheduledTime),
                                color = PillColorMapper.fromRaw(dose.colorHex),
                                status = ComplianceStatus.SKIPPED,
                                badgeText = if (!dose.skipReason.isNullOrBlank()) "Skipped: ${dose.skipReason}" else "Skipped",
                                scheduledTime = dose.scheduledTime
                            )
                        }

                    val allUiDoses = (mappedUiDoses + skippedUiDoses).sortedBy { it.scheduledTime }

                    val takenActivities = todayTakenDoses.map { dose ->
                        val takenTime = dose.takenTime ?: dose.scheduledTime

                        val (status, action) = when {
                            dose.complianceStatus == "SKIPPED" -> {
                                val reason = dose.skipReason?.ifBlank { null }
                                ComplianceStatus.SKIPPED to "skipped ${dose.name} ${dose.dosage}${if (reason != null) " ($reason)" else ""}"
                            }
                            dose.isPrn -> {
                                ComplianceStatus.ON_TIME to "took ${dose.name} ${dose.dosage} (As Needed)"
                            }
                            dose.complianceStatus == "LATE" -> ComplianceStatus.LATE to "took ${dose.name} ${dose.dosage}"
                            else -> ComplianceStatus.ON_TIME to "took ${dose.name} ${dose.dosage}"
                        }

                        RecentActivityUiModel(
                            id = dose.id,
                            patientName = patientName,
                            actionDescription = action,
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

                    _state.update { current ->
                        val updatedPatients = current.patients.map { patient ->
                            if (patient.id == patientId) {
                                val missed = todayPendingDoses.count { d ->
                                    (now - d.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS
                                }

                                val pendingLate = todayPendingDoses.count { d ->
                                    val diff = now - d.scheduledTime
                                    diff in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
                                }
                                val historyLate = todayTakenDoses.count { it.complianceStatus == "LATE" }
                                val late = pendingLate + historyLate

                                patient.copy(
                                    status = dailyStatus,
                                    lateDosesCount = late,
                                    missedDosesCount = missed,
                                    compliancePercentage = weeklyRate
                                )
                            } else patient
                        }

                        current.copy(
                            patients = updatedPatients,
                            selectedPatientId = patientId,
                            selectedPatientDoses = allUiDoses,
                            selectedPatientDailyStatus = if (nudgeJob?.isActive == true) current.selectedPatientDailyStatus else dailyStatus,
                            dailyStatusAlertText = if (nudgeJob?.isActive == true) current.dailyStatusAlertText else alertText,
                            weeklyCompliance = weeklyDays,
                            recentActivities = activities,
                            weeklyRatePercentage = weeklyRate,
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