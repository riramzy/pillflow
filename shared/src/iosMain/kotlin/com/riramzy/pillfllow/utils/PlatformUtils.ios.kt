package com.riramzy.pillfllow.utils

import platform.Foundation.NSURL
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

actual fun openPhoneDialer(phoneNumber: String) {
    val sanitized = phoneNumber
        .removePrefix("tel:")
        .trim()
        .filter { it.isDigit() || it == '+' }

    if (sanitized.isBlank()) return

    val url = NSURL.URLWithString("tel:$sanitized") ?: return
    val app = UIApplication.sharedApplication

    app.openURL(
        url = url,
        options = emptyMap<Any?, Any?>(),
        completionHandler = { success ->
            if (!success) {
                copyToClipboard(sanitized)
            }
        }
    )
}

actual fun copyToClipboard(text: String) {
    UIPasteboard.generalPasteboard.string = text
}

actual fun getDeviceCountryCode(): String =
    platform.Foundation.NSLocale.currentLocale.countryCode ?: "US"