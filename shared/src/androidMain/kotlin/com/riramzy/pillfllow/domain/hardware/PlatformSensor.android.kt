package com.riramzy.pillfllow.domain.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

actual class PlatformSensor: SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var listener: ((tiltX: Float, tiltY: Float) -> Unit)? = null

    actual fun startListening(context: Any?, onUpdate: (tiltX: Float, tiltY: Float) -> Unit) {
        listener = onUpdate
        val androidContext = context as? Context ?: return

        sensorManager = androidContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    actual fun stopListening() {
        sensorManager?.unregisterListener(this)
        listener = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val tiltX = event.values[0]
            val tiltY = event.values[1]
            listener?.invoke(tiltX, tiltY)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}