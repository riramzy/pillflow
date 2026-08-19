package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.data.local.database.PillFlowDatabase
import com.riramzy.pillfllow.data.local.database.getDatabaseBuilder
import com.riramzy.pillfllow.data.local.database.getRoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<PillFlowDatabase> {
        val dbBuilder = getDatabaseBuilder()
        getRoomDatabase(dbBuilder)
    }
}