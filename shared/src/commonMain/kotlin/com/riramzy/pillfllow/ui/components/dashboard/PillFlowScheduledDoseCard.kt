package com.riramzy.pillfllow.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillColor
import com.riramzy.pillfllow.ui.components.custom.PillFlowStatusCard
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.PillColor

@Composable
fun PillFlowScheduledDoseCard(
    modifier: Modifier = Modifier,
    name: String = "Aspirin",
    dose: String = "500mg",
    time: String = "8:00 PM",
    color: PillColor = PillColor.CORAL_RED,
    status: ComplianceStatus = ComplianceStatus.DEFAULT,
    badgeText: String = "100% On-Time"

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                PillFlowPillColor(
                    modifier = Modifier.size(24.dp),
                    color = color
                )

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = dose,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            PillFlowStatusCard(
                modifier = Modifier.weight(1f),
                status = status,
                customText = badgeText
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowScheduledDoseCardPreview() {
    PillFlowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            PillFlowScheduledDoseCard(
                name = "Aspirin",
                dose = "500mg",
                time = "8:00 PM",
                color = PillColor.CORAL_RED
            )

            PillFlowScheduledDoseCard(
                name = "Vitamin C",
                dose = "100mg",
                time = "9:00 PM",
                color = PillColor.CITRUS_GOLD
            )

            PillFlowScheduledDoseCard(
                name = "Ibuprofen",
                dose = "200mg",
                time = "10:00 PM",
                color = PillColor.SOFT_PURPLE
            )

            PillFlowScheduledDoseCard(
                name = "Paracetamol",
                dose = "500mg",
                time = "11:00 PM",
                color = PillColor.SKY_BLUE
            )

            PillFlowScheduledDoseCard(
                name = "Amoxicillin",
                dose = "500mg",
                time = "12:00 AM",
                color = PillColor.MINT_GREEN
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowScheduledDoseCardPreviewDark() {
    PillFlowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            PillFlowScheduledDoseCard(
                name = "Aspirin",
                dose = "500mg",
                time = "8:00 PM",
                color = PillColor.CORAL_RED
            )

            PillFlowScheduledDoseCard(
                name = "Vitamin C",
                dose = "100mg",
                time = "9:00 PM",
                color = PillColor.CITRUS_GOLD
            )

            PillFlowScheduledDoseCard(
                name = "Ibuprofen",
                dose = "200mg",
                time = "10:00 PM",
                color = PillColor.SOFT_PURPLE
            )

            PillFlowScheduledDoseCard(
                name = "Paracetamol",
                dose = "500mg",
                time = "11:00 PM",
                color = PillColor.SKY_BLUE
            )

            PillFlowScheduledDoseCard(
                name = "Amoxicillin",
                dose = "500mg",
                time = "12:00 AM",
                color = PillColor.MINT_GREEN
            )
        }
    }
}