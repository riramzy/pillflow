package com.riramzy.pillfllow.ui.components.dashboard.caregiver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowStatusCard
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.ComplianceStatus
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.call
import pillfllow.shared.generated.resources.nugde

@Composable
fun PillFlowPatientDailyStatusCard(
    patientName: String = "Mary",
    lastUpdatedText: String = "Last updated 5 mins ago",
    status: ComplianceStatus = ComplianceStatus.MISSED,
    statusAlertText: String = "ALERT: Vitamin C 1000mg was due 35 mins ago!",
    onCallClick: () -> Unit = {},
    onNudgeClick: () -> Unit = {},
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
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Column {
                Text(
                    text = "$patientName's Daily Status",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = lastUpdatedText,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }


            PillFlowStatusCard(
                status = status,
                customText = statusAlertText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillFlowButton(
                    text = "Call",
                    withIcon = true,
                    icon = Res.drawable.call,
                    onClick = { onCallClick() },
                    modifier = Modifier.weight(1f)
                )

                PillFlowButton(
                    text = "Nudge",
                    withIcon = true,
                    icon = Res.drawable.nugde,
                    onClick = { onNudgeClick() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPatientDailyStatusCardPreview() {
    PillFlowTheme {
        PillFlowPatientDailyStatusCard(modifier = Modifier.padding(15.dp))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPatientDailyStatusCardPreviewDark() {
    PillFlowTheme {
        PillFlowPatientDailyStatusCard(modifier = Modifier.padding(15.dp))
    }
}