package com.riramzy.pillfllow.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.domain.hardware.PlatformSensor
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.domain.repo.NotificationsRepo
import com.riramzy.pillfllow.ui.components.custom.PillFlowBottomNavBar
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowEmptyStateCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowLoadingCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowPullToRefreshBox
import com.riramzy.pillfllow.ui.components.custom.PillFlowSnackbar
import com.riramzy.pillfllow.ui.components.custom.PillFlowTopAppBar
import com.riramzy.pillfllow.ui.components.custom.PillFlowUserCard
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowComplianceCard
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowFloatingNudgeBanner
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowPillJarSandbox
import com.riramzy.pillfllow.ui.components.dashboard.patient.PillFlowScheduledDoseCard
import com.riramzy.pillfllow.ui.components.prescriptions.PillFlowPrescriptionCabinetCard
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardAction
import com.riramzy.pillfllow.ui.state.dashboard.PatientDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.ScheduledDoseUiModel
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.dashboard.PatientDashboardViewModel
import com.riramzy.pillfllow.utils.app.Screen
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import com.riramzy.pillfllow.utils.pill.PillColor
import com.riramzy.pillfllow.utils.pill.PillShape
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.add
import pillfllow.shared.generated.resources.compliance_ontime
import pillfllow.shared.generated.resources.pills

