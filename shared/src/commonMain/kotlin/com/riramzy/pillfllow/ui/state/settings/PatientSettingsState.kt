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
    val pendingSensitivity: PhysicsSensitivity? = null,
    val isLoading: Boolean = false,
    val isRegenerating: Boolean = false,
    val errorMessage: String? = null
)

sealed interface PatientSettingsAction {
    data object RegenerateCode: PatientSettingsAction
    data class RequestChangeSensitivity(val sensitivity: PhysicsSensitivity) : PatientSettingsAction
    data object ConfirmChangeSensitivity : PatientSettingsAction
    data object DismissSensitivityDialog : PatientSettingsAction
    data class UpdateProfile(
        val firstName: String,
        val lastName: String,
        val email: String,
        val avatarRes: String
    ): PatientSettingsAction
    data object DismissError: PatientSettingsAction
    data class SignOut(val onSignedOut: () -> Unit = {}): PatientSettingsAction
}