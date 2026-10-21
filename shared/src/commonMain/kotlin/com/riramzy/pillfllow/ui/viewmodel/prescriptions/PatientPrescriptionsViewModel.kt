package com.riramzy.pillfllow.ui.viewmodel.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.DeletePrescriptionUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetMedicationsForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SavePrescriptionUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SyncAndMaintainScheduleUseCase
import com.riramzy.pillfllow.ui.state.prescriptions.PatientPrescriptionsAction
import com.riramzy.pillfllow.ui.state.prescriptions.PatientPrescriptionsState
import com.riramzy.pillfllow.ui.state.prescriptions.PrescriptionUiModel
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.pill.PillColorMapper
import com.riramzy.pillfllow.utils.pill.PillShapeMapper
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatRelativeNextDose
import com.riramzy.pillfllow.utils.platform.formatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PatientPrescriptionsViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getMedicationsForUserUseCase: GetMedicationsForUserUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val savePrescriptionUseCase: SavePrescriptionUseCase,
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    private val syncAndMaintainScheduleUseCase: SyncAndMaintainScheduleUseCase
): ViewModel() {
    private val _state = MutableStateFlow(PatientPrescriptionsState())
    val state: StateFlow<PatientPrescriptionsState> = _state.asStateFlow()

    init {
        observePrescriptions()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePrescriptions() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().filterNotNull().flatMapLatest { user ->
                combine(
                    getPendingDosesForUserUseCase(user.id),
                    getMedicationsForUserUseCase(user.id)
                ) { pendingDoses, medications ->
                    val now = currentTimeMillis()

                    val earliestDose = pendingDoses.minByOrNull { it.scheduledTime }
                    val nextTime = earliestDose?.let { formatTime(it.scheduledTime) } ?: "--:--"
                    val nextMed = earliestDose?.let { "${it.name} ${it.dosage}" } ?: "None"

                    val prescriptions = medications.map { med ->
                        val nextDose = pendingDoses
                            .filter { it.name.equals(med.name, ignoreCase = true) }
                            .minByOrNull { it.scheduledTime }

                        val pillShape = PillShapeMapper.fromRaw(med.shape)

                        val pillColor = PillColorMapper.fromRaw(med.colorHex)

                        PrescriptionUiModel(
                            id = med.id,
                            medicationName = med.name,
                            dosage = med.dosage,
                            pillShape = pillShape,
                            pillColor = pillColor,
                            scheduleText = if (med.isPrn) "As Needed (PRN)" else "${med.frequency}, ${med.timeOfDay}",
                            instructionsText = med.instructions.ifBlank { "Take as prescribed" },
                            nextDoseText = if (med.isPaused) "Paused" else if (med.isPrn) "Take as needed" else formatRelativeNextDose(nextDose?.scheduledTime, now),
                            isPrn = med.isPrn
                        )
                    }

                    PatientPrescriptionsState(
                        prescriptions = prescriptions,
                        activeCount = medications.size,
                        nextDoseTime = nextTime,
                        nextDoseMedication = nextMed,
                        isLoading = false
                    )

                    prescriptions to (nextTime to nextMed)
                }
            }.collectLatest { (prescriptions, nextDoseInfo) ->
                _state.update { current ->
                    current.copy(
                        prescriptions = prescriptions,
                        activeCount = prescriptions.size,
                        nextDoseTime = nextDoseInfo.first,
                        nextDoseMedication = nextDoseInfo.second,
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

    fun openEditSheet(medicationId: String) {
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
        scheduledTimesMillis: List<Long> = emptyList(),
        isPrn: Boolean = false
    ) {
        val isEditing = _state.value.editingMedicationId != null
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val user = observeCurrentUserUseCase.once()
            if (user == null) {
                _state.update { it.copy(isSaving = false) }
                return@launch
            }

            val result = savePrescriptionUseCase(
                editingMedicationId = _state.value.editingMedicationId,
                userId = user.id,
                name = name,
                dosage = dosage,
                instructions = instructions,
                frequency = frequency,
                timeOfDay = timeOfDay,
                colorHex = colorHex,
                shape = shape,
                scheduledTimesMillis = scheduledTimesMillis,
                isPrn = isPrn
            )

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            isAddSheetOpen = false,
                            editingMedicationId = null,
                            successMessage = if (isEditing) "Prescription updated successfully" else "Prescription added successfully"
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message ?: "Failed to save prescription. Please try again."
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    fun deletePrescription(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = deletePrescriptionUseCase(id)) {
                is Result.Success -> {
                    _state.update { it.copy(successMessage = "Prescription deleted successfully") }
                }

                is Result.Error -> {
                    _state.update { it.copy(errorMessage = result.message ?: "Failed to delete prescription. Please try again.") }
                }

                else -> Unit
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(successMessage = null, errorMessage = null) }
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

    fun onAction(action: PatientPrescriptionsAction) {
        when (action) {
            is PatientPrescriptionsAction.OpenAddSheet -> openAddSheet()
            is PatientPrescriptionsAction.OpenEditSheet -> openEditSheet(action.medicationId)
            is PatientPrescriptionsAction.CloseAddSheet -> closeAddSheet()
            is PatientPrescriptionsAction.SavePrescription -> savePrescription(
                action.name,
                action.dosage,
                action.instructions,
                action.frequency,
                action.timeOfDay,
                action.colorHex,
                action.shape,
                action.scheduledTimesMillis,
                action.isPrn
            )
            is PatientPrescriptionsAction.DeletePrescription -> deletePrescription(action.id)
            is PatientPrescriptionsAction.ClearMessages -> clearMessages()
            is PatientPrescriptionsAction.Refresh -> refresh()
        }
    }
}