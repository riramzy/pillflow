package com.riramzy.pillfllow.utils

import kotlinx.coroutines.CancellationException

inline fun <T> safeCall(block: () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: CancellationException) {
       throw e
    } catch (e: Throwable) {
        Result.Error(e, e.message)
    }
}