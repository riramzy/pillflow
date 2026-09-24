package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.data.remote.dto.NudgeDto
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.randomUUID
import dev.gitlive.firebase.firestore.FirebaseFirestore

class NudgePatientUseCase(
    private val firestore: FirebaseFirestore
) {
    suspend operator fun invoke(
        patientId: String,
        caregiverId: String,
        caregiverName: String
    ) = safeCall {
        val nudge = NudgeDto(
            id = randomUUID(),
            patientId = patientId,
            caregiverId = caregiverId,
            caregiverName = caregiverName,
            timestamp = currentTimeMillis(),
            isHandled = false
        )

        firestore
            .collection("nudges")
            .document(nudge.id)
            .set(nudge)
    }
}