package com.riramzy.pillfllow.ui.state.settings

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import org.jetbrains.compose.resources.DrawableResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1

data class CaregiverSettingsState(
    val user: UserEntity? = null,
    val userName: String = "",
    val userEmail: String = "",
    val avatarRes: DrawableResource = Res.drawable.avatar1,
    val inputCode: String = "",
    val isConfirmSheetOpen: Boolean = false,
    val pendingPatientToLink: UserEntity? = null,
    val relationInput: String = "",
    val activePatients: List<PairedPatientUiModel> = emptyList(),
    val isLinking: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed interface CaregiverSettingsAction {
    data class InputCodeChanged(val code: String): CaregiverSettingsAction
    data class InitiateLink(val code: String): CaregiverSettingsAction
    data class RelationChanged(val relation: String): CaregiverSettingsAction
    data object ConfirmLink: CaregiverSettingsAction
    data object DismissConfirmSheet: CaregiverSettingsAction
    data class UnpairPatient(val patientId: String, val pairingId: String): CaregiverSettingsAction
    data class UpdateProfile(
        val firstName: String,
        val lastName: String,
        val email: String,
        val avatarRes: String
    ): CaregiverSettingsAction
    data object DismissError: CaregiverSettingsAction
    data object DismissSuccess: CaregiverSettingsAction
    data class SignOut(val onSignedOut: () -> Unit = {}): CaregiverSettingsAction
}