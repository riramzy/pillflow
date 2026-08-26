package com.riramzy.pillfllow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    indices = [Index("userId")],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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