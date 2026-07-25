package com.riramzy.pillfllow.ui.viewmodel

import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.data.local.entity.PillEntity

data class PillFlowState(
    val isLoading: Boolean = true,
    val isCaregiver: Boolean = false,
    val pendingDoses: List<PendingDoseWithMedication> = emptyList(),
    val sandboxPills: List<PillEntity> = emptyList(),
    val medications: List<MedicationEntity> = emptyList(),
    val tiltX: Float = 0f,
    val tiltY: Float = 0.5f,
    val errorMessage: String? = null
)