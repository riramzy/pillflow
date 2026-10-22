package com.riramzy.pillfllow.ui.components.prescriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
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
import com.riramzy.pillfllow.ui.state.prescriptions.LowSupplyMedicationUiModel
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.compliance_late
import pillfllow.shared.generated.resources.compliance_missed
import pillfllow.shared.generated.resources.compliance_ontime

@Composable
fun PillFlowPrescriptionCabinetCard(
    modifier: Modifier = Modifier,
    trackedCount: Int = 3,
    minDaysOfSupply: Int? = 15,
    lowSupplyMedications: List<LowSupplyMedicationUiModel> = emptyList(),
    onRefillClick: (medicationId: String?) -> Unit = {}
) {
    if (trackedCount <= 0) return
    val hasOutOfStock = lowSupplyMedications.any { it.remainingPills == 0 }
    val hasLowSupply = lowSupplyMedications.isNotEmpty()

    val indicatorColor = when {
        hasOutOfStock -> IndicatorColor.RED.color
        hasLowSupply -> IndicatorColor.YELLOW.color
        else -> IndicatorColor.GREEN.color
    }

    val titleText = when {
        hasOutOfStock -> {
            val count = lowSupplyMedications.count { it.remainingPills == 0 }
            "$count ${if (count == 1) "Prescription" else "Prescriptions"} Out of Stock"
        }
        hasLowSupply -> {
            val count = lowSupplyMedications.size
            "$count ${if (count == 1) "Prescription" else "Prescriptions"} Running Low"
        }
        else -> "All $trackedCount ${if (trackedCount == 1) "Prescription" else "Prescriptions"} Stocked"
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
        Column(
            modifier = Modifier
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = indicatorColor,
                            shape = CircleShape
                        )
                )

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (hasLowSupply) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    lowSupplyMedications.forEach { med ->
                        val isOut = med.remainingPills == 0
                        val daysText = med.daysLeft?.let {
                            " (Runs out in $it ${if (it == 1) "day" else "days"})"
                        } ?: ""

                        val detail = if (isOut) {
                            "${med.name} (${med.dosage}): 0 pills left — Refill immediately"
                        } else {
                            "${med.name} (${med.dosage}): ${med.remainingPills} pills remaining$daysText"
                        }

                        PillFlowPrescriptionMetadataRow(
                            title = detail,
                            icon = if (isOut) Res.drawable.compliance_missed else Res.drawable.compliance_late,
                        )
                    }

                    val (btnLabel, btnBg, btnTextColor) = if (hasOutOfStock) {
                        Triple(
                            "Refill Immediately",
                            IndicatorColor.RED_CONTAINER.color,
                            IndicatorColor.RED.color
                        )
                    } else {
                        Triple(
                            "Refill Prescription",
                            IndicatorColor.YELLOW_CONTAINER.color,
                            IndicatorColor.YELLOW.color
                        )
                    }

                    PillFlowButton(
                        text = btnLabel,
                        customColor = btnBg,
                        customTextColor = btnTextColor,
                        onClick = { onRefillClick(lowSupplyMedications.firstOrNull()?.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                }
            } else {
                PillFlowPrescriptionMetadataRow(
                    title = "${minDaysOfSupply ?: 15}+ days of supply remaining across your medicine cabinet",
                    icon = Res.drawable.compliance_ontime
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPrescriptionCabinetCardPreview() {
    PillFlowTheme {
        PillFlowPrescriptionCabinetCard(
            modifier = Modifier.padding(15.dp),
            lowSupplyMedications = listOf(
                LowSupplyMedicationUiModel(
                    name = "Aspirin",
                    dosage = "500mg",
                    remainingPills = 5,
                    daysLeft = 2
                )
            )
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPrescriptionCabinetCardPreviewDark() {
    PillFlowTheme {
        PillFlowPrescriptionCabinetCard(
            modifier = Modifier.padding(15.dp),
            lowSupplyMedications = listOf(
                LowSupplyMedicationUiModel(
                    name = "Aspirin",
                    dosage = "500mg",
                    remainingPills = 5,
                    daysLeft = 2
                )
            )
        )
    }
}