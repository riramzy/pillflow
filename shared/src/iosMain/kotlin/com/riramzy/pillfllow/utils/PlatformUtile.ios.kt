package com.riramzy.pillfllow.utils

actual fun openPhoneDialer(phoneNumber: String) {
    val url = platform.Foundation.NSURL.URLWithString("tel:$phoneNumber") ?: return
    platform.UIKit.UIApplication.sharedApplication.openURL(url)
}