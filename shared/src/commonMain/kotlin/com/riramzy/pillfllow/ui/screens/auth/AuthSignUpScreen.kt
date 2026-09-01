package com.riramzy.pillfllow.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowInputField
import com.riramzy.pillfllow.ui.components.custom.PillFlowSnackbar
import com.riramzy.pillfllow.ui.state.auth.AuthState
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.auth.AuthViewModel
import com.riramzy.pillfllow.utils.UserType
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.pillflow_logo

@Composable
fun AuthSignUpScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    selectedRole: UserType = UserType.PATIENT,
    onAuthSuccess: (UserType) -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by authViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(selectedRole) {
        authViewModel.onRoleSelected(selectedRole)
    }

    AuthSignUpScreenContent(
        state = state,
        onFirstNameChanged = authViewModel::onFirstNameChanged,
        onLastNameChanged = authViewModel::onLastNameChanged,
        onEmailChanged = authViewModel::onEmailChanged,
        onPasswordChanged = authViewModel::onPasswordChanged,
        onConfirmPasswordChanged = authViewModel::onConfirmPasswordChanged,
        onToggleAuthMode = {
            authViewModel.onToggleAuthMode()
            onNavigateToSignIn()
        },
        onSubmit = {
            authViewModel.signUp(
                onSuccess = { onAuthSuccess(state.selectedRole) }
            )
        },
        onErrorDismissed = authViewModel::onErrorDismissed,
        modifier = modifier
    )
}

@Composable
fun AuthSignUpScreenContent(
    state: AuthState = AuthState(),
    onFirstNameChanged: (String) -> Unit = {},
    onLastNameChanged: (String) -> Unit = {},
    onEmailChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onConfirmPasswordChanged: (String) -> Unit = {},
    onToggleAuthMode: () -> Unit = {},
    onSubmit: () -> Unit = {},
    onErrorDismissed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            onErrorDismissed()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface
            ),
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = 40.dp)
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
                    verticalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Sign Up as a ${state.selectedRole.label}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = when (state.selectedRole) {
                                UserType.PATIENT -> "Schedule your pills and add some fun to the process"
                                UserType.CAREGIVER -> "Support your loved ones and stay on top of their medication schedules"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PillFlowInputField(
                            label = "First Name",
                            placeholder = "Enter your first name",
                            value = state.firstName,
                            onValueChange = { onFirstNameChanged(it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                        )

                        PillFlowInputField(
                            label = "Last Name",
                            placeholder = "Enter your last name",
                            value = state.lastName,
                            onValueChange = { onLastNameChanged(it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                        )

                        PillFlowInputField(
                            label = "Email",
                            placeholder = "Enter your email",
                            value = state.email,
                            onValueChange = { onEmailChanged(it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                        )

                        PillFlowInputField(
                            label = "Password",
                            placeholder = "Enter your password",
                            value = state.password,
                            onValueChange = { onPasswordChanged(it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibility Toggle",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { passwordVisible = !passwordVisible }
                                )
                            }
                        )

                        PillFlowInputField(
                            label = "Confirm Password",
                            placeholder = "Confirm your password",
                            value = state.confirmPassword,
                            onValueChange = { onConfirmPasswordChanged(it) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibility Toggle",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { confirmPasswordVisible = !confirmPasswordVisible }
                                )
                            }
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PillFlowButton(
                            text = if (state.isLoading) "Creating Account..." else "Sign Up",
                            isEnabled = !state.isLoading,
                            onClick = { onSubmit() },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Already have an account? Sign In",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleAuthMode() }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AuthSignUpScreenPreview() {
    PillFlowTheme {
        AuthSignUpScreenContent()
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AuthSignUpScreenPreviewDark() {
    PillFlowTheme {
        AuthSignUpScreenContent()
    }
}