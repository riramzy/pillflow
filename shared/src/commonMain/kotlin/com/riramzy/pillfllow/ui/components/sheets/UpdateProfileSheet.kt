package com.riramzy.pillfllow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowAvatarSelector
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowInputField
import com.riramzy.pillfllow.ui.theme.PillFlowTheme

@Composable
fun UpdateProfileSheet(
    initialFirstName: String,
    initialLastName: String,
    initialEmail: String,
    initialAvatar: String,
    onSaveProfile: (firstName: String, lastName: String, email: String, avatarRes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var email by remember { mutableStateOf(initialEmail) }
    var selectedAvatar by remember { mutableStateOf(initialAvatar) }

    val isFirstLetterCapital = (firstName.firstOrNull()?.isUpperCase() == true) &&
            (lastName.firstOrNull()?.isUpperCase() == true)

    val isFormValid = firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            email.isNotBlank() &&
            isFirstLetterCapital

    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Save Profile Changes",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to update your profile with these changes?",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                PillFlowButton(
                    text = "Confirm",
                    onClick = {
                        showConfirmDialog = false
                        onSaveProfile(firstName.trim(), lastName.trim(), email.trim(), selectedAvatar)
                    }
                )
            },
            dismissButton = {
                PillFlowButton(
                    text = "Cancel",
                    customColor = MaterialTheme.colorScheme.surface,
                    customTextColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { showConfirmDialog = false }
                )
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(15.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        Column {
            Text(
                text = "Update Profile",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Customize your avatar and profile information",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            PillFlowInputField(
                label = "First Name",
                value = firstName,
                placeholder = "Enter First Name",
                onValueChange = { firstName = it },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            PillFlowInputField(
                label = "Last Name",
                value = lastName,
                placeholder = "Enter Last Name",
                onValueChange = { lastName = it },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            PillFlowInputField(
                label = "Email",
                placeholder = "Enter Email",
                value = email,
                onValueChange = { email = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            PillFlowAvatarSelector(
                selectedAvatar = selectedAvatar,
                onAvatarSelected = { selectedAvatar = it }
            )
        }

        PillFlowButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Save Changes",
            isEnabled = isFormValid,
            onClick = { showConfirmDialog = true }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateProfileSheetPreview() {
    PillFlowTheme {
        UpdateProfileSheet(
            initialFirstName = "",
            initialLastName = "",
            initialAvatar = "",
            initialEmail = "",
            onSaveProfile = { _, _, _, _ -> },
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun UpdateProfileSheetPreviewDark() {
    PillFlowTheme {
        UpdateProfileSheet(
            initialFirstName = "",
            initialLastName = "",
            initialAvatar = "",
            initialEmail = "",
            onSaveProfile = { _, _, _, _ -> },
        )
    }
}