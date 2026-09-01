package com.riramzy.pillfllow.ui.state.prescriptions

import com.riramzy.pillfllow.utils.PillColor
import com.riramzy.pillfllow.utils.PillShape

data class PatientPrescriptionsState(
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

data class PrescriptionUiModel(
    val id: Long,
    val medicationName: String,
    val dosage: String,
    val pillShape: PillShape,
    val pillColor: PillColor,
    val scheduleText: String,
    val instructionsText: String,
    val nextDoseText: String
)