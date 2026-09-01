package com.riramzy.pillfllow.ui.viewmodel.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.prescriptions.CaregiverPrescriptionsState
import com.riramzy.pillfllow.ui.state.prescriptions.PrescriptionUiModel
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.PillColor
import com.riramzy.pillfllow.utils.PillShape
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.formatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
import pillfllow.shared.generated.resources.avatar5
import pillfllow.shared.generated.resources.avatar6
import pillfllow.shared.generated.resources.avatar7
import pillfllow.shared.generated.resources.avatar8

class CaregiverPrescriptionsViewModel(
    private val medicationRepo: MedicationRepo,
    private val authRepo: AuthRepo,
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo
): ViewModel() {
    private val _state = MutableStateFlow(CaregiverPrescriptionsState())
    val state: StateFlow<CaregiverPrescriptionsState> = _state.asStateFlow()

    private val _selectedPatientId = MutableStateFlow<String?>(null)
    val selectedPatientId: StateFlow<String?> = _selectedPatientId.asStateFlow()

    init {
        observeCaregiverAndPatients()
        observePrescriptionsForSelectedPatient()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCaregiverAndPatients() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.currentUser
                .filterNotNull()
                .flatMapLatest { caregiver ->
                    pairingRepo.getPairingsForCaregiver(caregiver.id)
                }
                .collect { pairings ->
                    val now = currentTimeMillis()
                    val graceWindowMillis = 30 * 60 * 1000L

                    val patientModels = pairings
                        .filter { it.status == "ACTIVE" }
                        .mapNotNull { pairing ->
                            val patient = userRepo.getUserByIdOnce(pairing.patientId)

                            patient?.let { user ->
                                val patientDoses =
                                    medicationRepo.getPendingDosesForUser(user.id).firstOrNull()
                                        ?: emptyList()

                                val missedCount =
                                    patientDoses.count { now - it.scheduledTime > graceWindowMillis }
                                val lateCount =
                                    patientDoses.count { now >= it.scheduledTime && (now - it.scheduledTime) <= graceWindowMillis }

                                val status = when {
                                    missedCount > 0 -> ComplianceStatus.MISSED
                                    lateCount > 0 -> ComplianceStatus.LATE
                                    else -> ComplianceStatus.ON_TIME
                                }

                                val score = if (patientDoses.isEmpty()) {
                                    100
                                } else {
                                    (100 - ((missedCount * 100) / patientDoses.size))
                                }

                                val avatar = when (user.avatarRes) {
                                    "avatar1" -> Res.drawable.avatar1
                                    "avatar2" -> Res.drawable.avatar2
                                    "avatar3" -> Res.drawable.avatar3
                                    "avatar4" -> Res.drawable.avatar4
                                    "avatar5" -> Res.drawable.avatar5
                                    "avatar6" -> Res.drawable.avatar6
                                    "avatar7" -> Res.drawable.avatar7
                                    else -> Res.drawable.avatar8
                                }

                                PairedPatientUiModel(
                                    id = user.id,
                                    name = user.firstName,
                                    relation = pairing.relation,
                                    phoneNumber = pairing.phoneNumber,
                                    avatar = avatar,
                                    status = status,
                                    lateDosesCount = lateCount,
                                    missedDosesCount = missedCount,
                                    compliancePercentage = score
                                )
                            }
                        }

                    if (_selectedPatientId.value == null &&
                        patientModels.isNotEmpty()
                    ) {
                        _selectedPatientId.value = patientModels.first().id
                    }

                    _state.update {
                        it.copy(
                            pairedPatients = patientModels,
                            selectedPatientId = _selectedPatientId.value ?: ""
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePrescriptionsForSelectedPatient() {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedPatientId
                .filterNotNull()
                .flatMapLatest { patientId ->
                    combine(
                        medicationRepo.getPendingDosesForUser(patientId),
                        medicationRepo.getMedicationForUser(patientId)
                    ) { pendingDoses, meds ->
                        val now = currentTimeMillis()

                        val earliestDose = pendingDoses.minByOrNull { it.scheduledTime }

                        val prescriptions = meds.map { med ->
                            val nextDose = pendingDoses
                                .filter { it.name.equals(med.name, ignoreCase = true) }
                                .minByOrNull { it.scheduledTime }

                            val pillShape = runCatching {
                                PillShape.valueOf(med.shape.uppercase())
                            }.getOrDefault(PillShape.CAPSULE)

                            val pillColor = PillColor.entries.firstOrNull {
                                it.name.equals(med.colorHex, ignoreCase = true) ||
                                        it.label.equals(med.colorHex, ignoreCase = true)
                            } ?: PillColor.SKY_BLUE

                            PrescriptionUiModel(
                                id = med.id,
                                medicationName = med.name,
                                dosage = med.dosage,
                                pillColor = pillColor,
                                pillShape = pillShape,
                                scheduleText = "${med.frequency}, ${med.timeOfDay}",
                                instructionsText = med.instructions.ifBlank { "Take as prescribed" },
                                nextDoseText = formatRelativeNextDose(nextDose?.scheduledTime, now)
                            )
                        }

                        Triple(prescriptions, meds.size, earliestDose)
                    }
                }.collectLatest { (prescriptions, count, earliestDose) ->
                    _state.update { state ->
                        state.copy(
                            prescriptions = prescriptions,
                            activeCount = count,
                            nextDoseTime = earliestDose?.let { formatTime(it.scheduledTime) } ?: "--:--",
                            nextDoseMedication = earliestDose?.let { "${it.name} ${it.dosage}" } ?: "None",
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun openAddSheet() {
        _state.update {
            it.copy(
                isAddSheetOpen = true,
                editingMedicationId = null
            )
        }
    }

    fun openEditSheet(medicationId: Long) {
        _state.update {
            it.copy(
                isAddSheetOpen = true,
                editingMedicationId = medicationId
            )
        }
    }

    fun closeAddSheet() {
        _state.update {
            it.copy(
                isAddSheetOpen = false,
                editingMedicationId = null
            )
        }
    }

    fun savePrescription(
        name: String,
        dosage: String,
        instructions: String,
        frequency: String,
        timeOfDay: String,
        colorHex: String,
        shape: String,
        scheduledTimesMillis: List<Long> = emptyList()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val patientId = _selectedPatientId.value ?: return@launch

            val medication = MedicationEntity(
                id = _state.value.editingMedicationId ?: 0L,
                userId = patientId,
                name = name,
                dosage = dosage,
                frequency = frequency,
                timeOfDay = timeOfDay,
                colorHex = colorHex,
                shape = shape,
                instructions = instructions,
                isSynced = false
            )

            val medId = medicationRepo.insertMedication(medication)

            if (scheduledTimesMillis.isNotEmpty()) {
                val scheduledDoses = scheduledTimesMillis.map { time ->
                    ScheduledDoseEntity(
                        medicationId = if (medication.id > 0) medication.id else medId,
                        scheduledTime = time,
                        complianceStatus = "PENDING",
                        isTaken = false,
                        isSynced = false
                    )
                }
                medicationRepo.insertScheduledDoses(scheduledDoses)
            }
            closeAddSheet()
        }
    }

    fun deletePrescription(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            medicationRepo.deleteMedicationById(id)
        }
    }

    fun selectPatient(patientId: String) {
        _selectedPatientId.value = patientId
        _state.update { it.copy(selectedPatientId = patientId) }
    }
}