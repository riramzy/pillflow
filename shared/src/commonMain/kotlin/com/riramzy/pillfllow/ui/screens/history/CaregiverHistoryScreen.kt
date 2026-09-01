package com.riramzy.pillfllow.ui.screens.history

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.ui.components.custom.PillFlowBottomNavBar
import com.riramzy.pillfllow.ui.components.custom.PillFlowLogCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowTopAppBar
import com.riramzy.pillfllow.ui.components.dashboard.caregiver.PillFlowPatientCarousel
import com.riramzy.pillfllow.ui.components.history.MonthDaysCompliance
import com.riramzy.pillfllow.ui.components.history.PillFlowMonthlyHeatmapCard
import com.riramzy.pillfllow.ui.components.history.PillFlowMonthlyScoreCard
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.history.HistoryLogRecordUiModel
import com.riramzy.pillfllow.ui.state.history.HistoryState
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.history.HistoryViewModel
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.Screen
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2

@Composable
fun CaregiverHistoryScreen(
    historyViewModel: HistoryViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by historyViewModel.state.collectAsStateWithLifecycle()

    CaregiverHistoryScreenContent(
        state = state,
        onPatientSelected = historyViewModel::selectPatient,
        onNavigateToHome = onNavigateToHome,
        onNavigateToPrescriptions = onNavigateToPrescriptions,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier
    )
}

