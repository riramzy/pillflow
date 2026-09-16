package com.riramzy.pillfllow.domain.usecase.caregiver

import com.riramzy.pillfllow.domain.hardware.PlatformNotifier

class NudgePatientUseCase(
    private val platformNotifier: PlatformNotifier = PlatformNotifier()
) {
    operator fun invoke(patientName: String) {
        platformNotifier.sendInstantNudge(
            title = "Caregiver Reminder",
            message = "Time to take your medication, $patientName!"
        )
    }
}