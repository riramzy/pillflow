package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis

class SkipDoseUseCase(
    private val medicationRepo: MedicationRepo
) {
    suspend operator fun invoke(doseId: String, reason: String): Result<Unit> = safeCall {
        medicationRepo.markScheduledDoseSkipped(
            id = doseId,
            takenTime = currentTimeMillis(),
            skipReason = reason
        )
    }
}