package com.riramzy.pillfllow.ui.components.prescriptions

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillColor
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillShape
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import com.riramzy.pillfllow.utils.pill.PillColor
import com.riramzy.pillfllow.utils.pill.PillShape
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.dot
import pillfllow.shared.generated.resources.retake
import pillfllow.shared.generated.resources.take
import pillfllow.shared.generated.resources.time

@Composable
fun PillFlowAsNeededPrescriptionCard(
    modifier: Modifier = Modifier,
    medicationName: String = "Metformin",
    dosage: String = "500mg",
    pillShape: PillShape = PillShape.CAPSULE,
    pillColor: PillColor = PillColor.SKY_BLUE,
    metadataText: String = "Twice Daily, 08:00 AM, 08:00 PM",
    isAlreadyTook: Boolean = false,
    onTakeClick: () -> Unit = {},
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

                PillFlowButton(
                    modifier = Modifier.height(35.dp),
                    text = if (isAlreadyTook) "Retake" else "Take",
                    withIcon = true,
                    icon = if (isAlreadyTook) Res.drawable.retake else Res.drawable.take,
                    onClick = onTakeClick
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                if (isAlreadyTook) {
                    PillFlowPrescriptionMetadataRow(
                        icon = Res.drawable.dot,
                        title = metadataText,
                        customIconColor = IndicatorColor.GREEN.color,
                        customIconBackgroundColor = IndicatorColor.GREEN.color,
                        modifier = Modifier
                            .padding(4.dp)
                    )
                } else {
                    PillFlowPrescriptionMetadataRow(
                        icon = Res.drawable.time,
                        title = metadataText,
                        modifier = Modifier
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowAsNeededPrescriptionCardPreview() {
    PillFlowTheme {
        PillFlowAsNeededPrescriptionCard(modifier = Modifier.padding(15.dp))
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowAsNeededPrescriptionCardPreviewDark() {
    PillFlowTheme {
        PillFlowAsNeededPrescriptionCard(modifier = Modifier.padding(15.dp))
    }
}