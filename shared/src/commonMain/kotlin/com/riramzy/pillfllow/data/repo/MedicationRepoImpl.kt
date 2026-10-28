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

    override suspend fun getMedicationByIdOnce(id: String): MedicationEntity? =
        medicationDao.getMedicationByIdOnce(id)

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

    override suspend fun logPrnDose(dose: ScheduledDoseEntity) {
        val existingDose = medicationDao.getScheduledDoseById(dose.id)
        val shouldDecrement = existingDose == null

        medicationDao.insertScheduledDoses(listOf(dose))

        if (shouldDecrement) {
            medicationDao.decrementRemainingPills(dose.medicationId)
            syncMedicationInventoryToRemote(dose.medicationId)
        }

        val now = currentTimeMillis()
        val med = medicationDao.getMedicationByIdOnce(dose.medicationId)
        val userId = med?.userId ?: sessionManager.currentUser.value?.id ?: ""
        val patientName = userDao?.getUserByIdOnce(userId)?.firstName
            ?: sessionManager.currentUser.value?.firstName
            ?: "Patient"

        runCatching {
            val dto = dose.toDto(
                userId = userId,
                medicationName = med?.name ?: "",
                dosage = med?.dosage ?: "",
                updatedAt = now
            )

            firestore
                .collection("scheduled_doses")
                .document(dose.id)
                .set(dto)

            val logDto = DoseLogDto(
                id = "log_${dose.id}",
                patientId = userId,
                patientName = patientName,
                medicationName = med?.name ?: "",
                dosage = med?.dosage ?: "",
                actionType = "DOSE_PRN",
                complianceStatus = "PRN",
                scheduledTime = dose.scheduledTime,
                timestamp = dose.takenTime ?: now
            )

            firestore
                .collection("dose_logs")
                .document(logDto.id)
                .set(logDto)

            medicationDao.markScheduledDoseSynced(dose.id)
        }
    }

    override fun getMedicationForUser(userId: String): Flow<List<MedicationEntity>> = channelFlow {
        syncMedicationsForUser(userId)
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
                val patientName = pairings[dose.userId]?.relation?.ifBlank { null }
                    ?: users[dose.userId]?.firstName?.ifBlank { null }
                    ?: "Your patient"

                val lateEscalationTime = dose.scheduledTime + DoseStateMachine.ON_TIME_WINDOW_MILLIS + 60_000L
                if (lateEscalationTime > now) {
                    platformNotifier.scheduleCaregiverEscalation(
                        doseId = dose.id,
                        pillName = dose.name,
                        patientName = patientName,
                        triggerTimeMillis = lateEscalationTime,
                        isMissed = false
                    )
                }

                val missedEscalationTime = dose.scheduledTime + DoseStateMachine.LATE_WINDOW_MILLIS + 60_000L
                if (missedEscalationTime > now) {
                    platformNotifier.scheduleCaregiverEscalation(
                        doseId = dose.id,
                        pillName = dose.name,
                        patientName = patientName,
                        triggerTimeMillis = missedEscalationTime,
                        isMissed = true
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
                    val advanceTarget = dose.scheduledTime - 30 * 60 * 1000L

                    if (advanceTarget > now) {
                        platformNotifier.scheduleDoseReminder(
                            doseId = dose.id,
                            pillName = medName,
                            triggerTimeMillis = advanceTarget,
                            stage = DoseReminderStage.ADVANCE_30MIN
                        )
                    }

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
        val existingDose = medicationDao.getScheduledDoseById(id)
        val shouldDecrement = isTaken && existingDose?.isTaken != true

        medicationDao.markScheduledDoseTaken(id, takenTime, isTaken, complianceStatus)

        if (shouldDecrement && existingDose != null) {
            medicationDao.decrementRemainingPills(existingDose.medicationId)
            syncMedicationInventoryToRemote(existingDose.medicationId)
        }

        if (isTaken) {
            platformNotifier.cancelReminder(doseId = id)
        }

        runCatching {
            val localDose = medicationDao.getScheduledDoseById(id)
            val med = localDose?.let { medicationDao.getMedicationByIdOnce(it.medicationId) }
            val userId = med?.userId ?: sessionManager.currentUser.value?.id ?: ""
            val patientName = userDao?.getUserByIdOnce(userId)?.firstName
                ?: sessionManager.currentUser.value?.firstName
                ?: "Patient"

            if (localDose != null) {
                val dto = localDose.toDto(
                    userId = userId,
                    medicationName = med?.name ?: "",
                    dosage = med?.dosage ?: "",
                    updatedAt = currentTimeMillis()
                )

                firestore
                    .collection("scheduled_doses")
                    .document(id)
                    .set(dto)

                medicationDao.markScheduledDoseSynced(id)

                val logDto = DoseLogDto(
                    id = "log_${id}",
                    patientId = userId,
                    patientName = patientName,
                    medicationName = med?.name ?: "",
                    dosage = med?.dosage ?: "",
                    actionType = if (isTaken) "DOSE_TAKEN" else "DOSE_SKIPPED",
                    complianceStatus = complianceStatus,
                    scheduledTime = localDose.scheduledTime,
                    timestamp = takenTime
                )

                firestore
                    .collection("dose_logs")
                    .document(logDto.id)
                    .set(logDto)
            }
        }
    }

    override suspend fun markScheduledDoseSkipped(
        id: String,
        takenTime: Long,
        skipReason: String
    ) {
        medicationDao.markScheduledDoseSkipped(
            id,
            takenTime,
            skipReason
        )

        platformNotifier.cancelReminder(doseId = id)

        runCatching {
            val localDose = medicationDao.getScheduledDoseById(id)
            val med = localDose?.let { medicationDao.getMedicationByIdOnce(it.medicationId) }
            val userId = med?.userId ?: sessionManager.currentUser.value?.id ?: ""
            val patientName = userDao?.getUserByIdOnce(userId)?.firstName
                ?: sessionManager.currentUser.value?.firstName
                ?: "Patient"

            if (localDose != null) {
                val dto = localDose.toDto(
                    userId = userId,
                    medicationName = med?.name ?: "",
                    dosage = med?.dosage ?: "",
                    updatedAt = currentTimeMillis()
                )

                firestore
                    .collection("scheduled_doses")
                    .document(id)
                    .set(dto)

                medicationDao.markScheduledDoseSynced(id)

                val logDto = DoseLogDto(
                    id = "log_${id}",
                    patientId = userId,
                    patientName = patientName,
                    medicationName = med?.name ?: "",
                    dosage = med?.dosage ?: "",
                    actionType = "DOSE_SKIPPED",
                    complianceStatus = "SKIPPED",
                    scheduledTime = localDose.scheduledTime,
                    timestamp = takenTime,
                    notes = skipReason
                )

                firestore
                    .collection("dose_logs")
                    .document(logDto.id)
                    .set(logDto)
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

        medicationDao.deletePendingDosesByMedicationId(id)

        medicationDao.archiveMedicationById(id)

        runCatching {
            firestore
                .collection("medications")
                .document(id)
                .update("isArchived" to true)
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

    override suspend fun getTakenDoseTimesForMedication(medicationId: String): Set<Long> =
        medicationDao.getTakenDoseTimesForMedication(medicationId).toSet()

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

    override suspend fun syncUnsyncedData() {
        runCatching {
            val unsyncedMeds = medicationDao.getUnsyncedMedications()
            unsyncedMeds.forEach { med ->
                runCatching {
                    firestore
                        .collection("medications")
                        .document(med.id)
                        .set(med.toDto())

                    medicationDao.markMedicationSynced(med.id)
                }
            }

            val unsyncedDoses = medicationDao.getUnsyncedScheduledDosesOnce()
            val allMeds = medicationDao.getAllMedicationsOnce().associateBy { it.id }
            val now = currentTimeMillis()

            unsyncedDoses.forEach { dose ->
                runCatching {
                    if (dose.complianceStatus == "SKIPPED") {
                        markScheduledDoseSkipped(
                            id = dose.id,
                            takenTime = dose.takenTime ?: now,
                            skipReason = dose.skipReason ?: ""
                        )
                    } else if (dose.isTaken && (dose.complianceStatus == "PRN" || allMeds[dose.medicationId]?.isPrn == true)) {
                        logPrnDose(dose)
                    } else if (dose.isTaken) {
                        markScheduledDoseTaken(
                            id = dose.id,
                            takenTime = dose.takenTime ?: now,
                            isTaken = true,
                            complianceStatus = dose.complianceStatus
                        )
                    } else {
                        val med = allMeds[dose.medicationId]
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
            }
        }
    }

    override suspend fun refillMedication(
        medicationId: String,
        newRemainingPills: Int
    ) {
        medicationDao.updateRemainingPills(medicationId, newRemainingPills.coerceAtLeast(0))
        syncMedicationInventoryToRemote(medicationId)
    }

    private suspend fun syncMedicationInventoryToRemote(medicationId: String) {
        val updatedMed = medicationDao.getMedicationByIdOnce(medicationId) ?: return
        val now = currentTimeMillis()

        runCatching {
            firestore
                .collection("medications")
                .document(medicationId)
                .set(updatedMed.toDto(updatedAt = now))

            medicationDao.markMedicationSynced(medicationId)
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

                        local.filter {
                            it.id !in remoteIds &&
                                    it.isSynced
                        }.forEach { med ->
                            val pendingDosesIds = medicationDao.getPendingDoseIdsForMedication(med.id)

                            pendingDosesIds.forEach { doseId ->
                                platformNotifier.cancelReminder(doseId = doseId)
                            }

                            medicationDao.deletePendingDosesByMedicationId(med.id)
                            medicationDao.deleteMedicationById(med.id)
                        }

                        val localMap = local.associateBy { it.id }

                        val reconciledRemote = remote.map { remoteMed ->
                            val localMed = localMap[remoteMed.id]
                            if (localMed != null && !localMed.isSynced) localMed else remoteMed
                        }

                        if (reconciledRemote.isNotEmpty()) medicationDao.insertMedications(reconciledRemote)
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
                            val localDosesMap = medicationDao.getAllScheduledDosesOnce().associateBy { it.id }

                            val reconciledEntities = doses.map { remote ->
                                val local = localDosesMap[remote.id]
                                if (local != null && local.isTaken && !remote.isTaken) {
                                    this@runCatching.launch(Dispatchers.IO) {
                                        if (local.complianceStatus == "SKIPPED") {
                                            markScheduledDoseSkipped(
                                                id = remote.id,
                                                takenTime = local.takenTime ?: currentTimeMillis(),
                                                skipReason = local.skipReason ?: ""
                                            )
                                        } else if (local.complianceStatus == "PRN") {
                                            logPrnDose(local)
                                        } else {
                                            markScheduledDoseTaken(
                                                id = remote.id,
                                                takenTime = local.takenTime ?: currentTimeMillis(),
                                                isTaken = true,
                                                complianceStatus = local.complianceStatus
                                            )
                                        }
                                    }

                                    local
                                } else {
                                    remote
                                }
                            }

                            medicationDao.insertScheduledDoses(reconciledEntities)

                            val currentUserId = sessionManager.currentUser.value?.id

                            if (userId == currentUserId) {
                                reconciledEntities.filter { it.isTaken }.forEach { dose ->
                                    platformNotifier.cancelReminder(doseId = dose.id)
                                }

                                val allMedsMap = medicationDao.getAllMedicationsOnce().associateBy { it.id }
                                val now = currentTimeMillis()

                                reconciledEntities.filter { !it.isTaken }.forEach { dose ->
                                    val med = allMedsMap[dose.medicationId]
                                    val medName = med?.name ?: "Medication"

                                    if (med != null && !med.isPaused && !med.isPrn && !med.isArchived) {
                                        if (dose.scheduledTime > now) {
                                            val advanceTarget = dose.scheduledTime - 30 * 60 * 1000L
                                            if (advanceTarget > now) {
                                                platformNotifier.scheduleDoseReminder(
                                                    doseId = dose.id,
                                                    pillName = medName,
                                                    triggerTimeMillis = advanceTarget,
                                                    stage = DoseReminderStage.ADVANCE_30MIN
                                                )
                                            }

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
}