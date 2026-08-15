package com.riramzy.pillfllow.ui.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.IndicatorColor

data class MonthDaysCompliance(
    val dayNumber: String,
    val status: ComplianceStatus
)

val defaultSampleDays = listOf(
    MonthDaysCompliance("1", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("2", ComplianceStatus.LATE),
    MonthDaysCompliance("3", ComplianceStatus.MISSED),
    MonthDaysCompliance("4", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("5", ComplianceStatus.LATE),
    MonthDaysCompliance("6", ComplianceStatus.MISSED),
    MonthDaysCompliance("7", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("8", ComplianceStatus.LATE),
    MonthDaysCompliance("9", ComplianceStatus.MISSED),
    MonthDaysCompliance("10", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("11", ComplianceStatus.LATE),
    MonthDaysCompliance("12", ComplianceStatus.MISSED),
    MonthDaysCompliance("13", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("14", ComplianceStatus.LATE),
    MonthDaysCompliance("15", ComplianceStatus.MISSED),
    MonthDaysCompliance("16", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("17", ComplianceStatus.LATE),
    MonthDaysCompliance("18", ComplianceStatus.MISSED),
    MonthDaysCompliance("19", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("20", ComplianceStatus.LATE),
    MonthDaysCompliance("21", ComplianceStatus.MISSED),
    MonthDaysCompliance("22", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("23", ComplianceStatus.LATE),
    MonthDaysCompliance("24", ComplianceStatus.MISSED),
    MonthDaysCompliance("25", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("26", ComplianceStatus.LATE),
    MonthDaysCompliance("27", ComplianceStatus.MISSED),
    MonthDaysCompliance("28", ComplianceStatus.ON_TIME),
    MonthDaysCompliance("29", ComplianceStatus.LATE),
    MonthDaysCompliance("30", ComplianceStatus.MISSED),
    MonthDaysCompliance("31", ComplianceStatus.ON_TIME)
)

@Composable
fun PillFlowMonthlyHeatmapCard(
    monthDays: List<MonthDaysCompliance> = defaultSampleDays,
    monthYearText: String = "July 2026",
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
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                    bottom = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = monthYearText,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )

            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dayNames.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            val weeks = monthDays.chunked(7)

            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 7) {
                        val dayItem = week.getOrNull(i)

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (dayItem != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val dotColor = when (dayItem.status) {
                                        ComplianceStatus.ON_TIME -> IndicatorColor.GREEN.color
                                        ComplianceStatus.LATE -> IndicatorColor.YELLOW.color
                                        ComplianceStatus.MISSED -> IndicatorColor.RED.color
                                        ComplianceStatus.DEFAULT -> MaterialTheme.colorScheme.primaryContainer
                                    }

                                    PillFlowPillColor(
                                        customColor = dotColor,
                                        isWithBorder = false,
                                        modifier = Modifier.size(14.dp)
                                    )

                                    Text(
                                        text = dayItem.dayNumber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowMonthlyHeatmapCardPreview() {
    PillFlowTheme {
        PillFlowMonthlyHeatmapCard(modifier = Modifier.padding(15.dp))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowMonthlyHeatmapCardPreviewDark() {
    PillFlowTheme {
        PillFlowMonthlyHeatmapCard(modifier = Modifier.padding(15.dp))
    }
}