package com.riramzy.pillfllow.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.riramzy.pillfllow.data.local.dao.PillFlowDao
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity

@Database(
    entities = [
        MedicationEntity::class,
        ScheduledDoseEntity::class
    ],
    version = 1
)
@ConstructedBy(PillFlowDatabaseConstructor::class)
abstract class PillFlowDatabase: RoomDatabase() {
    abstract val dao: PillFlowDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PillFlowDatabaseConstructor: RoomDatabaseConstructor<PillFlowDatabase> {
    override fun initialize(): PillFlowDatabase
}