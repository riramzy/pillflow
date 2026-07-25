package com.riramzy.pillfllow.domain.hardware

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual class PlatformHaptics {
    actual fun tickCollision(context: Any?) {
        val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)

        generator.prepare()
        generator.impactOccurred()
    }

    actual fun pulseDispensed(context: Any?) {
        val generator = UINotificationFeedbackGenerator()

        generator.prepare()
        generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }
}