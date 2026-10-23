package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis

class LogDoseTakenUseCase(
    private val medicationRepo: MedicationRepo,
    private val platformNotifier: PlatformNotifier = PlatformNotifier()
) {
    suspend operator fun invoke(
        doseId: String,
        scheduledTime: Long? = null
    ): Result<Unit> = safeCall {
        val now = currentTimeMillis()
        val dose = medicationRepo.getScheduledDoseById(doseId)
        val targetTime = scheduledTime ?: dose?.scheduledTime ?: now

        val compliance = DoseStateMachine.evaluateCompliance(
            scheduledTimeMillis = targetTime,
            actionTimeMillis = now
        )

        medicationRepo.markScheduledDoseTaken(
            id = doseId,
            takenTime = now,
            isTaken = true,
            complianceStatus = compliance.name
        )

        platformNotifier.cancelReminder(doseId = doseId)

        dose?.medicationId?.let { medId ->
            val updatedMed = medicationRepo.getMedicationByIdOnce(medId)
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
}