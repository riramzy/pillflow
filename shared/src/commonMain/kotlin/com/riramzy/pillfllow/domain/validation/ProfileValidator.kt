package com.riramzy.pillfllow.domain.validation

object ProfileValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    sealed interface Result {
        data object Valid : Result
        data class Invalid(val reason: String) : Result
    }

    fun validate(
        firstName: String,
        lastName: String,
        email: String
    ): Result {
        val cleanFirst = firstName.trim()
        val cleanLast = lastName.trim()
        val cleanEmail = email.trim()

        return when {
            cleanFirst.isBlank() -> Result.Invalid("Please enter your first name")

            cleanLast.isBlank() -> Result.Invalid("Please enter your last name")

            cleanFirst.first().isLowerCase() || cleanLast.first().isLowerCase() ->
                Result.Invalid("First and last names must start with a capital letter")

            !cleanEmail.matches(EMAIL_REGEX) ->
                Result.Invalid("Please enter a valid email address (e.g. name@example.com)")

            else -> Result.Valid
        }
    }
}