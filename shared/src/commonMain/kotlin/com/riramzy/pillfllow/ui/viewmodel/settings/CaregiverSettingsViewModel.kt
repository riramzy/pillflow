package com.riramzy.pillfllow.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.LogoutUseCase
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.ConfirmPairingUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.InitiatePairingUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.UnlinkPatientUseCase
import com.riramzy.pillfllow.domain.usecase.patient.UpdateUserProfileUseCase
import com.riramzy.pillfllow.ui.state.settings.CaregiverSettingsAction
import com.riramzy.pillfllow.ui.state.settings.CaregiverSettingsState
import com.riramzy.pillfllow.utils.Result
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

class CaregiverSettingsViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val initiatePairingUseCase: InitiatePairingUseCase,
    private val confirmPairingUseCase: ConfirmPairingUseCase,
    private val unlinkPatientUseCase: UnlinkPatientUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
): ViewModel() {
    private val _state = MutableStateFlow(CaregiverSettingsState())
    val state: StateFlow<CaregiverSettingsState> = _state.asStateFlow()

    init {
        observeCaregiverData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCaregiverData() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().filterNotNull().flatMapLatest { user ->
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

                getCaregiverPatientsUseCase(user.id)
            }.collectLatest { patients ->
                _state.update { it.copy(activePatients = patients) }
            }
        }
    }

    fun onInputCodeChanged(newCode: String) {
        val sanitizedCode = newCode.filter { it.isDigit() }.take(6)
        _state.update { it.copy(inputCode = sanitizedCode) }
    }

    fun onInitiateLink(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLinking = true, errorMessage = null) }

            when (val result = initiatePairingUseCase(code)) {
                is Result.Success -> {
                    val (_, patient) = result.data
                    _state.update {
                        it.copy(
                            isConfirmSheetOpen = true,
                            pendingPatientToLink = patient,
                            isLinking = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLinking = false,
                            errorMessage = result.message ?: "Invalid pairing code"
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun onRelationChanged(newRelation: String) {
        _state.update { it.copy(relationInput = newRelation) }
    }

    fun onConfirmLink() {
        val caregiverId = state.value.user?.id ?: return
        val pendingPatient = state.value.pendingPatientToLink ?: return
        val relation = state.value.relationInput.ifBlank { "Patient" }
        val code = state.value.inputCode

        viewModelScope.launch(Dispatchers.IO) {
            confirmPairingUseCase(caregiverId, pendingPatient, relation, code)

            _state.update {
                it.copy(
                    isConfirmSheetOpen = false,
                    pendingPatientToLink = null,
                    relationInput = "",
                    inputCode = "",
                    isLinking = false,
                    successMessage = "Patient linked successfully"
                )
            }
        }
    }

    fun onDismissConfirmSheet() {
        _state.update {
            it.copy(
                isConfirmSheetOpen = false,
                pendingPatientToLink = null,
                isLinking = false
            )
        }
    }

    fun onUnpairPatient(patientId: String, pairingId: String) {
        val caregiverId = state.value.user?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            unlinkPatientUseCase(caregiverId, patientId, pairingId)
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

    fun onSuccessDismissed() {
        _state.update { it.copy(successMessage = null) }
    }

    fun onSignOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch {
            logoutUseCase()
            onSignedOut()
        }
    }

    fun onAction(action: CaregiverSettingsAction) {
        when (action) {
            is CaregiverSettingsAction.InputCodeChanged -> onInputCodeChanged(action.code)
            is CaregiverSettingsAction.InitiateLink -> onInitiateLink(action.code)
            is CaregiverSettingsAction.RelationChanged -> onRelationChanged(action.relation)
            is CaregiverSettingsAction.ConfirmLink -> onConfirmLink()
            is CaregiverSettingsAction.DismissConfirmSheet -> onDismissConfirmSheet()
            is CaregiverSettingsAction.UnpairPatient -> onUnpairPatient(action.patientId, action.pairingId)
            is CaregiverSettingsAction.UpdateProfile -> onUpdateProfile(action.firstName, action.lastName, action.email, action.avatarRes)
            is CaregiverSettingsAction.DismissError -> onErrorDismissed()
            is CaregiverSettingsAction.DismissSuccess -> onSuccessDismissed()
            is CaregiverSettingsAction.SignOut -> onSignOut(action.onSignedOut)
        }
    }
}