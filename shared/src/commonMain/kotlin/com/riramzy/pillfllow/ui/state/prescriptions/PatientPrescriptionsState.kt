package com.riramzy.pillfllow.ui.state.prescriptions

import androidx.compose.ui.graphics.Color
import com.riramzy.pillfllow.utils.pill.PillColor
import com.riramzy.pillfllow.utils.pill.PillShape
import org.jetbrains.compose.resources.DrawableResource

data class PatientPrescriptionsState(
    val prescriptions: List<PrescriptionUiModel> = emptyList(),
    val activeCount: Int = 0,
    val nextDoseTime: String = "--:--",
    val nextDoseMedication: String = "None",
    val isAddSheetOpen: Boolean = false,
    val editingMedicationId: String? = null,
    val refillingMedicationId: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
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
    val nextDoseText: String,
    val isPrn: Boolean = false,
    val remainingPills: Int? = null,
    val refillThreshold: Int = 5,
    val daysLeft: Int? = null
)

data class LowSupplyMedicationUiModel(
    val id: String = "",
    val name: String = "",
    val dosage: String = "",
    val remainingPills: Int = 0,
    val daysLeft: Int? = null
)

data class StorageExtensionStyle(
    val badgeBg: Color,
    val badgeTint: Color,
    val badgeIcon: DrawableResource,
    val badgeText: String,
    val btnBg: Color,
    val btnTextColor: Color
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
        val scheduledTimesMillis: List<Long> = emptyList(),
        val isPrn: Boolean = false,
        val remainingPills: Int? = null,
        val refillThreshold: Int = 5
    ): PatientPrescriptionsAction
    data class OpenRefillSheet(val medicationId: String): PatientPrescriptionsAction
    data object CloseRefillSheet: PatientPrescriptionsAction
    data class RefillPrescription(val medicationId: String, val addedPills: Int): PatientPrescriptionsAction
    data class DeletePrescription(val id: String): PatientPrescriptionsAction
    data object ClearMessages: PatientPrescriptionsAction
    data object Refresh: PatientPrescriptionsAction
}