package com.riramzy.pillfllow.ui.components.dashboard.caregiver

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.dashboard.PatientCarouselMode
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.avatar3

@Composable
fun PillFlowPatientCarousel(
    modifier: Modifier = Modifier,
    patients: List<PairedPatientUiModel> = emptyList(),
    selectedPatientId: String = "",
    mode: PatientCarouselMode = PatientCarouselMode.LiveDaily,
    onPatientSelected: (String) -> Unit = {},
) {
    val activePatient = patients.find { it.id == selectedPatientId } ?: patients.firstOrNull()
    val unselectedPatients = patients.filter { it.id != activePatient?.id }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize(),
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
                    compliancePercentage = patient.compliancePercentage,
                    mode = mode
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
    compliancePercentage: Int = 100,
    mode: PatientCarouselMode = PatientCarouselMode.LiveDaily,
    isCaregiver: Boolean = false
) {
    val (statusText, statusColor: Color) = when (mode) {
        is PatientCarouselMode.LiveDaily -> {
            when (status) {
                ComplianceStatus.DEFAULT -> "All Good!" to MaterialTheme.colorScheme.primary
                ComplianceStatus.ON_TIME -> "Compliance: $compliancePercentage%" to IndicatorColor.GREEN.color
                ComplianceStatus.LATE -> "$lateDosesCount Dose(s) Late" to IndicatorColor.YELLOW.color
                ComplianceStatus.MISSED -> "$missedDosesCount Dose(s) Missed" to IndicatorColor.RED.color
            }
        }

        is PatientCarouselMode.MonthlyHistory -> {
            if (!mode.hasRecords) {
                "No Logs This Month" to MaterialTheme.colorScheme.primary
            } else {
                val color = when {
                    mode.scorePercentage >= 80 -> IndicatorColor.GREEN.color
                    mode.scorePercentage >= 50 -> IndicatorColor.YELLOW.color
                    else -> IndicatorColor.RED.color
                }

                "Score: ${mode.scorePercentage}%" to color
            }
        }

        is PatientCarouselMode.Prescriptions -> {
            val text = if (mode.prescriptionCount == 1) "1 Prescription" else "${mode.prescriptionCount} Prescriptions"
            val color = if (mode.prescriptionCount > 0) MaterialTheme.colorScheme.primary else IndicatorColor.YELLOW.color
            text to color
        }
    }

    Row(
        modifier = modifier
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedContent(
            targetState = avatar,
            transitionSpec = {
                (
                        slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetX = { it * 4 }
                ) + fadeIn(
                            animationSpec = tween(180))
                ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            targetOffsetX = { it * 4 }
                        ) + fadeOut(animationSpec = tween(150))
                    )
            },
            label = "AvatarSpringSwap"
        ) { currentAvatar ->
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
                    painter = painterResource(currentAvatar),
                    contentDescription = "null",
                    modifier = Modifier.size(35.dp)
                )
            }
        }

        AnimatedContent(
            targetState = "$name - $relation",
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(140))
            },
            label = "PatientNameFade"
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isCaregiver) {
                    Text(
                        text = "$name - $relation",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = relation,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }


                if (!isCaregiver) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = statusColor,
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
    }
}

@Composable
fun PatientCard(
    modifier: Modifier = Modifier,
    avatar: DrawableResource = Res.drawable.avatar1,
    onClick: () -> Unit = {}
) {
    AnimatedContent(
        targetState = avatar,
        transitionSpec = {
            (
                    slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetX = { -it * 2 }
                    ) + fadeIn(animationSpec = tween(180))
                    ).togetherWith(
                    slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        targetOffsetX = { it * 2 }
                    ) + fadeOut(animationSpec = tween(150))
                )
        },
        label = "PatientCardAvatarSwap"
    ) { currentAvatar ->
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
                painter = painterResource(currentAvatar),
                contentDescription = "null",
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPatientCarouselPreview() {
    PillFlowTheme {
        val sampleList = listOf(
            PairedPatientUiModel(id = "1", name = "Mary", relation = "Mom", avatar = Res.drawable.avatar2),
            PairedPatientUiModel(id = "2", name = "John", relation = "Dad", avatar = Res.drawable.avatar1),
            PairedPatientUiModel(id = "3", name = "Jane", relation = "Sister", avatar = Res.drawable.avatar3)
        )

        PillFlowPatientCarousel(
            patients = sampleList,
            selectedPatientId = "1",
            modifier = Modifier.padding(15.dp)
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPatientCarouselPreviewDark() {
    PillFlowTheme {
        val sampleList = listOf(
            PairedPatientUiModel(id = "1", name = "Mary", relation = "Mom", avatar = Res.drawable.avatar2),
            PairedPatientUiModel(id = "2", name = "John", relation = "Dad", avatar = Res.drawable.avatar1),
            PairedPatientUiModel(id = "3", name = "Jane", relation = "Sister", avatar = Res.drawable.avatar3)
        )
        PillFlowPatientCarousel(
            patients = sampleList,
            selectedPatientId = "1",
            modifier = Modifier.padding(15.dp)
        )
    }
}