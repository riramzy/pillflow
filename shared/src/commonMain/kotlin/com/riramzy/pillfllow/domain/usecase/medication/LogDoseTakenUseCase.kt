package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.Result
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.safeCall

class LogDoseTakenUseCase(
    private val medicationRepo: MedicationRepo,
    private val platformNotifier: PlatformNotifier = PlatformNotifier()
) {
    suspend operator fun invoke(
        doseId: String,
        scheduledTime: Long? = null
    ): Result<Unit> = safeCall {
        val now = currentTimeMillis()

        val targetTime = scheduledTime ?: now

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
    }
}