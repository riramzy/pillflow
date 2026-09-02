package com.riramzy.pillfllow.ui.screens.prescriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riramzy.pillfllow.ui.components.custom.PillFlowBottomNavBar
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowEmptyStateCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowTopAppBar
import com.riramzy.pillfllow.ui.components.dashboard.caregiver.PillFlowPatientCarousel
import com.riramzy.pillfllow.ui.components.prescriptions.PillFlowPrescriptionCard
import com.riramzy.pillfllow.ui.components.prescriptions.PillFlowPrescriptionsSummaryCard
import com.riramzy.pillfllow.ui.components.sheets.PrescriptionSheet
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.state.prescriptions.CaregiverPrescriptionsState
import com.riramzy.pillfllow.ui.state.prescriptions.PrescriptionUiModel
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.ui.viewmodel.prescriptions.CaregiverPrescriptionsViewModel
import com.riramzy.pillfllow.utils.ComplianceStatus
import com.riramzy.pillfllow.utils.PillColor
import com.riramzy.pillfllow.utils.PillShape
import com.riramzy.pillfllow.utils.Screen
import org.koin.compose.viewmodel.koinViewModel
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.add
import pillfllow.shared.generated.resources.avatar1
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.pills
import pillfllow.shared.generated.resources.settings
import pillfllow.shared.generated.resources.user_patient

