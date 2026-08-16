package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.PillShape
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.arrow_down

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillFlowSelector(
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String,
    items: List<Pair<String, @Composable (() -> Unit)>>,
    selectedItem: Pair<String, @Composable (() -> Unit)>? = null,
    onItemSelected: (Pair<String, @Composable (() -> Unit)>) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                bottom = 10.dp,
                start = 10.dp,
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    if (onClick != null) {
                        onClick()
                    } else {
                        expanded = !expanded
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .menuAnchor(
                            enabled = true,
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =  MaterialTheme.colorScheme.primary.copy(0.2f)
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            selectedItem?.second()

                            Row {
                                Text(
                                    text = selectedItem?.first ?: placeholder,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )

                                if (selectedItem != null) {
                                    Text(
                                        text = " - Selected",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = vectorResource(Res.drawable.arrow_down),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    },
                    modifier = Modifier
                        .exposedDropdownSize()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer
                        ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(item.first, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            },
                            leadingIcon = { item.second() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowSelectorPreview() {
    PillFlowTheme {
        PillFlowSelector(
            title = "Pill Shape",
            placeholder = "Select Pill Shape",
            items = listOf(
                Pair(PillShape.CIRCLE.label, { PillFlowPillShape(shape = PillShape.CIRCLE) }),
                Pair(PillShape.OVAL.label, { PillFlowPillShape(shape = PillShape.OVAL) }),
                Pair(PillShape.CAPSULE.label, { PillFlowPillShape(shape = PillShape.CAPSULE) })
            ),
            modifier = Modifier.padding(15.dp),
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowSelectorPreviewDark() {
    PillFlowTheme {
        PillFlowSelector(
            title = "Pill Shape",
            placeholder = "Select Pill Shape",
            items = listOf(
                Pair(PillShape.CIRCLE.label, { PillFlowPillShape(shape = PillShape.CIRCLE) }),
                Pair(PillShape.OVAL.label, { PillFlowPillShape(shape = PillShape.OVAL) }),
                Pair(PillShape.CAPSULE.label, { PillFlowPillShape(shape = PillShape.CAPSULE) })
            ),
            modifier = Modifier.padding(15.dp),
        )
    }
}