package com.riramzy.pillfllow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pairings",
    indices = [
        Index("caregiverId"),
        Index("patientId"),
        Index("pairingCode", unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["caregiverId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CaregiverPatientPairingEntity (
    @PrimaryKey val pairingId: String,
    val caregiverId: String,
    val patientId: String,
    val relation: String,
    val pairingCode: String,
    val status: String,
    val createdAt: Long,
)