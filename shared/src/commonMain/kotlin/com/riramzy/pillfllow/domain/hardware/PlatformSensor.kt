package com.riramzy.pillfllow.domain.hardware

expect class PlatformSensor {
    fun startListening(context: Any? = null, onUpdate: (tiltX: Float, tiltY: Float) -> Unit)
    fun stopListening()
}