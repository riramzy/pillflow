package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import kotlinx.coroutines.flow.Flow

class GetPendingDosesForPatientsUseCase(
    private val medicationRepo: MedicationRepo
) {
    operator fun invoke(patientIds: List<String>): Flow<List<PendingDoseWithMedication>> {
        return medicationRepo.getPendingDosesForPatients(patientIds)
    }
}