package com.riramzy.pillfllow.ui.viewmodel.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.medication.DeletePrescriptionUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetMedicationsForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SavePrescriptionUseCase
import com.riramzy.pillfllow.ui.state.prescriptions.CaregiverPrescriptionsAction
import com.riramzy.pillfllow.ui.state.prescriptions.CaregiverPrescriptionsState
import com.riramzy.pillfllow.ui.state.prescriptions.PrescriptionUiModel
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.formatTime
import com.riramzy.pillfllow.utils.pill.PillColor
import com.riramzy.pillfllow.utils.pill.PillShape
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
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase
): ViewModel() {
    private val _state = MutableStateFlow(CaregiverPrescriptionsState())
    val state: StateFlow<CaregiverPrescriptionsState> = _state.asStateFlow()

    private val _selectedPatientId = MutableStateFlow<String?>(null)
    val selectedPatientId: StateFlow<String?> = _selectedPatientId.asStateFlow()

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
                        if (patients.isNotEmpty() && (_selectedPatientId.value == null || !validIds.contains(_selectedPatientId.value))) {
                            _selectedPatientId.value = patients.first().id
                        }
                        _state.update {
                            it.copy(
                                pairedPatients = patients,
                                selectedPatientId = _selectedPatientId.value,
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
            _selectedPatientId.filterNotNull().flatMapLatest { patientId ->
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
                _state.update { current ->
                    current.copy(
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
        viewModelScope.launch(Dispatchers.IO) {
            val patientId = _selectedPatientId.value ?: return@launch

            savePrescriptionUseCase(
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
            closeAddSheet()
        }
    }

    fun deletePrescription(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            deletePrescriptionUseCase(id)
        }
    }

    fun selectPatient(patientId: String) {
        _selectedPatientId.value = patientId
        _state.update { it.copy(selectedPatientId = patientId) }
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
        }
    }
}