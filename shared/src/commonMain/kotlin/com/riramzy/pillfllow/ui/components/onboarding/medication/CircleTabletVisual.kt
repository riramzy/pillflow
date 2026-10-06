package com.riramzy.pillfllow.ui.components.onboarding.medication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun CircleTabletVisual(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val radius = size.minDimension / 2f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.6f), color, color.copy(alpha = 0.85f)),
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                radius = radius
            ),
            radius = radius,
            center = center
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = radius * 0.22f,
            center = Offset(center.x - radius * 0.38f, center.y - radius * 0.38f)
        )
    }
}
