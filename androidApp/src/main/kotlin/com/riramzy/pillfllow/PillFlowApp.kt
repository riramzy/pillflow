package com.riramzy.pillfllow

import android.app.Application
import com.riramzy.pillfllow.di.initKoinAndroid

class PillFlowApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}