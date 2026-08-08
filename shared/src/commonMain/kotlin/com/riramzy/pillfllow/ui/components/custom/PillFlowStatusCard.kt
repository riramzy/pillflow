package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.IndicatorColor
import com.riramzy.pillfllow.utils.Status

@Composable
fun PillFlowStatusCard(
    modifier: Modifier = Modifier,
    status: Status = Status.DEFAULT,
    customText: String? = null
) {
    var color: Color
    var backgroundColor: Color
    var text: String

    when (status) {
        Status.DEFAULT -> {
            color = MaterialTheme.colorScheme.onPrimary
            backgroundColor = MaterialTheme.colorScheme.primary
            text = "Take"
        }
        Status.ON_TIME -> {
            color = IndicatorColor.GREEN.color
            backgroundColor = IndicatorColor.GREEN_CONTAINER.color
            text = "On Time"
        }
        Status.LATE -> {
            color = IndicatorColor.YELLOW.color
            backgroundColor = IndicatorColor.YELLOW_CONTAINER.color
            text = "Late"
        }
        Status.MISSED -> {
            color = IndicatorColor.RED.color
            backgroundColor = IndicatorColor.RED_CONTAINER.color
            text = "Missed"
        }
    }

    Box(
        modifier = modifier
            .width(130.dp)
            .height(20.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = customText ?: text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 2.dp)
        )

    }
}

@Preview
@Composable
fun PillFlowStatusCardPreview() {
    PillFlowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            PillFlowStatusCard(status = Status.DEFAULT, customText = "Take")
            PillFlowStatusCard(status = Status.ON_TIME, customText = "Taken: 9.02AM")
            PillFlowStatusCard(status = Status.LATE, customText = "Late: 30m ago")
            PillFlowStatusCard(status = Status.MISSED)
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowStatusCardPreviewDark() {
    PillFlowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            PillFlowStatusCard(status = Status.DEFAULT, customText = "Take")
            PillFlowStatusCard(status = Status.ON_TIME, customText = "Taken: 9.02AM")
            PillFlowStatusCard(status = Status.LATE, customText = "Late: 30m ago")
            PillFlowStatusCard(status = Status.MISSED)
        }
    }
}