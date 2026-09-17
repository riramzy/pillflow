package com.riramzy.pillfllow.domain.storage

import platform.Foundation.NSUserDefaults


actual class AppPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, defaultValue: String): String {
        return defaults.stringForKey(key) ?: defaultValue
    }

    actual fun setString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
}