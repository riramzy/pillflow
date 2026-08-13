package com.riramzy.pillfllow.ui.components.custom

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
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.IndicatorColor

@Composable
fun PillFlowActivityCard(
    patientName: String = "Mary",
    actionDescription: String = "took Aspirin 500mg",
    timestampText: String = "Today at 08:02 AM",
    status: ComplianceStatus = ComplianceStatus.MISSED,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ComplianceStatus.DEFAULT -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ComplianceStatus.ON_TIME -> IndicatorColor.GREEN_CONTAINER.color to IndicatorColor.GREEN.color
        ComplianceStatus.LATE -> IndicatorColor.YELLOW_CONTAINER.color to IndicatorColor.YELLOW.color
        ComplianceStatus.MISSED -> IndicatorColor.RED_CONTAINER.color to IndicatorColor.RED.color
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PillFlowPillColor(
                customColor = textColor,
                isWithBorder = false,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = "$patientName $actionDescription ,$timestampText",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowActivityCardPreview() {
    PillFlowTheme {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PillFlowActivityCard(
                patientName = "Mary",
                actionDescription = "took Aspirin 500mg",
                timestampText = "Today at 08:02 AM",
                status = ComplianceStatus.DEFAULT,
            )
            PillFlowActivityCard(
                patientName = "Mary",
                actionDescription = "took Aspirin 500mg",
                timestampText = "Today at 08:02 AM",
                status = ComplianceStatus.ON_TIME,
            )
            PillFlowActivityCard(
                patientName = "Mary",
                actionDescription = "took Aspirin 500mg",
                timestampText = "Today at 08:02 AM",
                status = ComplianceStatus.LATE,
            )
            PillFlowActivityCard(
                patientName = "Mary",
                actionDescription = "took Aspirin 500mg",
                timestampText = "Today at 08:02 AM",
                status = ComplianceStatus.MISSED,
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowActivityCardPreviewDark() {
    PillFlowTheme {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PillFlowActivityCard(
                patientName = "Mary",
                actionDescription = "took Aspirin 500mg",
                timestampText = "Today at 08:02 AM",
                status = ComplianceStatus.DEFAULT,
            )
            PillFlowActivityCard(
                patientName = "Mary",
                actionDescription = "took Aspirin 500mg",
                timestampText = "Today at 08:02 AM",
                status = ComplianceStatus.ON_TIME,
            )
            PillFlowActivityCard(
                patientName = "Mary",
                actionDescription = "took Aspirin 500mg",
                timestampText = "Today at 08:02 AM",
                status = ComplianceStatus.LATE,
            )
            PillFlowActivityCard(
                patientName = "Mary",
                actionDescription = "took Aspirin 500mg",
                timestampText = "Today at 08:02 AM",
                status = ComplianceStatus.MISSED,
            )
        }
    }
}