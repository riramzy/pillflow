package com.riramzy.pillfllow.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.data.local.entity.CaregiverPatientPairingEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.MedicationRepo
import com.riramzy.pillfllow.domain.repo.PairingRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.settings.CaregiverSettingsState
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
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
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val pairingRepo: PairingRepo,
    private val medicationRepo: MedicationRepo
): ViewModel() {
    private val _state = MutableStateFlow(CaregiverSettingsState())
    val state: StateFlow<CaregiverSettingsState> = _state.asStateFlow()

    init {
        observeCaregiverData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCaregiverData() {
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

                    pairingRepo.getPairingsForCaregiver(user.id)
                }.collectLatest { pairings ->
                    val now = currentTimeMillis()
                    val graceWindowMillis = 30 * 60 * 1000L

                    val patientModels = pairings
                        .filter { it.status == "ACTIVE" }
                        .mapNotNull { pairing ->
                            val patient = userRepo.getUserByIdOnce(pairing.patientId)

                            patient?.let { user ->
                                val doses = medicationRepo.getPendingDosesForUser(user.id).firstOrNull() ?: emptyList()

                                val missedCount = doses.count { now - it.scheduledTime > graceWindowMillis }
                                val lateCount = doses.count { now >= it.scheduledTime && (now - it.scheduledTime) <= graceWindowMillis }

                                val patientStatus = when {
                                    missedCount > 0 -> ComplianceStatus.MISSED
                                    lateCount > 0 -> ComplianceStatus.LATE
                                    else -> ComplianceStatus.ON_TIME
                                }

                                val patientScore = if (doses.isEmpty()) 100 else (100 - ((missedCount * 100) / doses.size))

                                val avatar = when (user.avatarRes) {
                                    "avatar1" -> Res.drawable.avatar1
                                    "avatar2" -> Res.drawable.avatar2
                                    "avatar3" -> Res.drawable.avatar3
                                    "avatar4" -> Res.drawable.avatar4
                                    "avatar5" -> Res.drawable.avatar5
                                    "avatar6" -> Res.drawable.avatar6
                                    "avatar7" -> Res.drawable.avatar7
                                    else -> Res.drawable.avatar8
                                }

                                PairedPatientUiModel(
                                    pairingId = pairing.pairingId,
                                    id = user.id,
                                    name = user.firstName,
                                    relation = pairing.relation,
                                    phoneNumber = pairing.phoneNumber,
                                    avatar = avatar,
                                    status = patientStatus,
                                    lateDosesCount = lateCount,
                                    missedDosesCount = missedCount,
                                    compliancePercentage = patientScore
                                )
                            }
                        }

                    _state.update { it.copy(activePatients = patientModels) }
                }
        }
    }

    fun onInputCodeChanged(newCode: String) {
        val sanitizedCode = newCode.filter { it.isDigit() }.take(6)
        _state.update { it.copy(inputCode = sanitizedCode) }
    }

    fun onInitiateLink() {
        viewModelScope.launch(Dispatchers.IO) {
            if (state.value.inputCode.length != 6) {
                _state.update { it.copy(errorMessage = "Invalid Pairing Code") }
                return@launch
            } else {
                val pendingPairing = pairingRepo.getPendingPairingByCode(state.value.inputCode)

                if (pendingPairing == null) {
                    _state.update { it.copy(errorMessage = "Invalid Pairing Code") }
                } else {
                    val patient = userRepo.getUserByIdOnce(pendingPairing.patientId)

                    _state.update {
                        it.copy(
                            isConfirmSheetOpen = true,
                            pendingPatientToLink = patient,
                            errorMessage = null,
                            isLinking = true
                        )
                    }
                }
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
            pairingRepo.deletePendingPairingsForPatient(pendingPatient.id)

            val activePairing = CaregiverPatientPairingEntity(
                pairingId = "pair_${currentTimeMillis()}",
                caregiverId = caregiverId,
                patientId = pendingPatient.id,
                phoneNumber = pendingPatient.phoneNumber,
                relation = relation,
                pairingCode = code,
                status = "ACTIVE",
                createdAt = currentTimeMillis()
            )

            pairingRepo.insertPairing(activePairing)

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

    fun onUnpairPatient(pairingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pairingRepo.deletePairingById(pairingId)
        }
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

    fun onSuccessDismissed() {
        _state.update { it.copy(successMessage = null) }
    }

    fun onSignOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.signOut()
            onSignedOut()
        }
    }
}