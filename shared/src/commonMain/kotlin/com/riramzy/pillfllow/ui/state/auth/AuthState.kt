package com.riramzy.pillfllow.ui.state.auth

import com.riramzy.pillfllow.utils.Country
import com.riramzy.pillfllow.utils.UserType
import com.riramzy.pillfllow.utils.allCountries
import com.riramzy.pillfllow.utils.getDeviceCountryCode

data class AuthState(
    val selectedRole: UserType = UserType.PATIENT,
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedAvatar: String = "avatar1",
    val phoneNumber: String = "",
    val selectedCountry: Country = allCountries.firstOrNull { it.code.equals(getDeviceCountryCode(), ignoreCase = true) } ?: allCountries.first(),
    val isSignUp: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

sealed interface AuthAction {
    data class SelectRole(val role: UserType): AuthAction
    data class FirstNameChanged(val firstName: String): AuthAction
    data class LastNameChanged(val lastName: String): AuthAction
    data class EmailChanged(val email: String): AuthAction
    data class PasswordChanged(val password: String): AuthAction
    data class ConfirmPasswordChanged(val confirmPassword: String): AuthAction
    data class AvatarSelected(val avatar: String): AuthAction
    data class PhoneNumberChanged(val phoneNumber: String): AuthAction
    data class CountrySelected(val country: Country): AuthAction
    data object ToggleAuthMode: AuthAction
    data class SignUp(val onSuccess: () -> Unit = {}): AuthAction
    data class SignIn(val onSuccess: () -> Unit = {}): AuthAction
    data object DismissError: AuthAction
}