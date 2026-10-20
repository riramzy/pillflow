package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.domain.usecase.medication.SyncAndMaintainScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

fun initKoinForIos() = initKoin()

fun triggerAppSyncAndSchedule(onComplete: (() -> Unit)? = null) {
    val koin = runCatching { KoinPlatformTools.defaultContext().get() }.getOrNull() ?: return
    val useCase = koin.get<SyncAndMaintainScheduleUseCase>()

    CoroutineScope(Dispatchers.IO).launch {
        useCase()
        onComplete?.invoke()
    }
}