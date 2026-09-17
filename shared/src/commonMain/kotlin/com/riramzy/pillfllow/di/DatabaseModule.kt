package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.data.local.database.PillFlowDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule: Module = module {
    single { get<PillFlowDatabase>().userDao }
    single { get<PillFlowDatabase>().medicationDao }
    single { get<PillFlowDatabase>().pairingDao }
}