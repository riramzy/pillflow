package com.riramzy.pillfllow.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.SignInUseCase
import com.riramzy.pillfllow.domain.usecase.auth.SignUpUseCase
import com.riramzy.pillfllow.ui.state.auth.AuthAction
import com.riramzy.pillfllow.ui.state.auth.AuthState
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.UserType
import com.riramzy.pillfllow.utils.platform.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
): ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onRoleSelected(role: UserType) {
        _state.update { it.copy(selectedRole = role) }
    }

    fun onFirstNameChanged(firstName: String) {
        _state.update { it.copy(firstName = firstName) }
    }

    fun onLastNameChanged(lastName: String) {
        _state.update { it.copy(lastName = lastName) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun onAvatarSelected(avatar: String) {
        _state.update { it.copy(selectedAvatar = avatar) }
    }

    fun onPhoneNumberChanged(phoneNumber: String) {
        _state.update { it.copy(phoneNumber = phoneNumber) }
    }

    fun onCountrySelected(country: Country) {
        _state.update { it.copy(selectedCountry = country) }
    }

    fun onToggleAuthMode() {
        _state.update { it.copy(isSignUp = !it.isSignUp) }
    }

    fun signUp(onSuccess: () -> Unit = {}) {
        val currentState = state.value
        val cleanFirstName = currentState.firstName.trim()
        val cleanLastName = currentState.lastName.trim()
        val cleanEmail = currentState.email.trim()
        val cleanPassword = currentState.password.trim()
        val cleanConfirmPassword = currentState.confirmPassword.trim()
        val cleanPhone = currentState.phoneNumber.trim()

        if (cleanFirstName.isBlank() || cleanLastName.isBlank() || cleanEmail.isBlank() || cleanPassword.isBlank()) {
            _state.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _state.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        if (cleanPassword.length < 6) {
            _state.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }

        if (cleanPassword != cleanConfirmPassword) {
            _state.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        if (cleanPhone.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter your phone number") }
            return
        }

        if (cleanPhone.length < currentState.selectedCountry.minLength) {
            _state.update { it.copy(errorMessage = "Please enter a valid phone number for ${currentState.selectedCountry.name}") }
            return
        }

        val fullE164Phone = "${currentState.selectedCountry.dialCode}$cleanPhone"

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = signUpUseCase(
                email = cleanEmail,
                pass = cleanPassword,
                firstName = cleanFirstName,
                lastName = cleanLastName,
                role = currentState.selectedRole,
                phoneNumber = fullE164Phone,
                avatarRes = currentState.selectedAvatar
            )

            when (result) {
                is Result.Success -> {
                    val resolvedRole = if (result.data.userType.equals("CAREGIVER", ignoreCase = true)) {
                        UserType.CAREGIVER
                    } else {
                        UserType.PATIENT
                    }

                    _state.update { it.copy(isLoading = false, isAuthenticated = true, userId = result.data.id, selectedRole = resolvedRole) }
                    onSuccess()
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "An unexpected error occurred. Please try again."
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    fun signIn(onSuccess: () -> Unit = {}) {
        val currentState = state.value
        val cleanEmail = currentState.email.trim()
        val cleanPassword = currentState.password.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter your email and password") }
            return
        }

        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _state.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = signInUseCase(
                email = cleanEmail,
                password = cleanPassword
            )

            when (result) {
                is Result.Success -> {
                    val resolvedRole = if (result.data.userType.equals("CAREGIVER", ignoreCase = true)) {
                        UserType.CAREGIVER
                    } else {
                        UserType.PATIENT
                    }

                    _state.update { it.copy(isLoading = false, isAuthenticated = true, userId = result.data.id, selectedRole = resolvedRole) }
                    onSuccess()
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "An unexpected error occurred. Please try again."
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    fun onErrorDismissed() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.SelectRole -> onRoleSelected(action.role)
            is AuthAction.FirstNameChanged -> onFirstNameChanged(action.firstName)
            is AuthAction.LastNameChanged -> onLastNameChanged(action.lastName)
            is AuthAction.EmailChanged -> onEmailChanged(action.email)
            is AuthAction.PasswordChanged -> onPasswordChanged(action.password)
            is AuthAction.ConfirmPasswordChanged -> onConfirmPasswordChanged(action.confirmPassword)
            is AuthAction.AvatarSelected -> onAvatarSelected(action.avatar)
            is AuthAction.PhoneNumberChanged -> onPhoneNumberChanged(action.phoneNumber)
            is AuthAction.CountrySelected -> onCountrySelected(action.country)
            is AuthAction.ToggleAuthMode -> onToggleAuthMode()
            is AuthAction.SignUp -> signUp(action.onSuccess)
            is AuthAction.SignIn -> signIn(action.onSuccess)
            is AuthAction.DismissError -> onErrorDismissed()
        }
    }
}