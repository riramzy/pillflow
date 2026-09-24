package com.riramzy.pillfllow.utils.platform

expect fun openPhoneDialer(phoneNumber: String)

expect fun copyToClipboard(text: String)

expect fun getDeviceCountryCode(): String
