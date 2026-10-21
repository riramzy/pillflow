package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.randomUUID

class LogPrnDoseUseCase(
    private val medicationRepo: MedicationRepo
) {
    suspend operator fun invoke(medicationId: String): Result<Unit> = safeCall {
        val now = currentTimeMillis()
        val med = medicationRepo.getMedicationByIdOnce(medicationId) ?: return@safeCall

        val prnDose = ScheduledDoseEntity(
            id = randomUUID(),
            medicationId = medicationId,
            scheduledTime = now,
            takenTime = now,
            complianceStatus = "PRN",
            isTaken = true,
            isSynced = false
        )

        medicationRepo.logPrnDose(prnDose)

        if (med.remainingPills != null && med.remainingPills > 0) {
            val updated = med.copy(
                remainingPills = (med.remainingPills - 1).coerceAtLeast(0),
                isSynced = false
            )

            medicationRepo.insertMedication(updated)
        }
    }
}