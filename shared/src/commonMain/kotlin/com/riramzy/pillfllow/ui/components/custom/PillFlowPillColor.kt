package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.PillColor

@Composable
fun PillFlowPillColor(
    modifier: Modifier = Modifier,
    color: PillColor = PillColor.CORAL_RED,
    customColor: Color? = null,
    isWithBorder: Boolean = false
) {
    Box(
        modifier = modifier
            .size(12.dp)
            .background(
                color = customColor?: color.color,
                shape = CircleShape
            )
            .border(
                width = if (isWithBorder) 1.dp else 0.dp,
                color = if (isWithBorder) MaterialTheme.colorScheme.primary else color.color,
                shape = CircleShape
            )

    )
}

@Preview
@Composable
fun PillFlowPillColorPreview() {
    PillFlowTheme {
        PillFlowPillColor()
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowPillColorPreviewDark() {
    PillFlowTheme {
        PillFlowPillColor()
    }
}