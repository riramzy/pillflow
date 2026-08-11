package com.riramzy.pillfllow.ui.components.auth

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.UserType
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.question
import pillfllow.shared.generated.resources.user_caregiver
import pillfllow.shared.generated.resources.user_patient

@Composable
fun PillFlowUserTypeCard(
    modifier: Modifier = Modifier,
    type: UserType = UserType.PATIENT,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor =
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(0.6f)
                },
            contentColor =
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
        ),
        shape = RoundedCornerShape(50.dp),
        onClick = { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary.copy(0.2f)
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(0.2f)
                                },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(
                            when (type) {
                                UserType.CAREGIVER -> Res.drawable.user_caregiver
                                UserType.PATIENT -> Res.drawable.user_patient
                            }
                        ),
                        modifier = Modifier.size(26.dp),
                        contentDescription = "null"
                    )
                }

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (type == UserType.CAREGIVER) "I'm a Caregiver" else "I'm a User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )

                    Text(
                        text = if (type == UserType.CAREGIVER) "Monitor your loved ones remotely" else "Track your daily medications",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                    )
                }
            }

            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Done else vectorResource(Res.drawable.question),
                    modifier = Modifier.size(20.dp),
                    contentDescription = "null"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowUserTypeCardPreview() {
    PillFlowTheme {
        var selectedType by remember { mutableStateOf(UserType.CAREGIVER) }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            PillFlowUserTypeCard(
                type = UserType.CAREGIVER,
                isSelected = selectedType == UserType.CAREGIVER,
                onSelect = { selectedType = UserType.CAREGIVER }
            )
            PillFlowUserTypeCard(
                type = UserType.PATIENT,
                isSelected = selectedType == UserType.PATIENT,
                onSelect = { selectedType = UserType.PATIENT }
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowUserTypeCardPreviewDark() {
    PillFlowTheme {
        var selectedType by remember { mutableStateOf(UserType.CAREGIVER) }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            PillFlowUserTypeCard(
                type = UserType.CAREGIVER,
                isSelected = selectedType == UserType.CAREGIVER,
                onSelect = { selectedType = UserType.CAREGIVER }
            )
            PillFlowUserTypeCard(
                type = UserType.PATIENT,
                isSelected = selectedType == UserType.PATIENT,
                onSelect = { selectedType = UserType.PATIENT }
            )
        }
    }
}