@Composable
fun CaregiverPrescriptionsScreen(
    caregiverPrescriptionsViewModel: CaregiverPrescriptionsViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by caregiverPrescriptionsViewModel.state.collectAsStateWithLifecycle()

    CaregiverPrescriptionsScreenContent(
        state = state,
        onSelectedPatient = caregiverPrescriptionsViewModel::selectPatient,
        onOpenAddSheet = caregiverPrescriptionsViewModel::openAddSheet,
        onOpenEditSheet = caregiverPrescriptionsViewModel::openEditSheet,
        onCloseAddSheet = caregiverPrescriptionsViewModel::closeAddSheet,
        onSavePrescription = caregiverPrescriptionsViewModel::savePrescription,
        onDeletePrescription = caregiverPrescriptionsViewModel::deletePrescription,
        onNavigateToHome = onNavigateToHome,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverPrescriptionsScreenContent(
    state: CaregiverPrescriptionsState = CaregiverPrescriptionsState(),
    onSelectedPatient: (String) -> Unit = {},
    onOpenAddSheet: () -> Unit = {},
    onOpenEditSheet: (Long) -> Unit = {},
    onCloseAddSheet: () -> Unit = {},
    onSavePrescription: (String, String, String, String, String, String, String, List<Long>) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onDeletePrescription: (Long) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val editingPrescription = state.prescriptions.find { it.id == state.editingMedicationId }

    if (state.isAddSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = onCloseAddSheet,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PrescriptionSheet(
                initialMedication = editingPrescription,
                onMedicationSaved = { name, dosage, instructions, freq, time, color, shape, scheduledTimeMillis ->
                    onSavePrescription(
                        name,
                        dosage,
                        instructions,
                        freq,
                        time,
                        color,
                        shape,
                        scheduledTimeMillis
                    )
                },
                modifier = Modifier.padding(bottom = 25.dp)
            )
        }
    }

    var prescriptionToDelete by remember { mutableStateOf<PrescriptionUiModel?>(null) }

    if (prescriptionToDelete != null) {
        AlertDialog(
            onDismissRequest = { prescriptionToDelete = null },
            title = {
                Text(
                    text = "Delete Prescription",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${prescriptionToDelete?.medicationName}? This will cancel all upcoming reminders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                PillFlowButton(
                    text = "Delete",
                    customColor = MaterialTheme.colorScheme.error,
                    customTextColor = MaterialTheme.colorScheme.onError,
                    onClick = {
                        prescriptionToDelete?.let { onDeletePrescription(it.id) }
                        prescriptionToDelete = null
                    }
                )
            },
            dismissButton = {
                PillFlowButton(
                    text = "Cancel",
                    customColor = MaterialTheme.colorScheme.surface,
                    customTextColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { prescriptionToDelete = null }
                )
            }
        )
    }

    Scaffold(
        topBar = { PillFlowTopAppBar(modifier = Modifier.padding(15.dp)) },
        floatingActionButton = {
            PillFlowBottomNavBar(
                selectedPage = Screen.Prescriptions.route,
                onHomeClick = onNavigateToHome,
                onHistoryClick = onNavigateToHistory,
                onPrescriptionsClick = {},
                onSettingsClick = onNavigateToSettings
            )
                               },
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
                        text = "Prescriptions",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Manage medications, dosages & schedules",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (state.pairedPatients.isEmpty()) {
                item {
                    PillFlowEmptyStateCard(
                        title = "No Paired Patients Yet",
                        description = "Link with a patient using their 6-digit pairing code in Settings to view and manage their prescriptions.",
                        icon = Res.drawable.user_patient,
                        buttonText = "Go to Settings",
                        buttonIcon = Res.drawable.settings,
                        onButtonClick = onNavigateToSettings
                    )
                }
            } else {
                item {
                    PillFlowPatientCarousel(
                        patients = state.pairedPatients,
                        selectedPatientId = state.selectedPatientId
                            ?: state.pairedPatients.first().id,
                        onPatientSelected = onSelectedPatient,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                }

                if (state.isEmpty) {
                    item {
                        PillFlowEmptyStateCard(
                            title = "No Prescriptions Yet",
                            description = "Add your daily medications to track dosage schedules and roll pills in your 3D dish.",
                            icon = Res.drawable.pills,
                            buttonText = "Add First Prescription",
                            buttonIcon = Res.drawable.add,
                            onButtonClick = onOpenAddSheet,
                            modifier = Modifier.padding(horizontal = 15.dp)
                        )
                    }
                } else {
                    item {
                        PillFlowPrescriptionsSummaryCard(
                            activeCount = state.activeCount,
                            nextDoseTime = state.nextDoseTime,
                            nextDoseMedication = state.nextDoseMedication,
                            modifier = Modifier.padding(horizontal = 15.dp)
                        )
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp)
                        ) {
                            Text(
                                text = "Active Prescriptions",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            state.prescriptions.forEach { prescription ->
                                PillFlowPrescriptionCard(
                                    medicationName = prescription.medicationName,
                                    dosage = prescription.dosage,
                                    pillShape = prescription.pillShape,
                                    pillColor = prescription.pillColor,
                                    scheduleText = prescription.scheduleText,
                                    instructionsText = prescription.instructionsText,
                                    nextDoseText = prescription.nextDoseText,
                                    onEditClick = { onOpenEditSheet(prescription.id) },
                                    onDeleteClick = { prescriptionToDelete = prescription }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CaregiverPrescriptionsScreenPreview() {
    PillFlowTheme {
        CaregiverPrescriptionsScreenContent(
            state = CaregiverPrescriptionsState(
                prescriptions = listOf(
                    PrescriptionUiModel(
                        id = 1,
                        medicationName = "Aspirin",
                        dosage = "500mg",
                        pillShape = PillShape.CIRCLE,
                        pillColor = PillColor.CORAL_RED,
                        scheduleText = "Once Daily",
                        instructionsText = "Take with a full glass of water",
                        nextDoseText = "Next Dose (2/3)"
                    ),
                    PrescriptionUiModel(
                        id = 2,
                        medicationName = "Ibuprofen",
                        dosage = "200mg",
                        pillShape = PillShape.OVAL,
                        pillColor = PillColor.CITRUS_GOLD,
                        scheduleText = "Twice Daily",
                        instructionsText = "Take with a full glass of water",
                        nextDoseText = "Next Dose (2/3)"
                    )
                ),
                pairedPatients = listOf(
                    PairedPatientUiModel(
                        id = "1",
                        name = "John Doe",
                        relation = "Father",
                        phoneNumber = "123-456-7890",
                        avatar = Res.drawable.avatar1,
                        status = ComplianceStatus.ON_TIME,
                        lateDosesCount = 0,
                        missedDosesCount = 0,
                        compliancePercentage = 100
                    ),
                    PairedPatientUiModel(
                        id = "2",
                        name = "Jane Smith",
                        relation = "Mother",
                        phoneNumber = "987-654-3210",
                        avatar = Res.drawable.avatar2,
                        status = ComplianceStatus.LATE,
                        lateDosesCount = 2,
                        missedDosesCount = 0,
                        compliancePercentage = 80
                    ),
                    PairedPatientUiModel(
                        id = "3",
                        name = "Bob Johnson",
                        relation = "Brother",
                        phoneNumber = "555-555-5555",
                        avatar = Res.drawable.avatar1,
                        status = ComplianceStatus.MISSED,
                        lateDosesCount = 0,
                        missedDosesCount = 1,
                        compliancePercentage = 90
                    )
                ),
                selectedPatientId = "2",
                activeCount = 2,
                nextDoseTime = "8:00 AM",
                nextDoseMedication = "Aspirin 500mg",
                isLoading = false,
                isAddSheetOpen = false,
                editingMedicationId = null,
                errorMessage = null,
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CaregiverPrescriptionsScreenPreviewDark() {
    PillFlowTheme {
        CaregiverPrescriptionsScreenContent(
            state = CaregiverPrescriptionsState(
                prescriptions = listOf(
                    PrescriptionUiModel(
                        id = 1,
                        medicationName = "Aspirin",
                        dosage = "500mg",
                        pillShape = PillShape.CIRCLE,
                        pillColor = PillColor.CORAL_RED,
                        scheduleText = "Once Daily",
                        instructionsText = "Take with a full glass of water",
                        nextDoseText = "Next Dose (2/3)"
                    ),
                    PrescriptionUiModel(
                        id = 2,
                        medicationName = "Ibuprofen",
                        dosage = "200mg",
                        pillShape = PillShape.OVAL,
                        pillColor = PillColor.CITRUS_GOLD,
                        scheduleText = "Twice Daily",
                        instructionsText = "Take with a full glass of water",
                        nextDoseText = "Next Dose (2/3)"
                    )
                ),
                pairedPatients = listOf(
                    PairedPatientUiModel(
                        id = "1",
                        name = "John Doe",
                        relation = "Father",
                        phoneNumber = "123-456-7890",
                        avatar = Res.drawable.avatar1,
                        status = ComplianceStatus.ON_TIME,
                        lateDosesCount = 0,
                        missedDosesCount = 0,
                        compliancePercentage = 100
                    ),
                    PairedPatientUiModel(
                        id = "2",
                        name = "Jane Smith",
                        relation = "Mother",
                        phoneNumber = "987-654-3210",
                        avatar = Res.drawable.avatar2,
                        status = ComplianceStatus.LATE,
                        lateDosesCount = 2,
                        missedDosesCount = 0,
                        compliancePercentage = 80
                    ),
                    PairedPatientUiModel(
                        id = "3",
                        name = "Bob Johnson",
                        relation = "Brother",
                        phoneNumber = "555-555-5555",
                        avatar = Res.drawable.avatar1,
                        status = ComplianceStatus.MISSED,
                        lateDosesCount = 0,
                        missedDosesCount = 1,
                        compliancePercentage = 90
                    )
                ),
                selectedPatientId = "2",
                activeCount = 2,
                nextDoseTime = "8:00 AM",
                nextDoseMedication = "Aspirin 500mg",
                isLoading = false,
                isAddSheetOpen = false,
                editingMedicationId = null,
                errorMessage = null,
            )
        )
    }
}