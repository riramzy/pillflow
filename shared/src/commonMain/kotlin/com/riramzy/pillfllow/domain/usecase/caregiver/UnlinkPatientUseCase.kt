package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall

class UnlinkPatientUseCase(
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(caregiverId: String, patientId: String, pairingId: String): Result<Unit> = safeCall {
        userRepo.unlinkCaregiverAndPatient(caregiverId, patientId)

        if (pairingId.isNotBlank()) {
            pairingRepo.deletePairingById(pairingId)
        }
    }
}