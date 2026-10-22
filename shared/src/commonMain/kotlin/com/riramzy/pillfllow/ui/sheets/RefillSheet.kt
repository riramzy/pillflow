package com.riramzy.pillfllow.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.components.custom.PillFlowInputField
import com.riramzy.pillfllow.ui.components.custom.PillFlowSelectionChip
import com.riramzy.pillfllow.ui.components.prescriptions.PillFlowLivePillStatus
import com.riramzy.pillfllow.ui.theme.PillFlowTheme

@Composable
fun RefillSheet(
    modifier: Modifier = Modifier,
    medicationName: String,
    dosage: String,
    currentPills: Int?,
    dosesPerDay: Int = 1,
    onDismiss: () -> Unit,
    onConfirmRefill: (addedPills: Int) -> Unit
) {
    var selectedPreset by remember { mutableStateOf<Int?>(30) }
    var customText by remember { mutableStateOf("30") }
    val addedPills = customText.toIntOrNull() ?: 0
    val current = currentPills ?: 0
    val newTotal = (current + addedPills).coerceAtLeast(0)
    val daysOfSupply = if (dosesPerDay > 0) newTotal / dosesPerDay else null
    val presets = listOf(10, 30, 60, 90)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(15.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        Column {
            Text(
                text = "Refill $medicationName ($dosage)",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Refill prescription so it doesn't run out!",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PillFlowLivePillStatus(
                    title = "Current Pills",
                    count = current
                )

                PillFlowLivePillStatus(
                    title = "New Total",
                    count = newTotal
                )

                PillFlowLivePillStatus(
                    title = "Days of Supply",
                    count = daysOfSupply ?: 0,
                    isAround = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                presets.forEach { preset ->
                    PillFlowSelectionChip(
                        selectee = "+$preset",
                        isSelected = preset == selectedPreset,
                        modifier = Modifier.clickable {
                            selectedPreset = preset
                            customText = preset.toString()
                        }
                    )
                }
            }

            PillFlowInputField(
                label = "Pills to Add",
                placeholder = "Enter amount",
                value = customText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    customText = filtered
                    selectedPreset = filtered.toIntOrNull()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            PillFlowButton(
                modifier = Modifier.weight(1f),
                text = "Cancel",
                customColor = MaterialTheme.colorScheme.surface,
                customTextColor = MaterialTheme.colorScheme.onSurface,
                onClick = onDismiss
            )
            PillFlowButton(
                modifier = Modifier.weight(1f),
                text = "Confirm",
                isEnabled = addedPills > 0,
                onClick = { onConfirmRefill(addedPills) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RefillSheetPreview() {
    PillFlowTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            RefillSheet(
                medicationName = "Aspirin",
                dosage = "500mg",
                currentPills = 10,
                onDismiss = {},
                onConfirmRefill = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun RefillSheetPreviewDark() {
    PillFlowTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            RefillSheet(
                medicationName = "Aspirin",
                dosage = "500mg",
                currentPills = 10,
                onDismiss = {},
                onConfirmRefill = {}
            )
        }
    }
}