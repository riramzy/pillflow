package com.riramzy.pillfllow.ui.components.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowInputField
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.platform.Country
import com.riramzy.pillfllow.utils.platform.allCountries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerSheet(
    onDismissRequest: () -> Unit = {},
    onCountrySelected: (Country) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        CountryPickerSheetContent(
            onDismissRequest = onDismissRequest,
            onCountrySelected = onCountrySelected
        )
    }
}

@Composable
fun CountryPickerSheetContent(
    onDismissRequest: () -> Unit = {},
    onCountrySelected: (Country) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank())
            allCountries
        else {
            val q = searchQuery.trim().lowercase()

            allCountries.filter {
                it.name.lowercase().contains(q) ||
                        it.dialCode.contains(q) ||
                        it.code.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(15.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column {
            Text(
                text = "Select Country",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Select your country to continue",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
        }


        PillFlowInputField(
            label = "",
            isWithLabel = false,
            placeholder = "Search by country name or code",
            value = searchQuery,
            onValueChange = { searchQuery = it },
            trailingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(filteredCountries, key = { it.code }) { country ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCountrySelected(country)
                            onDismissRequest()
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = country.flag, fontSize = 24.sp)
                        Text(text = country.name, style = MaterialTheme.typography.bodyLarge)
                    }

                    Text(
                        text = country.dialCode,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CountryPickerSheetPreview() {
    PillFlowTheme {
        Surface {
            CountryPickerSheetContent()
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun CountryPickerSheetPreviewDark() {
    PillFlowTheme {
        Surface {
            CountryPickerSheetContent()
        }
    }
}