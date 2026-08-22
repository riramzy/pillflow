package com.riramzy.pillfllow.data.local.entity

data class DoseHistoryEntity(
    val id: Long,
    val medicationId: Long,
    val name: String,
    val dosage: String,
    val colorHex: String,
    val shape: String,
    val scheduledTime: Long,
    val takenTime: Long?,
    val complianceStatus: String,
    val isTaken: Boolean
)