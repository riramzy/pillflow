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