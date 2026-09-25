package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.remote.dto.NudgeDto
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.LogDoseTakenUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GetPhysicsSensitivityUseCase
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.utils.pill.PillColorMapper
import com.riramzy.pillfllow.utils.pill.PillShape
import com.riramzy.pillfllow.utils.pill.PillShapeMapper
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatTime
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PatientDashboardViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val logDoseTakenUseCase: LogDoseTakenUseCase,
    private val getPhysicsSensitivityUseCase: GetPhysicsSensitivityUseCase,
    private val platformNotifier: PlatformNotifier = PlatformNotifier(),
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _state = MutableStateFlow(PatientDashboardState())
    val state: StateFlow<PatientDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
        observeNudges()
        observePhysicsSensitivity()
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
                        tickerFlow()
                    ) { pendingDoses, now ->
                        val activeDishDoses = pendingDoses.filter { dose ->
                            DoseComplianceEvaluator.evaluateDoseCard(
                                dose.scheduledTime,
                                now,
                                isTaken = false
                            ).isDishEligible
                        }

                        val mappedPills = activeDishDoses.mapIndexed { index, dose ->
                            DoseComplianceEvaluator.evaluateDoseCard(
                                dose.scheduledTime,
                                now,
                                isTaken = false
                            ).isDishEligible

                            val color = PillColorMapper.fromRaw(dose.colorHex).color

                            val shape = PillShapeMapper.fromRaw(dose.shape, default = PillShape.CIRCLE)

                            PillEntity(
                                id = dose.id,
                                name = dose.name,
                                color = color,
                                shape = shape,
                                radius = 32f,
                                position = Vector2D(
                                    x = 350f + (index * 40f) % 120f,
                                    y = 480f + (index * 30f) % 100f
                                )
                            )
                        }

                        val mappedUiDoses = pendingDoses.map { dose ->
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

                        val complianceInfo = DoseComplianceEvaluator.evaluatePatientComplianceCard(
                            pendingDoses,
                            now
                        )

                        _state.update {
                            it.copy(
                                scheduledDoses = mappedUiDoses,
                                pills = mappedPills,
                                totalDoses = activeDishDoses.size,
                                complianceStatus = complianceInfo.status,
                                complianceTitle = complianceInfo.title,
                                complianceSubtitle = complianceInfo.subtitle,
                                complianceBadgeText = complianceInfo.badgeText,
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
            logDoseTakenUseCase(doseId = doseId, scheduledTime = resolvedTime)
        }
    }

    fun onAction(action: PatientDashboardAction) {
        when (action) {
            is PatientDashboardAction.LogDose -> logDose(action.doseId, action.scheduledTime)
        }
    }

    private fun tickerFlow(periodMillis: Long = 30_000L): Flow<Long> = flow {
        while (true) {
            emit(currentTimeMillis())
            delay(periodMillis.milliseconds)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeNudges() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase()
                .flatMapLatest { user ->
                    if (user == null) {
                        emptyFlow()
                    } else {
                        firestore.collection("nudges")
                            .where { "patientId" equalTo user.id }
                            .where { "isHandled" equalTo false }
                            .snapshots
                    }
                }
                .collect { snapshot ->
                    snapshot.documents.forEach { doc ->
                        val nudge = doc.data<NudgeDto>()
                        platformNotifier.sendInstantNudge(
                            title = "Caregiver Reminder",
                            message = "${nudge.caregiverName} wants to remind you to take your medication!"
                        )
                        firestore.collection("nudges").document(nudge.id).delete()
                    }
                }
        }
    }
}