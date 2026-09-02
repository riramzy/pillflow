package com.riramzy.pillfllow.ui.state.dashboard

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.PillColor

data class PatientDashboardState(
    val user: UserEntity? = null,
    val scheduledDoses: List<ScheduledDoseUiModel> = emptyList(),
    val pills: List<PillEntity> = emptyList(),
    val hasPrescriptions: Boolean = false,
    val complianceStatus: ComplianceStatus = ComplianceStatus.ON_TIME,
    val complianceTitle: String = "All Set For Today!",
    val complianceSubtitle: String = "All scheduled doses completed",
    val complianceBadgeText: String = "100% On-Time",
    val adherenceScore: Int = 100,
    val dosesTaken: Int = 0,
    val totalDoses: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isNewUserWithoutPrescriptions: Boolean
        get() = !hasPrescriptions && scheduledDoses.isEmpty()
}


data class ScheduledDoseUiModel(
    val id: Long,
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