package com.riramzy.pillfllow.data.local.entity

import androidx.room.Embedded

data class PendingDoseWithMedication(
    @Embedded val dose : ScheduledDoseEntity,
    @Embedded val medication : MedicationEntity
)