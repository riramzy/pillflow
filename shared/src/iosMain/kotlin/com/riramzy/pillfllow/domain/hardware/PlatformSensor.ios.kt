package com.riramzy.pillfllow.domain.hardware

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

actual class PlatformSensor {
    private val motionManager = CMMotionManager()

    @OptIn(ExperimentalForeignApi::class)
    actual fun startListening(context: Any?, onUpdate: (tiltX: Float, tiltY: Float) -> Unit) {
        if (motionManager.isAccelerometerAvailable()) {
            motionManager.accelerometerUpdateInterval = 1.0 / 60.0

            motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, error ->
                data?.let {
                    it.acceleration.useContents {
                        val tiltX = (x * 9.81).toFloat()
                        val tiltY = (-y * 9.81).toFloat()
                        onUpdate(tiltX, tiltY)
                    }
                }
            }
        }
    }

    actual fun stopListening() {
        if (motionManager.isAccelerometerActive()) {
            motionManager.stopAccelerometerUpdates()
        }
    }
}