package com.riramzy.pillfllow.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.ui.state.auth.AuthState
import com.riramzy.pillfllow.utils.UserType
import com.riramzy.pillfllow.utils.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AuthViewModel(
    val userRepo: UserRepo
): ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onRoleSelected(role: UserType) {
        _state.update { it.copy(selectedRole = role) }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun onToggleAuthMode() {
        _state.update { it.copy(isSignUp = !it.isSignUp) }
    }

    fun authenticate(onSuccess: () -> Unit = {}) {
        if (state.value.isSignUp) {
            signUp(onSuccess)
        } else {
            signIn(onSuccess)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun signUp(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val newUserId = Uuid.random().toString()

                userRepo.insertUser(
                    UserEntity(
                        id = newUserId,
                        name = state.value.name,
                        email = state.value.email,
                        userType = state.value.selectedRole.toString(),
                        createdAt = currentTimeMillis(),
                        avatarRes = "avatar1"
                    )
                )

                _state.update { it.copy(isLoading = false, isAuthenticated = true, userId = newUserId) }
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun signIn(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val user = userRepo.getUserByIdOnce(state.value.userId)

                if (user != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            name = user.name,
                            userId = user.id,
                            email = user.email,
                            selectedRole = UserType.valueOf(user.userType)
                        )
                    }

                    onSuccess()
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}