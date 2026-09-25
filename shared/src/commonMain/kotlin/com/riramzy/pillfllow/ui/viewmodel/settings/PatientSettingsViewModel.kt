package com.riramzy.pillfllow.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.LogoutUseCase
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GeneratePairingCodeUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GetPatientPairingStatusUseCase
import com.riramzy.pillfllow.domain.usecase.patient.GetPhysicsSensitivityUseCase
import com.riramzy.pillfllow.domain.usecase.patient.SetPhysicsSensitivityUseCase
import com.riramzy.pillfllow.domain.usecase.patient.UpdateUserProfileUseCase
import com.riramzy.pillfllow.ui.state.settings.PatientSettingsAction
import com.riramzy.pillfllow.ui.state.settings.PatientSettingsState
import com.riramzy.pillfllow.utils.app.AvatarMapper
import com.riramzy.pillfllow.utils.physics.PhysicsSensitivity
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

class PatientSettingsViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getPatientPairingStatusUseCase: GetPatientPairingStatusUseCase,
    private val generatePairingCodeUseCase: GeneratePairingCodeUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val getPhysicsSensitivityUseCase: GetPhysicsSensitivityUseCase,
    private val setPhysicsSensitivityUseCase: SetPhysicsSensitivityUseCase,
    private val logoutUseCase: LogoutUseCase
): ViewModel() {
    private val _state = MutableStateFlow(PatientSettingsState())
    val state: StateFlow<PatientSettingsState> = _state.asStateFlow()

    init {
        observePatientData()
    }

    init {
        viewModelScope.launch {
            getPhysicsSensitivityUseCase().collectLatest { saved ->
                _state.update { it.copy(physicsSensitivity = saved) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePatientData() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase()
                .filterNotNull()
                .flatMapLatest { user ->
                    val avatarRes = AvatarMapper.fromRaw(user.avatarRes)

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

    private fun onRequestChangeSensitivity(newSensitivity: PhysicsSensitivity) {
        if (newSensitivity != state.value.physicsSensitivity) {
            _state.update { it.copy(pendingSensitivity = newSensitivity) }
        }
    }

    private fun onConfirmChangeSensitivity() {
        val target = state.value.pendingSensitivity ?: return
        setPhysicsSensitivityUseCase(target)
        _state.update { it.copy(physicsSensitivity = target, pendingSensitivity = null) }
    }

    private fun onDismissSensitivityDialog() {
        _state.update { it.copy(pendingSensitivity = null) }
    }

    fun onAction(action: PatientSettingsAction) {
        when (action) {
            is PatientSettingsAction.RegenerateCode -> onRegenerateCode()
            is PatientSettingsAction.RequestChangeSensitivity -> onRequestChangeSensitivity(action.sensitivity)
            is PatientSettingsAction.ConfirmChangeSensitivity -> onConfirmChangeSensitivity()
            is PatientSettingsAction.DismissSensitivityDialog -> onDismissSensitivityDialog()
            is PatientSettingsAction.UpdateProfile -> onUpdateProfile(action.firstName, action.lastName, action.email, action.avatarRes)
            is PatientSettingsAction.DismissError -> onErrorDismissed()
            is PatientSettingsAction.SignOut -> onSignOut(action.onSignedOut)
        }
    }
}
