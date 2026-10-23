package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall

class RefillPrescriptionUseCase(
    private val medicationRepo: MedicationRepo
) {
    suspend operator fun invoke(
        medicationId: String,
        addedPills: Int
    ): Result<Unit> = safeCall {
        val med = medicationRepo.getMedicationByIdOnce(medicationId)
            ?: throw IllegalArgumentException("Medication not found")

        val current = med.remainingPills ?: 0
        val newTotal = (current + addedPills).coerceAtLeast(0)

        medicationRepo.refillMedication(medicationId, newTotal)
    }

    companion object {
        fun estimateDaysOfSupply(
            remainingPills: Int?,
            timeOfDay: String,
            isPrn: Boolean,
        ): Int? {
            if (remainingPills == null || isPrn) return null

            val dosesPerDay = timeOfDay
                .split(",")
                .count { it.isNotBlank() }
                .coerceAtLeast(1)

            return remainingPills / dosesPerDay
        }
    }
}