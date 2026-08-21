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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.ui.components.custom.PillFlowActivityCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowBottomNavBar
import com.riramzy.pillfllow.ui.components.custom.PillFlowTopAppBar
import com.riramzy.pillfllow.ui.components.dashboard.caregiver.PillFlowPatientCarousel
import com.riramzy.pillfllow.ui.components.dashboard.caregiver.PillFlowPatientDailyStatusCard
import com.riramzy.pillfllow.ui.components.dashboard.caregiver.PillFlowPatientWeeklyOverviewCard
import com.riramzy.pillfllow.ui.state.dashboard.CaregiverDashboardState
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.dashboard.RecentActivityUiModel
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.dashboard.CaregiverDashboardViewModel
import com.riramzy.pillfllow.utils.ComplianceStatus
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.avatar3

@Composable
fun CaregiverDashboardScreen(
    caregiverDashboardViewModel: CaregiverDashboardViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state = caregiverDashboardViewModel.state.collectAsStateWithLifecycle()

    CaregiverDashboardScreenContent(
        state = state.value,
        selectPatient = caregiverDashboardViewModel::selectPatient,
        callPatient = caregiverDashboardViewModel::callPatient,
        nudgePatient = caregiverDashboardViewModel::nudgePatient,
        modifier = modifier
    )
}

@Composable
fun CaregiverDashboardScreenContent(
    state: CaregiverDashboardState = CaregiverDashboardState(),
    selectPatient: (String) -> Unit = {},
    callPatient: (String) -> Unit = {},
    nudgePatient: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { PillFlowTopAppBar(modifier = Modifier.padding(15.dp)) },
        floatingActionButton = { PillFlowBottomNavBar() },
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
                        text = "Hello, ${state.caregiver?.firstName ?: "User"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Monitoring ${state.patients.size} family members",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            item {
                PillFlowPatientCarousel(
                    patients = state.patients,
                    selectedPatientId = state.selectedPatientId,
                    onPatientSelected = selectPatient,
                    modifier = Modifier.padding(horizontal = 15.dp)
                )
            }

            item {
                PillFlowPatientDailyStatusCard(
                    patientName = state.activePatient?.name ?: "",
                    lastUpdatedText = state.lastUpdatedText,
                    status = state.selectedPatientDailyStatus,
                    statusAlertText = state.dailyStatusAlertText,
                    onCallClick = { callPatient(state.selectedPatientId) },
                    onNudgeClick = { nudgePatient(state.selectedPatientId) },
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
                        text = "Weekly Overview",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    PillFlowPatientWeeklyOverviewCard(
                        weeklyRatePercentage = state.weeklyRatePercentage,
                        weeklyDays = state.weeklyCompliance,
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
                        text = "Live Activity",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    state.recentActivities.forEach { action ->
                        PillFlowActivityCard(
                            patientName = action.patientName,
                            actionDescription = action.actionDescription,
                            timestampText = action.timestampText,
                            status = action.status
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CaregiverDashboardScreenPreview() {
    PillFlowTheme {
        CaregiverDashboardScreenContent(
            state = CaregiverDashboardState(
                patients = listOf(
                    PairedPatientUiModel(
                        id = "1",
                        name = "Mary",
                        relation = "Mom",
                        avatar = Res.drawable.avatar2,
                        compliancePercentage = 80,
                        status = ComplianceStatus.LATE,
                        lateDosesCount = 2,
                        missedDosesCount = 1
                    ),
                    PairedPatientUiModel(
                        id = "2",
                        name = "John",
                        relation = "Dad",
                        avatar = Res.drawable.avatar1,
                        compliancePercentage = 90,
                        status = ComplianceStatus.ON_TIME,
                        lateDosesCount = 0,
                        missedDosesCount = 0
                    ),
                    PairedPatientUiModel(
                        id = "3",
                        name = "Jane",
                        relation = "Sister",
                        avatar = Res.drawable.avatar3,
                        compliancePercentage = 100,
                        status = ComplianceStatus.ON_TIME,
                        lateDosesCount = 0,
                        missedDosesCount = 0
                    )
                ),
                recentActivities = listOf(
                    RecentActivityUiModel(
                        id = "1",
                        patientName = "Mary",
                        actionDescription = "took Aspirin 500mg",
                        timestampText = "Today at 08:02 AM",
                        status = ComplianceStatus.LATE
                    ),
                    RecentActivityUiModel(
                        id = "2",
                        patientName = "John",
                        actionDescription = "took Aspirin 500mg",
                        timestampText = "Today at 08:02 AM",
                        status = ComplianceStatus.ON_TIME
                    ),
                ),
                selectedPatientId = "1",
                selectedPatientDoses = emptyList(),
                selectedPatientDailyStatus = ComplianceStatus.ON_TIME,
                dailyStatusAlertText = "All medications on track!",
                lastUpdatedText = "Updated just now",
                weeklyRatePercentage = 95,
                isLoading = false,
                errorMessage = null
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CaregiverDashboardScreenPreviewDark() {
    PillFlowTheme {
        CaregiverDashboardScreenContent(
            state = CaregiverDashboardState(
                patients = listOf(
                    PairedPatientUiModel(
                        id = "1",
                        name = "Mary",
                        relation = "Mom",
                        avatar = Res.drawable.avatar2,
                        compliancePercentage = 80,
                        status = ComplianceStatus.LATE,
                        lateDosesCount = 2,
                        missedDosesCount = 1
                    ),
                    PairedPatientUiModel(
                        id = "2",
                        name = "John",
                        relation = "Dad",
                        avatar = Res.drawable.avatar1,
                        compliancePercentage = 90,
                        status = ComplianceStatus.ON_TIME,
                        lateDosesCount = 0,
                        missedDosesCount = 0
                    ),
                    PairedPatientUiModel(
                        id = "3",
                        name = "Jane",
                        relation = "Sister",
                        avatar = Res.drawable.avatar3,
                        compliancePercentage = 100,
                        status = ComplianceStatus.ON_TIME,
                        lateDosesCount = 0,
                        missedDosesCount = 0
                    )
                ),
                recentActivities = listOf(
                    RecentActivityUiModel(
                        id = "1",
                        patientName = "Mary",
                        actionDescription = "took Aspirin 500mg",
                        timestampText = "Today at 08:02 AM",
                        status = ComplianceStatus.LATE
                    ),
                    RecentActivityUiModel(
                        id = "2",
                        patientName = "John",
                        actionDescription = "took Aspirin 500mg",
                        timestampText = "Today at 08:02 AM",
                        status = ComplianceStatus.ON_TIME
                    ),
                ),
                selectedPatientId = "1",
                selectedPatientDoses = emptyList(),
                selectedPatientDailyStatus = ComplianceStatus.ON_TIME,
                dailyStatusAlertText = "All medications on track!",
                lastUpdatedText = "Updated just now",
                weeklyRatePercentage = 95,
                isLoading = false,
                errorMessage = null
            )
        )
    }
}