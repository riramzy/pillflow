package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.Result
import com.riramzy.pillfllow.utils.safeCall

class InitiatePairingUseCase(
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(pairingCode: String): Result<Pair<CaregiverPatientPairingEntity, UserEntity>> = safeCall {
        val pendingPairing = pairingRepo.getPendingPairingByCode(pairingCode.trim())
            ?: throw IllegalArgumentException("Invalid or expired pairing code")

        val patient = userRepo.getUserByIdOnce(pendingPairing.patientId)
            ?: throw IllegalStateException("Patient profile could not be found")

        pendingPairing to patient
    }
}