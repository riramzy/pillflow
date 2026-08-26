package com.riramzy.pillfllow.ui.components.prescriptions

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillColor
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillShape
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.PillColor
import com.riramzy.pillfllow.utils.PillShape
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.delete
import pillfllow.shared.generated.resources.edit
import pillfllow.shared.generated.resources.next
import pillfllow.shared.generated.resources.note
import pillfllow.shared.generated.resources.time

@Composable
fun PillFlowPrescriptionCard(
    medicationName: String = "Metformin",
    dosage: String = "500mg",
    pillShape: PillShape = PillShape.CAPSULE,
    pillColor: PillColor = PillColor.SKY_BLUE,
    scheduleText: String = "Twice Daily, 08:00 AM, 08:00 PM",
    instructionsText: String = "Take with a full glass of water",
    nextDoseText: String = "Next dose in 2 hours",
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillFlowPillColor(
                        customColor = pillColor.color,
                    )

                    PillFlowPillShape(
                        shape = pillShape
                    )

                    Text(
                        text = "$medicationName, $dosage",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { onEditClick() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .size(35.dp)
                    ) {
                        Image(
                            imageVector = vectorResource(Res.drawable.edit),
                            contentDescription = "Edit",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDeleteClick() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .size(35.dp)
                    ) {
                        Image(
                            imageVector = vectorResource(Res.drawable.delete),
                            contentDescription = "Delete",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .size(14.dp)
                        )
                    }
                }

            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                PrescriptionMetadataRow(
                    icon = Res.drawable.time,
                    title = scheduleText
                )

                PrescriptionMetadataRow(
                    icon = Res.drawable.note,
                    title = instructionsText
                )

                PrescriptionMetadataRow(
                    icon = Res.drawable.next,
                    title = nextDoseText
                )
            }
        }
    }
}

@Composable
fun PrescriptionMetadataRow(
    icon: DrawableResource = Res.drawable.time,
    title: String = "Twice Daily, 08:00 AM, 08:00 PM",
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = vectorResource(icon),
                contentDescription = "Pills Icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(10.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPrescriptionCardPreview() {
    PillFlowTheme {
        PillFlowPrescriptionCard(
            modifier = Modifier.padding(15.dp),
            medicationName = "Metformin",
            dosage = "500mg",
            pillShape = PillShape.CAPSULE,
            pillColor = PillColor.SKY_BLUE
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPrescriptionCardPreviewDark() {
    PillFlowTheme {
        PillFlowPrescriptionCard(
            modifier = Modifier.padding(15.dp),
            medicationName = "Aspirin",
            dosage = "500mg",
            pillShape = PillShape.OVAL,
            pillColor = PillColor.SOFT_PURPLE
        )
    }
}