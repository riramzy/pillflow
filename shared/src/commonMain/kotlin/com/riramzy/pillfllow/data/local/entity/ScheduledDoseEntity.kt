package com.riramzy.pillfllow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.riramzy.pillfllow.utils.randomUUID

@Entity(
    tableName = "scheduled_doses",
    indices = [Index("medicationId")]
)
data class ScheduledDoseEntity(
    @PrimaryKey val id: String = randomUUID(),
    val medicationId: String,
    val takenTime: Long? = null,
    val scheduledTime: Long,
    val complianceStatus: String = "PENDING",
    val isTaken: Boolean = false,
    val isSynced: Boolean = false
)