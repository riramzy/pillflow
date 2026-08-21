package com.riramzy.pillfllow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity (
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String = "+1234567890",
    val userType: String,
    val avatarRes: String,
    val createdAt: Long
)