package com.riramzy.pillfllow.domain.repo

import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import kotlinx.coroutines.flow.Flow

interface PairingRepo {
    suspend fun insertPairing(pairing: CaregiverPatientPairingEntity)
    fun getPairingsForCaregiver(caregiverId: String): Flow<List<CaregiverPatientPairingEntity>>
    suspend fun getPairingsForCaregiverOnce(caregiverId: String): List<CaregiverPatientPairingEntity>
    fun getPairingsByPairingCode(pairingCode: String): Flow<CaregiverPatientPairingEntity?>
    suspend fun getPairingsByPairingCodeOnce(pairingCode: String): CaregiverPatientPairingEntity?
    fun getPairingsForPatient(patientId: String): Flow<List<CaregiverPatientPairingEntity>>
    suspend fun getPairingsForPatientOnce(patientId: String): List<CaregiverPatientPairingEntity>
    suspend fun updatePairingStatus(pairingId: String, status: String)
    suspend fun deletePairing(pairing: CaregiverPatientPairingEntity)
}