package com.riramzy.pillfllow.ui.components.prescriptions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillColor
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillShape
import com.riramzy.pillfllow.ui.state.prescriptions.StorageExtensionStyle
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import com.riramzy.pillfllow.utils.pill.PillColor
import com.riramzy.pillfllow.utils.pill.PillShape
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.compliance_late
import pillfllow.shared.generated.resources.compliance_missed
import pillfllow.shared.generated.resources.compliance_ontime
import pillfllow.shared.generated.resources.delete
import pillfllow.shared.generated.resources.edit
import pillfllow.shared.generated.resources.next
import pillfllow.shared.generated.resources.note
import pillfllow.shared.generated.resources.time

@Composable
fun PillFlowPrescriptionCard(
    modifier: Modifier = Modifier,
    medicationName: String = "Metformin",
    dosage: String = "500mg",
    pillShape: PillShape = PillShape.CAPSULE,
    pillColor: PillColor = PillColor.SKY_BLUE,
    scheduleText: String = "Twice Daily, 08:00 AM, 08:00 PM",
    instructionsText: String = "Take with a full glass of water",
    nextDoseText: String = "Next dose in 2 hours",
    remainingPills: Int? = null,
    refillThreshold: Int = 5,
    daysLeft: Int? = null,
    onRefillClick: (() -> Unit)? = null,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Card(
            modifier = Modifier
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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(2f)
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
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                    PillFlowPrescriptionMetadataRow(
                        icon = Res.drawable.time,
                        title = scheduleText
                    )

                    PillFlowPrescriptionMetadataRow(
                        icon = Res.drawable.note,
                        title = instructionsText
                    )

                    PillFlowPrescriptionMetadataRow(
                        icon = Res.drawable.next,
                        title = nextDoseText
                    )
                }
            }
        }

        if (remainingPills != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                val isOut = remainingPills == 0
                val isLow = remainingPills in 1..refillThreshold

                val daysText = daysLeft?.let { " • Runs out in $it ${if (it == 1) "day" else "days"}" } ?: ""
                val daysStockedText = daysLeft?.let { ", ~$it ${if (it == 1) "day" else "days"} left" } ?: ""

                val style = when {
                    isOut -> StorageExtensionStyle(
                        badgeBg = IndicatorColor.RED_CONTAINER.color,
                        badgeTint = IndicatorColor.RED.color,
                        badgeIcon = Res.drawable.compliance_missed,
                        badgeText = "Out of Stock: 0 pills left",
                        btnBg = IndicatorColor.RED_CONTAINER.color,
                        btnTextColor = IndicatorColor.RED.color
                    )
                    isLow -> StorageExtensionStyle(
                        badgeBg = IndicatorColor.YELLOW_CONTAINER.color,
                        badgeTint = IndicatorColor.YELLOW.color,
                        badgeIcon = Res.drawable.compliance_late,
                        badgeText = "Low Supply: $remainingPills left$daysText",
                        btnBg = IndicatorColor.YELLOW_CONTAINER.color,
                        btnTextColor = IndicatorColor.YELLOW.color
                    )
                    else -> StorageExtensionStyle(
                        badgeBg = IndicatorColor.GREEN_CONTAINER.color,
                        badgeTint = IndicatorColor.GREEN.color,
                        badgeIcon = Res.drawable.compliance_ontime,
                        badgeText = "$remainingPills pills in stock$daysStockedText",
                        btnBg = MaterialTheme.colorScheme.primary,
                        btnTextColor = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillFlowPrescriptionMetadataRow(
                        icon = style.badgeIcon,
                        customIconColor = style.badgeTint,
                        customIconBackgroundColor = style.badgeBg,
                        title = style.badgeText,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 7.dp)
                    )

                    PillFlowButton(
                        text = "Refill",
                        customColor = style.btnBg,
                        customTextColor = style.btnTextColor,
                        onClick = { onRefillClick?.invoke() },
                        modifier = Modifier
                            .height(40.dp)
                    )
                }
            }
        }
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
            pillColor = PillColor.SKY_BLUE,
            scheduleText = "Twice Daily, 08:00 AM, 08:00 PM",
            instructionsText = "Take with a full glass of water",
            nextDoseText = "Next dose in 2 hours",
            remainingPills = 0,
            refillThreshold = 5,
            daysLeft = 1,
            onRefillClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPrescriptionCardPreviewDark() {
    PillFlowTheme {
        PillFlowPrescriptionCard(
            modifier = Modifier.padding(15.dp),
            medicationName = "Metformin",
            dosage = "500mg",
            pillShape = PillShape.CAPSULE,
            pillColor = PillColor.SKY_BLUE,
            scheduleText = "Twice Daily, 08:00 AM, 08:00 PM",
            instructionsText = "Take with a full glass of water",
            nextDoseText = "Next dose in 2 hours",
            remainingPills = 5,
            refillThreshold = 5,
            daysLeft = 2,
            onRefillClick = {}
        )
    }
}