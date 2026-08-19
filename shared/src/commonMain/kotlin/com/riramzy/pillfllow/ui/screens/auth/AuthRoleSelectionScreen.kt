package com.riramzy.pillfllow.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.ui.components.auth.PillFlowUserTypeCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.state.auth.AuthState
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.auth.AuthViewModel
import com.riramzy.pillfllow.utils.UserType
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.pillflow_logo

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
    state: AuthState = AuthState(),
    onRoleSelected: (UserType) -> Unit = {},
    onContinueClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .wrapContentSize()
            ) {
                Image(
                    painter = painterResource(Res.drawable.pillflow_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(50.dp)
                )

                Text(
                    text = "PillFlow",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Medication adherence, reimagined",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary.copy(0.5f)
            )
        }

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
                        onSelect = { onRoleSelected(UserType.PATIENT) }
                    )

                    PillFlowUserTypeCard(
                        type = UserType.CAREGIVER,
                        isSelected = state.selectedRole == UserType.CAREGIVER,
                        onSelect = { onRoleSelected(UserType.CAREGIVER) }
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