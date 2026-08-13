package com.riramzy.pillfllow.ui.components.dashboard.caregiver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import com.riramzy.pillfllow.utils.IndicatorColor

data class DayCompliance(
    val dayName: String,
    val status: ComplianceStatus
)

val defaultSampleDays = listOf(
    DayCompliance("Mon", ComplianceStatus.ON_TIME),
    DayCompliance("Tue", ComplianceStatus.LATE),
    DayCompliance("Wed", ComplianceStatus.MISSED),
    DayCompliance("Thu", ComplianceStatus.ON_TIME),
    DayCompliance("Fri", ComplianceStatus.LATE),
    DayCompliance("Sat", ComplianceStatus.MISSED),
    DayCompliance("Sun", ComplianceStatus.ON_TIME)
)

@Composable
fun PillFlowPatientWeeklyOverviewCard(
    weeklyDays: List<DayCompliance> = defaultSampleDays,
    weeklyRatePercentage: Int = 95,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weeklyDays.forEach { (dayName, status) ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )

                        val dayColor = when (status) {
                            ComplianceStatus.DEFAULT -> MaterialTheme.colorScheme.primary
                            ComplianceStatus.ON_TIME -> IndicatorColor.GREEN.color
                            ComplianceStatus.LATE -> IndicatorColor.YELLOW.color
                            ComplianceStatus.MISSED -> IndicatorColor.RED.color
                        }

                        PillFlowPillColor(
                            customColor = dayColor,
                            isWithBorder = false,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            PillFlowStatusCard(
                modifier = Modifier
                    .wrapContentSize(),
                status = ComplianceStatus.DEFAULT,
                customText = "Weekly Rate: $weeklyRatePercentage%",
                customTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                customBackgroundColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPatientWeeklyOverviewCardPreview() {
    PillFlowTheme {
        PillFlowPatientWeeklyOverviewCard()
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPatientWeeklyOverviewCardPreviewDark() {
    PillFlowTheme {
        PillFlowPatientWeeklyOverviewCard()
    }
}