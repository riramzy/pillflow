package com.riramzy.pillfllow.ui.state.history

import com.riramzy.pillfllow.ui.components.history.MonthDaysCompliance
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.utils.ComplianceStatus

data class HistoryState(
    val isCaregiver: Boolean = false,
    val pairedPatients: List<PairedPatientUiModel> = emptyList(),
    val selectedPatientId: String = "",
    val monthYearTitle: String = "July 2026",
    val scorePercentage: Int = 0,
    val onTimeCount: Int = 0,
    val lateCount: Int = 0,
    val missedCount: Int = 0,
    val monthlyComplianceDays: List<MonthDaysCompliance> = emptyList(),
    val logRecords: List<HistoryLogRecordUiModel> = emptyList(),
    val isLoading: Boolean = false
) {
    val hasPairedPatients: Boolean get() = pairedPatients.isNotEmpty()
    val hasRecords: Boolean get() = logRecords.isNotEmpty()
    val selectedPatient: PairedPatientUiModel?
        get() = pairedPatients.find { it.id == selectedPatientId }
    val selectedPatientName: String
        get() = selectedPatient?.name ?: ""
}

data class HistoryLogRecordUiModel(
    val id: String,
    val patientName: String,
    val actionTitle: String,
    val actionDescription: String,
    val timestampText: String,
    val status: ComplianceStatus
)