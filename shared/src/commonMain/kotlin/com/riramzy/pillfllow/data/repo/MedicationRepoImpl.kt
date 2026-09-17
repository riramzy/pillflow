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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MedicationRepoImpl(
    private val medicationDao: MedicationDao,
    private val platformNotifier: PlatformNotifier = PlatformNotifier(),
    private val firestore: FirebaseFirestore
) : MedicationRepo {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = medicationDao.getAllMedications()

    override suspend fun insertMedication(medication: MedicationEntity): String {
        medicationDao.insertMedication(medication)
        val now = currentTimeMillis()
        runCatching {
            val dto = medication.toDto(createdAt = now, updatedAt = now)
            firestore
                .collection("medications")
                .document(medication.id)
                .set(dto)
            medicationDao.markMedicationSynced(medication.id)
        }
        return medication.id
    }

    override fun getMedicationForUser(userId: String): Flow<List<MedicationEntity>> = channelFlow {
        launch(Dispatchers.IO) {
            runCatching {
                firestore
                    .collection("medications")
                    .where { "userId" equalTo userId }
                    .snapshots
                    .collect { querySnapshot ->
                        val remoteEntities = querySnapshot.documents.map { doc ->
                            doc.data<MedicationDto>().toEntity(isSynced = true)
                        }
                        val remoteIds = remoteEntities.map { it.id }.toSet()
                        // Bidirectional Reconciler: find local synced items missing from remote
                        val localMeds = medicationDao.getAllMedicationsOnce().filter { it.userId == userId }
                        val orphanedMeds = localMeds.filter { it.id !in remoteIds && it.isSynced }
                        orphanedMeds.forEach { med ->
                            medicationDao.deleteScheduledDosesByMedicationId(med.id)
                            medicationDao.deleteMedicationById(med.id)
                        }
                        if (remoteEntities.isNotEmpty()) {
                            medicationDao.insertMedications(remoteEntities)
                        }
                    }
            }
        }
        medicationDao.getMedicationForUser(userId).distinctUntilChanged().collect { send(it) }
    }

    override fun getPendingDosesForUser(userId: String): Flow<List<PendingDoseWithMedication>> = channelFlow {
        launch(Dispatchers.IO) {
            runCatching {
                firestore
                    .collection("scheduled_doses")
                    .where { "userId" equalTo userId }
                    .snapshots
                    .collect { querySnapshot ->
                        val doses = querySnapshot.documents.map { doc ->
                            doc.data<ScheduledDoseDto>().toEntity(isSynced = true)
                        }
                        if (doses.isNotEmpty()) {
                            medicationDao.insertScheduledDoses(doses)
                        }
                    }
            }
        }
        medicationDao.getPendingDosesForUser(userId).distinctUntilChanged().collect { send(it) }
    }

    override fun getPendingDosesForPatients(patientIds: List<String>): Flow<List<PendingDoseWithMedication>> = channelFlow {
        if (patientIds.isEmpty()) {
            medicationDao.getPendingDosesForPatients(patientIds).distinctUntilChanged().collect { send(it) }
            return@channelFlow
        }

        patientIds.forEach { patientId ->
            launch(Dispatchers.IO) {
                runCatching {
                    firestore
                        .collection("scheduled_doses")
                        .where { "userId" equalTo patientId }
                        .snapshots
                        .collect { querySnapshot ->
                            val doses = querySnapshot.documents.map { doc ->
                                doc.data<ScheduledDoseDto>().toEntity(isSynced = true)
                            }
                            if (doses.isNotEmpty()) {
                                medicationDao.insertScheduledDoses(doses)
                            }
                        }
                }
            }
        }

        medicationDao.getPendingDosesForPatients(patientIds).collect { send(it) }
    }

    override suspend fun insertScheduledDoses(scheduledDose: List<ScheduledDoseEntity>): List<String> {
        medicationDao.insertScheduledDoses(scheduledDose)
        val now = currentTimeMillis()

        scheduledDose.forEach { dose ->
            val med = medicationDao.getAllMedicationsOnce().firstOrNull { it.id == dose.medicationId }
            val medName = med?.name ?: "Medication"

            if (!dose.isTaken && dose.scheduledTime > now) {
                val reminderTime = (dose.scheduledTime - 30 * 60 * 1000L).coerceAtLeast(now + 1000L)

                platformNotifier.scheduleDoseReminder(
                    doseId = dose.id,
                    pillName = medName,
                    triggerTimeMillis = reminderTime
                )
            }

            runCatching {
                val med = medicationDao.getAllMedicationsOnce().firstOrNull { it.id == dose.medicationId }
                val dto = dose.toDto(
                    userId = med?.userId ?: "",
                    medicationName = med?.name ?: "",
                    dosage = med?.dosage ?: "",
                    updatedAt = now
                )
                firestore
                    .collection("scheduled_doses")
                    .document(dose.id)
                    .set(dto)
                medicationDao.markScheduledDoseSynced(dose.id)
            }
        }
        return scheduledDose.map { it.id }
    }

    override suspend fun markScheduledDoseTaken(
        id: String,
        takenTime: Long,
        isTaken: Boolean,
        complianceStatus: String
    ) {
        medicationDao.markScheduledDoseTaken(id, takenTime, isTaken, complianceStatus)

        if (isTaken) {
            platformNotifier.cancelReminder(doseId = id)
        }

        runCatching {
            firestore
                .collection("scheduled_doses")
                .document(id)
                .update(
                    "isTaken" to isTaken,
                    "takenTime" to takenTime,
                    "complianceStatus" to complianceStatus,
                    "updatedAt" to currentTimeMillis()
            )

            val doseDoc = firestore
                .collection("scheduled_doses")
                .document(id)
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

    override suspend fun deleteMedicationById(id: String) {
        val pendingDoseIds = medicationDao.getPendingDoseIdsForMedication(id)
        pendingDoseIds.forEach { doseId ->
            platformNotifier.cancelReminder(doseId = doseId)
            runCatching {
                firestore.collection("scheduled_doses").document(doseId).delete()
            }
        }
        medicationDao.deleteScheduledDosesByMedicationId(id)
        medicationDao.deleteMedicationById(id)

        runCatching {
            firestore.collection("medications").document(id).delete()
        }
    }

    override suspend fun getPendingDoseIdsForMedication(medicationId: String): List<String> =
        medicationDao.getPendingDoseIdsForMedication(medicationId)

    override suspend fun getUnsyncedMedications(): List<MedicationEntity> =
        medicationDao.getUnsyncedMedications()

    override suspend fun markMedicationSynced(id: String) =
        medicationDao.markMedicationSynced(id)

    override fun getPendingDosesWithMedication(): Flow<List<PendingDoseWithMedication>> =
        medicationDao.getPendingDosesWithMedication()

    override fun getUnsyncedScheduledDoses(): Flow<List<ScheduledDoseEntity>> =
        medicationDao.getUnsyncedScheduledDoses()

    override suspend fun markScheduledDoseSynced(id: String) =
        medicationDao.markScheduledDoseSynced(id)

    override fun getDoseHistoryForUser(userId: String): Flow<List<DoseHistoryEntity>> =
        medicationDao.getDoseHistoryForUser(userId).distinctUntilChanged()

    override suspend fun getDoseHistoryForUserOnce(userId: String): List<DoseHistoryEntity> =
        medicationDao.getDoseHistoryForUserOnce(userId)
}