@Composable
fun PatientDashboardScreen(
    modifier: Modifier = Modifier,
    patientDashboardViewModel: PatientDashboardViewModel = koinViewModel(),
    notificationsRepo: NotificationsRepo = koinInject(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val state by patientDashboardViewModel.state.collectAsStateWithLifecycle()
    val notificationState by notificationsRepo.state.collectAsStateWithLifecycle()

    PatientDashboardScreenContent(
        state = state,
        hasUnreadNotifications = notificationState.hasUnread,
        onAction = patientDashboardViewModel::onAction,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToPrescriptions = onNavigateToPrescriptions,
        onNavigateToSettings = onNavigateToSettings,
        onProfileClick = onProfileClick,
        onNotificationsClick = onNotificationsClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreenContent(
    modifier: Modifier = Modifier,
    state: PatientDashboardState = PatientDashboardState(),
    hasUnreadNotifications: Boolean = false,
    onAction: (PatientDashboardAction) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    var doseToSkip by remember { mutableStateOf<ScheduledDoseUiModel?>(null) }
    var skipReason by remember { mutableStateOf("Doctor Advised") }
    val presetReasons = listOf("Doctor Advised", "Fasting / Lab Work", "Side Effects / Nausea", "Out of Supply")

    val snackbarHostState = remember { SnackbarHostState() }

    var showPrnSheet by remember { mutableStateOf(false) }

    if (showPrnSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrnSheet = false }
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                Column {
                    Text(
                        text = "Log As-Needed Medication",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Tap a medication to record a dose taken now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                state.prnMedications.forEach { prnMed ->
                    PillFlowScheduledDoseCard(
                        name = prnMed.name,
                        dose = prnMed.dosage,
                        time = "As Needed",
                        color = prnMed.color,
                        status = ComplianceStatus.DEFAULT,
                        badgeText = prnMed.lastTakenText ?: "Log Dose",
                        modifier = Modifier.clickable {
                            onAction(PatientDashboardAction.LogPrnDose(prnMed.medicationId))
                            showPrnSheet = false
                        }
                    )
                }
            }
        }
    }

    if (doseToSkip != null) {
        AlertDialog(
            onDismissRequest = { doseToSkip = null },
            title = {
                Text(
                    text = "Skip ${doseToSkip?.name}?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Select or enter a clinical reason for skipping this dose:",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OptIn(ExperimentalLayoutApi::class)
                    FlowRow {
                        presetReasons.forEach { reason ->
                            FilterChip(
                                selected = skipReason == reason,
                                onClick = { skipReason = reason },
                                label = {
                                    Text(
                                        text = reason,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                PillFlowButton(
                    text = "Skip",
                    customColor = IndicatorColor.BLUE_CONTAINER.color,
                    customTextColor = IndicatorColor.BLUE.color,
                    onClick = {
                        doseToSkip?.let {
                            onAction(PatientDashboardAction.SkipDose(it.id, skipReason.ifBlank { "Doctor Advised" }))
                        }
                        doseToSkip = null
                    }
                )
            },
            dismissButton = {
                PillFlowButton(
                    text = "Cancel",
                    customColor = MaterialTheme.colorScheme.surface,
                    customTextColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { doseToSkip = null }
                )
            }
        )
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            onAction(PatientDashboardAction.DismissError)
        }
    }

    val sensor = remember { PlatformSensor() }
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }
    val multiplier = state.physicsSensitivity.multiplier

    DisposableEffect(multiplier) {
        sensor.startListening { x, y ->
            tiltX = x * multiplier
            tiltY = y * multiplier
        }
        onDispose { sensor.stopListening() }
    }

    Scaffold(
        topBar = {
            PillFlowTopAppBar(
                hasUnreadNotifications = hasUnreadNotifications,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                modifier = Modifier.padding(15.dp)
            )
                 },
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    PillFlowSnackbar(
                        snackbarData = data,
                        isError = true
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.statusBarsPadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PillFlowPullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onAction(PatientDashboardAction.Refresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.isLoading) {
                    PillFlowLoadingCard(message = "Loading...")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(bottom = 100.dp),
                    ) {
                        item {
                            PillFlowUserCard(
                                userName = state.user?.firstName ?: "User",
                                headerDosesSubtitle = state.headerDosesSubtitle,
                                modifier = Modifier
                                    .padding(horizontal = 15.dp)
                            )
                        }

                        item {
                            PillFlowPillJarSandbox(
                                pillsState = state.pills,
                                totalRemainingDoses = state.totalDoses,
                                tiltX = tiltX,
                                tiltY = tiltY,
                                onLogMedication = { pillId ->
                                    val scheduledTime = state.scheduledDoses.firstOrNull { dose ->
                                        dose.id == pillId
                                    }?.scheduledTime

                                    onAction(
                                        PatientDashboardAction.LogDose(
                                            doseId = pillId,
                                            scheduledTime = scheduledTime
                                        )
                                    )
                                },
                                onSkipMedication = { pillId ->
                                    val targetDose =
                                        state.scheduledDoses.firstOrNull { it.id == pillId }
                                    if (targetDose != null) {
                                        skipReason = "Doctor Advised"
                                        doseToSkip = targetDose
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 15.dp)
                            )
                        }

                        if (state.trackedMedicationsCount > 0) {
                            item {
                                PillFlowPrescriptionCabinetCard(
                                    trackedCount = state.trackedMedicationsCount,
                                    minDaysOfSupply = state.minDaysOfSupply,
                                    lowSupplyMedications = state.lowSupplyMedications,
                                    onRefillClick = { onNavigateToPrescriptions() },
                                    modifier = Modifier.padding(horizontal = 15.dp)
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
                                    text = "Daily Compliance",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                state.complianceCards.forEach { card ->
                                    PillFlowComplianceCard(
                                        title = card.title,
                                        subtitle = card.subtitle,
                                        badgeText = card.badgeText,
                                        status = card.status
                                    )
                                }
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Daily Schedule",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    if (state.prnMedications.isNotEmpty()) {
                                        Surface(
                                            onClick = { showPrnSheet = true },
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "+ As-Needed",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }

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

            AnimatedVisibility(
                visible = state.activeNudges.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .zIndex(10f)
            ) {
                val nudge = state.activeNudges.firstOrNull()
                if (nudge != null) {
                    PillFlowFloatingNudgeBanner(
                        title = nudge.title,
                        message = nudge.description,
                        onDismiss = { onAction(PatientDashboardAction.DismissNudge(nudge.id)) }
                    )
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
                        id = "1",
                        name = "Aspirin",
                        dosage = "500mg",
                        timeFormatted = "8:00 PM",
                        color = PillColor.CORAL_RED,
                        status = ComplianceStatus.LATE,
                        badgeText = "2 Doses Left",
                        scheduledTime = 0L
                    ),
                    ScheduledDoseUiModel(
                        id = "2",
                        name = "Vitamin D",
                        dosage = "500mg",
                        timeFormatted = "8:00 PM",
                        color = PillColor.SKY_BLUE,
                        status = ComplianceStatus.LATE,
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
                complianceStatus = ComplianceStatus.DEFAULT,
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
                        id = "1",
                        name = "Aspirin",
                        dosage = "500mg",
                        timeFormatted = "8:00 PM",
                        color = PillColor.CORAL_RED,
                        status = ComplianceStatus.LATE,
                        badgeText = "2 Doses Left",
                        scheduledTime = 0L
                    ),
                    ScheduledDoseUiModel(
                        id = "2",
                        name = "Vitamin D",
                        dosage = "500mg",
                        timeFormatted = "8:00 PM",
                        color = PillColor.SKY_BLUE,
                        status = ComplianceStatus.LATE,
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
                complianceStatus = ComplianceStatus.DEFAULT,
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