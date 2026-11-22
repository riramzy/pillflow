package com.riramzy.pillfllow.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.auth.SocialAuthBridge
import com.riramzy.pillfllow.domain.auth.SocialProvider
import com.riramzy.pillfllow.domain.repo.SocialSignInOutcome
import com.riramzy.pillfllow.domain.usecase.auth.CompleteSocialProfileUseCase
import com.riramzy.pillfllow.domain.usecase.auth.GetIncompleteAuthSessionUseCase
import com.riramzy.pillfllow.domain.usecase.auth.SignInUseCase
import com.riramzy.pillfllow.domain.usecase.auth.SignUpUseCase
import com.riramzy.pillfllow.domain.usecase.auth.SocialSignInUseCase
import com.riramzy.pillfllow.ui.state.auth.AuthAction
import com.riramzy.pillfllow.ui.state.auth.AuthState
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.Result.Error
import com.riramzy.pillfllow.utils.app.Result.Success
import com.riramzy.pillfllow.utils.platform.Country
import com.riramzy.pillfllow.utils.user.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val socialSignInUseCase: SocialSignInUseCase,
    private val completeSocialProfileUseCase: CompleteSocialProfileUseCase,
    socialAuthBridge: SocialAuthBridge,
    private val getIncompleteAuthSessionUseCase: GetIncompleteAuthSessionUseCase
): ViewModel() {
    private val _state = MutableStateFlow(
        AuthState(
            isGoogleSignInAvailable = socialAuthBridge.supportsGoogleSignIn,
            isAppleSignInAvailable = socialAuthBridge.supportsAppleSignIn
        )
    )
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private var hasLoadedIncompleteSession = false

    fun onRoleSelected(role: UserType) {
        _state.update { it.copy(selectedRole = role, isRoleExplicitlySelected = true) }
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

    fun signUp(onSuccess: () -> Unit = {}) {
        val currentState = state.value

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = signUpUseCase(
                email = currentState.email,
                pass = currentState.password,
                confirmPass = currentState.confirmPassword,
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                role = currentState.selectedRole,
                phoneNumber = currentState.phoneNumber,
                country = currentState.selectedCountry,
                avatarRes = currentState.selectedAvatar
            )

            handleAuthResult(result, onSuccess)
        }
    }

    fun signIn(onSuccess: () -> Unit = {}) {
        val currentState = state.value

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = signInUseCase(
                email = currentState.email,
                password = currentState.password
            )

            handleAuthResult(result, onSuccess)
        }
    }

    private fun signInWithSocial(
        provider: SocialProvider,
        onExistingUser: () -> Unit,
        onNeedsProfile: () -> Unit
    ) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, activeSocialProvider = provider, errorMessage = null) }
            when (val result = socialSignInUseCase(provider)) {
                is Success -> when (val outcome = result.data) {
                    is SocialSignInOutcome.ExistingUser -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                activeSocialProvider = null,
                                isAuthenticated = true,
                                userId = outcome.user.id,
                                selectedRole = UserType.fromRaw(outcome.user.userType)
                            )
                        }
                        onExistingUser()
                    }
                    is SocialSignInOutcome.NeedsProfileCompletion -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                activeSocialProvider = null,
                                userId = outcome.uid,
                                email = outcome.email,
                                firstName = outcome.suggestedFirstName,
                                lastName = outcome.suggestedLastName,
                                isEmailReadOnly = outcome.email.isNotBlank()
                            )
                        }
                        onNeedsProfile()
                    }

                    is SocialSignInOutcome.Cancelled -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                activeSocialProvider = null
                            )
                        }
                    }
                }
                is Error -> _state.update {
                    it.copy(isLoading = false, activeSocialProvider = null, errorMessage = result.message)
                }
                else -> _state.update { it.copy(isLoading = false, activeSocialProvider = null) }
            }
        }
    }

    fun loadIncompleteSession(preselectedRole: UserType? = null) {
        if (preselectedRole != null) {
            _state.update {
                it.copy(
                    selectedRole = preselectedRole,
                    isRoleExplicitlySelected = true
                )
            }
        }

        if (hasLoadedIncompleteSession) return
        hasLoadedIncompleteSession = true

        viewModelScope.launch {
            val session = getIncompleteAuthSessionUseCase() ?: return@launch

            _state.update {
                it.copy(
                    userId = session.uid,
                    email = it.email.ifBlank { session.email },
                    firstName = it.firstName.ifBlank { session.suggestedFirstName },
                    lastName = it.lastName.ifBlank { session.suggestedLastName },
                    isEmailReadOnly = session.email.isNotBlank()
                )
            }
        }
    }

    fun completeSocialProfile(onSuccess: () -> Unit = {}) {
        val currentState = state.value

        if (!currentState.isRoleExplicitlySelected) {
            _state.update {
                it.copy(errorMessage = "Please select whether you are a Patient or a Caregiver.")
            }
            return
        }

        if (currentState.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = completeSocialProfileUseCase(
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                email = currentState.email,
                role = currentState.selectedRole,
                phoneNumber = currentState.phoneNumber,
                country = currentState.selectedCountry,
                avatarRes = currentState.selectedAvatar
            )

            handleAuthResult(result, onSuccess)
        }
    }

    fun onErrorDismissed() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun handleAuthResult(
        result: Result<UserEntity>,
        onSuccess: () -> Unit
    ) {
        when (result) {
            is Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        userId = result.data.id,
                        selectedRole = UserType.fromRaw(result.data.userType)
                    )
                }

                onSuccess()
            }

            is Error -> {
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
            is AuthAction.SignUp -> signUp(action.onSuccess)
            is AuthAction.SignIn -> signIn(action.onSuccess)
            is AuthAction.SignInWithGoogle -> signInWithSocial(SocialProvider.GOOGLE, action.onExistingUser, action.onNeedsProfile)
            is AuthAction.SignInWithApple -> signInWithSocial(SocialProvider.APPLE, action.onExistingUser, action.onNeedsProfile)
            is AuthAction.CompleteSocialProfile -> completeSocialProfile(action.onSuccess)
            is AuthAction.LoadIncompleteSession -> loadIncompleteSession(action.preselectedRole)
            is AuthAction.DismissError -> onErrorDismissed()
        }
    }
}