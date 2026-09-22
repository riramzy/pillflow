package com.riramzy.pillfllow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NudgeDto(
    val id: String = "",
    val patientId: String = "",
    val caregiverId: String = "",
    val caregiverName: String = "",
    val timestamp: Long = 0L,
    val isHandled: Boolean = false
)