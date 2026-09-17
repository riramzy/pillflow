package com.riramzy.pillfllow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.riramzy.pillfllow.utils.randomUUID

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
    val isSynced: Boolean = false
)