package com.riramzy.pillfllow.utils.app

object ErrorMapper {
    fun mapAuthError(throwable: Throwable): String {
        val message = throwable.message.orEmpty()

        return when {
            message.contains("17004") || message.contains("INVALID_LOGIN_CREDENTIALS") ||
                    message.contains("17009") || message.contains("ERROR_WRONG_PASSWORD") ||
                    message.contains("17011") || message.contains("ERROR_USER_NOT_FOUND") ->
                        "Incorrect email or password. Please check your credentials and try again."

            message.contains("17007") || message.contains("EMAIL_ALREADY_IN_USE") ||
                    message.contains("already in use", ignoreCase = true) ->
                        "An account with this email already exists. Please sign in instead."

            message.contains("17008") || message.contains("INVALID_EMAIL") ||
                    message.contains("badly formatted", ignoreCase = true) ->
                        "Please enter a valid email address."

            message.contains("17026") || message.contains("WEAK_PASSWORD") ||
                    message.contains("at least 6 characters", ignoreCase = true) ->
                        "Password must be at least 6 characters long."


            message.contains("17020") || message.contains("NETWORK_ERROR") ||
                    message.contains("network error", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ||
                    message.contains("NSURLErrorDomain") ->
                        "Unable to connect to the server. Please check your internet connection."

            message.contains("17010") || message.contains("TOO_MANY_REQUESTS") ->
                "Too many attempts. Access is temporarily locked for your security. Please try again later."

            message.contains("User profile not found in cloud or local cache") ->
                "Unable to load profile data. Please try signing in again."

            else -> "Authentication failed. Please check your credentials and try again."
        }
    }

    fun mapGenericError(throwable: Throwable): String {
        val message = throwable.message.orEmpty()

        return when {
            message.contains("network", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ||
                    message.contains("UNAVAILABLE") ->
                        "Connection failed. Please check your internet connection."

            message.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Access denied. You do not have permission to perform this action."

            else -> throwable.message?.takeIf {
                !it.contains("Error Domain=")
            } ?: "An unexpected error occurred. Please try again."
        }
    }
}