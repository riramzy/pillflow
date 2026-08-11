package com.riramzy.pillfllow.ui.components.dashboard

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowStatusCard
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.IndicatorColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.compliance_late
import pillfllow.shared.generated.resources.compliance_missed
import pillfllow.shared.generated.resources.compliance_ontime
import pillfllow.shared.generated.resources.nugde

@Composable
fun PillFlowComplianceCard(
    modifier: Modifier = Modifier,
    status: ComplianceStatus = ComplianceStatus.ON_TIME,
    title: String = "Take Medication",
    subtitle: String = "Take",
    badgeText: String = "100% On-Time"
) {
    var icon: DrawableResource
    var color: Color
    var backgroundColor: Color

    when (status) {
        ComplianceStatus.DEFAULT -> {
            icon = Res.drawable.nugde
            color = MaterialTheme.colorScheme.onPrimary
            backgroundColor = MaterialTheme.colorScheme.primary
        }

        ComplianceStatus.ON_TIME -> {
            icon = Res.drawable.compliance_ontime
            color = IndicatorColor.GREEN.color
            backgroundColor = IndicatorColor.GREEN_CONTAINER.color
        }

        ComplianceStatus.LATE -> {
            icon = Res.drawable.compliance_late
            color = IndicatorColor.YELLOW.color
            backgroundColor = IndicatorColor.YELLOW_CONTAINER.color
        }

        ComplianceStatus.MISSED -> {
            icon = Res.drawable.compliance_missed
            color = IndicatorColor.RED.color
            backgroundColor = IndicatorColor.RED_CONTAINER.color
        }
    }

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
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = backgroundColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(icon),
                    contentDescription = "null",
                    modifier = Modifier.size(10.dp),
                    tint = color
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )

                PillFlowStatusCard(
                    status = status,
                    customText = badgeText
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PilFlowComplianceCardPreview() {
    PillFlowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            PillFlowComplianceCard(
                title = "All Set For Today!",
                subtitle = "All 3 scheduled doses completed",
                badgeText = "100% On-Time",
                status = ComplianceStatus.ON_TIME
            )

            PillFlowComplianceCard(
                title = "Next: Aspirin 500mg",
                subtitle = "Scheduled for 8:00 PM (In 2 hours)",
                badgeText = "2 Doses Left",
                status = ComplianceStatus.LATE
            )

            PillFlowComplianceCard(
                title = "Overdue: Vitamin C",
                subtitle = "Was due at 2:00 PM (35 mins ago)",
                badgeText = "Grace Expired",
                status = ComplianceStatus.MISSED
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PilFlowComplianceCardPreviewDark() {
    PillFlowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            PillFlowComplianceCard(
                title = "All Set For Today!",
                subtitle = "All 3 scheduled doses completed",
                badgeText = "100% On-Time",
                status = ComplianceStatus.ON_TIME
            )

            PillFlowComplianceCard(
                title = "Next: Aspirin 500mg",
                subtitle = "Scheduled for 8:00 PM (In 2 hours)",
                badgeText = "2 Doses Left",
                status = ComplianceStatus.LATE
            )

            PillFlowComplianceCard(
                title = "Overdue: Vitamin C",
                subtitle = "Was due at 2:00 PM (35 mins ago)",
                badgeText = "Grace Expired",
                status = ComplianceStatus.MISSED
            )
        }
    }
}
