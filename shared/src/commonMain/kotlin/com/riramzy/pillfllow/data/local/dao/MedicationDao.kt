package com.riramzy.pillfllow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.riramzy.pillfllow.data.local.entity.DoseHistoryEntity
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications")
    suspend fun getAllMedicationsOnce(): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE isSynced = 0")
    suspend fun getUnsyncedMedications(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Query("SELECT id FROM scheduled_doses WHERE medicationId = :medicationId AND isTaken = 0")
    suspend fun getPendingDoseIdsForMedication(medicationId: String): List<String>

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: String)

    @Query("UPDATE medications SET isSynced = 1 WHERE id = :id")
    suspend fun markMedicationSynced(id: String)

    @Query("SELECT * FROM scheduled_doses WHERE medicationId = :medicationId")
    fun getScheduledDosesForMedication(medicationId: String): Flow<List<ScheduledDoseEntity>>

    @Query("SELECT * FROM medications WHERE userId = :userId")
    fun getMedicationForUser(userId: String): Flow<List<MedicationEntity>>

    @Query("""
        SELECT 
            scheduled_doses.id AS id, 
            medications.name AS name, 
            medications.dosage AS dosage, 
            medications.colorHex AS colorHex, 
            medications.shape AS shape,
            scheduled_doses.scheduledTime AS scheduledTime
        FROM scheduled_doses
        INNER JOIN medications ON scheduled_doses.medicationId = medications.id
        WHERE scheduled_doses.isTaken = 0 AND medications.userId = :userId
        ORDER BY scheduled_doses.scheduledTime ASC
    """)
    fun getPendingDosesForUser(userId: String): Flow<List<PendingDoseWithMedication>>

    @Query("""
        SELECT 
            scheduled_doses.id AS id, 
            medications.name AS name, 
            medications.dosage AS dosage, 
            medications.colorHex AS colorHex, 
            medications.shape AS shape,
            scheduled_doses.scheduledTime AS scheduledTime
        FROM scheduled_doses
        INNER JOIN medications ON scheduled_doses.medicationId = medications.id
        WHERE scheduled_doses.isTaken = 0 AND medications.userId IN (:patientIds)
        ORDER BY scheduled_doses.scheduledTime ASC
    """)
    fun getPendingDosesForPatients(patientIds: List<String>): Flow<List<PendingDoseWithMedication>>

    @Query("""
        SELECT 
            scheduled_doses.id AS id, 
            medications.name AS name, 
            medications.dosage AS dosage, 
            medications.colorHex AS colorHex, 
            medications.shape AS shape,
            scheduled_doses.scheduledTime AS scheduledTime
        FROM scheduled_doses
        INNER JOIN medications ON scheduled_doses.medicationId = medications.id
        WHERE scheduled_doses.isTaken = 0
        ORDER BY scheduled_doses.scheduledTime ASC
    """)
    fun getPendingDosesWithMedication(): Flow<List<PendingDoseWithMedication>>

    @Query("SELECT * FROM scheduled_doses WHERE isSynced = 0")
    fun getUnsyncedScheduledDoses(): Flow<List<ScheduledDoseEntity>>

    @Query("UPDATE scheduled_doses SET isSynced = 1 WHERE id = :id")
    suspend fun markScheduledDoseSynced(id: String)

    @Query("UPDATE scheduled_doses SET isTaken = :isTaken, takenTime = :takenTime, complianceStatus = :complianceStatus, isSynced = 0 WHERE id = :id")
    suspend fun markScheduledDoseTaken(id: String, takenTime: Long, isTaken: Boolean, complianceStatus: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledDoses(scheduledDose: List<ScheduledDoseEntity>)

    @Query("""
    SELECT 
        scheduled_doses.id AS id,
        scheduled_doses.medicationId AS medicationId,
        medications.name AS name,
        medications.dosage AS dosage,
        medications.colorHex AS colorHex,
        medications.shape AS shape,
        scheduled_doses.scheduledTime AS scheduledTime,
        scheduled_doses.takenTime AS takenTime,
        scheduled_doses.complianceStatus AS complianceStatus,
        scheduled_doses.isTaken AS isTaken
    FROM scheduled_doses
    INNER JOIN medications ON scheduled_doses.medicationId = medications.id
    WHERE medications.userId = :userId
    ORDER BY scheduled_doses.scheduledTime DESC
""")
    fun getDoseHistoryForUser(userId: String): Flow<List<DoseHistoryEntity>>

    @Query("""
    SELECT 
        scheduled_doses.id AS id,
        scheduled_doses.medicationId AS medicationId,
        medications.name AS name,
        medications.dosage AS dosage,
        medications.colorHex AS colorHex,
        medications.shape AS shape,
        scheduled_doses.scheduledTime AS scheduledTime,
        scheduled_doses.takenTime AS takenTime,
        scheduled_doses.complianceStatus AS complianceStatus,
        scheduled_doses.isTaken AS isTaken
    FROM scheduled_doses
    INNER JOIN medications ON scheduled_doses.medicationId = medications.id
    WHERE medications.userId = :userId
    ORDER BY scheduled_doses.scheduledTime DESC
""")
    suspend fun getDoseHistoryForUserOnce(userId: String): List<DoseHistoryEntity>

    @Query("DELETE FROM medications")
    suspend fun clearAllMedications()

    @Query("DELETE FROM scheduled_doses")
    suspend fun clearAllScheduledDoses()

    @Query("DELETE FROM scheduled_doses WHERE medicationId = :medicationId")
    suspend fun deleteScheduledDosesByMedicationId(medicationId: String)

    @Query("DELETE FROM scheduled_doses WHERE id = :id")
    suspend fun deleteScheduledDoseById(id: String)

    @Query("SELECT * FROM scheduled_doses WHERE isTaken = 0")
    suspend fun getAllPendingScheduledDosesOnce(): List<ScheduledDoseEntity>

    @Query("SELECT * FROM scheduled_doses WHERE id = :id")
    suspend fun getScheduledDoseById(id: String): ScheduledDoseEntity?
}