@Composable
fun CaregiverHistoryScreenContent(
    state: HistoryState = HistoryState(),
    onPatientSelected: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToPrescriptions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { PillFlowTopAppBar(modifier = Modifier.padding(15.dp)) },
        floatingActionButton = {
            PillFlowBottomNavBar(
                selectedPage = Screen.History.route,
                onHomeClick = onNavigateToHome,
                onHistoryClick = {},
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
                        text = "Adherence History",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "30-day compliance tracking & log history",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (state.pairedPatients.isNotEmpty()) {
                item {
                    PillFlowPatientCarousel(
                        patients = state.pairedPatients,
                        selectedPatientId = state.selectedPatientId,
                        onPatientSelected = onPatientSelected,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                }
            }

            item {
                PillFlowMonthlyScoreCard(
                    monthYearText = state.monthYearTitle,
                    scorePercentage = state.scorePercentage,
                    onTimeCount = state.onTimeCount,
                    lateCount = state.lateCount,
                    missedCount = state.missedCount,
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Monthly Heatmap",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    PillFlowMonthlyHeatmapCard(
                        monthDays = state.monthlyComplianceDays,
                        monthYearText = state.monthYearTitle,
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Log Records",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    state.logRecords.forEach { log ->
                        PillFlowLogCard(
                            actionTitle = log.actionTitle,
                            actionDescription = log.actionDescription,
                            timestampText = log.timestampText,
                            status = log.status
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CaregiverHistoryScreenPreview() {
    PillFlowTheme {
        CaregiverHistoryScreenContent(
            state = HistoryState(
                isCaregiver = false,
                pairedPatients = listOf(
                    PairedPatientUiModel(
                        id = "1",
                        name = "Mary",
                        avatar = Res.drawable.avatar1
                    ),
                    PairedPatientUiModel(
                        id = "2",
                        name = "John",
                        avatar = Res.drawable.avatar2
                    ),
                    PairedPatientUiModel(
                        id = "3",
                        name = "Jane",
                        avatar = Res.drawable.avatar1
                    )
                ),
                selectedPatientId = "",
                monthYearTitle = "July 2026",
                scorePercentage = 100,
                onTimeCount = 10,
                lateCount = 10,
                missedCount = 10,
                monthlyComplianceDays = listOf(
                    MonthDaysCompliance(
                        dayNumber = "1",
                        status = ComplianceStatus.ON_TIME
                    ),
                    MonthDaysCompliance(
                        dayNumber = "2",
                        status = ComplianceStatus.LATE
                    ),
                    MonthDaysCompliance(
                        dayNumber = "3",
                        status = ComplianceStatus.MISSED
                    ),
                    MonthDaysCompliance(
                        dayNumber = "4",
                        status = ComplianceStatus.DEFAULT
                    ),
                    MonthDaysCompliance(
                        dayNumber = "5",
                        status = ComplianceStatus.DEFAULT
                    ),
                    MonthDaysCompliance(
                        dayNumber = "6",
                        status = ComplianceStatus.DEFAULT
                    ),
                    MonthDaysCompliance(
                        dayNumber = "7",
                        status = ComplianceStatus.DEFAULT
                    ),
                    MonthDaysCompliance(
                        dayNumber = "8",
                        status = ComplianceStatus.DEFAULT
                    ),
                ),
                logRecords = listOf(
                    HistoryLogRecordUiModel(
                        id = "1",
                        patientName = "Mary",
                        actionTitle = "July 29, Aspirin 500mg",
                        actionDescription = "Scheduled 8:00 AM",
                        timestampText = "Logged 8",
                        status = ComplianceStatus.ON_TIME
                    ),
                    HistoryLogRecordUiModel(
                        id = "2",
                        patientName = "Mary",
                        actionTitle = "July 29, Aspirin 500mg",
                        actionDescription = "Scheduled 8:00 AM",
                        timestampText = "Logged 8",
                        status = ComplianceStatus.LATE
                    ),
                    HistoryLogRecordUiModel(
                        id = "3",
                        patientName = "Mary",
                        actionTitle = "July 29, Aspirin 500mg",
                        actionDescription = "Scheduled 8:00 AM",
                        timestampText = "Logged 8",
                        status = ComplianceStatus.MISSED
                    )
                ),
                isLoading = false
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CaregiverHistoryScreenPreviewDark() {
    PillFlowTheme {
        CaregiverHistoryScreenContent(
            state = HistoryState(
                isCaregiver = false,
                pairedPatients = listOf(
                    PairedPatientUiModel(
                        id = "1",
                        name = "Mary",
                        avatar = Res.drawable.avatar1
                    ),
                    PairedPatientUiModel(
                        id = "2",
                        name = "John",
                        avatar = Res.drawable.avatar2
                    ),
                    PairedPatientUiModel(
                        id = "3",
                        name = "Jane",
                        avatar = Res.drawable.avatar1
                    )
                ),
                selectedPatientId = "",
                monthYearTitle = "July 2026",
                scorePercentage = 100,
                onTimeCount = 10,
                lateCount = 10,
                missedCount = 10,
                monthlyComplianceDays = listOf(
                    MonthDaysCompliance(
                        dayNumber = "1",
                        status = ComplianceStatus.ON_TIME
                    ),
                    MonthDaysCompliance(
                        dayNumber = "2",
                        status = ComplianceStatus.LATE
                    ),
                    MonthDaysCompliance(
                        dayNumber = "3",
                        status = ComplianceStatus.MISSED
                    ),
                    MonthDaysCompliance(
                        dayNumber = "4",
                        status = ComplianceStatus.DEFAULT
                    ),
                    MonthDaysCompliance(
                        dayNumber = "5",
                        status = ComplianceStatus.DEFAULT
                    ),
                    MonthDaysCompliance(
                        dayNumber = "6",
                        status = ComplianceStatus.DEFAULT
                    ),
                    MonthDaysCompliance(
                        dayNumber = "7",
                        status = ComplianceStatus.DEFAULT
                    ),
                    MonthDaysCompliance(
                        dayNumber = "8",
                        status = ComplianceStatus.DEFAULT
                    ),
                ),
                logRecords = listOf(
                    HistoryLogRecordUiModel(
                        id = "1",
                        patientName = "Mary",
                        actionTitle = "July 29, Aspirin 500mg",
                        actionDescription = "Scheduled 8:00 AM",
                        timestampText = "Logged 8",
                        status = ComplianceStatus.ON_TIME
                    ),
                    HistoryLogRecordUiModel(
                        id = "2",
                        patientName = "Mary",
                        actionTitle = "July 29, Aspirin 500mg",
                        actionDescription = "Scheduled 8:00 AM",
                        timestampText = "Logged 8",
                        status = ComplianceStatus.LATE
                    ),
                    HistoryLogRecordUiModel(
                        id = "3",
                        patientName = "Mary",
                        actionTitle = "July 29, Aspirin 500mg",
                        actionDescription = "Scheduled 8:00 AM",
                        timestampText = "Logged 8",
                        status = ComplianceStatus.MISSED
                    )
                ),
                isLoading = false
            )
        )
    }
}