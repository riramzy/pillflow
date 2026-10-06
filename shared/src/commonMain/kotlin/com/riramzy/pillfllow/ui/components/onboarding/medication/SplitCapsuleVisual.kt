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
fun SplitCapsuleVisual(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val capWidth = size.width
        val capHeight = size.height
        val cornerRad = CornerRadius(capWidth / 2f, capWidth / 2f)

        val splitBrush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to primaryColor,
                0.499f to primaryColor,
                0.500f to secondaryColor,
                1.0f to secondaryColor
            ),
            start = Offset(center.x, 0f),
            end = Offset(center.x, capHeight)
        )

        drawRoundRect(
            brush = splitBrush,
            size = size,
            cornerRadius = cornerRad
        )

        val glossWidth = capWidth * 0.22f
        val glossHeight = capHeight * 0.65f

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.15f)
                ),
                startY = capHeight * 0.15f,
                endY = capHeight * 0.80f
            ),
            topLeft = Offset(capWidth * 0.18f, capHeight * 0.16f),
            size = Size(glossWidth, glossHeight),
            cornerRadius = CornerRadius(glossWidth / 2f, glossWidth / 2f)
        )
    }
}