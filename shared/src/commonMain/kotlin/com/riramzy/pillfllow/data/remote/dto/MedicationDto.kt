package com.riramzy.pillfllow.data.remote.dto

import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import kotlinx.serialization.Serializable

@Serializable
data class MedicationDto(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val timeOfDay: String = "",
    val colorHex: String = "",
    val shape: String = "",
    val instructions: String = "",
    val scheduleTimesMillis: List<Long> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val remainingPills: Int? = null,
    val refillThreshold: Int = 5,
    val isPaused: Boolean = false,
    val isPrn: Boolean = false,
    val isArchived: Boolean = false
)

fun MedicationDto.toEntity(isSynced: Boolean = false): MedicationEntity = MedicationEntity(
    id = id,
    userId = userId,
    name = name,
    dosage = dosage,
    frequency = frequency,
    timeOfDay = timeOfDay,
    colorHex = colorHex,
    shape = shape,
    instructions = instructions,
    remainingPills = remainingPills,
    refillThreshold = refillThreshold,
    isPaused = isPaused,
    isPrn = isPrn,
    isSynced = isSynced,
    isArchived = isArchived
)

fun MedicationEntity.toDto(
    scheduleTimesMillis: List<Long> = emptyList(),
    createdAt: Long = 0L,
    updatedAt: Long = 0L
): MedicationDto = MedicationDto(
    id = id,
    userId = userId,
    name = name,
    dosage = dosage,
    frequency = frequency,
    timeOfDay = timeOfDay,
    colorHex = colorHex,
    shape = shape,
    instructions = instructions,
    scheduleTimesMillis = scheduleTimesMillis,
    createdAt = createdAt,
    updatedAt = updatedAt,
    remainingPills = remainingPills,
    refillThreshold = refillThreshold,
    isPaused = isPaused,
    isPrn = isPrn,
    isArchived = isArchived
)