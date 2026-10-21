package com.riramzy.pillfllow.ui.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.riramzy.pillfllow.ui.components.custom.PillFlowStatusCard
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.ComplianceStatus

@Composable
fun PillFlowMonthlyScoreCard(
    modifier: Modifier = Modifier,
    monthYearText: String = "October 2026",
    scorePercentage: Int = 96,
    onTimeCount: Int = 28,
    lateCount: Int = 3,
    missedCount: Int = 1,
    skippedCount: Int = 0,
    hasRecords: Boolean = true
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Monthly Score",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.6f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(vertical = 10.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = monthYearText,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = if (hasRecords)
                                        "On-Time Compliance overall"
                                    else
                                        "No adherence data recorded yet",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Text(
                            text = if (hasRecords) "$scorePercentage%" else "--%",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasRecords) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PillFlowStatusCard(
                                    modifier = Modifier.weight(1f),
                                    status = ComplianceStatus.ON_TIME,
                                    customText = "$onTimeCount On Time"
                                )

                                PillFlowStatusCard(
                                    modifier = Modifier.weight(1f),
                                    status = ComplianceStatus.LATE,
                                    customText = "$lateCount Late"
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PillFlowStatusCard(
                                    modifier = Modifier.weight(1f),
                                    status = ComplianceStatus.MISSED,
                                    customText = "$missedCount Missed"
                                )

                                PillFlowStatusCard(
                                    modifier = Modifier.weight(1f),
                                    status = ComplianceStatus.SKIPPED,
                                    customText = "$skippedCount Skipped"
                                )
                            }
                        }
                    } else {
                        PillFlowStatusCard(
                            modifier = Modifier.fillMaxWidth(),
                            status = ComplianceStatus.DEFAULT,
                            customText = "No Data Recorded"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowMonthlyScoreCardPreview() {
    PillFlowTheme {
        PillFlowMonthlyScoreCard(modifier = Modifier.padding(15.dp))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowMonthlyScoreCardPreviewDark() {
    PillFlowTheme {
        PillFlowMonthlyScoreCard(modifier = Modifier.padding(15.dp))
    }
}