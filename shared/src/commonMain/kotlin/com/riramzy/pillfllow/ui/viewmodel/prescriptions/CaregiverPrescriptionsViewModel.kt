package com.riramzy.pillfllow.ui.viewmodel.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.medication.DeletePrescriptionUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetMedicationsForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SavePrescriptionUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SyncAndMaintainScheduleUseCase
import com.riramzy.pillfllow.ui.state.prescriptions.CaregiverPrescriptionsAction
import com.riramzy.pillfllow.ui.state.prescriptions.CaregiverPrescriptionsState
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

class CaregiverPrescriptionsViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val getPendingDosesForUserUseCase: GetPendingDosesForUserUseCase,
    private val getMedicationsForUserUseCase: GetMedicationsForUserUseCase,
    private val savePrescriptionUseCase: SavePrescriptionUseCase,
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    private val syncAndMaintainScheduleUseCase: SyncAndMaintainScheduleUseCase,
    private val sessionManager: SessionManager
): ViewModel() {
    private val _state = MutableStateFlow(CaregiverPrescriptionsState())
    val state: StateFlow<CaregiverPrescriptionsState> = _state.asStateFlow()

    val selectedPatientId: StateFlow<String?> = sessionManager.selectedPatientId

    init {
        observePatients()
        observePrescriptionsForSelectedPatient()
    }

    private fun observePatients() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().collectLatest { caregiver ->
                caregiver?.let { user ->
                    getCaregiverPatientsUseCase(user.id).collectLatest { patients ->
                        val validIds = patients.map { it.id }
                        if (patients.isNotEmpty() && (selectedPatientId.value == null || !validIds.contains(selectedPatientId.value))) {
                            sessionManager.setSelectedPatientId(patients.first().id)
                        }

                        _state.update {
                            it.copy(
                                pairedPatients = patients,
                                selectedPatientId = selectedPatientId.value,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePrescriptionsForSelectedPatient() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedPatientId
                .filterNotNull()
                .flatMapLatest { patientId ->
                    combine(
                    getPendingDosesForUserUseCase(patientId),
                    getMedicationsForUserUseCase(patientId)
                    ) { pendingDoses, meds ->
                        val now = currentTimeMillis()

                        val earliestDose = pendingDoses.minByOrNull { it.scheduledTime }

                        val prescriptions = meds.map { med ->
                            val nextDose = pendingDoses
                                .filter { it.name.equals(med.name, ignoreCase = true) }
                                .minByOrNull { it.scheduledTime }

                            val pillShape = PillShapeMapper.fromRaw(med.shape)

                            val pillColor = PillColorMapper.fromRaw(med.colorHex)

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

                        patientId to Triple(prescriptions, meds.size, earliestDose)
                    }
            }.collectLatest { (patientId, data) ->
                    val (prescriptions, count, earliestDose) = data

                    _state.update { current ->
                        current.copy(
                            selectedPatientId = patientId,
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
        scheduledTimesMillis: List<Long> = emptyList()
    ) {
        val isEditing = _state.value.editingMedicationId != null
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            val patientId = selectedPatientId.value
            if (patientId == null) {
                _state.update { it.copy(isSaving = false) }
                return@launch
            }

            val result = savePrescriptionUseCase(
                editingMedicationId = _state.value.editingMedicationId,
                userId = patientId,
                name = name,
                dosage = dosage,
                instructions = instructions,
                frequency = frequency,
                timeOfDay = timeOfDay,
                colorHex = colorHex,
                shape = shape,
                scheduledTimesMillis = scheduledTimesMillis
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

    fun selectPatient(patientId: String) {
        sessionManager.setSelectedPatientId(patientId)
        _state.update { it.copy(selectedPatientId = patientId) }
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

    fun onAction(action: CaregiverPrescriptionsAction) {
        when (action) {
            is CaregiverPrescriptionsAction.SelectPatient -> selectPatient(action.patientId)
            is CaregiverPrescriptionsAction.OpenAddSheet -> openAddSheet()
            is CaregiverPrescriptionsAction.OpenEditSheet -> openEditSheet(action.medicationId)
            is CaregiverPrescriptionsAction.CloseAddSheet -> closeAddSheet()
            is CaregiverPrescriptionsAction.SavePrescription -> savePrescription(
                action.name,
                action.dosage,
                action.instructions,
                action.frequency,
                action.timeOfDay,
                action.colorHex,
                action.shape,
                action.scheduledTimesMillis
            )
            is CaregiverPrescriptionsAction.DeletePrescription -> deletePrescription(action.id)
            is CaregiverPrescriptionsAction.ClearMessages -> clearMessages()
            is CaregiverPrescriptionsAction.Refresh -> refresh()
        }
    }
}