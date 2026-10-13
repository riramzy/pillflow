package com.riramzy.pillfllow.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.riramzy.pillfllow.ui.components.auth.PillFlowAuthHeader
import com.riramzy.pillfllow.ui.components.auth.PillFlowUserTypeCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.state.auth.AuthState
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.auth.AuthViewModel
import com.riramzy.pillfllow.utils.user.UserType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoleSelectionScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    onRoleSelected: (UserType) -> Unit = {},
) {
    val state by authViewModel.state.collectAsStateWithLifecycle()

    AuthRoleSelectionScreenContent(
        state = state,
        onRoleSelected = { authViewModel.onRoleSelected(it) },
        onContinueClick = { onRoleSelected(state.selectedRole) }
    )
}

@Composable
fun AuthRoleSelectionScreenContent(
    modifier: Modifier = Modifier,
    state: AuthState = AuthState(),
    onRoleSelected: (UserType) -> Unit = {},
    onContinueClick: () -> Unit = {},
) {
    var infoDialogRole by remember { mutableStateOf<UserType?>(null) }

    if (infoDialogRole != null) {
        val isCaregiver = infoDialogRole == UserType.CAREGIVER

        AlertDialog(
            onDismissRequest = { infoDialogRole = null },
            shape = RoundedCornerShape(25.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = if (isCaregiver) "Caregiver Account" else "Patient Account",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = if (isCaregiver) {
                        "Designed for family members or caregivers who want to:\n\n" +
                                "• Monitor loved ones' adherence in real-time\n" +
                                "• Receive alerts for late or missed doses\n" +
                                "• Send gentle reminder nudges\n" +
                                "• Manage prescriptions and review monthly history"
                    } else {
                        "Designed for individuals managing medications who want to:\n\n" +
                                "• Track daily doses with interactive dish physics\n" +
                                "• Receive exact schedule alarm notifications\n" +
                                "• Generate pairing codes to link with family caregivers\n" +
                                "• Review personal monthly adherence streaks"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                PillFlowButton(
                    text = "Select",
                    onClick = {
                        onRoleSelected(infoDialogRole!!)
                        infoDialogRole = null
                    }
                )
            },
            dismissButton = {
                PillFlowButton(
                    text = "Close",
                    onClick = { infoDialogRole = null },
                    customColor = MaterialTheme.colorScheme.surface,
                    customTextColor = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface
            )
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        PillFlowAuthHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Center)
                .padding(15.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Who are you?",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Select the role that best describes you",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PillFlowUserTypeCard(
                        type = UserType.PATIENT,
                        isSelected = state.selectedRole == UserType.PATIENT,
                        onSelect = { onRoleSelected(UserType.PATIENT) },
                        onInfoClick = { infoDialogRole = UserType.PATIENT }
                    )

                    PillFlowUserTypeCard(
                        type = UserType.CAREGIVER,
                        isSelected = state.selectedRole == UserType.CAREGIVER,
                        onSelect = { onRoleSelected(UserType.CAREGIVER) },
                        onInfoClick = { infoDialogRole = UserType.CAREGIVER }
                    )
                }

                PillFlowButton(
                    text = "Continue",
                    onClick = { onContinueClick() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthRoleSelectionScreenPreview() {
    PillFlowTheme {
        AuthRoleSelectionScreenContent()
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AuthRoleSelectionScreenPreviewDark() {
    PillFlowTheme {
        AuthRoleSelectionScreenContent()
    }
}