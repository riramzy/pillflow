package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.data.repo.AuthRepoImpl
import com.riramzy.pillfllow.data.repo.MedicationRepoImpl
import com.riramzy.pillfllow.data.repo.PairingRepoImpl
import com.riramzy.pillfllow.data.repo.UserRepoImpl
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import org.koin.core.module.Module
import org.koin.dsl.module

val repoModule: Module = module {
    single { SessionManager() }

    single<UserRepo> {
        UserRepoImpl(
            userDao = get(),
            firestore = get()
        )
    }

    single<MedicationRepo> {
        MedicationRepoImpl(
            medicationDao = get(),
            userDao = get(),
            pairingDao = get(),
            platformNotifier = get(),
            firestore = get(),
            sessionManager = get()
        )
    }

    single<PairingRepo> {
        PairingRepoImpl(
            pairingDao = get(),
            userDao = get(),
            firestore = get()
        )
    }

    single<AuthRepo> {
        AuthRepoImpl(
            firebaseAuth = get(),
            userRepo = get(),
            firestore = get()
        )
    }
}