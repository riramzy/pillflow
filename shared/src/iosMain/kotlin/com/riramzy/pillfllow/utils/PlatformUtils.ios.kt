package com.riramzy.pillfllow.utils

import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.UIKit.UIPasteboard

actual fun openPhoneDialer(phoneNumber: String) {
    val url = platform.Foundation.NSURL.URLWithString("tel:$phoneNumber") ?: return
    platform.UIKit.UIApplication.sharedApplication.openURL(url)
}

actual fun copyToClipboard(text: String) {
    UIPasteboard.generalPasteboard.string = text
}

actual fun getDeviceCountryCode(): String =
    platform.Foundation.NSLocale.currentLocale.countryCode ?: "US"