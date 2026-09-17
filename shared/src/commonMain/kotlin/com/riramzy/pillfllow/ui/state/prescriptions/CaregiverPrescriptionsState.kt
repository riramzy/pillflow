package com.riramzy.pillfllow.ui.state.prescriptions

import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel

data class CaregiverPrescriptionsState(
    val pairedPatients: List<PairedPatientUiModel> = emptyList(),
    val selectedPatientId: String? = null,
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

sealed interface CaregiverPrescriptionsAction {
    data class SelectPatient(val patientId: String): CaregiverPrescriptionsAction
    data object OpenAddSheet: CaregiverPrescriptionsAction
    data class OpenEditSheet(val medicationId: String): CaregiverPrescriptionsAction
    data object CloseAddSheet: CaregiverPrescriptionsAction
    data class SavePrescription(
        val name: String,
        val dosage: String,
        val instructions: String,
        val frequency: String,
        val timeOfDay: String,
        val colorHex: String,
        val shape: String,
        val scheduledTimesMillis: List<Long> = emptyList()
    ): CaregiverPrescriptionsAction
    data class DeletePrescription(val id: String): CaregiverPrescriptionsAction
}