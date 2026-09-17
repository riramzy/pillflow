package com.riramzy.pillfllow.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pairings",
    indices = [
        Index("caregiverId"),
        Index("patientId"),
        Index("pairingCode")
    ]
)
data class CaregiverPatientPairingEntity (
    @PrimaryKey val pairingId: String,
    val caregiverId: String,
    val patientId: String,
    val phoneNumber: String = "",
    val relation: String,
    val pairingCode: String,
    val status: String,
    val createdAt: Long,
)