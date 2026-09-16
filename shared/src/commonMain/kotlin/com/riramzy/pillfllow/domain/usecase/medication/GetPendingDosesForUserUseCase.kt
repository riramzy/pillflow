package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import kotlinx.coroutines.flow.Flow

class GetPendingDosesForUserUseCase(
    private val medicationRepo: MedicationRepo
) {
    operator fun invoke(userId: String): Flow<List<PendingDoseWithMedication>> {
        return medicationRepo.getPendingDosesForUser(userId)
    }
}