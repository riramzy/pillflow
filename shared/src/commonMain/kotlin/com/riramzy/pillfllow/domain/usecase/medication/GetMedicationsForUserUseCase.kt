package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import kotlinx.coroutines.flow.Flow

class GetMedicationsForUserUseCase(
    private val medicationRepo: MedicationRepo
) {
    operator fun invoke(userId: String): Flow<List<MedicationEntity>> {
        return medicationRepo.getMedicationForUser(userId)
    }
}