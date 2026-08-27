package com.riramzy.pillfllow.ui.components.sheets

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowInputField
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.avatar3
import pillfllow.shared.generated.resources.avatar4
import pillfllow.shared.generated.resources.avatar5
import pillfllow.shared.generated.resources.avatar6
import pillfllow.shared.generated.resources.avatar7
import pillfllow.shared.generated.resources.avatar8

@Composable
fun ConfirmPairingSheet(
    patient: UserEntity? = null,
    relation: String = "Mom",
    onRelationChange: (String) -> Unit = {},
    onConfirmClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val avatarDrawable = when (patient?.avatarRes) {
        "avatar2" -> Res.drawable.avatar2
        "avatar3" -> Res.drawable.avatar3
        "avatar4" -> Res.drawable.avatar4
        "avatar5" -> Res.drawable.avatar5
        "avatar6" -> Res.drawable.avatar6
        "avatar7" -> Res.drawable.avatar7
        "avatar8" -> Res.drawable.avatar8
        else -> Res.drawable.avatar1
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
                text = "Confirm Patient Link",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Verify patient identity and set your relationship",
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(avatarDrawable),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(35.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${patient?.firstName} ${patient?.lastName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = patient?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }


            }

            PillFlowInputField(
                label = "Relationship with Patient",
                placeholder = "e.g. Mom, Dad, Aunt, Patient",
                value = relation,
                onValueChange = { onRelationChange(it) }
            )
        }

        PillFlowButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Confirm and Link",
            isEnabled = relation.isNotBlank(),
            onClick = {
                onConfirmClick()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmPairingSheetPreview() {
    PillFlowTheme {
        ConfirmPairingSheet(
            patient = UserEntity(
                id = "1",
                firstName = "Ramzy",
                lastName = "Habel",
                email = "william.a.wheeler@example-pet-store.com",
                userType = "patient",
                avatarRes = "avatar1",
                createdAt = 0L
            )
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun ConfirmPairingSheetPreviewDark() {
    PillFlowTheme {
        ConfirmPairingSheet(
            patient = UserEntity(
                id = "1",
                firstName = "Ramzy",
                lastName = "Habel",
                email = "william.a.wheeler@example-pet-store.com",
                userType = "patient",
                avatarRes = "avatar1",
                createdAt = 0L
            )
        )
    }
}