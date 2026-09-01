package com.riramzy.pillfllow.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.components.custom.PillFlowButton
import com.riramzy.pillfllow.ui.theme.PillFlowTheme

@Composable
fun PillFlowPairingCard(
    pairingCode: String = "819263",
    onCopyClick: () -> Unit = {},
    onRegenerateClick: () -> Unit = {},
    isRegenerating: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Caregiver Pairing Code",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Share this short-lived code with your caregiver to link accounts",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (i in 0 until 6) {
                    val digitChar = pairingCode.getOrNull(i)?.toString() ?: "-"
                    PairingCodeCard(code = digitChar)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillFlowButton(
                    modifier = Modifier.weight(1f),
                    text = "Copy Code",
                    customColor = MaterialTheme.colorScheme.primaryContainer,
                    customTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    isEnabled = !isRegenerating && pairingCode.isNotBlank(),
                    onClick = { onCopyClick() }
                )

                PillFlowButton(
                    modifier = Modifier.weight(1f),
                    text = "Regenerate",
                    isEnabled = !isRegenerating,
                    onClick = { onRegenerateClick() }
                )
            }
        }
    }
}

@Composable
fun PairingCodeCard(
    modifier: Modifier = Modifier,
    code: String = "8"
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .width(45.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPairingCardPreview() {
    PillFlowTheme {
        PillFlowPairingCard(modifier = Modifier.padding(15.dp))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPairingCardPreviewDark() {
    PillFlowTheme {
        PillFlowPairingCard(modifier = Modifier.padding(15.dp))
    }
}