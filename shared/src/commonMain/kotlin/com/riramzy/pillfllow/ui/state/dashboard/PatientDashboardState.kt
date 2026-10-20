package com.riramzy.pillfllow.ui.state.dashboard

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.ui.state.notifications.NotificationAlertUiModel
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.physics.PhysicsSensitivity
import com.riramzy.pillfllow.utils.pill.PillColor

data class PatientDashboardState(
    val user: UserEntity? = null,
    val scheduledDoses: List<ScheduledDoseUiModel> = emptyList(),
    val pills: List<PillEntity> = emptyList(),
    val hasPrescriptions: Boolean = false,
    val complianceStatus: ComplianceStatus = ComplianceStatus.ON_TIME,
    val complianceTitle: String = "All Set For Today!",
    val complianceSubtitle: String = "All scheduled doses completed",
    val complianceBadgeText: String = "100% On-Time",
    val complianceCards: List<ComplianceCardUiModel> = emptyList(),
    val adherenceScore: Int = 100,
    val dosesTaken: Int = 0,
    val totalDoses: Int = 0,
    val remainingDosesCount: Int = 0,
    val missedDosesCount: Int = 0,
    val physicsSensitivity: PhysicsSensitivity = PhysicsSensitivity.NORMAL,
    val activeNudges: List<NotificationAlertUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
) {
    val isNewUserWithoutPrescriptions: Boolean
        get() = !hasPrescriptions && scheduledDoses.isEmpty()

    val headerDosesSubtitle: String
        get() = when {
            remainingDosesCount == 0 && missedDosesCount == 0 -> "All doses completed today"
            remainingDosesCount == 0 && missedDosesCount > 0 -> "No doses remaining today ($missedDosesCount missed)"
            missedDosesCount > 0 -> "You have $remainingDosesCount dose${if (remainingDosesCount == 1) "" else "s"} remaining today ($missedDosesCount missed)"
            else -> "You have $remainingDosesCount dose${if (remainingDosesCount == 1) "" else "s"} remaining today"
        }
}


data class ScheduledDoseUiModel(
    val id: String,
    val name: String,
    val dosage: String,
    val timeFormatted: String,
    val color: PillColor,
    val status: ComplianceStatus,
    val badgeText: String,
    val scheduledTime: Long
)

data class ComplianceCardUiModel(
    val status: ComplianceStatus,
    val title: String,
    val subtitle: String,
    val badgeText: String
)

sealed interface PatientDashboardAction {
    data class LogDose(val doseId: String, val scheduledTime: Long? = null): PatientDashboardAction
    data object DismissError: PatientDashboardAction
    data class DismissNudge(val nudgeId: String) : PatientDashboardAction
    data object Refresh : PatientDashboardAction
}