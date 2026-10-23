package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.randomUUID

class LogPrnDoseUseCase(
    private val medicationRepo: MedicationRepo,
    private val platformNotifier: PlatformNotifier = PlatformNotifier()
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

        val updatedMed = medicationRepo.getMedicationByIdOnce(medicationId)
        if (updatedMed?.remainingPills != null && updatedMed.remainingPills <= updatedMed.refillThreshold) {
            val alertBody = if (updatedMed.remainingPills == 0) {
                "${updatedMed.name} is out of stock! Refill immediately."
            } else {
                "${updatedMed.name} is running low (${updatedMed.remainingPills} pills left)."
            }

            platformNotifier.sendInstantNudge(
                title = "Refill Alert",
                message = alertBody
            )
        }
    }
}