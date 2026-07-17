package com.riramzy.pillfllow.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<PillFlowDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("pillflow.db")

    return Room.databaseBuilder<PillFlowDatabase>(
        appContext,
        dbFile.absolutePath
    )
}