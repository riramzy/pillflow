package com.riramzy.pillfllow.ui.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import com.riramzy.pillfllow.utils.user.AvatarMapper
import org.jetbrains.compose.resources.painterResource

@Composable
fun QuickProfileSheet(
    user: UserEntity?,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    customColor = IndicatorColor.RED_CONTAINER.color,
                    customTextColor = IndicatorColor.RED.color,
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
                    .size(70.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(AvatarMapper.fromRaw(user?.avatarRes)),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim().ifEmpty { "User" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Text(
                    text = user?.email.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp
                )

                Box(
                    modifier = Modifier
                        .wrapContentHeight()
                        .clip(shape = CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.userType ?: "USER",
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PillFlowButton(
                text = "Go To Settings",
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth()
            )

            PillFlowButton(
                text = "Sign Out",
                onClick = { showSignOutDialog = true },
                customColor = IndicatorColor.RED_CONTAINER.color,
                customTextColor = IndicatorColor.RED.color,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun QuickProfileSheetPreview() {
    PillFlowTheme {
        QuickProfileSheet(
            user = UserEntity(
                id = "1",
                firstName = "John",
                lastName = "Doe",
                email = "john.quincy.adams@examplepetstore.com",
                userType = "USER",
                avatarRes = "0",
                createdAt = 0L,
            ),
            onOpenSettings = {},
            onSignOut = {}
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun QuickProfileSheetPreviewDark() {
    PillFlowTheme {
        QuickProfileSheet(
            user = UserEntity(
                id = "1",
                firstName = "John",
                lastName = "Doe",
                email = "john.quincy.adams@examplepetstore.com",
                userType = "USER",
                avatarRes = "0",
                createdAt = 0L,
            ),
            onOpenSettings = {},
            onSignOut = {}
        )
    }
}