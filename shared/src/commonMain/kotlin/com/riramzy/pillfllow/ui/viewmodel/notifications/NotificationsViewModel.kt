package com.riramzy.pillfllow.ui.viewmodel.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.remote.dto.NudgeDto
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.LogDoseTakenUseCase
import com.riramzy.pillfllow.ui.state.notifications.NotificationAlertUiModel
import com.riramzy.pillfllow.ui.state.notifications.NotificationType
import com.riramzy.pillfllow.ui.state.notifications.NotificationsAction
import com.riramzy.pillfllow.ui.state.notifications.NotificationsState
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatTime
import com.riramzy.pillfllow.utils.platform.isSameDay
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val logDoseTakenUseCase: LogDoseTakenUseCase,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _dismissedIds = MutableStateFlow<Set<String>>(emptySet())
    private val _state = MutableStateFlow(NotificationsState(isLoading = true))
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        observeAlerts()
    }

    private fun observeAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().collectLatest { user ->
                if (user == null) {
                    _state.value = NotificationsState()
                    return@collectLatest
                }

                if (user.userType == "CAREGIVER") {
                    observeCaregiverAlerts(user.id)
                } else {
                    observePatientAlerts(user.id)
                }
            }
        }
    }

    private suspend fun observePatientAlerts(patientId: String) {
        combine(
            getPendingDosesForUserUseCase(patientId),
            firestore.collection("nudges").where { "patientId" equalTo patientId }.snapshots,
            _dismissedIds
        ) { pendingDoses, nudgeSnapshots, dismissed ->
            val now = currentTimeMillis()
            val alerts = mutableListOf<NotificationAlertUiModel>()

            nudgeSnapshots.documents.forEach { doc ->
                val nudge = doc.data<NudgeDto>()

                if (!nudge.isHandled && !dismissed.contains(nudge.id)) {
                    alerts.add(
                        NotificationAlertUiModel(
                            id = nudge.id,
                            title = "Caregiver Reminder",
                            description = "${nudge.caregiverName} reminded you to take your medication!",
                            timestampText = formatTime(nudge.timestamp),
                            type = NotificationType.CAREGIVER_NUDGE
                        )
                    )
                }
            }

            val todayDoses = pendingDoses
                .filter { isSameDay(it.scheduledTime, now) }
                .sortedBy { it.scheduledTime }

            todayDoses.forEach { dose ->
                if (dismissed.contains(dose.id)) return@forEach
                val eval = DoseComplianceEvaluator.evaluateDoseCard(dose.scheduledTime, now, isTaken = false)

                when {
                    eval.status == ComplianceStatus.MISSED || eval.status == ComplianceStatus.LATE -> {
                        alerts.add(
                            NotificationAlertUiModel(
                                id = dose.id,
                                title = "Overdue: ${dose.name}",
                                description = "${dose.name} ${dose.dosage} was due at ${formatTime(dose.scheduledTime)}",
                                timestampText = formatTime(dose.scheduledTime),
                                type = NotificationType.OVERDUE_DOSE,
                                actionLabel = "Take Dose"
                            )
                        )
                    }

                    now < dose.scheduledTime -> {
                        alerts.add(
                            NotificationAlertUiModel(
                                id = dose.id,
                                title = "Upcoming: ${dose.name}",
                                description = "${dose.name} ${dose.dosage} scheduled for ${formatTime(dose.scheduledTime)}",
                                timestampText = formatTime(dose.scheduledTime),
                                type = NotificationType.UPCOMING_DOSE
                            )
                        )
                    }
                }
            }

            NotificationsState(alerts = alerts, isLoading = false)
        }.collectLatest { newState ->
            _state.value = newState
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeCaregiverAlerts(caregiverId: String) {
        getCaregiverPatientsUseCase(caregiverId).flatMapLatest { patients ->
            if (patients.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                combine(patients.map { patient ->
                    getPendingDosesForUserUseCase(patient.id).map { doses ->
                        patient to doses
                    }
                }) { patientDosePairs ->
                    val now = currentTimeMillis()
                    val alerts = mutableListOf<NotificationAlertUiModel>()

                    patientDosePairs.forEach { (patient, pendingDoses) ->
                        val todayDoses = pendingDoses.filter { isSameDay(it.scheduledTime, now) }

                        todayDoses.forEach { dose ->
                            val eval = DoseComplianceEvaluator.evaluateDoseCard(dose.scheduledTime, now, isTaken = false)

                            if (eval.status == ComplianceStatus.MISSED || eval.status == ComplianceStatus.LATE) {
                                alerts.add(
                                    NotificationAlertUiModel(
                                        id = dose.id,
                                        title = "Missed: ${patient.name}",
                                        description = "${patient.name} missed ${dose.name} ${dose.dosage} due at ${formatTime(dose.scheduledTime)}",
                                        timestampText = formatTime(dose.scheduledTime),
                                        type = NotificationType.ADHERENCE_ALERT
                                    )
                                )
                            }
                        }
                    }

                    alerts
                }
            }
        }.combine(_dismissedIds) { alerts, dismissed ->
            NotificationsState(
                alerts = alerts.filterNot { dismissed.contains(it.id) },
                isLoading = false
            )
        }.collectLatest { newState ->
            _state.value = newState
        }
    }

    fun onAction(action: NotificationsAction) {
        when (action) {
            is NotificationsAction.DismissAlert -> {
                _dismissedIds.update { it + action.id }

                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        firestore
                            .collection("nudges")
                            .document(action.id).delete()
                    } catch (_: Exception) { }
                }
            }

            is NotificationsAction.ClearAll -> {
                val currentIds = _state.value.alerts.map { it.id }.toSet()
                _dismissedIds.update { it + currentIds }

                viewModelScope.launch(Dispatchers.IO) {
                    currentIds.forEach { id ->
                        try {
                            firestore.collection("nudges").document(id).delete()
                        } catch (_: Exception) { }
                    }
                }
            }
        }
    }

    fun takeDose(doseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            logDoseTakenUseCase(doseId = doseId)
            _dismissedIds.update { it + doseId }
        }
    }
}