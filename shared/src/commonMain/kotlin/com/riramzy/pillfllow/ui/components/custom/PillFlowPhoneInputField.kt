package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.sheets.CountryPickerSheet
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.platform.Country

@Composable
fun PillFlowPhoneInputField(
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    label: String = "Phone Number",
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        CountryPickerSheet(
            onDismissRequest = { showSheet = false },
            onCountrySelected = {
                onCountrySelected(it)
                showSheet = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                bottom = 10.dp,
                start = 15.dp,
            )
        )

        Card(
            modifier = Modifier
                .wrapContentSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(0.2f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { showSheet = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedCountry.flag,
                        fontSize = 16.sp
                    )

                    Text(
                        text = selectedCountry.dialCode,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Change country",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                BasicTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(selectedCountry.maxLength)
                        onPhoneNumberChange(digits)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.primary),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.Unspecified,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            if (phoneNumber.isEmpty()) {
                                Text(
                                    text = "Enter your phone number",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary.copy(0.3f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPhoneInputFieldPreview() {
    PillFlowTheme {
        PillFlowPhoneInputField(
            selectedCountry = Country(
                code = "US",
                name = "United States",
                dialCode = "+1",
                flag = "🇺🇸"
            ),
            onCountrySelected = {},
            phoneNumber = "",
            onPhoneNumberChange = {},
            modifier = Modifier.padding(15.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPhoneInputFieldPreviewDark() {
    PillFlowTheme {
        PillFlowPhoneInputField(
            selectedCountry = Country(
                code = "US",
                name = "United States",
                dialCode = "+1",
                flag = "🇺🇸"
            ),
            onCountrySelected = {},
            phoneNumber = "",
            onPhoneNumberChange = {},
            modifier = Modifier.padding(15.dp)
        )
    }
}