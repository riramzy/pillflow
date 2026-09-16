package com.riramzy.pillfllow.domain.usecase.medication

import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import kotlinx.coroutines.flow.Flow

class GetDoseHistoryForUserUseCase(
    private val medicationRepo: MedicationRepo
) {
    operator fun invoke(userId: String): Flow<List<DoseHistoryEntity>> {
        return medicationRepo.getDoseHistoryForUser(userId)
    }
}