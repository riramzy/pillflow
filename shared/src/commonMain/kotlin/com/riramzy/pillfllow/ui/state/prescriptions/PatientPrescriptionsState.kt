package com.riramzy.pillfllow.ui.state.prescriptions

import com.riramzy.pillfllow.utils.pill.PillColor
import com.riramzy.pillfllow.utils.pill.PillShape

data class PatientPrescriptionsState(
    val prescriptions: List<PrescriptionUiModel> = emptyList(),
    val activeCount: Int = 0,
    val nextDoseTime: String = "--:--",
    val nextDoseMedication: String = "None",
    val isLoading: Boolean = true,
    val isAddSheetOpen: Boolean = false,
    val editingMedicationId: String? = null,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = prescriptions.isEmpty()
}

data class PrescriptionUiModel(
    val id: String,
    val medicationName: String,
    val dosage: String,
    val pillShape: PillShape,
    val pillColor: PillColor,
    val scheduleText: String,
    val instructionsText: String,
    val nextDoseText: String
)

sealed interface PatientPrescriptionsAction {
    data object OpenAddSheet: PatientPrescriptionsAction
    data class OpenEditSheet(val medicationId: String): PatientPrescriptionsAction
    data object CloseAddSheet: PatientPrescriptionsAction
    data class SavePrescription(
        val name: String,
        val dosage: String,
        val instructions: String,
        val frequency: String,
        val timeOfDay: String,
        val colorHex: String,
        val shape: String,
        val scheduledTimesMillis: List<Long> = emptyList()
    ): PatientPrescriptionsAction
    data class DeletePrescription(val id: String): PatientPrescriptionsAction
}