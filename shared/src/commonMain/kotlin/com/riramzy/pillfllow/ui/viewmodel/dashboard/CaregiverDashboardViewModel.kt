package com.riramzy.pillfllow.ui.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.ComplianceDayUiModel
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.dashboard.RecentActivityUiModel
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.PillColor
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.formatTime
import com.riramzy.pillfllow.utils.openPhoneDialer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
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
import kotlin.time.Duration.Companion.milliseconds

class CaregiverDashboardViewModel(
    private val pairingRepo: PairingRepo,
    private val medicationRepo: MedicationRepo,
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val platformNotifier: PlatformNotifier = PlatformNotifier()
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
            authRepo.currentUser.collectLatest { caregiver ->
                _state.update { it.copy(caregiver = caregiver) }

                caregiver?.let { user ->
                    pairingRepo.getPairingsForCaregiver(user.id).collectLatest { pairings ->
                        val now = currentTimeMillis()
                        val graceWindowMillis = 30 * 60 * 1000L

                        val patientModels = pairings.mapNotNull { pairing ->
                            val patient = userRepo.getUserByIdOnce(pairing.patientId)

                            patient?.let { user ->
                                val patientDoses = medicationRepo.getPendingDosesForUser(user.id).firstOrNull() ?: emptyList()

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

                                val avatarRes = when(user.avatarRes) {
                                    "avatar1" -> Res.drawable.avatar1
                                    "avatar2" -> Res.drawable.avatar2
                                    "avatar3" -> Res.drawable.avatar3
                                    else -> Res.drawable.avatar4
                                }

                                PairedPatientUiModel(
                                    id = user.id,
                                    name = user.firstName,
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

                        if (_selectedPatientId.value == null &&
                            patientModels.isNotEmpty() ) {
                            _selectedPatientId.value = patientModels.first().id
                        }

                        _state.update {
                            it.copy(
                                patients = patientModels,
                                selectedPatientId = _selectedPatientId.value ?: ""
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
                    medicationRepo.getPendingDosesForUser(patientId)
                }
                .collectLatest { pendingDoses ->
                    val now = currentTimeMillis()
                    val graceWindowMillis = 30 * 60 * 1000L

                    val mappedUiDoses = pendingDoses.map { dose ->
                        val isOverdue = (now - dose.scheduledTime) > graceWindowMillis
                        val isDueNow = now >= dose.scheduledTime && !isOverdue

                        val pillColor = PillColor.entries.firstOrNull {
                            it.name.equals(dose.colorHex, ignoreCase = true)
                        }

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
                            color = pillColor ?: PillColor.CORAL_RED,
                            status = cardStatus,
                            badgeText = badgeText,
                            scheduledTime = dose.scheduledTime
                        )
                    }

                    val earliestDose = pendingDoses.minByOrNull { it.scheduledTime }

                    val (status, alertText) = when {
                        earliestDose == null -> {
                            ComplianceStatus.ON_TIME to "All Set For Today!"
                        }

                        (now - earliestDose.scheduledTime) > graceWindowMillis -> {
                            ComplianceStatus.MISSED to "ALERT: ${earliestDose.name} ${earliestDose.dosage} was due at ${formatTime(earliestDose.scheduledTime)}"
                        }

                        now >= earliestDose.scheduledTime -> {
                            ComplianceStatus.LATE to "DUE NOW: ${earliestDose.name} ${earliestDose.dosage} is scheduled for ${formatTime(earliestDose.scheduledTime)}"
                        }

                        else -> {
                            ComplianceStatus.ON_TIME to "Next: ${earliestDose.name} ${earliestDose.dosage} is scheduled for ${formatTime(earliestDose.scheduledTime)}"
                        }
                    }

                    val (weeklyDays, weeklyRate) = calculateWeeklyCompliance(pendingDoses, now)

                    val patientName = _state.value.activePatient?.name ?: "Patient"
                    val activities = generateLiveActivities(patientName, pendingDoses, now)

                    _state.update {
                        it.copy(
                            selectedPatientDoses = mappedUiDoses,
                            selectedPatientDailyStatus = status,
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

    private fun calculateWeeklyCompliance(
        pendingDoses: List<PendingDoseWithMedication>,
        now: Long
    ): Pair<List<ComplianceDayUiModel>, Int> {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val graceWindowMillis = 30 * 60 * 1000L

        val overdueCount = pendingDoses.count { (now - it.scheduledTime) > graceWindowMillis }
        val lateCount = pendingDoses.count { now >= it.scheduledTime && (now - it.scheduledTime) <= graceWindowMillis }

        val todayStatus = when {
            overdueCount > 0 -> ComplianceStatus.MISSED
            lateCount > 0 -> ComplianceStatus.LATE
            else -> ComplianceStatus.ON_TIME
        }

        val weeklyDays = dayNames.mapIndexed { index, dayName ->
            when {
                index < 4 -> ComplianceDayUiModel(dayName, ComplianceStatus.ON_TIME)
                index == 4 -> ComplianceDayUiModel(dayName, todayStatus)
                else -> ComplianceDayUiModel(dayName, ComplianceStatus.DEFAULT)
            }
        }

        val totalRecorded = 5
        val missedDays = if (todayStatus == ComplianceStatus.MISSED) 1 else 0
        val rate = (((totalRecorded - missedDays) * 100) / totalRecorded)

        return weeklyDays to rate
    }

    private fun generateLiveActivities(
        patientName: String,
        doses: List<PendingDoseWithMedication>,
        now: Long
    ): List<RecentActivityUiModel> {
        val graceWindowMillis = 30 * 60 * 1000L

        return doses.take(3).map { dose ->
            val isOverdue = (now - dose.scheduledTime) > graceWindowMillis
            val isDueNow = now >= dose.scheduledTime && !isOverdue

            val (status, action) = when {
                isOverdue -> ComplianceStatus.MISSED to "missed ${dose.name} ${dose.dosage}"
                isDueNow -> ComplianceStatus.LATE to "has ${dose.name} ${dose.dosage} due now"
                else -> ComplianceStatus.ON_TIME to "scheduled for ${dose.name} ${dose.dosage}"
            }

            RecentActivityUiModel(
                id = dose.id.toString(),
                patientName = patientName,
                actionDescription = action,
                timestampText = "at ${formatTime(dose.scheduledTime)}",
                status = status
            )
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

        viewModelScope.launch {
            _state.update {
                it.copy(
                    dailyStatusAlertText = "Nudge reminder sent to $patientName!",
                    lastUpdatedText = "Updated just now"
                )
            }

            platformNotifier.sendInstantNudge(
                title = "Caregiver Reminder",
                message = "Time to take your medication!"
            )

            delay(3000L.milliseconds)
            _state.update {
                it.copy(dailyStatusAlertText = previousAlert)
            }
        }
    }
}