package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.dao.PairingDao
import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.domain.repo.PairingRepo
import kotlinx.coroutines.flow.Flow

class PairingRepoImpl(
    private val pairingDao: PairingDao
): PairingRepo {
    override suspend fun insertPairing(pairing: CaregiverPatientPairingEntity) = pairingDao.insertPairing(pairing)
    override fun getPairingsForCaregiver(caregiverId: String): Flow<List<CaregiverPatientPairingEntity>> = pairingDao.getPairingsForCaregiver(caregiverId)
    override fun getPairingsForPatient(patientId: String): Flow<List<CaregiverPatientPairingEntity>> = pairingDao.getPairingsForPatient(patientId)
    override suspend fun updatePairingStatus(pairingId: String, status: String) = pairingDao.updatePairingStatus(pairingId, status)
    override suspend fun deletePairing(pairing: CaregiverPatientPairingEntity) = pairingDao.deletePairing(pairing)
    override suspend fun getPairingsForCaregiverOnce(caregiverId: String): List<CaregiverPatientPairingEntity> = pairingDao.getPairingsForCaregiverOnce(caregiverId)
    override fun getPairingsByPairingCode(pairingCode: String): Flow<CaregiverPatientPairingEntity?> = pairingDao.getPairingsByPairingCode(pairingCode)
    override suspend fun deletePendingPairingsForPatient(patientId: String): Int = pairingDao.deletePendingPairingsForPatient(patientId)
    override suspend fun getPairingsByPairingCodeOnce(pairingCode: String): CaregiverPatientPairingEntity? = pairingDao.getPairingsByPairingCodeOnce(pairingCode)
    override suspend fun getPairingsForPatientOnce(patientId: String): List<CaregiverPatientPairingEntity> = pairingDao.getPairingsForPatientOnce(patientId)
    override suspend fun deletePairingById(pairingId: String): Int = pairingDao.deletePairingById(pairingId)
    override suspend fun getPendingPairingByCode(pairingCode: String): CaregiverPatientPairingEntity? = pairingDao.getPendingPairingByCode(pairingCode)
}