package com.riramzy.pillfllow.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.SignInUseCase
import com.riramzy.pillfllow.domain.usecase.auth.SignUpUseCase
import com.riramzy.pillfllow.ui.state.auth.AuthAction
import com.riramzy.pillfllow.ui.state.auth.AuthState
import com.riramzy.pillfllow.utils.Result
import com.riramzy.pillfllow.utils.UserType
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

    fun onToggleAuthMode() {
        _state.update { it.copy(isSignUp = !it.isSignUp) }
    }

    fun signUp(onSuccess: () -> Unit = {}) {
        val currentState = state.value

        if (currentState.firstName.isBlank() ||
            currentState.lastName.isBlank() ||
            currentState.email.isBlank() ||
            currentState.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        if (!currentState.email.contains("@") || !currentState.email.contains(".")) {
            _state.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        if (currentState.password.length < 6) {
            _state.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = signUpUseCase(
                email = state.value.email,
                pass = state.value.password,
                firstName = state.value.firstName,
                lastName = state.value.lastName,
                role = state.value.selectedRole
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
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> Unit
            }
        }
    }

    fun signIn(onSuccess: () -> Unit = {}) {
        val currentState = state.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter your email and password") }
            return
        }

        if (!currentState.email.contains("@") || !currentState.email.contains(".")) {
            _state.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = signInUseCase(
                email = state.value.email,
                password = state.value.password
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
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
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
            is AuthAction.ToggleAuthMode -> onToggleAuthMode()
            is AuthAction.SignUp -> signUp(action.onSuccess)
            is AuthAction.SignIn -> signIn(action.onSuccess)
            is AuthAction.DismissError -> onErrorDismissed()
        }
    }
}