package com.riramzy.pillfllow.domain.hardware

expect class PlatformHaptics {
    fun tickCollision(context: Any? = null)
    fun pulseDispensed(context: Any? = null)
}