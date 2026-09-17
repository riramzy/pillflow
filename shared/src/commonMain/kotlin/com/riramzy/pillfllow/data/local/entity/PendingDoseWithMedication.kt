package com.riramzy.pillfllow.data.local.entity

data class PendingDoseWithMedication(
    val id: String,
    val name: String,
    val dosage: String,
    val colorHex: String,
    val shape: String = "CAPSULE",
    val scheduledTime: Long
)