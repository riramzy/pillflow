package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.Result
import com.riramzy.pillfllow.utils.randomUUID
import com.riramzy.pillfllow.utils.safeCall

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
        scheduledTimesMillis: List<Long> = emptyList()
    ): Result<String> = safeCall {
        val medicationId = editingMedicationId ?: randomUUID()

        val medication = MedicationEntity(
            id = medicationId,
            userId = userId,
            name = name,
            dosage = dosage,
            frequency = frequency,
            timeOfDay = timeOfDay,
            colorHex = colorHex,
            shape = shape,
            instructions = instructions,
            isSynced = false
        )

        medicationRepo.insertMedication(medication)

        if (scheduledTimesMillis.isNotEmpty()) {
            val scheduledDoses = scheduledTimesMillis.map { time ->
                ScheduledDoseEntity(
                    medicationId = medication.id,
                    scheduledTime = time,
                    complianceStatus = "PENDING",
                    isTaken = false,
                    isSynced = false
                )
            }

            medicationRepo.insertScheduledDoses(scheduledDoses)
        }

        medicationId
    }
}