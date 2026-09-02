package com.riramzy.pillfllow.data.remote.dto

import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import kotlinx.serialization.Serializable

@Serializable
data class PairingDto(
    val pairingId: String = "",
    val caregiverId: String = "",
    val patientId: String = "",
    val phoneNumber: String = "",
    val relation: String = "Patient",
    val pairingCode: String = "",
    val status: String = "PENDING",
    val createdAt: Long = 0L,
    val updatedAt: Long? = null
)

fun PairingDto.toEntity(): CaregiverPatientPairingEntity = CaregiverPatientPairingEntity(
    pairingId = pairingId,
    caregiverId = caregiverId,
    patientId = patientId,
    phoneNumber = phoneNumber,
    relation = relation,
    pairingCode = pairingCode,
    status = status,
    createdAt = createdAt
)

fun CaregiverPatientPairingEntity.toDto(updatedAt: Long? = null): PairingDto = PairingDto(
    pairingId = pairingId,
    caregiverId = caregiverId,
    patientId = patientId,
    phoneNumber = phoneNumber,
    relation = relation,
    pairingCode = pairingCode,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)