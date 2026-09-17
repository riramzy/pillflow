package com.riramzy.pillfllow.data.remote.dto

import com.riramzy.pillfllow.data.local.entity.UserEntity
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "+1234567890",
    val userType: String = "PATIENT",
    val avatarRes: String = "avatar1",
    val fcmToken: String? = null,
    val pairedPatientIds: List<String> = emptyList(),
    val pairedCaregiverId: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phoneNumber = phoneNumber,
    userType = userType,
    avatarRes = avatarRes,
    pairedPatientIdsString = pairedPatientIds.joinToString(","),
    pairedCaregiverId = pairedCaregiverId,
    createdAt = createdAt
)

fun UserEntity.toDto(
    fcmToken: String? = null,
    updatedAt: Long = 0L
): UserDto = UserDto(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phoneNumber = phoneNumber,
    userType = userType,
    avatarRes = avatarRes,
    fcmToken = fcmToken,
    pairedPatientIds = pairedPatientIds,
    pairedCaregiverId = pairedCaregiverId,
    createdAt = createdAt,
    updatedAt = updatedAt
)