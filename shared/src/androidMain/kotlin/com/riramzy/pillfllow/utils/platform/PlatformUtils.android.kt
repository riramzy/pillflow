package com.riramzy.pillfllow.utils.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.koin.mp.KoinPlatformTools
import java.util.Locale

actual fun openPhoneDialer(phoneNumber: String) {
    val context = KoinPlatformTools.defaultContext().get().get<Context>()

    val intent = Intent(
        Intent.ACTION_DIAL,
        "tel:$phoneNumber".toUri()
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}

actual fun copyToClipboard(text: String) {
    val context = KoinPlatformTools.defaultContext().get().get<Context>()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Pairing Code", text)
    clipboard.setPrimaryClip(clip)
}

actual fun getDeviceCountryCode(): String =
    Locale.getDefault().country.ifBlank { "US" }