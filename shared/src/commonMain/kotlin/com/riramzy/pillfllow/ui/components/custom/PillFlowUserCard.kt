package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import com.riramzy.pillfllow.ui.theme.PillFlowTheme

@Composable
fun PillFlowUserCard(
    modifier: Modifier = Modifier,
    userName: String = "Ramzy",
    headerDosesSubtitle: String = "Keep up the good work!"
) {
    Card(
        modifier = modifier
            .wrapContentSize(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = headerDosesSubtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Preview
@Composable
fun PillFlowUserCardPreview() {
    PillFlowTheme {
        PillFlowUserCard()
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowUserCardPreviewDark() {
    PillFlowTheme {
        PillFlowUserCard()
    }
}