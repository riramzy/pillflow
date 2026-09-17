package com.riramzy.pillfllow.data.remote.dto

import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledDoseDto(
    val id: String = "",
    val userId: String = "",
    val medicationId: String = "",
    val medicationName: String = "",
    val dosage: String = "",
    val scheduledTime: Long = 0L,
    val takenTime: Long? = null,
    val complianceStatus: String = "PENDING",
    val isTaken: Boolean = false,
    val updatedAt: Long = 0L
)

fun ScheduledDoseDto.toEntity(
    localId: String = id,
    localMedicationId: String = medicationId,
    isSynced: Boolean = true
): ScheduledDoseEntity = ScheduledDoseEntity(
    id = localId,
    medicationId = localMedicationId,
    takenTime = takenTime,
    scheduledTime = scheduledTime,
    complianceStatus = complianceStatus,
    isTaken = isTaken,
    isSynced = isSynced
)

fun ScheduledDoseEntity.toDto(
    userId: String,
    remoteDoseId: String = id,
    remoteMedicationId: String = medicationId,
    medicationName: String = "",
    dosage: String = "",
    updatedAt: Long = 0L
): ScheduledDoseDto = ScheduledDoseDto(
    id = remoteDoseId,
    userId = userId,
    medicationId = remoteMedicationId,
    medicationName = medicationName,
    dosage = dosage,
    scheduledTime = scheduledTime,
    takenTime = takenTime,
    complianceStatus = complianceStatus,
    isTaken = isTaken,
    updatedAt = updatedAt
)