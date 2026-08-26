package com.riramzy.pillfllow.domain.repo

import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import kotlinx.coroutines.flow.Flow

interface MedicationRepo {
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun getUnsyncedMedications(): List<MedicationEntity>
    suspend fun insertMedication(medication: MedicationEntity): Long
    suspend fun getPendingDoseIdsForMedication(medicationId: Long): List<Long>
    suspend fun deleteMedicationById(id: Long)
    suspend fun markMedicationSynced(id: Long)
    fun getMedicationForUser(userId: String): Flow<List<MedicationEntity>>
    fun getPendingDosesForUser(userId: String): Flow<List<PendingDoseWithMedication>>
    fun getPendingDosesForPatients(patientIds: List<String>): Flow<List<PendingDoseWithMedication>>
    fun getPendingDosesWithMedication(): Flow<List<PendingDoseWithMedication>>
    fun getUnsyncedScheduledDoses(): Flow<List<ScheduledDoseEntity>>
    suspend fun markScheduledDoseSynced(id: Long)
    suspend fun markScheduledDoseTaken(id: Long, takenTime: Long, isTaken: Boolean, complianceStatus: String)
    suspend fun insertScheduledDoses(scheduledDose: List<ScheduledDoseEntity>): List<Long>
    fun getDoseHistoryForUser(userId: String): Flow<List<DoseHistoryEntity>>
    suspend fun getDoseHistoryForUserOnce(userId: String): List<DoseHistoryEntity>
}