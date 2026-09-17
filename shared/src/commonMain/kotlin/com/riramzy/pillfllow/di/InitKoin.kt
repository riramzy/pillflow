package com.riramzy.pillfllow.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            platformModule,
            databaseModule,
            firebaseModule,
            repoModule,
            useCaseModule,
            viewModelModule
        )
    }
}