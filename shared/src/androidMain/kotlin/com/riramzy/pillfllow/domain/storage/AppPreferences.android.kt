package com.riramzy.pillfllow.domain.storage

import android.content.Context
import androidx.core.content.edit
import org.koin.mp.KoinPlatformTools

actual class AppPreferences {
    private val prefs by lazy {
        val context = KoinPlatformTools.defaultContext().get().get<Context>()
        context.getSharedPreferences("pillflow_preferences", Context.MODE_PRIVATE)
    }
    actual fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
    actual fun setString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }
}