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

            val caregiverRelation = when (active?.relation?.trim()?.lowercase()) {
                "mom", "mother" -> "Son"
                "dad", "father" -> "Son"
                "son" -> "Parent"
                "daughter" -> "Parent"
                "grandma", "grandmother" -> "Grandson"
                "grandpa", "grandfather" -> "Grandson"
                "patient", "", null -> if (active != null) "Caregiver" else null
                else -> active.relation.ifBlank { "Caregiver" }
            }

            PairingStatus(
                pendingCode = pending?.pairingCode,
                hasActivePairing = active != null,
                activePairingId = active?.pairingId,
                caregiverId = active?.caregiverId,
                caregiverName = caregiverName,
                relation = caregiverRelation
            )
        }
    }
}