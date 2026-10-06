package com.riramzy.pillfllow.ui.components.onboarding.medication

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.pill.PillShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PillFlowHoveringPill(
    modifier: Modifier = Modifier,
    shape: PillShape,
    baseColor: Color,
    secondaryColor: Color = baseColor,
    size: DpSize,
    baseRotation: Float = 0f,
    cycleDurationMillis: Int = 3600,
    phaseOffsetRad: Float = 0f,
    elevationAmplitude: Dp = 8.dp,
) {
    val transition = rememberInfiniteTransition(label = "PillHover")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SinePhase"
    )

    val currentPhase = phase + phaseOffsetRad
    val offsetY = sin(currentPhase) * elevationAmplitude.value
    val rotationDrift = cos(currentPhase) * 2.5f

    val normalizedH = (offsetY / elevationAmplitude.value).coerceIn(-1f, 1f)
    val shadowScale = 1.0f - (normalizedH * 0.15f)
    val shadowAlpha = 0.28f - (normalizedH * 0.12f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = size.width * 0.9f, height = 16.dp)
                .offset(y = size.height * 0.45f)
                .graphicsLayer {
                    scaleX = shadowScale
                    scaleY = shadowScale
                    alpha = shadowAlpha
                }
                .background(Color(0x33422606), CircleShape)
                .blur(4.dp)
        )

        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationY = offsetY.dp.toPx()
                    rotationZ = baseRotation + rotationDrift
                }
        ) {
            when (shape) {
                PillShape.CIRCLE -> CircleTabletVisual(baseColor)
                PillShape.CAPSULE -> SplitCapsuleVisual(baseColor, secondaryColor)
                PillShape.OVAL -> OvalPillVisual(baseColor)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowHoveringPillPreview() {
    PillFlowTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFFFFF5EB))
                .padding(all = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillFlowHoveringPill(
                    shape = PillShape.CIRCLE,
                    baseColor = Color(0xFF00A86B),
                    size = DpSize(48.dp, 48.dp),
                    baseRotation = 0f,
                    cycleDurationMillis = 4000,
                    phaseOffsetRad = 0f
                )

                PillFlowHoveringPill(
                    shape = PillShape.CAPSULE,
                    baseColor = Color(0xFFECA66B),
                    secondaryColor = Color(0xFFD97706),
                    size = DpSize(38.dp, 78.dp),
                    baseRotation = 42f,
                    cycleDurationMillis = 3400,
                    phaseOffsetRad = 2.09f
                )

                PillFlowHoveringPill(
                    shape = PillShape.OVAL,
                    baseColor = Color(0xFF4F46E5),
                    size = DpSize(36.dp, 74.dp),
                    baseRotation = -35f,
                    cycleDurationMillis = 4600,
                    phaseOffsetRad = 4.19f
                )
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowHoveringPillPreviewDark() {
    PillFlowTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFFFFF5EB))
                .padding(all = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillFlowHoveringPill(
                    shape = PillShape.CIRCLE,
                    baseColor = Color(0xFF00A86B),
                    size = DpSize(48.dp, 48.dp),
                    baseRotation = 0f,
                    cycleDurationMillis = 4000,
                    phaseOffsetRad = 0f
                )

                PillFlowHoveringPill(
                    shape = PillShape.CAPSULE,
                    baseColor = Color(0xFFECA66B),
                    secondaryColor = Color(0xFFD97706),
                    size = DpSize(38.dp, 78.dp),
                    baseRotation = 42f,
                    cycleDurationMillis = 3400,
                    phaseOffsetRad = 2.09f
                )

                PillFlowHoveringPill(
                    shape = PillShape.OVAL,
                    baseColor = Color(0xFF4F46E5),
                    size = DpSize(36.dp, 74.dp),
                    baseRotation = -35f,
                    cycleDurationMillis = 4600,
                    phaseOffsetRad = 4.19f
                )
            }
        }
    }
}
