package com.riramzy.pillfllow.domain.medication

import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.domain.usecase.medication.RefillPrescriptionUseCase
import com.riramzy.pillfllow.ui.state.prescriptions.LowSupplyMedicationUiModel
import com.riramzy.pillfllow.ui.state.prescriptions.PrescriptionUiModel
import com.riramzy.pillfllow.utils.pill.PillColorMapper
import com.riramzy.pillfllow.utils.pill.PillShapeMapper
import com.riramzy.pillfllow.utils.platform.findNextActionDoseTime
import com.riramzy.pillfllow.utils.platform.formatRelativeNextDose
import com.riramzy.pillfllow.utils.platform.formatTime

data class CabinetEvaluation(
    val trackedCount: Int,
    val minDaysOfSupply: Int?,
    val lowSupplyMedications: List<LowSupplyMedicationUiModel>
)

data class PrescriptionsOverviewEvaluation(
    val prescriptions: List<PrescriptionUiModel>,
    val activeCount: Int,
    val nextDoseTime: String,
    val nextDoseMedication: String
)

object PrescriptionEvaluator {
    fun evaluateCabinet(
        medications: List<MedicationEntity>
    ): CabinetEvaluation {
        val trackedMeds = medications.filter { !it.isArchived && it.remainingPills != null }

        val lowSupply = trackedMeds
            .filter { it.remainingPills!! <= it.refillThreshold }
            .map { med ->
                LowSupplyMedicationUiModel(
                    id = med.id,
                    name = med.name,
                    dosage = med.dosage,
                    remainingPills = med.remainingPills!!,
                    daysLeft = RefillPrescriptionUseCase.estimateDaysOfSupply(
                        med.remainingPills,
                        med.timeOfDay,
                        med.isPrn
                    )
                )
            }

        val minDays = trackedMeds
            .mapNotNull {
                RefillPrescriptionUseCase.estimateDaysOfSupply(
                    it.remainingPills,
                    it.timeOfDay,
                    it.isPrn
                )
            }

        return CabinetEvaluation(
            trackedCount = trackedMeds.size,
            minDaysOfSupply = minDays.minOrNull(),
            lowSupplyMedications = lowSupply
        )
    }

    fun evaluatePrescriptionsOverview(
        medications: List<MedicationEntity>,
        pendingDoses: List<PendingDoseWithMedication>,
        now: Long
    ): PrescriptionsOverviewEvaluation {
        val nextActionMillis = findNextActionDoseTime(pendingDoses.map { it.scheduledTime }, now)

        val earliestDose = nextActionMillis?.let { target ->
            pendingDoses.firstOrNull { it.scheduledTime == target }
        }

        val nextTime = earliestDose?.let { formatTime(it.scheduledTime) } ?: "--:--"
        val nextMed = earliestDose?.let { " ${it.name} ${it.dosage}" } ?: "None"

        val items = medications
            .map { med ->
                val medDosesTimes = pendingDoses
                    .filter {
                        it.id.startsWith("${med.id}_") ||
                                it.name.equals(med.name, ignoreCase = true)
                    }
                    .map { it.scheduledTime }

                val nextDoseTimeMillis = findNextActionDoseTime(medDosesTimes, now)

                PrescriptionUiModel(
                    id = med.id,
                    medicationName = med.name,
                    dosage = med.dosage,
                    pillShape = PillShapeMapper.fromRaw(med.shape),
                    pillColor = PillColorMapper.fromRaw(med.colorHex),
                    scheduleText = if (med.isPrn) "As Needed (PRN)" else "${med.frequency}, ${med.timeOfDay}",
                    instructionsText = med.instructions.ifBlank { "Take as prescribed" },
                    nextDoseText = when {
                        med.isPaused -> "Paused"
                        med.isPrn -> "Take as needed"
                        else -> formatRelativeNextDose(nextDoseTimeMillis, now)
                    },
                    isPrn = med.isPrn,
                    remainingPills = med.remainingPills,
                    refillThreshold = med.refillThreshold,
                    daysLeft = RefillPrescriptionUseCase.estimateDaysOfSupply(
                        med.remainingPills,
                        med.timeOfDay,
                        med.isPrn
                    )
                )
            }

        return PrescriptionsOverviewEvaluation(
            prescriptions = items,
            activeCount = medications.count { !it.isArchived && !it.isPaused },
            nextDoseTime = nextTime,
            nextDoseMedication = nextMed
        )
    }
}