package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.Result
import com.riramzy.pillfllow.utils.safeCall

class DeletePrescriptionUseCase(
    private val medicationRepo: MedicationRepo
) {
    suspend operator fun invoke(medicationId: String): Result<Unit> = safeCall {
        medicationRepo.deleteMedicationById(medicationId)
    }
}