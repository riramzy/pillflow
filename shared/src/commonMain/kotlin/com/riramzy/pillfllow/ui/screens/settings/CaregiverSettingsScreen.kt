package com.riramzy.pillfllow.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.ui.components.custom.PillFlowBottomNavBar
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowPatientCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowSnackbar
import com.riramzy.pillfllow.ui.components.custom.PillFlowTopAppBar
import com.riramzy.pillfllow.ui.components.settings.PillFlowPatientPairingCard
import com.riramzy.pillfllow.ui.components.settings.PillFlowUserProfileCard
import com.riramzy.pillfllow.ui.components.sheets.ConfirmPairingSheet
import com.riramzy.pillfllow.ui.components.sheets.UpdateProfileSheet
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.settings.CaregiverSettingsState
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.settings.CaregiverSettingsViewModel
import com.riramzy.pillfllow.utils.Screen
import com.riramzy.pillfllow.utils.openPhoneDialer
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2

@Composable
fun CaregiverSettingsScreen(
    caregiverSettingsViewModel: CaregiverSettingsViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by caregiverSettingsViewModel.state.collectAsStateWithLifecycle()

    CaregiverSettingsScreenContent(
        state = state,
        onInputCodeChanged = caregiverSettingsViewModel::onInputCodeChanged,
        onInitiateLink = caregiverSettingsViewModel::onInitiateLink,
        onRelationChanged = caregiverSettingsViewModel::onRelationChanged,
        onConfirmLink = caregiverSettingsViewModel::onConfirmLink,
        onDismissConfirmSheet = caregiverSettingsViewModel::onDismissConfirmSheet,
        onUnpairPatient = caregiverSettingsViewModel::onUnpairPatient,
        onUpdateProfile = caregiverSettingsViewModel::onUpdateProfile,
        onErrorDismissed = caregiverSettingsViewModel::onErrorDismissed,
        onSuccessDismissed = caregiverSettingsViewModel::onSuccessDismissed,
        onSignOut = caregiverSettingsViewModel::onSignOut,
        onNavigateToHome = onNavigateToHome,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToPrescriptions = onNavigateToPrescriptions,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverSettingsScreenContent(
    state: CaregiverSettingsState = CaregiverSettingsState(),
    onInputCodeChanged: (String) -> Unit = {},
    onInitiateLink: () -> Unit = {},
    onRelationChanged: (String) -> Unit = {},
    onConfirmLink: () -> Unit = {},
    onDismissConfirmSheet: () -> Unit = {},
    onUnpairPatient: (String) -> Unit = {},
    onUpdateProfile: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onErrorDismissed: () -> Unit = {},
    onSuccessDismissed: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            onErrorDismissed()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { success ->
            snackbarHostState.showSnackbar(success)
            onSuccessDismissed()
        }
    }

    if (state.isConfirmSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismissConfirmSheet,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            ConfirmPairingSheet(
                patient = state.pendingPatientToLink,
                relation = state.relationInput,
                onRelationChange = onRelationChanged,
                onConfirmClick = onConfirmLink,
            )
        }
    }

    var showUpdateProfileSheet by remember { mutableStateOf(false) }

    if (showUpdateProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUpdateProfileSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            UpdateProfileSheet(
                initialFirstName = state.user?.firstName ?: "",
                initialLastName = state.user?.lastName ?: "",
                initialEmail = state.userEmail,
                initialAvatar = state.user?.avatarRes ?: "avatar1",
                onSaveProfile = { firstName, lastName, email, avatar ->
                    onUpdateProfile(firstName, lastName, email, avatar)
                    showUpdateProfileSheet = false
                },
            )
        }
    }

    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of your account?",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                PillFlowButton(
                    text = "Sign Out",
                    customColor = MaterialTheme.colorScheme.error,
                    customTextColor = MaterialTheme.colorScheme.onError,
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                )
            },
            dismissButton = {
                PillFlowButton(
                    text = "Cancel",
                    customColor = MaterialTheme.colorScheme.surface,
                    customTextColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { showSignOutDialog = false }
                )
            }
        )
    }

    var patientToUnlink by remember { mutableStateOf<PairedPatientUiModel?>(null) }

    if (patientToUnlink != null) {
        AlertDialog(
            onDismissRequest = { patientToUnlink = null },
            title = {
                Text(
                    text = "Unlink Patient",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                    },
            text = {
                Text(
                    text = "Are you sure you want to unlink ${patientToUnlink?.name}?",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                   },
            confirmButton = {
                PillFlowButton(
                    text = "Unlink",
                    customColor = MaterialTheme.colorScheme.tertiary,
                    customTextColor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = {
                        patientToUnlink?.let { onUnpairPatient(it.pairingId) }
                        patientToUnlink = null
                    }
                )
            },
            dismissButton = {
                PillFlowButton(
                    text = "Cancel",
                    customColor = MaterialTheme.colorScheme.surface,
                    customTextColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { patientToUnlink = null }
                )
            }
        )
    }

    Scaffold(
        topBar = { PillFlowTopAppBar(modifier = Modifier.padding(15.dp)) },
        floatingActionButton = {
            PillFlowBottomNavBar(
                selectedPage = Screen.Settings.route,
                onHomeClick = onNavigateToHome,
                onHistoryClick = onNavigateToHistory,
                onPrescriptionsClick = onNavigateToPrescriptions
            )
                               },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            ) { data ->
                PillFlowSnackbar(
                    snackbarData = data,
                    isError = state.errorMessage != null
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.statusBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Caregiver link codes, active patients & profile settings",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                PillFlowUserProfileCard(
                    userName = state.userName,
                    userEmail = state.userEmail,
                    avatar = state.avatarRes,
                    onEditClick = { showUpdateProfileSheet = true },
                    onSignOutClick = { showSignOutDialog = true },
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
            }

            item {
                PillFlowPatientPairingCard(
                    pairingCode = state.inputCode,
                    onCodeChange = { onInputCodeChanged(it) },
                    onLinkClick = onInitiateLink,
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Active Patients",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    state.activePatients.forEach { patient ->
                        PillFlowPatientCard(
                            patient = patient,
                            onPatientClick = {
                                if (patient.phoneNumber.isNotBlank()) {
                                    openPhoneDialer(patient.phoneNumber)
                                }
                            },
                            onUnlinkClick = { patientToUnlink = patient }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CaregiverSettingsScreenPreview() {
    PillFlowTheme {
        CaregiverSettingsScreenContent(
            state = CaregiverSettingsState(
                user = UserEntity(
                    id = "1",
                    firstName = "John",
                    lastName = "Doe",
                    email = "johndoe@gmai.com",
                    phoneNumber = "1234567890",
                    avatarRes = "avatar1",
                    createdAt = 1234567890L,
                    userType = "CAREGIVER"
                ),
                userName = "John Doe",
                userEmail = "johndoe@gmai.com",
                activePatients = listOf(
                    PairedPatientUiModel(
                        id = "1",
                        name = "Mary",
                        relation = "Mom",
                        avatar = Res.drawable.avatar2
                    ),
                    PairedPatientUiModel(
                        id = "2",
                        name = "John",
                        relation = "Dad",
                        avatar = Res.drawable.avatar1
                    ),
                ),
                isLoading = false,
                errorMessage = null
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CaregiverSettingsScreenPreviewDark() {
    PillFlowTheme {
        CaregiverSettingsScreenContent(
            state = CaregiverSettingsState(
                user = UserEntity(
                    id = "1",
                    firstName = "John",
                    lastName = "Doe",
                    email = "johndoe@gmai.com",
                    phoneNumber = "1234567890",
                    avatarRes = "avatar1",
                    createdAt = 1234567890L,
                    userType = "CAREGIVER"
                ),
                userName = "John Doe",
                userEmail = "johndoe@gmai.com",
                activePatients = listOf(
                    PairedPatientUiModel(
                        id = "1",
                        name = "Mary",
                        relation = "Mom",
                        avatar = Res.drawable.avatar2
                    ),
                    PairedPatientUiModel(
                        id = "2",
                        name = "John",
                        relation = "Dad",
                        avatar = Res.drawable.avatar1
                    ),
                ),
                isLoading = false,
                errorMessage = null
            )
        )
    }
}