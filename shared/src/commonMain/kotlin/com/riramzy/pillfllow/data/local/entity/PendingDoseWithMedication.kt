package com.riramzy.pillfllow.data.local.entity

data class PendingDoseWithMedication(
    val id: Long,
    val name: String,
    val dosage: String,
    val colorHex: String,
    val scheduledTime: Long
)