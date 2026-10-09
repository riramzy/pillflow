package com.riramzy.pillfllow.domain.usecase.patient

import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.user.PairingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPatientPairingStatusUseCase(
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo
) {
    operator fun invoke(patientId: String): Flow<PairingStatus> {
        return pairingRepo.getPairingsForPatient(patientId).map { pairings ->
            val pending = pairings.firstOrNull { it.status == "PENDING" }
            val active = pairings.firstOrNull { it.status == "ACTIVE" }

            val caregiverUser = if (active != null) {
                userRepo.getUserByIdOnce(active.caregiverId)
            } else null

            val caregiverName = caregiverUser?.let { "${it.firstName} ${it.lastName}".trim() }
                ?: if (active != null) "Connected Caregiver" else null

            PairingStatus(
                pendingCode = pending?.pairingCode,
                hasActivePairing = active != null,
                activePairingId = active?.pairingId,
                caregiverId = active?.caregiverId,
                caregiverName = caregiverName,
                relation = active?.relation
            )
        }
    }
}