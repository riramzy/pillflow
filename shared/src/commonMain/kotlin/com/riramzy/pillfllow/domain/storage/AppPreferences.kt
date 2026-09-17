package com.riramzy.pillfllow.domain.storage

expect class AppPreferences() {
    fun getString(key: String, defaultValue: String): String
    fun setString(key: String, value: String)
}