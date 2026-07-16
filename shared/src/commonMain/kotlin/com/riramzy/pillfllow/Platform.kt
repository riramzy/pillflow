package com.riramzy.pillfllow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform