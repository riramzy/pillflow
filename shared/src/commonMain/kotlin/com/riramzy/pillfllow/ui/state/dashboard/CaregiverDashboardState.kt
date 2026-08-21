package com.riramzy.pillfllow.ui.state.dashboard

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.defaultSampleDays
import org.jetbrains.compose.resources.DrawableResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.avatar1

data class CaregiverDashboardState(
    val caregiver: UserEntity? = null,
    val patients: List<PairedPatientUiModel> = emptyList(),
    val selectedPatientId: String = "",
    val selectedPatientDoses: List<ScheduledDoseUiModel> = emptyList(),
    val selectedPatientDailyStatus: ComplianceStatus = ComplianceStatus.ON_TIME,
    val dailyStatusAlertText: String = "All medications on track!",
    val recentActivities: List<RecentActivityUiModel> = emptyList(),
    val lastUpdatedText: String = "Updated just now",
    val weeklyCompliance: List<ComplianceDayUiModel> = defaultSampleDays,
    val weeklyRatePercentage: Int = 95,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val activePatient: PairedPatientUiModel?
        get() = patients.find { it.id == selectedPatientId } ?: patients.firstOrNull()
}

data class ComplianceDayUiModel(
    val dayName: String,
    val status: ComplianceStatus
)

data class PairedPatientUiModel(
    val id: String = "1",
    val name: String = "Mary",
    val relation: String = "Mom",
    val phoneNumber: String = "",
    val avatar: DrawableResource = Res.drawable.avatar1,
    val status: ComplianceStatus = ComplianceStatus.ON_TIME,
    val lateDosesCount: Int = 0,
    val missedDosesCount: Int = 0,
    val compliancePercentage: Int = 100
)

data class RecentActivityUiModel(
    val id: String = "1",
    val patientName: String = "Mary",
    val actionDescription: String = "took Aspirin 500mg",
    val timestampText: String = "Today at 08:02 AM",
    val status: ComplianceStatus = ComplianceStatus.ON_TIME
)