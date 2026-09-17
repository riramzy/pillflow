package com.riramzy.pillfllow.domain.session

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.storage.AppPreferences
import com.riramzy.pillfllow.utils.PhysicsSensitivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(
    private val appPreferences: AppPreferences = AppPreferences()
) {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val initialSensitivity = runCatching {
        PhysicsSensitivity.valueOf(
            appPreferences.getString("physics_sensitivity", PhysicsSensitivity.NORMAL.name)
        )
    }.getOrDefault(PhysicsSensitivity.NORMAL)

    private val _physicsSensitivity = MutableStateFlow(initialSensitivity)
    val physicsSensitivity: StateFlow<PhysicsSensitivity> = _physicsSensitivity.asStateFlow()

    fun setUser(user: UserEntity) {
        _currentUser.value = user
    }

    fun clearUser() {
        _currentUser.value = null
    }

    fun setPhysicsSensitivity(sensitivity: PhysicsSensitivity) {
        _physicsSensitivity.value = sensitivity
        appPreferences.setString("physics_sensitivity", sensitivity.name)
    }
}