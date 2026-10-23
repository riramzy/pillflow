package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.scheduler.MedicationScheduler
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.randomUUID

class SavePrescriptionUseCase(
    private val medicationRepo: MedicationRepo
) {
    suspend operator fun invoke(
        editingMedicationId: String?,
        userId: String,
        name: String,
        dosage: String,
        instructions: String,
        frequency: String,
        timeOfDay: String,
        colorHex: String,
        shape: String,
        scheduledTimesMillis: List<Long> = emptyList(),
        isPrn: Boolean = false,
        remainingPills: Int? = null,
        refillThreshold: Int = 5
    ): Result<String> = safeCall {
        val medicationId = editingMedicationId ?: randomUUID()
        val existing = editingMedicationId?.let { medicationRepo.getMedicationByIdOnce(it) }

        val medication = MedicationEntity(
            id = medicationId,
            userId = userId,
            name = name,
            dosage = dosage,
            frequency = if (isPrn) "As Needed" else frequency,
            timeOfDay = if (isPrn) "As Needed" else timeOfDay,
            colorHex = colorHex,
            shape = shape,
            instructions = instructions,
            isPaused = existing?.isPaused ?: false,
            isPrn = isPrn,
            remainingPills = remainingPills ?: existing?.remainingPills,
            refillThreshold = if (remainingPills != null) refillThreshold else (existing?.refillThreshold ?: 5),
            isSynced = false
        )

        medicationRepo.insertMedication(medication)

        if (editingMedicationId != null) {
            medicationRepo.deletePendingDosesForMedication(editingMedicationId)
        }

        if (!isPrn && !medication.isPaused && scheduledTimesMillis.isNotEmpty()) {
            val existingTakenTimes = if (editingMedicationId != null) {
                medicationRepo.getTakenDoseTimesForMedication(editingMedicationId)
            } else {
                emptySet()
            }

            val scheduledDoses = MedicationScheduler.generateRollingDoses(
                medicationId = medicationId,
                baseTimesToday = scheduledTimesMillis,
                existingTimes = existingTakenTimes
            )

            medicationRepo.insertScheduledDoses(scheduledDoses)
        }

        medicationId
    }
}