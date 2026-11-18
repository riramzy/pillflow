package com.riramzy.pillfllow.di

import com.riramzy.pillfllow.data.AndroidSocialAuthBridge
import com.riramzy.pillfllow.data.local.database.PillFlowDatabase
import com.riramzy.pillfllow.data.local.database.getDatabaseBuilder
import com.riramzy.pillfllow.data.local.database.getRoomDatabase
import com.riramzy.pillfllow.domain.auth.SocialAuthBridge
import com.riramzy.pillfllow.utils.platform.AndroidActivityTracker
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<PillFlowDatabase> {
        val dbBuilder = getDatabaseBuilder(context = get())
        getRoomDatabase(dbBuilder)
    }

    single { AndroidActivityTracker() }

    single<SocialAuthBridge> { AndroidSocialAuthBridge(context = get(), activityTracker = get()) }
}