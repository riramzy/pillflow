package com.riramzy.pillfllow.ui.viewmodel.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.compliance.DoseComplianceEvaluator
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.domain.usecase.caregiver.GetCaregiverPatientsUseCase
import com.riramzy.pillfllow.domain.usecase.medication.GetDoseHistoryForUserUseCase
import com.riramzy.pillfllow.ui.state.history.HistoryAction
import com.riramzy.pillfllow.ui.state.history.HistoryState
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.platform.formatMonthYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getCaregiverPatientsUseCase: GetCaregiverPatientsUseCase,
    private val getDoseHistoryForUserUseCase: GetDoseHistoryForUserUseCase
): ViewModel() {
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    private val _selectedPatientId = MutableStateFlow<String?>(null)

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

                        if (patientModels.isNotEmpty() && (_selectedPatientId.value == null || !validIds.contains(_selectedPatientId.value))) {
                            _selectedPatientId.value = patientModels.first().id
                        }

                        _state.update {
                            it.copy(
                                isCaregiver = true,
                                pairedPatients = patientModels,
                                selectedPatientId = _selectedPatientId.value ?: ""
                            )
                        }
                    }
                } else if (user != null) {
                    _selectedPatientId.value = user.id
                    _state.update { it.copy(selectedPatientId = user.id, isCaregiver = false) }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDoseHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedPatientId.filterNotNull().flatMapLatest { patientId ->
                getDoseHistoryForUserUseCase(patientId)
            }.collectLatest { historyDoses ->
                val now = currentTimeMillis()
                val currentMonthTitle = formatMonthYear(now)

                val analytics = DoseComplianceEvaluator.evaluateHistoryAnalytics(
                    historyDoses,
                    now
                )

                _state.update {
                    it.copy(
                        monthYearTitle = currentMonthTitle,
                        scorePercentage = analytics.scorePercentage,
                        onTimeCount = analytics.onTimeCount,
                        lateCount = analytics.lateCount,
                        missedCount = analytics.missedCount,
                        monthlyComplianceDays = analytics.monthlyHeatmap,
                        logRecords = analytics.logRecords,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectPatient(patientId: String) {
        _selectedPatientId.value = patientId
        _state.update { it.copy(selectedPatientId = patientId) }
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.SelectPatient -> selectPatient(action.patientId)
        }
    }
}