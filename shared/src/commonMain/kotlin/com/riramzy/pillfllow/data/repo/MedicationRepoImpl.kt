package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.dao.MedicationDao
import com.riramzy.pillfllow.data.local.dao.PairingDao
import com.riramzy.pillfllow.data.local.dao.UserDao
import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.data.remote.dto.DoseLogDto
import com.riramzy.pillfllow.data.remote.dto.MedicationDto
import com.riramzy.pillfllow.data.remote.dto.ScheduledDoseDto
import com.riramzy.pillfllow.data.remote.dto.toDto
import com.riramzy.pillfllow.data.remote.dto.toEntity
import com.riramzy.pillfllow.domain.compliance.DoseStateMachine
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.medication.DoseReminderStage
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MedicationRepoImpl(
    private val medicationDao: MedicationDao,
    private val userDao: UserDao? = null,
    private val pairingDao: PairingDao? = null,
    private val platformNotifier: PlatformNotifier = PlatformNotifier(),
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager
): MedicationRepo {
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
        syncMedicationsForUser(userId)
        syncScheduledDosesForUser(userId)
        medicationDao.getPendingDosesForUser(userId).distinctUntilChanged().collect { send(it) }
    }

    override fun getPendingDosesForPatients(patientIds: List<String>): Flow<List<PendingDoseWithMedication>> = channelFlow {
        if (patientIds.isEmpty()) {
            medicationDao.getPendingDosesForPatients(patientIds).distinctUntilChanged().collect { send(it) }
            return@channelFlow
        }

        patientIds.forEach { patientId ->
            syncMedicationsForUser(patientId)
            syncScheduledDosesForUser(patientId)
        }

        val users = userDao?.getAllUsersOnce()?.associateBy { it.id } ?: emptyMap()

        val caregiverId = sessionManager.currentUser.value?.id

        val pairings = if (caregiverId != null) {
            pairingDao?.getPairingsForCaregiverOnce(caregiverId)?.associateBy { it.patientId } ?: emptyMap()
        } else emptyMap()

        medicationDao.getPendingDosesForPatients(patientIds).distinctUntilChanged().collect { doses ->
            val now = currentTimeMillis()

            doses.forEach { dose ->
                val escalationTime = dose.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS + 60_000L

                if (escalationTime > now) {
                    val patientName = pairings[dose.userId]?.relation?.ifBlank { null }
                        ?: users[dose.userId]?.firstName?.ifBlank { null }
                        ?: "Your patient"

                    platformNotifier.scheduleCaregiverEscalation(
                        doseId = dose.id,
                        pillName = dose.name,
                        patientName = patientName,
                        triggerTimeMillis = escalationTime
                    )
                }
            }

            send(doses)
        }
    }

    override suspend fun insertScheduledDoses(scheduledDose: List<ScheduledDoseEntity>): List<String> {
        medicationDao.insertScheduledDoses(scheduledDose)
        val now = currentTimeMillis()

        scheduledDose.forEach { dose ->
            val med = medicationDao.getAllMedicationsOnce().firstOrNull { it.id == dose.medicationId }
            val medName = med?.name ?: "Medication"

            val currentUserId = sessionManager.currentUser.value?.id

            if (med?.userId == currentUserId && !dose.isTaken) {
                if (dose.scheduledTime > now) {
                    val tMinus30 = (dose.scheduledTime - 30 * 60 * 1000L).coerceAtLeast(now + 1000L)

                    platformNotifier.scheduleDoseReminder(
                        doseId = dose.id,
                        pillName = medName,
                        triggerTimeMillis = tMinus30,
                        stage = DoseReminderStage.ADVANCE_30MIN
                    )

                    platformNotifier.scheduleDoseReminder(
                        doseId = dose.id,
                        pillName = medName,
                        triggerTimeMillis = dose.scheduledTime,
                        stage = DoseReminderStage.DUE_NOW
                    )
                }

                val tPlus15 = dose.scheduledTime + 15 * 60 * 1000L

                if (tPlus15 > now) {
                    platformNotifier.scheduleDoseReminder(
                        doseId = dose.id,
                        pillName = medName,
                        triggerTimeMillis = tPlus15,
                        stage = DoseReminderStage.PRE_EXPIRY_15MIN
                    )
                }
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

    override suspend fun getScheduledDoseById(id: String): ScheduledDoseEntity? =
        medicationDao.getScheduledDoseById(id)

    override fun getUnsyncedScheduledDoses(): Flow<List<ScheduledDoseEntity>> =
        medicationDao.getUnsyncedScheduledDoses()

    override suspend fun markScheduledDoseSynced(id: String) =
        medicationDao.markScheduledDoseSynced(id)

    override fun getDoseHistoryForUser(userId: String): Flow<List<DoseHistoryEntity>> = channelFlow {
        syncMedicationsForUser(userId)
        syncScheduledDosesForUser(userId)

        medicationDao.getDoseHistoryForUser(userId)
            .distinctUntilChanged()
            .collect { send(it) }
    }

    override suspend fun getDoseHistoryForUserOnce(userId: String): List<DoseHistoryEntity> =
        medicationDao.getDoseHistoryForUserOnce(userId)

    override suspend fun deletePendingDosesForMedication(medicationId: String) {
        val pendingIds = medicationDao.getPendingDoseIdsForMedication(medicationId)
        pendingIds.forEach { platformNotifier.cancelReminder(doseId = it) }

        medicationDao.deletePendingDosesByMedicationId(medicationId)

        runCatching {
            val snapshot = firestore.collection("scheduled_doses")
                .where { "medicationId" equalTo medicationId }
                .get()

            snapshot.documents.forEach { doc ->
                val isTaken = doc.get<Boolean?>("isTaken") ?: false

                if (!isTaken) {
                    platformNotifier.cancelReminder(doseId = doc.id)
                    firestore.collection("scheduled_doses").document(doc.id).delete()
                }
            }
        }
    }

    private fun CoroutineScope.syncMedicationsForUser(userId: String) {
        launch(Dispatchers.IO) {
            runCatching {
                firestore
                    .collection("medications")
                    .where { "userId" equalTo userId }
                    .snapshots
                    .collect { querySnapshot ->
                        val remote = querySnapshot.documents.map {
                            it.data<MedicationDto>().toEntity(isSynced = true)
                        }

                        val remoteIds = remote.map { it.id }.toSet()

                        val local = medicationDao.getAllMedicationsOnce().filter { it.userId == userId }

                        local.filter { it.id !in remoteIds && it.isSynced }.forEach {
                            medicationDao.deleteScheduledDosesByMedicationId(it.id)
                            medicationDao.deleteMedicationById(it.id)
                        }

                        if (remote.isNotEmpty()) medicationDao.insertMedications(remote)
                    }
            }
        }
    }

    private fun CoroutineScope.syncScheduledDosesForUser(userId: String) {
        launch(Dispatchers.IO) {
            runCatching {
                firestore
                    .collection("scheduled_doses")
                    .where { "userId" equalTo userId }
                    .snapshots
                    .collect { querySnapshot ->
                        val doses = querySnapshot.documents.map {
                            it.data<ScheduledDoseDto>().toEntity(isSynced = true)
                        }

                        val remoteIds = doses.map { it.id }.toSet()
                        val localSyncedIds = medicationDao.getSyncedScheduledDoseIdsForUser(userId)

                        val orphanedIds = localSyncedIds.filter { it !in remoteIds }
                        if (orphanedIds.isNotEmpty()) {
                            orphanedIds.forEach { platformNotifier.cancelReminder(doseId = it) }
                            medicationDao.deleteScheduledDosesByIds(orphanedIds)
                        }

                        if (doses.isNotEmpty()) {
                            val remoteEntities = querySnapshot.documents.map { it.data<ScheduledDoseDto>().toEntity(isSynced = true) }

                            if (remoteEntities.isNotEmpty()) {
                                medicationDao.insertScheduledDoses(remoteEntities)
                            }

                            val currentUserId = sessionManager.currentUser.value?.id

                            if (userId == currentUserId) {
                                val now = currentTimeMillis()

                                doses.filter { !it.isTaken }.forEach { dose ->
                                    val med = medicationDao.getAllMedicationsOnce().firstOrNull { it.id == dose.medicationId }
                                    val medName = med?.name ?: "Medication"

                                    if (dose.scheduledTime > now) {
                                        val tMinus30 = (dose.scheduledTime - 30 * 60 * 1000L).coerceAtLeast(now + 1000L)

                                        platformNotifier.scheduleDoseReminder(
                                            doseId = dose.id,
                                            pillName = medName,
                                            triggerTimeMillis = tMinus30,
                                            stage = DoseReminderStage.ADVANCE_30MIN
                                        )

                                        platformNotifier.scheduleDoseReminder(
                                            doseId = dose.id,
                                            pillName = medName,
                                            triggerTimeMillis = dose.scheduledTime,
                                            stage = DoseReminderStage.DUE_NOW
                                        )
                                    }

                                    val tPlus15 = dose.scheduledTime + 15 * 60 * 1000L

                                    if (tPlus15 > now) {
                                        platformNotifier.scheduleDoseReminder(
                                            doseId = dose.id,
                                            pillName = medName,
                                            triggerTimeMillis = tPlus15,
                                            stage = DoseReminderStage.PRE_EXPIRY_15MIN
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}