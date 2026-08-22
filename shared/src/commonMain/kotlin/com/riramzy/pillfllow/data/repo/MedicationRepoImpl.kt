package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.dao.MedicationDao
import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.utils.currentTimeMillis
import kotlinx.coroutines.flow.Flow

class MedicationRepoImpl(
    private val medicationDao: MedicationDao,
    private val platformNotifier: PlatformNotifier = PlatformNotifier()
): MedicationRepo {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = medicationDao.getAllMedications()

    override suspend fun insertMedication(medication: MedicationEntity): Long = medicationDao.insertMedication(medication)

    override fun getMedicationForUser(userId: String): Flow<List<MedicationEntity>> = medicationDao.getMedicationForUser(userId)

    override fun getPendingDosesForUser(userId: String): Flow<List<PendingDoseWithMedication>> = medicationDao.getPendingDosesForUser(userId)

    override suspend fun markScheduledDoseTaken(
        id: Long,
        takenTime: Long,
        isTaken: Boolean,
        complianceStatus: String
    ) = medicationDao.markScheduledDoseTaken(id, takenTime, isTaken, complianceStatus)

    override suspend fun insertScheduledDoses(scheduledDose: List<ScheduledDoseEntity>): List<Long> {
        val ids = medicationDao.insertScheduledDoses(scheduledDose)
        val now = currentTimeMillis()

        scheduledDose.forEachIndexed { index, dose ->
            if (!dose.isTaken && dose.scheduledTime > now) {
                val doseId = ids.getOrNull(index) ?: dose.id
                platformNotifier.scheduleDoseReminder(
                    doseId = doseId.toString(),
                    pillName = "Medication",
                    triggerTimeMillis = dose.scheduledTime
                )
            }
        }
        return ids
    }
    override suspend fun getUnsyncedMedications(): List<MedicationEntity> = medicationDao.getUnsyncedMedications()

    override suspend fun markMedicationSynced(id: Long) = medicationDao.markMedicationSynced(id)

    override fun getPendingDosesForPatients(patientIds: List<String>): Flow<List<PendingDoseWithMedication>> = medicationDao.getPendingDosesForPatients(patientIds)

    override fun getPendingDosesWithMedication(): Flow<List<PendingDoseWithMedication>> = medicationDao.getPendingDosesWithMedication()

    override fun getUnsyncedScheduledDoses(): Flow<List<ScheduledDoseEntity>> = medicationDao.getUnsyncedScheduledDoses()

    override suspend fun markScheduledDoseSynced(id: Long) = medicationDao.markScheduledDoseSynced(id)

    override fun getDoseHistoryForUser(userId: String): Flow<List<DoseHistoryEntity>> = medicationDao.getDoseHistoryForUser(userId)

    override suspend fun getDoseHistoryForUserOnce(userId: String): List<DoseHistoryEntity> = medicationDao.getDoseHistoryForUserOnce(userId)
}