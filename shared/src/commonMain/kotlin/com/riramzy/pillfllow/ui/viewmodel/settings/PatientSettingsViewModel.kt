package com.riramzy.pillfllow.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.LogoutUseCase
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GeneratePairingCodeUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GetPatientPairingStatusUseCase
import com.riramzy.pillfllow.domain.usecase.patient.UpdateUserProfileUseCase
import com.riramzy.pillfllow.ui.state.settings.PatientSettingsAction
import com.riramzy.pillfllow.ui.state.settings.PatientSettingsState
import com.riramzy.pillfllow.utils.PhysicsSensitivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.avatar3
import pillfllow.shared.generated.resources.avatar4
import pillfllow.shared.generated.resources.avatar5
import pillfllow.shared.generated.resources.avatar6
import pillfllow.shared.generated.resources.avatar7
import pillfllow.shared.generated.resources.avatar8

class PatientSettingsViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getPatientPairingStatusUseCase: GetPatientPairingStatusUseCase,
    private val generatePairingCodeUseCase: GeneratePairingCodeUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
): ViewModel() {
    private val _state = MutableStateFlow(PatientSettingsState())
    val state: StateFlow<PatientSettingsState> = _state.asStateFlow()

    init {
        observePatientData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePatientData() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase()
                .filterNotNull()
                .flatMapLatest { user ->
                    val avatarRes = when (user.avatarRes) {
                        "avatar1" -> Res.drawable.avatar1
                        "avatar2" -> Res.drawable.avatar2
                        "avatar3" -> Res.drawable.avatar3
                        "avatar4" -> Res.drawable.avatar4
                        "avatar5" -> Res.drawable.avatar5
                        "avatar6" -> Res.drawable.avatar6
                        "avatar7" -> Res.drawable.avatar7
                        else -> Res.drawable.avatar8
                    }

                    _state.update {
                        it.copy(
                            user = user,
                            userName = "${user.firstName} ${user.lastName}".trim(),
                            userEmail = user.email,
                            avatarRes = avatarRes
                        )
                    }

                    getPatientPairingStatusUseCase(user.id)
                }.collectLatest { status ->
                    val currentUser = state.value.user

                    if (status.pendingCode != null) {
                        _state.update { it.copy(pairingCode = status.pendingCode) }
                    } else if (!status.hasActivePairing && currentUser != null) {
                        generatePairingCodeUseCase(currentUser)
                    }
                }
        }
    }

    fun onRegenerateCode() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = state.value.user ?: return@launch
            _state.update { it.copy(isRegenerating = true) }
            generatePairingCodeUseCase(user)
            _state.update { it.copy(isRegenerating = false) }
        }
    }

    fun onSensitivitySelected(sensitivity: PhysicsSensitivity) {
        _state.update { it.copy(physicsSensitivity = sensitivity) }
    }

    fun onUpdateProfile(firstName: String, lastName: String, email: String, avatarRes: String) {
        val user = state.value.user ?: return

        viewModelScope.launch(Dispatchers.IO) {
            updateUserProfileUseCase(user, firstName, lastName, email, avatarRes)
        }
    }

    fun onErrorDismissed() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSignOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch {
            logoutUseCase()
            onSignedOut()
        }
    }

    fun onAction(action: PatientSettingsAction) {
        when (action) {
            is PatientSettingsAction.RegenerateCode -> onRegenerateCode()
            is PatientSettingsAction.SelectSensitivity -> onSensitivitySelected(action.sensitivity)
            is PatientSettingsAction.UpdateProfile -> onUpdateProfile(action.firstName, action.lastName, action.email, action.avatarRes)
            is PatientSettingsAction.DismissError -> onErrorDismissed()
            is PatientSettingsAction.SignOut -> onSignOut(action.onSignedOut)
        }
    }
}
