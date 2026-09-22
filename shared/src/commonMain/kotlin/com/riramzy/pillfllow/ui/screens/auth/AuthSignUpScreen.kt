package com.riramzy.pillfllow.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.riramzy.pillfllow.ui.components.auth.PillFlowAuthHeader
import com.riramzy.pillfllow.ui.components.custom.PillFlowAvatarSelector
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowInputField
import com.riramzy.pillfllow.ui.components.custom.PillFlowPhoneInputField
import com.riramzy.pillfllow.ui.components.custom.PillFlowSnackbar
import com.riramzy.pillfllow.ui.state.auth.AuthAction
import com.riramzy.pillfllow.ui.state.auth.AuthState
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.auth.AuthViewModel
import com.riramzy.pillfllow.utils.UserType
import org.koin.compose.viewmodel.koinViewModel

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
        onAuthSuccess = onAuthSuccess,
        onNavigateToSignIn = onNavigateToSignIn,
        onAction = authViewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun AuthSignUpScreenContent(
    state: AuthState = AuthState(),
    onAuthSuccess: (UserType) -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    onAction: (AuthAction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            onAction(AuthAction.DismissError)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface
            ),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                PillFlowAuthHeader()
            }
        },
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
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding()
                .padding(15.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
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
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PillFlowAvatarSelector(
                            selectedAvatar = state.selectedAvatar,
                            onAvatarSelected = { onAction(AuthAction.AvatarSelected(it)) },
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        PillFlowInputField(
                            label = "First Name",
                            placeholder = "Enter your first name",
                            value = state.firstName,
                            onValueChange = { onAction(AuthAction.FirstNameChanged(it)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                        )

                        PillFlowInputField(
                            label = "Last Name",
                            placeholder = "Enter your last name",
                            value = state.lastName,
                            onValueChange = { onAction(AuthAction.LastNameChanged(it)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                        )

                        PillFlowPhoneInputField(
                            selectedCountry = state.selectedCountry,
                            onCountrySelected = { onAction(AuthAction.CountrySelected(it)) },
                            phoneNumber = state.phoneNumber,
                            onPhoneNumberChange = { onAction(AuthAction.PhoneNumberChanged(it)) }
                        )

                        PillFlowInputField(
                            label = "Email",
                            placeholder = "Enter your email",
                            value = state.email,
                            onValueChange = { onAction(AuthAction.EmailChanged(it)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )

                        PillFlowInputField(
                            label = "Password",
                            placeholder = "Enter your password",
                            value = state.password,
                            onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
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
                            onValueChange = { onAction(AuthAction.ConfirmPasswordChanged(it)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
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
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PillFlowButton(
                            text = if (state.isLoading) "Creating Account..." else "Sign Up",
                            isEnabled = !state.isLoading,
                            onClick = { onAction(AuthAction.SignUp(onSuccess = { onAuthSuccess(state.selectedRole) })) },
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
                                .clickable {
                                    onAction(AuthAction.ToggleAuthMode)
                                    onNavigateToSignIn()
                                }
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