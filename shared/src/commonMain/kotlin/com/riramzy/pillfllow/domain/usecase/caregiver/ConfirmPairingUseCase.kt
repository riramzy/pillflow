package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis

class ConfirmPairingUseCase(
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(
        caregiverId: String,
        patient: UserEntity,
        relation: String,
        pairingCode: String
    ): Result<CaregiverPatientPairingEntity> = safeCall {
        pairingRepo.deletePendingPairingsForPatient(patient.id)

        val activePairing = CaregiverPatientPairingEntity(
            pairingId = "pair_${currentTimeMillis()}",
            caregiverId = caregiverId,
            patientId = patient.id,
            phoneNumber = patient.phoneNumber,
            relation = relation.ifBlank { "Patient" },
            pairingCode = pairingCode,
            status = "ACTIVE",
            createdAt = currentTimeMillis()
        )

        userRepo.linkCaregiverAndPatient(caregiverId, patient.id)
        pairingRepo.insertPairing(activePairing)
        activePairing
    }
}