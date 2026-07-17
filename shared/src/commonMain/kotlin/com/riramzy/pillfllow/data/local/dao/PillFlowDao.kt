package com.riramzy.pillfllow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PendingDoseWithMedication
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PillFlowDao {
    @Query("SELECT * FROM medications")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE isSynced = 0")
    suspend fun getUnsyncedMedications(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Query("UPDATE medications SET isSynced = 1 WHERE id = :id")
    suspend fun markMedicationSynced(id: Long)

    @Query("SELECT * FROM scheduled_doses WHERE medicationId = :medicationId")
    fun getScheduledDosesForMedication(medicationId: Long): Flow<List<ScheduledDoseEntity>>

    @Query("""
        SELECT * FROM scheduled_doses
        INNER JOIN medications ON scheduled_doses.medicationId = medications.id
        WHERE isTaken = 0
        ORDER BY scheduledTime ASC
    """)
    fun getPendingDosesWithMedication(): Flow<List<PendingDoseWithMedication>>

    @Query("SELECT * FROM scheduled_doses WHERE isSynced = 0")
    fun getUnsyncedScheduledDoses(): Flow<List<ScheduledDoseEntity>>

    @Query("UPDATE scheduled_doses SET isSynced = 1 WHERE id = :id")
    suspend fun markScheduledDoseSynced(id: Long)

    @Query("UPDATE scheduled_doses SET isTaken = :isTaken, takenTime = :takenTime, isSynced = 0 WHERE id = :id")
    suspend fun markScheduledDoseTaken(id: Long, takenTime: Long, isTaken: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledDoses(scheduledDose: List<ScheduledDoseEntity>)
}