package com.riramzy.pillfllow.ui.state.settings

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.utils.PhysicsSensitivity
import org.jetbrains.compose.resources.DrawableResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1

data class PatientSettingsState(
    val user: UserEntity? = null,
    val userName: String = "",
    val userEmail: String = "",
    val avatarRes: DrawableResource = Res.drawable.avatar1,
    val pairingCode: String = "",
    val physicsSensitivity: PhysicsSensitivity = PhysicsSensitivity.NORMAL,
    val isLoading: Boolean = false,
    val isRegenerating: Boolean = false,
    val errorMessage: String? = null
)