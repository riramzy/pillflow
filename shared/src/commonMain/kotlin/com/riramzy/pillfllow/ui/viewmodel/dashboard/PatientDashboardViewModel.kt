package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.data.remote.dto.NudgeDto
import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.LogDoseTakenUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GetPhysicsSensitivityUseCase
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceCardUiModel
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.formatTime
import com.riramzy.pillfllow.utils.getDayOfMonth
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.medication.parseColorHex
import com.riramzy.pillfllow.utils.pill.PillColor
import com.riramzy.pillfllow.utils.pill.PillShape
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
                        val stagingWindowMillis = 30 * 60 * 1000L
                        val graceWindowMillis = DoseStateMachine.GRACE_WINDOW_MILLIS

                        val activeDishDoses = pendingDoses.filter { dose ->
                            val windowStart = dose.scheduledTime - stagingWindowMillis
                            val windowEnd = dose.scheduledTime + graceWindowMillis
                            now in windowStart..windowEnd
                        }

                        val mappedPills = activeDishDoses.mapIndexed { index, dose ->
                            val resolvedPillColor = PillColor.entries.firstOrNull {
                                it.name.equals(dose.colorHex, ignoreCase = true) ||
                                        it.label.equals(dose.colorHex, ignoreCase = true)
                            }

                            val color = resolvedPillColor?.color ?: runCatching {
                                Color(parseColorHex(dose.colorHex))
                            }.getOrDefault(Color(0xFFE53935))

                            val shape = runCatching {
                                PillShape.valueOf(dose.shape.uppercase())
                            }.getOrDefault(PillShape.CIRCLE)

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
                            val isOverdue = (now - dose.scheduledTime) > graceWindowMillis
                            val isDueNow = now >= dose.scheduledTime && !isOverdue

                            val pillColor = PillColor.entries.firstOrNull {
                                it.name.equals(dose.colorHex, ignoreCase = true) ||
                                        it.label.equals(dose.colorHex, ignoreCase = true)
                            } ?: PillColor.CORAL_RED

                            val isTomorrow = getDayOfMonth(dose.scheduledTime) != getDayOfMonth(now)

                            val (cardStatus, badgeText) = when {
                                isOverdue -> ComplianceStatus.MISSED to "Grace Expired"
                                isDueNow -> ComplianceStatus.LATE to "Due Now"
                                isTomorrow -> ComplianceStatus.DEFAULT to "Tomorrow"
                                else -> ComplianceStatus.DEFAULT to "Upcoming"
                            }

                            ScheduledDoseUiModel(
                                id = dose.id,
                                name = dose.name,
                                dosage = dose.dosage,
                                timeFormatted = formatTime(dose.scheduledTime),
                                color = pillColor,
                                status = cardStatus,
                                badgeText = badgeText,
                                scheduledTime = dose.scheduledTime
                            )
                        }

                        val complianceInfo = computeComplianceInfo(pendingDoses)

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

    private fun computeComplianceInfo(pendingDoses: List<PendingDoseWithMedication>): ComplianceCardUiModel {
        val now = currentTimeMillis()
        val earliestDose = pendingDoses.minByOrNull { it.scheduledTime }
        val graceWindowMillis = 30 * 60 * 1000L

        return when {
            earliestDose == null -> ComplianceCardUiModel(
                status = ComplianceStatus.ON_TIME,
                title = "All Set For Today!",
                subtitle = "All scheduled doses completed",
                badgeText = "100% On-Time"
            )

            (now - earliestDose.scheduledTime) > graceWindowMillis -> ComplianceCardUiModel(
                status = ComplianceStatus.MISSED,
                title = "Overdue: ${earliestDose.name}",
                subtitle = "Was due earlier today",
                badgeText = "Grace Expired"
            )

            now >= earliestDose.scheduledTime -> {
                val specificDosesLeft = pendingDoses.count { it.name.equals(earliestDose.name, ignoreCase = true) }

                ComplianceCardUiModel(
                    status = ComplianceStatus.LATE,
                    title = "Due Now: ${earliestDose.name} ${earliestDose.dosage}",
                    subtitle = "Scheduled for today",
                    badgeText = "$specificDosesLeft ${if (specificDosesLeft == 1) "Dose" else "Doses"} Left"
                )
            }

            else -> {
                val isTomorrow = getDayOfMonth(earliestDose.scheduledTime) != getDayOfMonth(now)
                val subtitleText = if (isTomorrow) "Scheduled for tomorrow" else "Scheduled for today"
                val specificDosesLeft = pendingDoses.count { it.name.equals(earliestDose.name, ignoreCase = true) }
                val badgeText = "$specificDosesLeft ${if (specificDosesLeft == 1) "Dose" else "Doses"} Left"

                ComplianceCardUiModel(
                    status = ComplianceStatus.LATE,
                    title = "Next: ${earliestDose.name} ${earliestDose.dosage}",
                    subtitle = subtitleText,
                    badgeText = badgeText
                )
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