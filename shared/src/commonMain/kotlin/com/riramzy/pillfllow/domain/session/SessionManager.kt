package com.riramzy.pillfllow.domain.session

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.utils.PhysicsSensitivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _physicsSensitivity = MutableStateFlow(PhysicsSensitivity.NORMAL)
    val physicsSensitivity: StateFlow<PhysicsSensitivity> = _physicsSensitivity.asStateFlow()

    fun setUser(user: UserEntity) {
        _currentUser.value = user
    }

    fun clearUser() {
        _currentUser.value = null
    }

    fun setPhysicsSensitivity(sensitivity: PhysicsSensitivity) {
        _physicsSensitivity.value = sensitivity
    }
}