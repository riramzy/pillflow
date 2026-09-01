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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.riramzy.pillfllow.ui.components.custom.PillFlowSnackbar
import com.riramzy.pillfllow.ui.components.custom.PillFlowTopAppBar
import com.riramzy.pillfllow.ui.components.settings.PillFlowPairingCard
import com.riramzy.pillfllow.ui.components.settings.PillFlowPhysicsSensitivityCard
import com.riramzy.pillfllow.ui.components.settings.PillFlowUserProfileCard
import com.riramzy.pillfllow.ui.components.sheets.UpdateProfileSheet
import com.riramzy.pillfllow.ui.state.settings.PatientSettingsState
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.settings.PatientSettingsViewModel
import com.riramzy.pillfllow.utils.PhysicsSensitivity
import com.riramzy.pillfllow.utils.Screen
import com.riramzy.pillfllow.utils.copyToClipboard
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PatientSettingsScreen(
    patientSettingsViewModel: PatientSettingsViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by patientSettingsViewModel.state.collectAsStateWithLifecycle()

    PatientSettingsScreenContent(
        state = state,
        onRegenerateCode = patientSettingsViewModel::onRegenerateCode,
        onSensitivitySelected = patientSettingsViewModel::onSensitivitySelected,
        onUpdateProfile = patientSettingsViewModel::onUpdateProfile,
        onErrorDismissed = patientSettingsViewModel::onErrorDismissed,
        onSignOut = patientSettingsViewModel::onSignOut,
        onNavigateToHome = onNavigateToHome,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToPrescriptions = onNavigateToPrescriptions,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSettingsScreenContent(
    state: PatientSettingsState = PatientSettingsState(),
    onRegenerateCode: () -> Unit = {},
    onSensitivitySelected: (PhysicsSensitivity) -> Unit = {},
    onUpdateProfile: (firstName: String, lastName: String, email: String, avatarRes: String) -> Unit = { _, _, _, _ -> },
    onErrorDismissed: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            onErrorDismissed()
        }
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
                        text = "Link codes, sensor calibration & profile settings",
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
                PillFlowPairingCard(
                    pairingCode = state.pairingCode,
                    onCopyClick = {
                        if (state.pairingCode.isNotBlank()) {
                            copyToClipboard(state.pairingCode)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Pairing code copied to clipboard")
                            }
                        }
                    },
                    onRegenerateClick = { onRegenerateCode() },
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
            }

            item {
                PillFlowPhysicsSensitivityCard(
                    selectedSensitivity = state.physicsSensitivity,
                    onSensitivitySelected = { onSensitivitySelected(it) },
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun PatientSettingsScreenPreview() {
    PillFlowTheme {
        PatientSettingsScreenContent(
            state = PatientSettingsState(
                user = UserEntity(
                    id = "1",
                    firstName = "John",
                    lastName = "Doe",
                    email = "johndoe@gmai.com",
                    phoneNumber = "1234567890",
                    avatarRes = "avatar1",
                    createdAt = 1234567890L,
                    userType = "PATIENT"
                ),
                userName = "John Doe",
                userEmail = "johndoe@gmai.com",
                pairingCode = "123456",
                physicsSensitivity = PhysicsSensitivity.NORMAL,
                isLoading = false,
                isRegenerating = false,
                errorMessage = null
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PatientSettingsScreenPreviewDark() {
    PillFlowTheme {
        PatientSettingsScreenContent(
            state = PatientSettingsState(
                user = UserEntity(
                    id = "1",
                    firstName = "John",
                    lastName = "Doe",
                    email = "johndoe@gmai.com",
                    phoneNumber = "1234567890",
                    avatarRes = "avatar1",
                    createdAt = 1234567890L,
                    userType = "PATIENT"
                ),
                userName = "John Doe",
                userEmail = "johndoe@gmai.com",
                pairingCode = "123456",
                physicsSensitivity = PhysicsSensitivity.NORMAL,
                isLoading = false,
                isRegenerating = false,
                errorMessage = null
            )
        )
    }
}