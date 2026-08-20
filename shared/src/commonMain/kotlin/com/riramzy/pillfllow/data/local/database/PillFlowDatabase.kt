package com.riramzy.pillfllow.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.riramzy.pillfllow.data.local.dao.MedicationDao
import com.riramzy.pillfllow.data.local.dao.PairingDao
import com.riramzy.pillfllow.data.local.dao.UserDao
import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.data.local.entity.UserEntity

@Database(
    entities = [
        MedicationEntity::class,
        ScheduledDoseEntity::class,
        UserEntity::class,
        CaregiverPatientPairingEntity::class
    ],
    version = 1
)
@ConstructedBy(PillFlowDatabaseConstructor::class)
abstract class PillFlowDatabase: RoomDatabase() {
    abstract val medicationDao: MedicationDao
    abstract val userDao: UserDao
    abstract val pairingDao: PairingDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PillFlowDatabaseConstructor: RoomDatabaseConstructor<PillFlowDatabase> {
    override fun initialize(): PillFlowDatabase
}