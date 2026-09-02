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
    val updatedAt: Long = 0L
)

fun MedicationDto.toEntity(isSynced: Boolean = false): MedicationEntity = MedicationEntity(
    id = id.toLongOrNull() ?: 0L,
    userId = userId,
    name = name,
    dosage = dosage,
    frequency = frequency,
    timeOfDay = timeOfDay,
    colorHex = colorHex,
    shape = shape,
    instructions = instructions,
    isSynced = isSynced
)

fun MedicationEntity.toDto(
    scheduleTimesMillis: List<Long> = emptyList(),
    createdAt: Long = 0L,
    updatedAt: Long = 0L
): MedicationDto = MedicationDto(
    id = id.toString(),
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
    updatedAt = updatedAt
)