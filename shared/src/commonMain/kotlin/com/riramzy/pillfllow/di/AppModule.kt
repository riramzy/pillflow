package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.data.local.database.PillFlowDatabase
import com.riramzy.pillfllow.data.repo.MedicationRepoImpl
import com.riramzy.pillfllow.data.repo.PairingRepoImpl
import com.riramzy.pillfllow.data.repo.UserRepoImpl
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single { get<PillFlowDatabase>().userDao }
    single { get<PillFlowDatabase>().medicationDao }
    single { get<PillFlowDatabase>().pairingDao }

    single<UserRepo> { UserRepoImpl(get()) }
    single<MedicationRepo> { MedicationRepoImpl(get()) }
    single<PairingRepo> { PairingRepoImpl(get()) }
}