package com.riramzy.pillfllow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scheduled_doses",
    indices = [
        Index("medicationId"),
        Index(value = ["medicationId", "scheduledTime"], unique = true)
    ]
)
data class ScheduledDoseEntity(
    @PrimaryKey val id: String,
    val medicationId: String,
    val takenTime: Long? = null,
    val scheduledTime: Long,
    val complianceStatus: String = "PENDING",
    val isTaken: Boolean = false,
    val isSynced: Boolean = false
)