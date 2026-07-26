package com.riramzy.pillfllow.ui.viewmodel

import androidx.compose.ui.graphics.Color
import com.riramzy.pillfllow.data.local.dao.PillFlowDao
import com.riramzy.pillfllow.data.local.entity.MedicationEntity
import com.riramzy.pillfllow.data.local.entity.PillEntity
import com.riramzy.pillfllow.data.local.entity.ScheduledDoseEntity
import com.riramzy.pillfllow.domain.hardware.PlatformHaptics
import com.riramzy.pillfllow.domain.hardware.PlatformNotifier
import com.riramzy.pillfllow.domain.hardware.PlatformSensor
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.utils.PillFlowIntent
import com.riramzy.pillfllow.utils.PillFlowSideEffect
import com.riramzy.pillfllow.utils.PillShape
import com.riramzy.pillfllow.utils.currentTimeMillis
import com.riramzy.pillfllow.utils.parseColorHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PillFlowPresenter(
    private val dao: PillFlowDao,
    private val sensor: PlatformSensor = PlatformSensor(),
    private val haptics: PlatformHaptics = PlatformHaptics(),
    private val notifier: PlatformNotifier = PlatformNotifier(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(PillFlowState())
    val state: StateFlow<PillFlowState> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<PillFlowSideEffect>()
    val sideEffects: SharedFlow<PillFlowSideEffect> = _sideEffects.asSharedFlow()

    init {
        loadDashboardData()
    }

    fun processIntent(intent: PillFlowIntent) {
        when (intent) {
            is PillFlowIntent.LogDose -> logDose(intent.doseId)
            is PillFlowIntent.SelectRole -> _state.update { it.copy(isCaregiver = intent.isCaregiver) }
            is PillFlowIntent.UpdateTilt -> updateTilt(intent.tiltX, intent.tiltY)
            is PillFlowIntent.AddMedication -> addMedication(intent.medication, intent.scheduledTimesMillis)
            is PillFlowIntent.RefreshDashboard -> loadDashboardData()
        }
    }

    fun startAccelerometer(context: Any? = null) {
        sensor.startListening(context) { tiltX, tiltY ->
            _state.value = _state.value.copy(tiltX = tiltX, tiltY = tiltY)
        }
    }

    fun stopAccelerometer() {
        sensor.stopListening()
    }

    private fun updateTilt(tiltX: Float, tiltY: Float) {
        _state.update { it.copy(tiltX = tiltX, tiltY = tiltY) }
    }

    fun loadDashboardData() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                dao.getPendingDosesWithMedication().collect { pendingDoses ->
                    val pills = pendingDoses.mapIndexed { index, dose ->
                        PillEntity(
                            id = dose.id.toString(),
                            name = dose.name,
                            color = Color(parseColorHex(dose.colorHex)),
                            shape = PillShape.entries[index % PillShape.entries.size],
                            radius = 35f,
                            position = Vector2D(400f + (index * 100), 400f),
                        )
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            sandboxPills = pills,
                            pendingDoses = pendingDoses
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun logDose(doseId: Long) {
        scope.launch {
            val now = currentTimeMillis()
            dao.markScheduledDoseTaken(doseId, now, true)
            haptics.pulseDispensed()
            _sideEffects.emit(PillFlowSideEffect.ShowToast("Dose taken!"))
        }
    }

    fun addMedication(medication: MedicationEntity, scheduledTimesMillis: List<Long>) {
        scope.launch {
            val medicationId = dao.insertMedication(medication)

            val scheduledDoses = scheduledTimesMillis.map { time ->
                ScheduledDoseEntity(
                    medicationId = medicationId,
                    scheduledTime = time,
                    isTaken = false,
                    isSynced = false
                )
            }

            dao.insertScheduledDoses(scheduledDoses)

            scheduledDoses.forEach { dose ->
                notifier.scheduleDoseReminder(
                    doseId = dose.id.toString(),
                    pillName = medication.name,
                    triggerTimeMillis = dose.scheduledTime
                )
            }
        }
    }
}