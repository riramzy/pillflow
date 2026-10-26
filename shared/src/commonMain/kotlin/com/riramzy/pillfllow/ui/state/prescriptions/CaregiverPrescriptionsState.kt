package com.riramzy.pillfllow.ui.state.prescriptions

import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel

data class CaregiverPrescriptionsState(
    val pairedPatients: List<PairedPatientUiModel> = emptyList(),
    val selectedPatientId: String? = null,
    val prescriptions: List<PrescriptionUiModel> = emptyList(),
    val activeCount: Int = 0,
    val nextDoseTime: String = "--:--",
    val nextDoseMedication: String = "None",
    val editingMedicationId: String? = null,
    val refillingMedicationId: String? = null,
    val isAddSheetOpen: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
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
        val scheduledTimesMillis: List<Long> = emptyList(),
        val isPrn: Boolean = false,
        val remainingPills: Int? = null,
        val refillThreshold: Int = 5
    ): CaregiverPrescriptionsAction
    data class OpenRefillSheet(val medicationId: String): CaregiverPrescriptionsAction
    data object CloseRefillSheet: CaregiverPrescriptionsAction
    data class RefillPrescription(val medicationId: String, val addedPills: Int): CaregiverPrescriptionsAction
    data class DeletePrescription(val id: String): CaregiverPrescriptionsAction
    data object ClearMessages: CaregiverPrescriptionsAction
    data object Refresh: CaregiverPrescriptionsAction
}