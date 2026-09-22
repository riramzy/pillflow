package com.riramzy.pillfllow.domain.hardware

import com.riramzy.pillfllow.utils.medication.DoseReminderStage

expect class PlatformNotifier() {
    fun scheduleDoseReminder(
        context: Any? = null,
        doseId: String,
        pillName: String,
        triggerTimeMillis: Long,
        stage: DoseReminderStage = DoseReminderStage.ADVANCE_30MIN
    )

    fun scheduleCaregiverEscalation(
        context: Any? = null,
        doseId: String,
        pillName: String,
        patientName: String,
        triggerTimeMillis: Long
    )

    fun cancelReminder(context: Any? = null, doseId: String)

    fun cancelAllReminders(context: Any? = null)

    fun sendInstantNudge(context: Any? = null, title: String, message: String)
}