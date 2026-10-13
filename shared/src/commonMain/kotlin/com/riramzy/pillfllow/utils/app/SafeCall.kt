package com.riramzy.pillfllow.utils.app

import kotlinx.coroutines.CancellationException

inline fun <T> safeCall(
    errorMapper: (Throwable) -> String = ErrorMapper::mapGenericError,
    block: () -> T
): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.Error(e, errorMapper(e))
    }
}