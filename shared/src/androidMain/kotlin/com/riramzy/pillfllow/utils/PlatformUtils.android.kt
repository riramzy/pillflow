package com.riramzy.pillfllow.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.koin.mp.KoinPlatformTools

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