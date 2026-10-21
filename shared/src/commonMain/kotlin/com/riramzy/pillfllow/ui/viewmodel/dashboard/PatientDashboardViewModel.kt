package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.domain.repo.NotificationsRepo
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetDoseHistoryForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetMedicationsForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.LogDoseTakenUseCase
import com.riramzy.pillfllow.domain.usecase.medication.LogPrnDoseUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SkipDoseUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SyncAndMaintainScheduleUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GetPhysicsSensitivityUseCase
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.PrnMedicationUiModel
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.ui.state.notifications.NotificationType
import com.riramzy.pillfllow.ui.state.notifications.NotificationsAction
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.pill.PillColorMapper
import com.riramzy.pillfllow.utils.pill.PillShape
import com.riramzy.pillfllow.utils.pill.PillShapeMapper
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.isSameDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PatientDashboardViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val logDoseTakenUseCase: LogDoseTakenUseCase,
    private val getPhysicsSensitivityUseCase: GetPhysicsSensitivityUseCase,
    private val getDoseHistoryForUserUseCase: GetDoseHistoryForUserUseCase,
    private val syncAndMaintainScheduleUseCase: SyncAndMaintainScheduleUseCase,
    private val notificationsRepo: NotificationsRepo,
    private val getMedicationsForUserUseCase: GetMedicationsForUserUseCase,
    private val skipDoseUseCase: SkipDoseUseCase,
    private val logPrnDoseUseCase: LogPrnDoseUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(PatientDashboardState())
    val state: StateFlow<PatientDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
        observePhysicsSensitivity()
        observeNudges()
    }

    private fun observePhysicsSensitivity() {
        viewModelScope.launch(Dispatchers.IO) {
            getPhysicsSensitivityUseCase().collectLatest { sensitivity ->
                _state.update { it.copy(physicsSensitivity = sensitivity) }
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().collectLatest { currentUser ->
                _state.update { it.copy(user = currentUser) }

                currentUser?.let { user ->
                    combine(
                        getPendingDosesForUserUseCase(user.id),
                        getDoseHistoryForUserUseCase(user.id),
                        getMedicationsForUserUseCase(user.id),
                        tickerFlow()
                    ) { pendingDoses, historyDoses, medications, now ->
                        val todayPendingDoses = pendingDoses.filter { isSameDay(it.scheduledTime, now) }
                        val todayHistoryDoses = historyDoses.filter {
                            it.isTaken && isSameDay(it.scheduledTime, now)
                        }

                        val eligibleDishDoses = todayPendingDoses.filter { dose ->
                            DoseComplianceEvaluator.evaluateDoseCard(
                                dose.scheduledTime,
                                now,
                                isTaken = false
                            ).isDishEligible
                        }

                        val sortedDishDoses = eligibleDishDoses.sortedWith(
                            compareBy<PendingDoseWithMedication> { it.scheduledTime }
                                .thenBy { it.name }
                                .thenBy { it.id }
                        )

                        val currentDishDose = sortedDishDoses.firstOrNull()

                        val currentPillId = _state.value.pills.firstOrNull()?.id
                        val newPillId = currentDishDose?.id

                        val mappedPills = if (currentPillId == newPillId && currentDishDose != null) {
                            _state.value.pills
                        } else {
                            currentDishDose?.let { dose ->
                                val color = PillColorMapper.fromRaw(dose.colorHex).color
                                val shape = PillShapeMapper.fromRaw(dose.shape, default = PillShape.CIRCLE)
                                listOf(
                                    PillEntity(
                                        id = dose.id,
                                        name = dose.name,
                                        color = color,
                                        shape = shape,
                                        radius = 32f,
                                        position = Vector2D(x = 350f, y = 480f)
                                    )
                                )
                            } ?: emptyList()
                        }

                        val mappedUiDoses = todayPendingDoses.map { dose ->
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

                        val complianceCards = DoseComplianceEvaluator.evaluatePatientComplianceCards(
                            todayPendingDoses = todayPendingDoses,
                            todayHistoryDoses = todayHistoryDoses,
                            now = now
                        )

                        val remainingCount = mappedUiDoses.count { it.status != ComplianceStatus.MISSED }
                        val missedCount = mappedUiDoses.count { it.status == ComplianceStatus.MISSED }

                        val activePrnMeds = medications.filter { it.isPrn && !it.isPaused && !it.isArchived }

                        val prnUiModels = activePrnMeds.map { med ->
                            val lastTaken = historyDoses
                                .filter { it.medicationId == med.id && it.isTaken }
                                .maxByOrNull { it.takenTime ?: it.scheduledTime }

                            val lastTakenText = lastTaken?.let {
                                val t = it.takenTime ?: it.scheduledTime
                                "Last taken: ${formatTime(t)}"
                            }

                            PrnMedicationUiModel(
                                medicationId = med.id,
                                name = med.name,
                                dosage = med.dosage,
                                color = PillColorMapper.fromRaw(med.colorHex),
                                lastTakenText = lastTakenText
                            )
                        }

                        val skippedUiDoses = todayHistoryDoses
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

                        val allScheduledDoses = (mappedUiDoses + skippedUiDoses).sortedBy { it.scheduledTime }

                        _state.update {
                            it.copy(
                                hasPrescriptions = medications.isNotEmpty() || pendingDoses.isNotEmpty() || historyDoses.isNotEmpty(),
                                scheduledDoses = allScheduledDoses,
                                prnMedications = prnUiModels,
                                remainingDosesCount = remainingCount,
                                missedDosesCount = missedCount,
                                pills = mappedPills,
                                totalDoses = sortedDishDoses.size,
                                complianceCards = complianceCards,
                                isLoading = false
                            )
                        }
                    }.collect()
                }
            }
        }
    }

    fun logDose(doseId: String, scheduledTime: Long? = null) {
        val resolvedTime = scheduledTime
            ?: _state.value.scheduledDoses.firstOrNull { it.id == doseId }?.scheduledTime

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = logDoseTakenUseCase(doseId = doseId, scheduledTime = resolvedTime)) {
                is Result.Error -> {
                    _state.update {
                        it.copy(errorMessage = result.message ?: "Failed to record dose. Please try again.")
                    }
                }

                else -> Unit
            }
        }
    }

    fun logPrnDose(medicationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = logPrnDoseUseCase(medicationId)) {
                is Result.Error -> {
                    _state.update { it.copy(errorMessage = result.message ?: "Failed to log as-needed dose.") }
                }
                else -> Unit
            }
        }
    }

    fun onErrorDismissed() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun observeNudges() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationsRepo.state.collectLatest { notifState ->
                val nudges = notifState.alerts.filter { it.type == NotificationType.CAREGIVER_NUDGE }
                _state.update { it.copy(activeNudges = nudges) }
            }
        }
    }

    private fun dismissNudge(nudgeId: String) {
        notificationsRepo.onAction(NotificationsAction.DismissAlert(nudgeId))
    }

    fun skipDose(doseId: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = skipDoseUseCase(doseId, reason)) {
                is Result.Error -> {
                    _state.update { it.copy(errorMessage = result.message ?: "Failed to skip dose.") }
                }
                else -> Unit
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

    fun onAction(action: PatientDashboardAction) {
        when (action) {
            is PatientDashboardAction.LogDose -> logDose(action.doseId, action.scheduledTime)
            is PatientDashboardAction.LogPrnDose -> logPrnDose(action.medicationId)
            is PatientDashboardAction.SkipDose -> skipDose(action.doseId, action.reason)
            is PatientDashboardAction.DismissError -> onErrorDismissed()
            is PatientDashboardAction.DismissNudge -> dismissNudge(action.nudgeId)
            is PatientDashboardAction.Refresh -> refresh()
        }
    }

    private fun tickerFlow(periodMillis: Long = 30_000L): Flow<Long> = flow {
        while (true) {
            emit(currentTimeMillis())
            delay(periodMillis.milliseconds)
        }
    }
}