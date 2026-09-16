package com.riramzy.pillfllow.domain.usecase.patient

import com.riramzy.pillfllow.domain.repo.PairingRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class PatientPairingStatus(
    val pendingCode: String? = null,
    val hasActivePairing: Boolean = false
)

class GetPatientPairingStatusUseCase(
    private val pairingRepo: PairingRepo
) {
    operator fun invoke(patientId: String): Flow<PatientPairingStatus> {
        return pairingRepo.getPairingsForPatient(patientId).map { pairings ->
            val pending = pairings.firstOrNull { it.status == "PENDING" }
            val hasActive = pairings.any { it.status == "ACTIVE" }

            PatientPairingStatus(
                pendingCode = pending?.pairingCode,
                hasActivePairing = hasActive
            )
        }
    }
}