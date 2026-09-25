package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.utils.app.AvatarMapper
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class GetCaregiverPatientsUseCase(
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo,
    private val medicationRepo: MedicationRepo
) {
    operator fun invoke(caregiverId: String): Flow<List<PairedPatientUiModel>> {
        return pairingRepo.getPairingsForCaregiver(caregiverId).map { pairings ->
            val now = currentTimeMillis()

            val caregiver = userRepo.getUserByIdOnce(caregiverId)
            val activePairings = pairings.filter { it.status == "ACTIVE" }
            val activePatientIds = activePairings.map { it.patientId }
            val allPatientIds = ((caregiver?.pairedPatientIds ?: emptyList()) + activePatientIds).distinct()

            allPatientIds.map { patientId ->
                val pairing = pairings.find { it.patientId == patientId }
                val patient = userRepo.getUserByIdOnce(patientId)
                val relation = pairing?.relation?.ifBlank { "Patient" } ?: "Patient"
                val patientName = patient?.firstName?.ifBlank { null } ?: relation
                val patientDoses = medicationRepo.getPendingDosesForUser(patientId).firstOrNull() ?: emptyList()

                val summary = DoseComplianceEvaluator.evaluatePatientSummary(patientDoses, now)

                PairedPatientUiModel(
                    id = patientId,
                    pairingId = pairing?.pairingId ?: "",
                    name = patientName,
                    relation = relation,
                    phoneNumber = pairing?.phoneNumber?.ifBlank { null } ?: patient?.phoneNumber ?: "",
                    avatar = AvatarMapper.fromRaw(patient?.avatarRes),
                    status = summary.status,
                    lateDosesCount = summary.lateDosesCount,
                    missedDosesCount = summary.missedDosesCount,
                    compliancePercentage = summary.compliancePercentage
                )
            }
        }
    }
}