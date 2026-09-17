package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.LogDoseTakenUseCase
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceCardUiModel
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.PillColor
import com.riramzy.pillfllow.utils.PillShape
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.formatTime
import com.riramzy.pillfllow.utils.parseColorHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PatientDashboardViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val logDoseTakenUseCase: LogDoseTakenUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(PatientDashboardState())
    val state: StateFlow<PatientDashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().collectLatest { currentUser ->
                _state.update { it.copy(user = currentUser) }

                currentUser?.let { user ->
                    getPendingDosesForUserUseCase(user.id).collectLatest { pendingDoses ->
                        val now = currentTimeMillis()
                        val graceWindowMillis = 30 * 60 * 1000L

                        val mappedPills = pendingDoses.mapIndexed { index, dose ->
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

                            val (cardStatus, badgeText) = when {
                                isOverdue -> ComplianceStatus.MISSED to "Grace Expired"
                                isDueNow -> ComplianceStatus.LATE to "Due Now"
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
                                totalDoses = pendingDoses.size,
                                complianceStatus = complianceInfo.status,
                                complianceTitle = complianceInfo.title,
                                complianceSubtitle = complianceInfo.subtitle,
                                complianceBadgeText = complianceInfo.badgeText,
                                isLoading = false
                            )
                        }
                    }
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

            now >= earliestDose.scheduledTime -> ComplianceCardUiModel(
                status = ComplianceStatus.LATE,
                title = "Due Now: ${earliestDose.name} ${earliestDose.dosage}",
                subtitle = "Scheduled for today",
                badgeText = "${pendingDoses.size} Doses Left"
            )

            else -> ComplianceCardUiModel(
                status = ComplianceStatus.ON_TIME,
                title = "Next: ${earliestDose.name} ${earliestDose.dosage}",
                subtitle = "Scheduled for today",
                badgeText = "${pendingDoses.size} Doses Left"
            )
        }
    }

    fun logDose(doseId: String, scheduledTime: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            logDoseTakenUseCase(doseId = doseId, scheduledTime = scheduledTime)
        }
    }

    fun onAction(action: PatientDashboardAction) {
        when (action) {
            is PatientDashboardAction.LogDose -> logDose(action.doseId, action.scheduledTime)
        }
    }
}