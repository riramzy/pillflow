package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.dao.PairingDao
import com.riramzy.pillfllow.data.local.dao.UserDao
import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.data.remote.dto.PairingDto
import com.riramzy.pillfllow.data.remote.dto.UserDto
import com.riramzy.pillfllow.data.remote.dto.toDto
import com.riramzy.pillfllow.data.remote.dto.toEntity
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.utils.currentTimeMillis
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PairingRepoImpl(
    private val pairingDao: PairingDao,
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) : PairingRepo {
    override suspend fun insertPairing(pairing: CaregiverPatientPairingEntity) {
        pairingDao.insertPairing(pairing)
        runCatching {
            val dto = pairing.toDto(updatedAt = currentTimeMillis())
            firestore
                .collection("pairings")
                .document(pairing.pairingId)
                .set(dto)
        }
    }

    override fun getPairingsForCaregiver(caregiverId: String): Flow<List<CaregiverPatientPairingEntity>> {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                firestore
                    .collection("pairings")
                    .where { "caregiverId" equalTo caregiverId }
                    .where { "status" equalTo "ACTIVE" }
                    .snapshots
                    .collect { querySnapshot ->
                        querySnapshot.documents.forEach { doc ->
                            val entity = doc.data<PairingDto>().toEntity()
                            pairingDao.insertPairing(entity)

                            val patientDoc = firestore
                                .collection("users")
                                .document(entity.patientId)
                                .get()

                            if (patientDoc.exists) {
                                userDao.insertUser(patientDoc.data<UserDto>().toEntity())
                            }
                        }
                    }
            }
        }
        return pairingDao.getPairingsForCaregiver(caregiverId)
    }

    override suspend fun getPairingsForCaregiverOnce(caregiverId: String): List<CaregiverPatientPairingEntity> {
        runCatching {
            val querySnapshot = firestore
                .collection("pairings")
                .where { "caregiverId" equalTo caregiverId }
                .where { "status" equalTo "ACTIVE" }
                .get()

            querySnapshot.documents.forEach { doc ->
                val entity = doc.data<PairingDto>().toEntity()
                pairingDao.insertPairing(entity)
            }
        }
        return pairingDao.getPairingsForCaregiverOnce(caregiverId)
    }

    override fun getPairingsForPatient(patientId: String): Flow<List<CaregiverPatientPairingEntity>> {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                firestore
                    .collection("pairings")
                    .where { "patientId" equalTo patientId }
                    .snapshots
                    .collect { querySnapshot ->
                        querySnapshot.documents.forEach { doc ->
                            val entity = doc.data<PairingDto>().toEntity()
                            pairingDao.insertPairing(entity)
                        }
                    }
            }
        }
        return pairingDao.getPairingsForPatient(patientId)
    }

    override suspend fun getPairingsForPatientOnce(patientId: String): List<CaregiverPatientPairingEntity> {
        return pairingDao.getPairingsForPatientOnce(patientId)
    }

    override fun getPairingsByPairingCode(pairingCode: String): Flow<CaregiverPatientPairingEntity?> =
        pairingDao.getPairingsByPairingCode(pairingCode)

    override suspend fun getPendingPairingByCode(pairingCode: String): CaregiverPatientPairingEntity? {
        val remotePairing = runCatching {
            val querySnapshot = firestore
                .collection("pairings")
                .where { "pairingCode" equalTo pairingCode }
                .where { "status" equalTo "PENDING" }
                .get()

            querySnapshot.documents.firstOrNull()?.data<PairingDto>()?.toEntity()
        }.getOrNull()

        remotePairing?.let { pairing ->
            pairingDao.insertPairing(pairing)
            runCatching {
                val patientDoc = firestore
                    .collection("users")
                    .document(pairing.patientId)
                    .get()

                if (patientDoc.exists) {
                    val patientUser = patientDoc.data<UserDto>().toEntity()
                    userDao.insertUser(patientUser)
                }
            }
        }

        return remotePairing ?: pairingDao.getPendingPairingByCode(pairingCode)
    }

    override suspend fun getPairingsByPairingCodeOnce(pairingCode: String): CaregiverPatientPairingEntity? =
        pairingDao.getPairingsByPairingCodeOnce(pairingCode)

    override suspend fun updatePairingStatus(pairingId: String, status: String) {
        pairingDao.updatePairingStatus(pairingId, status)
        runCatching {
            firestore
                .collection("pairings")
                .document(pairingId)
                .update("status" to status, "updatedAt" to currentTimeMillis())
        }
    }

    override suspend fun deletePairing(pairing: CaregiverPatientPairingEntity) {
        pairingDao.deletePairing(pairing)
        runCatching {
            firestore
                .collection("pairings")
                .document(pairing.pairingId)
                .delete()
        }
    }

    override suspend fun deletePairingById(pairingId: String): Int {
        val count = pairingDao.deletePairingById(pairingId)
        runCatching {
            firestore
                .collection("pairings")
                .document(pairingId)
                .delete()
        }
        return count
    }

    override suspend fun deletePendingPairingsForPatient(patientId: String): Int {
        val count = pairingDao.deletePendingPairingsForPatient(patientId)
        runCatching {
            val querySnapshot = firestore
                .collection("pairings")
                .where { "patientId" equalTo patientId }
                .where { "status" equalTo "PENDING" }
                .get()
            querySnapshot.documents.forEach { doc ->
                doc.reference.delete()
            }
        }
        return count
    }
}