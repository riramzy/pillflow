package com.riramzy.pillfllow.utils.medication

fun parseColorHex(hex: String): Long {
    return hex.removePrefix("#").toLong(16) or 0xFF000000
}