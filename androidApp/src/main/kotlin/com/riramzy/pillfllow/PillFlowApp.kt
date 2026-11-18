package com.riramzy.pillfllow

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.riramzy.pillfllow.di.initKoinAndroid
import com.riramzy.pillfllow.utils.platform.AndroidActivityTracker
import com.riramzy.pillfllow.worker.RollingScheduleAndSyncWorker
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit

class PillFlowApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
        val activityTracker: AndroidActivityTracker by inject(AndroidActivityTracker::class.java)
        registerActivityLifecycleCallbacks(activityTracker)
        setupBackgroundWorkers()
    }

    private fun setupBackgroundWorkers() {
        val periodicSyncRequest = PeriodicWorkRequestBuilder<RollingScheduleAndSyncWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RollingScheduleAndSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }
}