package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis

class InitiatePairingUseCase(
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(pairingCode: String): Result<Pair<CaregiverPatientPairingEntity, UserEntity>> = safeCall {
        val cleanCode = pairingCode.trim()

        if (cleanCode.length != 6) {
            throw IllegalArgumentException("Please enter a valid 6-digit code.")
        }

        val pairing = pairingRepo.getPairingByCode(cleanCode)
            ?: throw IllegalArgumentException("Pairing code not found. Please check the 6 digits and try again.")

        if (pairing.status == "ACTIVE") {
            throw IllegalArgumentException("This pairing code has already been linked to a caregiver.")
        }

        if (pairing.expiresAt > 0L && currentTimeMillis() > pairing.expiresAt) {
            throw IllegalArgumentException("This pairing code has expired. Please ask the patient to generate a new code.")
        }

        val patient = userRepo.getUserByIdOnce(pairing.patientId)
            ?: throw IllegalStateException("Patient profile could not be found.")

        pairing to patient
    }
}