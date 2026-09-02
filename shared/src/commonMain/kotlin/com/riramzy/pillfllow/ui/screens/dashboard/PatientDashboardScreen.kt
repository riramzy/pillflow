package com.riramzy.pillfllow.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.domain.hardware.PlatformSensor
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.ui.components.custom.PillFlowBottomNavBar
import com.riramzy.pillfllow.ui.components.custom.PillFlowEmptyStateCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowTopAppBar
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowComplianceCard
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowPillJarSandbox
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowScheduledDoseCard
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.dashboard.PatientDashboardViewModel
import com.riramzy.pillfllow.utils.PillColor
import com.riramzy.pillfllow.utils.PillShape
import com.riramzy.pillfllow.utils.Screen
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.add
import pillfllow.shared.generated.resources.compliance_ontime
import pillfllow.shared.generated.resources.pills

@Composable
fun PatientDashboardScreen(
    patientDashboardViewModel: PatientDashboardViewModel = koinViewModel(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by patientDashboardViewModel.state.collectAsStateWithLifecycle()

    PatientDashboardScreenContent(
        state = state,
        logDose = patientDashboardViewModel::logDose,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToPrescriptions = onNavigateToPrescriptions,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier
    )
}

@Composable
fun PatientDashboardScreenContent(
    state: PatientDashboardState = PatientDashboardState(),
    logDose: (Long) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sensor = remember { PlatformSensor() }
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }


    DisposableEffect(Unit) {
        sensor.startListening { x, y ->
            tiltX = x
            tiltY = y
        }
        onDispose {
            sensor.stopListening()
        }
    }

    Scaffold(
        topBar = { PillFlowTopAppBar(modifier = Modifier.padding(15.dp)) },
        floatingActionButton = {
            PillFlowBottomNavBar(
                selectedPage = Screen.Home.route,
                onHomeClick = {},
                onHistoryClick = onNavigateToHistory,
                onPrescriptionsClick = onNavigateToPrescriptions,
                onSettingsClick = onNavigateToSettings
            )
                               },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Hello, ${state.user?.firstName ?: "User"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "You have ${state.scheduledDoses.size} doses remaining today",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            item {
                PillFlowPillJarSandbox(
                    pillsState = state.pills,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    onLogMedication = { pillId ->
                        pillId.toLongOrNull()?.let { id ->
                            logDose(id)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Daily Compliance",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    PillFlowComplianceCard(
                        title = state.complianceTitle,
                        subtitle = state.complianceSubtitle,
                        badgeText = state.complianceBadgeText,
                        status = state.complianceStatus
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Daily Schedule",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (state.scheduledDoses.isEmpty()) {
                        PillFlowEmptyStateCard(
                            title = if (state.isNewUserWithoutPrescriptions) "No Medications Scheduled" else "All Caught Up!",
                            description = if (state.isNewUserWithoutPrescriptions) "Add your medications to schedule doses." else "Great job! All daily doses completed",
                            icon = if (state.isNewUserWithoutPrescriptions) Res.drawable.pills else Res.drawable.compliance_ontime,
                            buttonText = if (state.isNewUserWithoutPrescriptions) "Add Prescription" else null,
                            buttonIcon = if (state.isNewUserWithoutPrescriptions) Res.drawable.add else null,
                            onButtonClick = onNavigateToPrescriptions
                        )
                    } else {
                        state.scheduledDoses.forEach { scheduledDose ->
                            PillFlowScheduledDoseCard(
                                name = scheduledDose.name,
                                dose = scheduledDose.dosage,
                                time = scheduledDose.timeFormatted,
                                color = scheduledDose.color,
                                status = scheduledDose.status,
                                badgeText = scheduledDose.badgeText,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PatientDashboardScreenPreview() {
    PillFlowTheme {
        PatientDashboardScreenContent(
            state = PatientDashboardState(
                scheduledDoses = listOf(
                    ScheduledDoseUiModel(
                        id = 1,
                        name = "Aspirin",
                        dosage = "500mg",
                        timeFormatted = "8:00 PM",
                        color = PillColor.CORAL_RED,
                        status = com.riramzy.pillfllow.utils.ComplianceStatus.LATE,
                        badgeText = "2 Doses Left",
                        scheduledTime = 0L
                    ),
                    ScheduledDoseUiModel(
                        id = 2,
                        name = "Vitamin D",
                        dosage = "500mg",
                        timeFormatted = "8:00 PM",
                        color = PillColor.SKY_BLUE,
                        status = com.riramzy.pillfllow.utils.ComplianceStatus.LATE,
                        badgeText = "2 Doses Left",
                        scheduledTime = 0L
                    )
                ),
                pills = listOf(
                    PillEntity(
                        id = "1",
                        name = "Aspirin",
                        color = PillColor.CORAL_RED.color,
                        radius = 32f,
                        shape = PillShape.CIRCLE,
                        position = Vector2D(x = 0f, y = 0f)
                    ),
                    PillEntity(
                        id = "2",
                        name = "Aspirin",
                        color = PillColor.CITRUS_GOLD.color,
                        radius = 32f,
                        shape = PillShape.CAPSULE,
                        position = Vector2D(x = 1f, y = 1f)
                    )
                ),
                complianceStatus = com.riramzy.pillfllow.utils.ComplianceStatus.LATE,
                complianceTitle = "Next: Aspirin 500mg",
                complianceSubtitle = "Scheduled for 8:00 PM (In 2 hours)",
                complianceBadgeText = "2 Doses Left",
                adherenceScore = 100,
                dosesTaken = 2,
                totalDoses = 3,
                isLoading = false,
                errorMessage = null,
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PatientDashboardScreenPreviewDark() {
    PillFlowTheme {
        PatientDashboardScreenContent(
            state = PatientDashboardState(
                scheduledDoses = listOf(
                    ScheduledDoseUiModel(
                        id = 1,
                        name = "Aspirin",
                        dosage = "500mg",
                        timeFormatted = "8:00 PM",
                        color = PillColor.CORAL_RED,
                        status = com.riramzy.pillfllow.utils.ComplianceStatus.LATE,
                        badgeText = "2 Doses Left",
                        scheduledTime = 0L
                    ),
                    ScheduledDoseUiModel(
                        id = 2,
                        name = "Vitamin D",
                        dosage = "500mg",
                        timeFormatted = "8:00 PM",
                        color = PillColor.SKY_BLUE,
                        status = com.riramzy.pillfllow.utils.ComplianceStatus.LATE,
                        badgeText = "2 Doses Left",
                        scheduledTime = 0L
                    )
                ),
                pills = listOf(
                    PillEntity(
                        id = "1",
                        name = "Aspirin",
                        color = PillColor.CORAL_RED.color,
                        radius = 32f,
                        shape = PillShape.CIRCLE,
                        position = Vector2D(x = 1.3f, y = 2.2f)
                    ),
                    PillEntity(
                        id = "2",
                        name = "Aspirin",
                        color = PillColor.CITRUS_GOLD.color,
                        radius = 32f,
                        shape = PillShape.CAPSULE,
                        position = Vector2D(x = 1.2f, y = 2f)
                    )
                ),
                complianceStatus = com.riramzy.pillfllow.utils.ComplianceStatus.LATE,
                complianceTitle = "Next: Aspirin 500mg",
                complianceSubtitle = "Scheduled for 8:00 PM (In 2 hours)",
                complianceBadgeText = "2 Doses Left",
                adherenceScore = 100,
                dosesTaken = 2,
                totalDoses = 3,
                isLoading = false,
                errorMessage = null,
            )
        )
    }
}