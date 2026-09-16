package com.riramzy.pillfllow.domain.session

import com.riramzy.pillfllow.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    val isCaregiver: Boolean
        get() = currentUser.value?.userType?.equals("CAREGIVER", ignoreCase = true) == true

    fun setUser(user: UserEntity) {
        _currentUser.value = user
    }

    fun clearUser() {
        _currentUser.value = null
    }
}