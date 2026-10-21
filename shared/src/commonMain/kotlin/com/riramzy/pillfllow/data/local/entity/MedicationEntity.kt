package com.riramzy.pillfllow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.riramzy.pillfllow.utils.platform.randomUUID

@Entity(
    tableName = "medications",
    indices = [Index("userId")]
)
data class MedicationEntity(
    @PrimaryKey val id: String = randomUUID(),
    val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val timeOfDay: String,
    val colorHex: String,
    val shape: String = "CAPSULE",
    val instructions: String = "",
    val remainingPills: Int? = null,
    val refillThreshold: Int = 5,
    val isPaused: Boolean = false,
    val isPrn: Boolean = false,
    val isSynced: Boolean = false,
    val isArchived: Boolean = false
)