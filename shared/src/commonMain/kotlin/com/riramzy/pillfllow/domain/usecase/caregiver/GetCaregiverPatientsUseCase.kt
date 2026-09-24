package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.DrawableResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.avatar3
import pillfllow.shared.generated.resources.avatar4
import pillfllow.shared.generated.resources.avatar5
import pillfllow.shared.generated.resources.avatar6
import pillfllow.shared.generated.resources.avatar7
import pillfllow.shared.generated.resources.avatar8

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

                val missedCount = patientDoses.count { (now - it.scheduledTime) > DoseStateMachine.LATE_WINDOW_MILLIS }

                val lateCount = patientDoses.count {
                    val diff = now - it.scheduledTime
                    diff in (DoseStateMachine.ON_TIME_WINDOW_MILLIS + 1)..DoseStateMachine.LATE_WINDOW_MILLIS
                }

                val patientStatus = when {
                    missedCount > 0 -> ComplianceStatus.MISSED
                    lateCount > 0 -> ComplianceStatus.LATE
                    else -> ComplianceStatus.ON_TIME
                }

                val patientScore = if (patientDoses.isEmpty()) {
                    100
                } else {
                    (100 - ((missedCount * 100) / patientDoses.size))
                }

                PairedPatientUiModel(
                    id = patientId,
                    pairingId = pairing?.pairingId ?: "",
                    name = patientName,
                    relation = relation,
                    phoneNumber = pairing?.phoneNumber?.ifBlank { null } ?: patient?.phoneNumber ?: "",
                    avatar = mapAvatar(patient?.avatarRes),
                    status = patientStatus,
                    lateDosesCount = lateCount,
                    missedDosesCount = missedCount,
                    compliancePercentage = patientScore
                )
            }
        }
    }
    private fun mapAvatar(avatarRes: String?): DrawableResource = when (avatarRes) {
        "avatar1" -> Res.drawable.avatar1
        "avatar2" -> Res.drawable.avatar2
        "avatar3" -> Res.drawable.avatar3
        "avatar4" -> Res.drawable.avatar4
        "avatar5" -> Res.drawable.avatar5
        "avatar6" -> Res.drawable.avatar6
        "avatar7" -> Res.drawable.avatar7
        else -> Res.drawable.avatar8
    }
}