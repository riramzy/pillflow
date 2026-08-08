package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.PillShape

@Composable
fun PillFlowPillShape(
    modifier: Modifier = Modifier,
    shape: PillShape = PillShape.CAPSULE
) {
    when (shape) {
        PillShape.CIRCLE -> {
            Box(
                modifier = modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .size(10.dp),
            )
        }
        PillShape.OVAL -> {
            Box(
                modifier = modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .width(10.dp)
                    .height(16.dp),
            )
        }
        PillShape.CAPSULE -> {
            Column {
                Box(
                    modifier = modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(topEnd = 10.dp, topStart = 10.dp)
                        )
                        .width(10.dp)
                        .height(8.dp),
                )
                Box(
                    modifier = modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(bottomEnd = 10.dp, bottomStart = 10.dp)
                        )
                        .width(10.dp)
                        .height(8.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun PillFlowPillShapePreview() {
    PillFlowTheme {
        PillFlowPillShape()
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowPillShapePreviewDark() {
    PillFlowTheme {
        PillFlowPillShape()
    }
}