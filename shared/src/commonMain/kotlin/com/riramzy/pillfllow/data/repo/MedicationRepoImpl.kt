package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.dao.MedicationDao
import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.data.remote.dto.DoseLogDto
import com.riramzy.pillfllow.data.remote.dto.MedicationDto
import com.riramzy.pillfllow.data.remote.dto.ScheduledDoseDto
import com.riramzy.pillfllow.data.remote.dto.toDto
import com.riramzy.pillfllow.data.remote.dto.toEntity
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.currentTimeMillis
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MedicationRepoImpl(
    private val medicationDao: MedicationDao,
    private val platformNotifier: PlatformNotifier = PlatformNotifier(),
    private val firestore: FirebaseFirestore
) : MedicationRepo {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = medicationDao.getAllMedications()

    override suspend fun insertMedication(medication: MedicationEntity): Long {
        val localId = medicationDao.insertMedication(medication)
        runCatching {
            val dto = medication.copy(id = localId).toDto(createdAt = currentTimeMillis(), updatedAt = currentTimeMillis())
            firestore
                .collection("medications")
                .document(localId.toString())
                .set(dto)
        }
        return localId
    }

    override fun getMedicationForUser(userId: String): Flow<List<MedicationEntity>> {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                firestore
                    .collection("medications")
                    .where { "userId" equalTo userId }
                    .snapshots
                    .collect { querySnapshot ->
                        querySnapshot.documents.forEach { doc ->
                            val entity = doc.data<MedicationDto>().toEntity(isSynced = true)
                            medicationDao.insertMedication(entity)
                        }
                    }
            }
        }
        return medicationDao.getMedicationForUser(userId)
    }

    override fun getPendingDosesForUser(userId: String): Flow<List<PendingDoseWithMedication>> {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                firestore
                    .collection("scheduled_doses")
                    .where { "userId" equalTo userId }
                    .snapshots
                    .collect { querySnapshot ->
                        querySnapshot.documents.forEach { doc ->
                            val entity = doc.data<ScheduledDoseDto>().toEntity(isSynced = true)
                            medicationDao.insertScheduledDoses(listOf(entity))
                        }
                    }
            }
        }
        return medicationDao.getPendingDosesForUser(userId)
    }

    override fun getPendingDosesForPatients(patientIds: List<String>): Flow<List<PendingDoseWithMedication>> {
        if (patientIds.isEmpty()) return medicationDao.getPendingDosesForPatients(patientIds)

        CoroutineScope(Dispatchers.IO).launch {
            patientIds.forEach { patientId ->
                runCatching {
                    firestore
                        .collection("scheduled_doses")
                        .where { "userId" equalTo patientId }
                        .snapshots
                        .collect { querySnapshot ->
                            querySnapshot.documents.forEach { doc ->
                                val entity = doc.data<ScheduledDoseDto>().toEntity(isSynced = true)
                                medicationDao.insertScheduledDoses(listOf(entity))
                            }
                        }
                }
            }
        }
        return medicationDao.getPendingDosesForPatients(patientIds)
    }

    override suspend fun insertScheduledDoses(scheduledDose: List<ScheduledDoseEntity>): List<Long> {
        val ids = medicationDao.insertScheduledDoses(scheduledDose)
        val now = currentTimeMillis()

        scheduledDose.forEachIndexed { index, dose ->
            val doseId = ids.getOrNull(index) ?: dose.id
            if (!dose.isTaken && dose.scheduledTime > now) {
                platformNotifier.scheduleDoseReminder(
                    doseId = doseId.toString(),
                    pillName = "Medication",
                    triggerTimeMillis = dose.scheduledTime
                )
            }

            runCatching {
                val med = medicationDao.getAllMedicationsOnce().firstOrNull { it.id == dose.medicationId }
                val userId = med?.userId ?: ""
                val medName = med?.name ?: ""
                val dosage = med?.dosage ?: ""

                val dto = dose.copy(id = doseId).toDto(
                    userId = userId,
                    remoteDoseId = doseId.toString(),
                    remoteMedicationId = dose.medicationId.toString(),
                    medicationName = medName,
                    dosage = dosage,
                    updatedAt = now
                )
                firestore
                    .collection("scheduled_doses")
                    .document(doseId.toString())
                    .set(dto)
            }
        }
        return ids
    }

    override suspend fun markScheduledDoseTaken(
        id: Long,
        takenTime: Long,
        isTaken: Boolean,
        complianceStatus: String
    ) {
        medicationDao.markScheduledDoseTaken(id, takenTime, isTaken, complianceStatus)

        runCatching {
            firestore
                .collection("scheduled_doses")
                .document(id.toString())
                .update(
                    "isTaken" to isTaken,
                    "takenTime" to takenTime,
                    "complianceStatus" to complianceStatus,
                    "updatedAt" to currentTimeMillis()
            )

            val doseDoc = firestore
                .collection("scheduled_doses")
                .document(id.toString())
                .get()

            if (doseDoc.exists) {
                val doseDto = doseDoc.data<ScheduledDoseDto>()
                val logDto = DoseLogDto(
                    id = "log_${currentTimeMillis()}_${id}",
                    patientId = doseDto.userId,
                    patientName = "",
                    medicationName = doseDto.medicationName,
                    dosage = doseDto.dosage,
                    actionType = if (isTaken) "DOSE_TAKEN" else "DOSE_SKIPPED",
                    complianceStatus = complianceStatus,
                    scheduledTime = doseDto.scheduledTime,
                    timestamp = takenTime
                )
                firestore.collection("dose_logs").document(logDto.id).set(logDto)
            }
        }
    }

    override suspend fun deleteMedicationById(id: Long) {
        val pendingDoseIds = medicationDao.getPendingDoseIdsForMedication(id)
        pendingDoseIds.forEach { doseId ->
            platformNotifier.cancelReminder(doseId = doseId.toString())
            runCatching {
                firestore
                    .collection("scheduled_doses")
                    .document(doseId.toString())
                    .delete()
            }
        }
        medicationDao.deleteMedicationById(id)

        runCatching {
            firestore
                .collection("medications")
                .document(id.toString())
                .delete()
        }
    }

    override suspend fun getPendingDoseIdsForMedication(medicationId: Long): List<Long> =
        medicationDao.getPendingDoseIdsForMedication(medicationId)

    override suspend fun getUnsyncedMedications(): List<MedicationEntity> =
        medicationDao.getUnsyncedMedications()

    override suspend fun markMedicationSynced(id: Long) =
        medicationDao.markMedicationSynced(id)

    override fun getPendingDosesWithMedication(): Flow<List<PendingDoseWithMedication>> =
        medicationDao.getPendingDosesWithMedication()

    override fun getUnsyncedScheduledDoses(): Flow<List<ScheduledDoseEntity>> =
        medicationDao.getUnsyncedScheduledDoses()

    override suspend fun markScheduledDoseSynced(id: Long) =
        medicationDao.markScheduledDoseSynced(id)

    override fun getDoseHistoryForUser(userId: String): Flow<List<DoseHistoryEntity>> =
        medicationDao.getDoseHistoryForUser(userId)

    override suspend fun getDoseHistoryForUserOnce(userId: String): List<DoseHistoryEntity> =
        medicationDao.getDoseHistoryForUserOnce(userId)
}