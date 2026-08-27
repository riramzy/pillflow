package com.riramzy.pillfllow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PairingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPairing(pairing: CaregiverPatientPairingEntity)

    @Query("SELECT * FROM pairings WHERE caregiverId = :caregiverId")
    fun getPairingsForCaregiver(caregiverId: String): Flow<List<CaregiverPatientPairingEntity>>

    @Query("SELECT * FROM pairings WHERE caregiverId = :caregiverId")
    fun getPairingsForCaregiverOnce(caregiverId: String): List<CaregiverPatientPairingEntity>

    @Query("SELECT * FROM pairings WHERE pairingCode = :pairingCode")
    fun getPairingsByPairingCode(pairingCode: String): Flow<CaregiverPatientPairingEntity?>

    @Query("SELECT * FROM pairings WHERE pairingCode = :pairingCode AND status = 'PENDING' LIMIT 1")
    suspend fun getPendingPairingByCode(pairingCode: String): CaregiverPatientPairingEntity?

    @Query("SELECT * FROM pairings WHERE pairingCode = :pairingCode")
    fun getPairingsByPairingCodeOnce(pairingCode: String): CaregiverPatientPairingEntity?

    @Query("SELECT * FROM pairings WHERE patientId = :patientId")
    fun getPairingsForPatient(patientId: String): Flow<List<CaregiverPatientPairingEntity>>

    @Query("SELECT * FROM pairings WHERE patientId = :patientId")
    fun getPairingsForPatientOnce(patientId: String): List<CaregiverPatientPairingEntity>

    @Query("UPDATE pairings SET status = :status WHERE pairingId = :pairingId")
    suspend fun updatePairingStatus(pairingId: String, status: String)

    @Delete
    suspend fun deletePairing(pairing: CaregiverPatientPairingEntity)

    @Query("DELETE FROM pairings WHERE pairingId = :pairingId")
    suspend fun deletePairingById(pairingId: String): Int

    @Query("DELETE FROM pairings WHERE patientId = :patientId AND status = 'PENDING'")
    suspend fun deletePendingPairingsForPatient(patientId: String): Int
}