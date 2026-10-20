package com.riramzy.pillfllow.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.riramzy.pillfllow.domain.usecase.medication.SyncAndMaintainScheduleUseCase
import org.koin.java.KoinJavaComponent.inject
import com.riramzy.pillfllow.utils.app.Result as AppResult

class RollingScheduleAndSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val syncAndMaintainScheduleUseCase: SyncAndMaintainScheduleUseCase by inject(SyncAndMaintainScheduleUseCase::class.java)

    override suspend fun doWork(): Result {
        return when (syncAndMaintainScheduleUseCase()) {
            is AppResult.Success -> Result.success()
            else -> Result.retry()
        }
    }
}