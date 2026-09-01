package com.riramzy.pillfllow.ui.state.prescriptions

import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel

data class CaregiverPrescriptionsState(
    val pairedPatients: List<PairedPatientUiModel> = emptyList(),
    val selectedPatientId: String? = null,
    val prescriptions: List<PrescriptionUiModel> = emptyList(),
    val activeCount: Int = 0,
    val nextDoseTime: String = "--:--",
    val nextDoseMedication: String = "None",
    val isLoading: Boolean = false,
    val isAddSheetOpen: Boolean = false,
    val editingMedicationId: Long? = null,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = prescriptions.isEmpty()
}