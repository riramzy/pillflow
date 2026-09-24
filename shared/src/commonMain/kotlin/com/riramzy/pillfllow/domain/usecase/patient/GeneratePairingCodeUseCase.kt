package com.riramzy.pillfllow.domain.usecase.patient

import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class GeneratePairingCodeUseCase(
    private val pairingRepo: PairingRepo
) {
    private val mutex = Mutex()

    suspend operator fun invoke(user: UserEntity): Result<String> = mutex.withLock {
        safeCall {
            val newCode = Random.nextInt(100000, 999999).toString()
            pairingRepo.deletePendingPairingsForPatient(user.id)

            val pairing = CaregiverPatientPairingEntity(
                pairingId = "pair_${currentTimeMillis()}",
                caregiverId = user.id,
                patientId = user.id,
                phoneNumber = user.phoneNumber,
                relation = "Patient",
                pairingCode = newCode,
                status = "PENDING",
                createdAt = currentTimeMillis()
            )

            pairingRepo.insertPairing(pairing)
            newCode
        }
    }
}