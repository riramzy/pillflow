package com.riramzy.pillfllow.ui.viewmodel.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.DeletePrescriptionUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetMedicationsForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetPendingDosesForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SavePrescriptionUseCase
import com.riramzy.pillfllow.ui.state.prescriptions.PatientPrescriptionsAction
import com.riramzy.pillfllow.ui.state.prescriptions.PatientPrescriptionsState
import com.riramzy.pillfllow.ui.state.prescriptions.PrescriptionUiModel
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
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase
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
                            scheduleText = "${med.frequency}, ${med.timeOfDay}",
                            instructionsText = med.instructions.ifBlank { "Take as prescribed" },
                            nextDoseText = formatRelativeNextDose(nextDose?.scheduledTime, now)
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
        scheduledTimesMillis: List<Long> = emptyList()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = observeCurrentUserUseCase.once() ?: return@launch

            savePrescriptionUseCase(
                editingMedicationId = _state.value.editingMedicationId,
                userId = user.id,
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

    fun onAction(action: PatientPrescriptionsAction) {
        when (action) {
            is PatientPrescriptionsAction.OpenAddSheet -> openAddSheet()
            is PatientPrescriptionsAction.OpenEditSheet -> openEditSheet(action.medicationId)
            is PatientPrescriptionsAction.CloseAddSheet -> closeAddSheet()
            is PatientPrescriptionsAction.SavePrescription -> savePrescription(
                action.name, action.dosage, action.instructions, action.frequency,
                action.timeOfDay, action.colorHex, action.shape, action.scheduledTimesMillis
            )
            is PatientPrescriptionsAction.DeletePrescription -> deletePrescription(action.id)
        }
    }
}