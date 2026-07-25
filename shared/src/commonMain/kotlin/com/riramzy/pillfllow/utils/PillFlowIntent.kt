package com.riramzy.pillfllow.utils

import com.riramzy.pillfllow.data.local.entity.MedicationEntity

sealed interface PillFlowIntent {
    data class LogDose(val doseId: Long) : PillFlowIntent
    data class AddMedication(val medication: MedicationEntity, val scheduledTimesMillis: List<Long>) : PillFlowIntent
    data class UpdateTilt(val tiltX: Float, val tiltY: Float) : PillFlowIntent
    data object RefreshDashboard : PillFlowIntent
    data class SelectRole(val isCaregiver: Boolean) : PillFlowIntent
}