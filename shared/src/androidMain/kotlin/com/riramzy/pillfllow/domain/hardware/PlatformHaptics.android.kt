package com.riramzy.pillfllow.domain.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import org.koin.mp.KoinPlatformTools

actual class PlatformHaptics {
    private fun resolveContext(context: Any?): Context? {
        return (context as? Context) ?: runCatching {
            KoinPlatformTools.defaultContext().get().get<Context>()
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    actual fun tickCollision(context: Any?) {
        val vibrator = getVibrator(resolveContext(context) ?: return) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(15L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15L)
        }
    }

    @SuppressLint("MissingPermission")
    actual fun pulseDispensed(context: Any?) {
        val vibrator = getVibrator(resolveContext(context) ?: return) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(70L)
        }
    }

    @Suppress("DEPRECATION")
    private fun getVibrator(context: Context): Vibrator? {
        return context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}