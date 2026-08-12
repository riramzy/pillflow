package com.riramzy.pillfllow.ui.components.dashboard.caregiver

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.IndicatorColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.avatar3

data class PairedPatientModel(
    val id: String = "1",
    val name: String = "Mary",
    val relation: String = "Mom",
    val avatar: DrawableResource = Res.drawable.avatar1,
    val status: ComplianceStatus = ComplianceStatus.ON_TIME,
    val lateDosesCount: Int = 0,
    val missedDosesCount: Int = 0,
    val compliancePercentage: Int = 100
)

@Composable
fun PillFlowPatientCarousel(
    patients: List<PairedPatientModel> = emptyList(),
    selectedPatientId: String = "",
    onPatientSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activePatient = patients.find { it.id == selectedPatientId } ?: patients.firstOrNull()
    val unselectedPatients = patients.filter { it.id != activePatient?.id }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            activePatient?.let { patient ->
                SelectedPatientCard(
                    name = patient.name,
                    avatar = patient.avatar,
                    relation = patient.relation,
                    status = patient.status,
                    lateDosesCount = patient.lateDosesCount,
                    missedDosesCount = patient.missedDosesCount,
                    compliancePercentage = patient.compliancePercentage
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(unselectedPatients) { patient ->
                    PatientCard(
                        avatar = patient.avatar,
                        onClick = { onPatientSelected(patient.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedPatientCard(
    modifier: Modifier = Modifier,
    name: String = "Mary",
    avatar: DrawableResource = Res.drawable.avatar2,
    relation: String = "Mom",
    status: ComplianceStatus = ComplianceStatus.MISSED,
    lateDosesCount: Int = 2,
    missedDosesCount: Int = 1,
    compliancePercentage: Int = 100
) {
    var statusColor: IndicatorColor
    var statusText: String

    when (status) {
        ComplianceStatus.DEFAULT -> {
            statusColor = IndicatorColor.YELLOW
            statusText = "All Good!"
        }

        ComplianceStatus.ON_TIME -> {
            statusColor = IndicatorColor.GREEN
            statusText = "Compliance: $compliancePercentage%"
        }

        ComplianceStatus.LATE -> {
            statusColor = IndicatorColor.YELLOW
            statusText = "$lateDosesCount Dose(s) Taken Late"
        }

        ComplianceStatus.MISSED -> {
            statusColor = IndicatorColor.RED
            statusText = "$missedDosesCount Dose(s) Missed"
        }
    }

    Row(
        modifier = modifier
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(avatar),
                contentDescription = "null",
                modifier = Modifier.size(35.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$name - $relation",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = statusColor.color,
                            shape = CircleShape
                        )
                )

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

        }
    }
}

@Composable
fun PatientCard(
    modifier: Modifier = Modifier,
    avatar: DrawableResource = Res.drawable.avatar1,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(avatar),
            contentDescription = "null",
            modifier = Modifier.size(35.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPatientCarouselPreview() {
    PillFlowTheme {
        val sampleList = listOf(
            PairedPatientModel(id = "1", name = "Mary", relation = "Mom", avatar = Res.drawable.avatar2),
            PairedPatientModel(id = "2", name = "John", relation = "Dad", avatar = Res.drawable.avatar1),
            PairedPatientModel(id = "3", name = "Jane", relation = "Sister", avatar = Res.drawable.avatar3)
        )

        PillFlowPatientCarousel(
            patients = sampleList,
            selectedPatientId = "1"
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPatientCarouselPreviewDark() {
    PillFlowTheme {
        val sampleList = listOf(
            PairedPatientModel(id = "1", name = "Mary", relation = "Mom", avatar = Res.drawable.avatar2),
            PairedPatientModel(id = "2", name = "John", relation = "Dad", avatar = Res.drawable.avatar1),
            PairedPatientModel(id = "3", name = "Jane", relation = "Sister", avatar = Res.drawable.avatar3)
        )
        PillFlowPatientCarousel(
            patients = sampleList,
            selectedPatientId = "1"
        )
    }
}