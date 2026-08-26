package com.riramzy.pillfllow.ui.viewmodel.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.ui.state.prescriptions.PrescriptionUiModel
import com.riramzy.pillfllow.ui.state.prescriptions.PrescriptionsState
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

class PrescriptionsViewModel(
    private val medicationRepo: MedicationRepo,
    private val authRepo: AuthRepo,
): ViewModel() {
    private val _state = MutableStateFlow(PrescriptionsState())
    val state: StateFlow<PrescriptionsState> = _state.asStateFlow()

    init {
        observePrescriptions()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePrescriptions() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.currentUser.filterNotNull().flatMapLatest { user ->
                combine(
                    medicationRepo.getPendingDosesForUser(user.id),
                    medicationRepo.getMedicationForUser(user.id)
                ) { pendingDoses, medications ->
                    val now = currentTimeMillis()

                    val earliestDose = pendingDoses.minByOrNull { it.scheduledTime }
                    val nextTime = earliestDose?.let { formatTime(it.scheduledTime) } ?: "--:--"
                    val nextMed = earliestDose?.let { "${it.name} ${it.dosage}" } ?: "None"

                    val prescriptions = medications.map { med ->
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
                            pillShape = pillShape,
                            pillColor = pillColor,
                            scheduleText = "${med.frequency}, ${med.timeOfDay}",
                            instructionsText = med.instructions.ifBlank { "Take as prescribed" },
                            nextDoseText = formatRelativeNextDose(nextDose?.scheduledTime, now)
                        )
                    }

                    PrescriptionsState(
                        prescriptions = prescriptions,
                        activeCount = medications.size,
                        nextDoseTime = nextTime,
                        nextDoseMedication = nextMed,
                        isLoading = false
                    )
                }
            }.collectLatest {
                _state.value = it
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
            val user = authRepo.currentUser.firstOrNull() ?: return@launch

            val medication = MedicationEntity(
                id = _state.value.editingMedicationId ?: 0L,
                userId = user.id,
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
}

private fun formatRelativeNextDose(
    scheduledTime: Long?,
    now: Long
): String {
    if (scheduledTime == null) return "No upcoming doses"
    val diffMillis = scheduledTime - now

    return when {
        diffMillis < -30 * 60 * 1000L -> "Overdue"
        diffMillis <= 0 -> "Due now"

        else -> {
            val minutes = (diffMillis / (1000 * 60)).toInt()
            val hours = minutes / 60

            when {
                minutes < 60 -> "Next dose in $minutes mins"
                hours < 24 -> "Next dose in $hours ${if (hours == 1) "hour" else "hours"}"
                else -> "Next dose at ${formatTime(scheduledTime)}"
            }
        }
    }
}