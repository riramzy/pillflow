package com.riramzy.pillfllow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowInputField
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillColor
import com.riramzy.pillfllow.ui.components.custom.PillFlowPillShape
import com.riramzy.pillfllow.ui.components.custom.PillFlowSelector
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.PillColor
import com.riramzy.pillfllow.utils.PillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineSheet(
    onMedicationSaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var medicationName by remember { mutableStateOf("") }

    var medicationDosage by remember { mutableStateOf("") }

    val shapesList: List<Pair<String, @Composable () -> Unit>> = listOf(
        Pair(PillShape.CIRCLE.label, { PillFlowPillShape(shape = PillShape.CIRCLE) }),
        Pair(PillShape.OVAL.label, { PillFlowPillShape(shape = PillShape.OVAL) }),
        Pair(PillShape.CAPSULE.label, { PillFlowPillShape(shape = PillShape.CAPSULE) })
    )

    var selectedShape: Pair<String, @Composable () -> Unit>? by remember { mutableStateOf(null) }

    val colorsList: List<Pair<String, @Composable () -> Unit>> = listOf(
        Pair(PillColor.CORAL_RED.label, { PillFlowPillColor(color = PillColor.CORAL_RED) }),
        Pair(PillColor.CITRUS_GOLD.label, { PillFlowPillColor(color = PillColor.CITRUS_GOLD) }),
        Pair(PillColor.SOFT_PURPLE.label, { PillFlowPillColor(color = PillColor.SOFT_PURPLE) }),
        Pair(PillColor.SKY_BLUE.label, { PillFlowPillColor(color = PillColor.SKY_BLUE) }),
        Pair(PillColor.MINT_GREEN.label, { PillFlowPillColor(color = PillColor.MINT_GREEN) })
    )

    var selectedColor: Pair<String, @Composable () -> Unit>? by remember { mutableStateOf(null) }

    val repetitionList: List<Pair<String, @Composable () -> Unit>> = listOf(
        Pair("Once Daily", { Text("1x", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }),
        Pair("Twice Daily", { Text("2x", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }),
        Pair("3 Times Daily", { Text("3x", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) })
    )

    var selectedRepetition: Pair<String, @Composable () -> Unit>? by remember { mutableStateOf(null) }

    var selectedTime: Pair<String, @Composable () -> Unit>? by remember { mutableStateOf(null) }

    val totalDoses = when (selectedRepetition?.first) {
        "Twice Daily" -> 2
        "3 Times Daily" -> 3
        else -> 1
    }

    var showTimeDialog by remember { mutableStateOf(false) }

    var currentDoseIndex by remember { mutableStateOf(0) }

    var accumulatedTimes by remember { mutableStateOf(mutableListOf<String>()) }

    var selectedTimeText by remember { mutableStateOf<String?>(null) }

    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )

    var medicationInstructions by remember { mutableStateOf("") }

    if (showTimeDialog) {
        val isLastDose = currentDoseIndex == totalDoses - 1

        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = {
                Text(
                    text = "Select Time for Dose ${currentDoseIndex + 1} of $totalDoses",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                PillFlowButton(
                    text = if (isLastDose) "Done" else "Next Dose (${currentDoseIndex + 2}/$totalDoses)",
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val isPm = hour >= 12
                        val displayHour = if (hour % 12 == 0) 12 else hour % 12
                        val amPm = if (isPm) "PM" else "AM"
                        val formattedMinute = if (minute < 10) "0$minute" else "$minute"
                        val timeStr = "$displayHour:$formattedMinute $amPm"

                        accumulatedTimes.add(timeStr)

                        if (!isLastDose) {
                            currentDoseIndex++
                        } else {
                            selectedTimeText = accumulatedTimes.joinToString(", ")
                            showTimeDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                PillFlowButton(
                    text = "Cancel",
                    customColor = MaterialTheme.colorScheme.surface,
                    customTextColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { showTimeDialog = false }
                )
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(15.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        Text(
            text = "Add Medication",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PillFlowInputField(
                label = "Name",
                placeholder = "e.g. Aspirin",
                value = medicationName,
                onValueChange = { medicationName = it }
            )

            PillFlowInputField(
                label = "Dosage",
                placeholder = "e.g. 500mg",
                value = medicationDosage,
                onValueChange = { medicationDosage = it }
            )

            PillFlowInputField(
                label = "Instructions",
                placeholder = "e.g. Take with a full glass of water",
                value = medicationInstructions,
                onValueChange = { medicationInstructions = it }
            )

            PillFlowSelector(
                title = "Pill Shape",
                placeholder = "Select Pill Shape",
                items = shapesList,
                selectedItem = selectedShape,
                onItemSelected = { selectedShape = it }
            )

            PillFlowSelector(
                title = "Pill Color",
                placeholder = "Select Pill Color",
                items = colorsList,
                selectedItem = selectedColor,
                onItemSelected = { selectedColor = it }
            )

            PillFlowSelector(
                title = "Frequency",
                placeholder = "Select Frequency",
                items = repetitionList,
                selectedItem = selectedRepetition,
                onItemSelected = {
                    selectedRepetition = it
                    selectedTime = null
                }
            )

            PillFlowSelector(
                title = "Time",
                placeholder = "Select Time",
                items = emptyList(),
                selectedItem = selectedTimeText?.let { Pair(it, { Text("⏰") }) },
                onClick = {
                    accumulatedTimes = mutableListOf()
                    currentDoseIndex = 0
                    showTimeDialog = true
                }
            )
        }

        PillFlowButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Save Prescription",
            onClick = { onMedicationSaved() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddMedicineSheetPreview() {
    PillFlowTheme {
        AddMedicineSheet()
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun AddMedicineSheetPreviewDark() {
    PillFlowTheme {
        AddMedicineSheet()
    }
}