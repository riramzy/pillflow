package com.riramzy.pillfllow.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.ui.state.settings.PatientSettingsState
import com.riramzy.pillfllow.utils.PhysicsSensitivity
import com.riramzy.pillfllow.utils.currentTimeMillis
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
    private val authRepo: AuthRepo,
    private val pairingRepo: PairingRepo,
    private val userRepo: UserRepo
): ViewModel() {
    private val _state = MutableStateFlow(PatientSettingsState())
    val state: StateFlow<PatientSettingsState> = _state.asStateFlow()

    init {
        observePatientData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePatientData() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.currentUser
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

                    pairingRepo.getPairingsForPatient(user.id)
                }.collectLatest { pairings ->
                    val pending = pairings.firstOrNull { it.status == "PENDING" }

                    if (pending != null) {
                        _state.update { it.copy(pairingCode = pending.pairingCode) }
                    } else {
                        generateCodeForUser(user = state.value.user ?: return@collectLatest)
                    }
                }
        }
    }

    fun onRegenerateCode() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = state.value.user ?: return@launch
            _state.update { it.copy(isRegenerating = true) }
            generateCodeForUser(user)
        }
    }

    fun onSensitivitySelected(sensitivity: PhysicsSensitivity) {
        _state.update { it.copy(physicsSensitivity = sensitivity) }
    }

    fun onUpdateProfile(firstName: String, lastName: String, email: String, avatarRes: String) {
        val user = state.value.user ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updatedUser = user.copy(
                firstName = firstName,
                lastName = lastName,
                email = email,
                avatarRes = avatarRes
            )
            userRepo.updateUser(updatedUser)
        }
    }

    fun onErrorDismissed() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSignOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.signOut()
            onSignedOut()
        }
    }

    private suspend fun generateCodeForUser(user: UserEntity) {
        val newCode = (100000..999999).random().toString()

        pairingRepo.deletePendingPairingsForPatient(user.id)

        val pairing = CaregiverPatientPairingEntity(
            pairingId = "pair_${currentTimeMillis()}",
            caregiverId = user.id,
            patientId = user.id,
            phoneNumber = user.phoneNumber,
            relation = "Patient",
            pairingCode = newCode,
            status = "PENDING",
            createdAt = currentTimeMillis()
        )

        pairingRepo.insertPairing(pairing)
        _state.update { it.copy(pairingCode = newCode, isRegenerating = false) }
    }
}
