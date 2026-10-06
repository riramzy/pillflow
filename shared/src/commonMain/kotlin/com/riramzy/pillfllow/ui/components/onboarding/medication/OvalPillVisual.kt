package com.riramzy.pillfllow.ui.components.onboarding.medication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun OvalPillVisual(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val pillWidth = size.width
        val pillHeight = size.height
        val cornerRad = CornerRadius(pillWidth / 2f, pillWidth / 2f)

        drawRoundRect(
            color = color,
            size = size,
            cornerRadius = cornerRad
        )

        val glossWidth = pillWidth * 0.22f
        val glossHeight = pillHeight * 0.60f

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.40f),
                    Color.White.copy(alpha = 0.12f)
                ),
                startY = pillHeight * 0.18f,
                endY = pillHeight * 0.78f
            ),
            topLeft = Offset(pillWidth * 0.18f, pillHeight * 0.18f),
            size = Size(glossWidth, glossHeight),
            cornerRadius = CornerRadius(glossWidth / 2f, glossWidth / 2f)
        )
    }
}
