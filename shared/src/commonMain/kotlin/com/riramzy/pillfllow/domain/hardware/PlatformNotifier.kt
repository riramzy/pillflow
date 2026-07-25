package com.riramzy.pillfllow.domain.hardware

expect class PlatformNotifier() {
    fun scheduleDoseReminder(
        context: Any? = null,
        doseId: String,
        pillName: String,
        triggerTimeMillis: Long
    )

    fun cancelReminder(context: Any? = null, doseId: String)
}