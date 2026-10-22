package com.riramzy.pillfllow.ui.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetDoseHistoryForUserUseCase
import com.riramzy.pillfllow.domain.usecase.medication.SyncAndMaintainScheduleUseCase
import com.riramzy.pillfllow.ui.state.history.HistoryAction
import com.riramzy.pillfllow.ui.state.history.HistoryState
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatMonthYear
import com.riramzy.pillfllow.utils.platform.shiftMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val getDoseHistoryForUserUseCase: GetDoseHistoryForUserUseCase,
    private val syncAndMaintainScheduleUseCase: SyncAndMaintainScheduleUseCase,
    private val sessionManager: SessionManager
): ViewModel() {
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    val selectedPatientId: StateFlow<String?> = sessionManager.selectedPatientId
    private val _selectedMonthMillis = MutableStateFlow(currentTimeMillis())

    init {
        observeUserAndRole()
        observeDoseHistory()
    }

    private fun observeUserAndRole() {
        viewModelScope.launch(Dispatchers.IO) {
            observeCurrentUserUseCase().collectLatest { user ->
                if (user?.userType?.equals("CAREGIVER", ignoreCase = true) == true) {
                    getCaregiverPatientsUseCase(user.id).collectLatest { patientModels ->
                        val validIds = patientModels.map { it.id }

                        if (patientModels.isNotEmpty() && (selectedPatientId.value == null || !validIds.contains(selectedPatientId.value))) {
                            sessionManager.setSelectedPatientId(patientModels.first().id)
                        }

                        _state.update {
                            it.copy(
                                isCaregiver = true,
                                pairedPatients = patientModels,
                                selectedPatientId = selectedPatientId.value ?: "",
                                isLoading = patientModels.isNotEmpty() && it.isLoading
                            )
                        }
                    }
                } else if (user != null) {
                    sessionManager.setSelectedPatientId(user.id)
                    _state.update { it.copy(selectedPatientId = user.id, isCaregiver = false) }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDoseHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                selectedPatientId.filterNotNull(),
                _selectedMonthMillis
            ) { patientId, selectedMonth ->
                patientId to selectedMonth
            }.flatMapLatest { (patientId, selectedMonth) ->
                getDoseHistoryForUserUseCase(patientId).map { historyDoses ->
                    patientId to Triple(historyDoses, selectedMonth, currentTimeMillis())
                }
            }.collectLatest { (patientId, data) ->
                val (historyDoses, selectedMonth, now) = data
                val monthTitle = formatMonthYear(selectedMonth)

                val analytics = DoseComplianceEvaluator.evaluateHistoryAnalytics(
                    historyDoses = historyDoses,
                    targetMonthMillis = selectedMonth,
                    now = now
                )

                _state.update {
                    it.copy(
                        selectedPatientId = patientId,
                        selectedMonthMillis = selectedMonth,
                        monthYearTitle = monthTitle,
                        scorePercentage = analytics.scorePercentage,
                        onTimeCount = analytics.onTimeCount,
                        lateCount = analytics.lateCount,
                        missedCount = analytics.missedCount,
                        skippedCount = analytics.skippedCount,
                        monthlyComplianceDays = analytics.monthlyHeatmap,
                        logRecords = analytics.logRecords,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectPatient(patientId: String) {
        sessionManager.setSelectedPatientId(patientId)
        _state.update { it.copy(selectedPatientId = patientId) }
    }

    private fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isRefreshing = true) }
            try {
                syncAndMaintainScheduleUseCase()
            } finally {
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.SelectPatient -> selectPatient(action.patientId)
            is HistoryAction.PreviousMonth -> _selectedMonthMillis.update { shiftMonth(it, -1) }
            is HistoryAction.NextMonth -> _selectedMonthMillis.update { shiftMonth(it, 1) }
            is HistoryAction.Refresh -> refresh()
        }
    }
}