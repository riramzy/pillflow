package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme

@Composable
fun PillFlowProgressDots(
    modifier: Modifier = Modifier,
    position: Int = 1
) {
    Card(
        modifier = modifier
            .wrapContentWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier
                .height(45.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (position) {
                1 -> {
                    ProgressDot(size = 25.dp, color = MaterialTheme.colorScheme.primary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                }

                2 -> {
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 25.dp, color = MaterialTheme.colorScheme.primary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                }

                3 -> {
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 25.dp, color = MaterialTheme.colorScheme.primary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                }

                4 -> {
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 15.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ProgressDot(size = 25.dp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ProgressDot(
    size: Dp,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(size)
            .background(color),
    )
}


@Preview(showBackground = true)
@Composable
fun PillFlowProgressDotsPreview() {
    PillFlowTheme {
        PillFlowProgressDots()
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowProgressDotsPreviewDark() {
    PillFlowTheme {
        PillFlowProgressDots()
    }
}