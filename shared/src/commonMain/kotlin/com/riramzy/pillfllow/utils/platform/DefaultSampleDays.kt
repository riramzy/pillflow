package com.riramzy.pillfllow.utils.platform

import com.riramzy.pillfllow.ui.state.dashboard.ComplianceDayUiModel
import com.riramzy.pillfllow.utils.medication.ComplianceStatus

val defaultSampleDays = listOf(
    ComplianceDayUiModel("Mon", ComplianceStatus.ON_TIME),
    ComplianceDayUiModel("Tue", ComplianceStatus.ON_TIME),
    ComplianceDayUiModel("Wed", ComplianceStatus.ON_TIME),
    ComplianceDayUiModel("Thu", ComplianceStatus.LATE),
    ComplianceDayUiModel("Fri", ComplianceStatus.ON_TIME),
    ComplianceDayUiModel("Sat", ComplianceStatus.ON_TIME),
    ComplianceDayUiModel("Sun", ComplianceStatus.ON_TIME)
)