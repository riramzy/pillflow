package com.riramzy.pillfllow.utils.platform

import com.riramzy.pillfllow.ui.state.dashboard.ComplianceDayUiModel
import com.riramzy.pillfllow.utils.medication.ComplianceStatus

val defaultSampleDays = listOf(
    ComplianceDayUiModel("Mon", ComplianceStatus.ON_TIME),
    ComplianceDayUiModel("Tue", ComplianceStatus.LATE),
    ComplianceDayUiModel("Wed", ComplianceStatus.MISSED),
    ComplianceDayUiModel("Thu", ComplianceStatus.ON_TIME),
    ComplianceDayUiModel("Fri", ComplianceStatus.LATE),
    ComplianceDayUiModel("Sat", ComplianceStatus.MISSED),
    ComplianceDayUiModel("Sun", ComplianceStatus.ON_TIME)
)