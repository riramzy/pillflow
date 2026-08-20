package com.riramzy.pillfllow.ui.state.auth

import com.riramzy.pillfllow.utils.UserType

data class AuthState(
    val selectedRole: UserType = UserType.PATIENT,
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignUp: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)