package com.riramzy.pillfllow.ui.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.ui.components.history.MonthDaysCompliance
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.history.HistoryLogRecordUiModel
import com.riramzy.pillfllow.ui.state.history.HistoryState
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.formatTime
import com.riramzy.pillfllow.utils.getDayOfMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.avatar3
import pillfllow.shared.generated.resources.avatar4

class HistoryViewModel(
    private val medicationRepo: MedicationRepo,
    private val authRepo: AuthRepo,
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo
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
            authRepo.currentUser.collectLatest { user ->
                if (user?.userType?.equals("CAREGIVER", ignoreCase = true) == true) {
                    user.let { caregiver ->
                        pairingRepo.getPairingsForCaregiver(caregiver.id).collectLatest { pairings ->
                            val now = currentTimeMillis()
                            val graceWindowMillis = 30 * 60 * 1000L

                            val patientModels = pairings.mapNotNull { pairing ->
                                val patient = userRepo.getUserByIdOnce(pairing.patientId)

                                patient?.let { currentPatient ->
                                    val patientDoses = medicationRepo.getPendingDosesForUser(currentPatient.id).firstOrNull() ?: emptyList()

                                    val missedCount = patientDoses.count { now - it.scheduledTime  > graceWindowMillis }
                                    val lateCount = patientDoses.count { now >= it.scheduledTime && (now - it.scheduledTime) <= graceWindowMillis }

                                    val patientStatus = when {
                                        missedCount > 0 -> ComplianceStatus.MISSED
                                        lateCount > 0 -> ComplianceStatus.LATE
                                        else -> ComplianceStatus.ON_TIME
                                    }

                                    val patientScore = if (patientDoses.isEmpty()) {
                                        100
                                    } else {
                                        (100 - ((missedCount * 100) / patientDoses.size))
                                    }

                                    val avatarRes = when(currentPatient.avatarRes) {
                                        "avatar1" -> Res.drawable.avatar1
                                        "avatar2" -> Res.drawable.avatar2
                                        "avatar3" -> Res.drawable.avatar3
                                        else -> Res.drawable.avatar4
                                    }

                                    PairedPatientUiModel(
                                        id = currentPatient.id,
                                        name = currentPatient.firstName,
                                        relation = pairing.relation,
                                        phoneNumber = pairing.phoneNumber,
                                        avatar = avatarRes,
                                        status = patientStatus,
                                        lateDosesCount = lateCount,
                                        missedDosesCount = missedCount,
                                        compliancePercentage = patientScore
                                    )
                                }
                            }

                            if (_selectedPatientId.value == null && patientModels.isNotEmpty()) {
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
                medicationRepo.getDoseHistoryForUser(patientId)
            }.collectLatest { historyDoses ->
                val now = currentTimeMillis()

                var onTime = 0
                var late = 0
                var missed = 0

                historyDoses.forEach { record ->
                    when {
                        record.isTaken && record.complianceStatus == "ON_TIME" -> onTime++
                        record.isTaken && record.complianceStatus == "LATE" -> late++
                        !record.isTaken && now > record.scheduledTime + (30 * 60 * 1000L) -> missed++
                        record.complianceStatus == "MISSED" -> missed++
                    }
                }

                val total = onTime + late + missed
                val score = if (total > 0) ((onTime * 100) / total) else 100

                val graceWindowMillis = 30 * 60 * 1000L

                val heatmapDays = (1..31).map { day ->
                    val dayDoses = historyDoses.filter { getDayOfMonth(it.scheduledTime) == day }

                    val dayStatus = when {
                        dayDoses.isEmpty() -> ComplianceStatus.DEFAULT
                        dayDoses.any {
                            !it.isTaken && now > (it.scheduledTime + graceWindowMillis) ||
                                    it.complianceStatus == "MISSED"
                        } -> ComplianceStatus.MISSED
                        dayDoses.any { it.complianceStatus == "LATE" } -> ComplianceStatus.LATE
                        dayDoses.all { it.isTaken && it.complianceStatus == "ON_TIME" } -> ComplianceStatus.ON_TIME
                        else -> ComplianceStatus.DEFAULT
                    }

                    MonthDaysCompliance(dayNumber = day.toString(), status = dayStatus)
                }

                val logRecords = historyDoses.map { record ->
                    val recordStatus = when {
                        record.isTaken && record.complianceStatus == "ON_TIME" -> ComplianceStatus.ON_TIME
                        record.isTaken && record.complianceStatus == "LATE" -> ComplianceStatus.LATE
                        !record.isTaken && now > (record.scheduledTime + graceWindowMillis) -> ComplianceStatus.MISSED
                        else -> ComplianceStatus.DEFAULT
                    }

                    val recordTimestampText = if (record.isTaken && record.takenTime != null) {
                        "Logged ${formatTime(record.takenTime)}"
                    } else {
                        "Dose Missed"
                    }

                    HistoryLogRecordUiModel(
                        id = record.id.toString(),
                        patientName = "",
                        actionTitle = "${record.name} ${record.dosage}",
                        actionDescription = "Scheduled ${formatTime(record.scheduledTime)}",
                        timestampText = recordTimestampText,
                        status = recordStatus,
                    )
                }

                _state.update {
                    it.copy(
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
}