package com.riramzy.pillfllow.domain.validation

import com.riramzy.pillfllow.utils.platform.Country

object AuthValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    sealed interface Result {
        data object Valid : Result
        data class Invalid(val reason: String) : Result
    }

    fun validateSignUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        phone: String,
        country: Country
    ): Result {
        val cleanFirst = firstName.trim()
        val cleanLast = lastName.trim()
        val cleanEmail = email.trim()
        val cleanPass = password.trim()
        val cleanConfirm = confirmPassword.trim()
        val cleanPhone = phone.trim()

        return when {
            cleanFirst.isBlank() || cleanLast.isBlank() || cleanEmail.isBlank() || cleanPass.isBlank() ->
                Result.Invalid("Please fill in all fields")

            !cleanEmail.matches(EMAIL_REGEX) ->
                Result.Invalid("Please enter a valid email address")

            cleanPass.length < 6 ->
                Result.Invalid("Password must be at least 6 characters")

            cleanPass != cleanConfirm ->
                Result.Invalid("Passwords do not match")

            cleanPhone.isBlank() ->
                Result.Invalid("Please enter your phone number")

            cleanPhone.length < country.minLength ->
                Result.Invalid("Please enter a valid phone number for ${country.name}")

            else -> Result.Valid
        }
    }

    fun validateSignIn(email: String, password: String): Result {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()

        return when {
            cleanEmail.isBlank() || cleanPass.isBlank() ->
                Result.Invalid("Please enter your email and password")

            !cleanEmail.matches(EMAIL_REGEX) ->
                Result.Invalid("Please enter a valid email address")

            else -> Result.Valid
        }
    }

    fun formatE164Phone(country: Country, rawPhone: String): String {
        return "${country.dialCode}${rawPhone.trim()}"
    }
}