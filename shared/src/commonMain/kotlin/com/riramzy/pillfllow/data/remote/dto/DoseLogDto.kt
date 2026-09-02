package com.riramzy.pillfllow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DoseLogDto(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val medicationName: String = "",
    val dosage: String = "",
    val actionType: String = "DOSE_TAKEN",
    val complianceStatus: String = "ON_TIME",
    val scheduledTime: Long = 0L,
    val timestamp: Long = 0L,
    val notes: String = ""
)