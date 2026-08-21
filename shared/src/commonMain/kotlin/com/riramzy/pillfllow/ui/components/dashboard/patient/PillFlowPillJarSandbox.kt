package com.riramzy.pillfllow.ui.components.dashboard.patient

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.domain.hardware.PlatformHaptics
import com.riramzy.pillfllow.domain.physics.PhysicsEngine
import com.riramzy.pillfllow.domain.physics.PillEntity
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.ui.components.custom.PillFlowStatusCard
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.PillShape
import kotlinx.coroutines.isActive

@Composable
fun PillFlowPillJarSandbox(
    pillsState: List<PillEntity>,
    tiltX: Float,
    tiltY: Float,
    onLogMedication: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f)
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(470.dp)
        ) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val center = remember(width, height) { Vector2D(width / 2f + 16f, height / 2f + 14f) }
            val radius = remember(width) { width * 0.36f }
            val chuteWidth = 200f
            val handleWidth = 110f

            val activePills = remember { mutableStateListOf<PillEntity>().apply { addAll(pillsState) } }
            var triggerRedraw by remember { mutableStateOf(0) }
            val jarCenterOffset = Offset(center.x, center.y)

            LaunchedEffect(pillsState) {
                activePills.clear()
                activePills.addAll(pillsState)
            }

            val haptics = remember { PlatformHaptics() }

            val engine = remember(center, radius) {
                PhysicsEngine(
                    jarRadius = radius,
                    jarCenter = center,
                    chuteWidth = chuteWidth,
                    onPillLogged = { pillId ->
                        haptics.pulseDispensed()
                        activePills.removeAll { it.id == pillId }
                        onLogMedication(pillId)
                    },
                    onCollision = {
                        haptics.tickCollision()
                    }
                )
            }

            val currentTiltX by rememberUpdatedState(tiltX)
            val currentTiltY by rememberUpdatedState(tiltY)

            LaunchedEffect(Unit) {
                var lastNanos = withFrameNanos { it }
                while (isActive) {
                    withFrameNanos { currentNanos ->
                        val elapsedSeconds = ((currentNanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.033f)
                        lastNanos = currentNanos
                        engine.update(activePills, currentTiltX, currentTiltY, elapsedSeconds)
                        triggerRedraw++
                    }
                }
            }

            // 1. HEADER
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Interactive Jar",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Tilt to guide pills to the left handle!",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // 2. THE STANDALONE 3D GLASS DISH
            PillFlowPillsDish(
                center = jarCenterOffset,
                radius = radius,
                chuteWidth = chuteWidth,
                handleWidth = handleWidth
            )

            // 3. PILLS RENDERING
            Canvas(modifier = Modifier.fillMaxSize()) {
                triggerRedraw

                activePills.forEach { pill ->
                    val baseColor = pill.color
                    val secondaryColor = pill.color.copy(0.7f)

                    when (pill.shape) {
                        PillShape.CIRCLE -> {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.6f),
                                        baseColor,
                                        baseColor.copy(alpha = 0.85f)
                                    ),
                                    center = Offset(pill.position.x - pill.radius * 0.3f, pill.position.y - pill.radius * 0.3f),
                                    radius = pill.radius
                                ),
                                radius = pill.radius,
                                center = Offset(pill.position.x, pill.position.y)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.4f),
                                radius = pill.radius * 0.2f,
                                center = Offset(pill.position.x - pill.radius * 0.4f, pill.position.y - pill.radius * 0.4f)
                            )
                        }

                        PillShape.OVAL -> {
                            val ovalWidth = pill.radius * 2.2f * 1.2f
                            val ovalHeight = pill.radius * 1.3f * 1.2f
                            val topLeft = Offset(pill.position.x - ovalWidth / 2f, pill.position.y - ovalHeight / 2f)

                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        baseColor,
                                        baseColor.copy(alpha = 0.85f)
                                    ),
                                    center = Offset(pill.position.x - ovalWidth * 0.2f, pill.position.y - ovalHeight * 0.2f),
                                    radius = ovalWidth / 1.5f
                                ),
                                topLeft = topLeft,
                                size = Size(ovalWidth, ovalHeight)
                            )

                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = Offset(pill.position.x - ovalWidth * 0.25f, pill.position.y - ovalHeight * 0.2f),
                                end = Offset(pill.position.x + ovalWidth * 0.25f, pill.position.y - ovalHeight * 0.2f),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }

                        PillShape.CAPSULE -> {
                            val capWidth = pill.radius * 1.3f * 1.2f
                            val capHeight = pill.radius * 2.4f * 1.2f
                            val topLeft = Offset(pill.position.x - capWidth / 2f, pill.position.y - capHeight / 2f)

                            val splitBrush = Brush.linearGradient(
                                colorStops = arrayOf(
                                    0.0f to baseColor,
                                    0.49f to baseColor,
                                    0.50f to secondaryColor,
                                    1.0f to secondaryColor
                                ),
                                start = Offset(pill.position.x, topLeft.y),
                                end = Offset(pill.position.x, topLeft.y + capHeight)
                            )
                            drawRoundRect(
                                brush = splitBrush,
                                topLeft = topLeft,
                                size = Size(capWidth, capHeight),
                                cornerRadius = CornerRadius(capWidth / 2f, capWidth / 2f)
                            )

                            drawLine(
                                color = Color.White.copy(alpha = 0.45f),
                                start = Offset(pill.position.x - capWidth * 0.25f, topLeft.y + capHeight * 0.15f),
                                end = Offset(pill.position.x - capWidth * 0.25f, topLeft.y + capHeight * 0.85f),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // 4. BOTTOM BADGE
            PillFlowStatusCard(
                customText = "${activePills.size} pills remaining",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPillJarSandboxPreview() {
    val dummyPills = listOf(
        PillEntity(id = "1", name = "Aspirin", color = Color.Red, shape = PillShape.CIRCLE, radius = 35f, position = Vector2D(400f, 600f)),
        PillEntity(id = "2", name = "Vitamin C", color = Color.Magenta, shape = PillShape.OVAL, radius = 35f, position = Vector2D(500f, 650f)),
        PillEntity(id = "3", name = "Antibiotic", color = Color.Blue, shape = PillShape.CAPSULE, radius = 30f, position = Vector2D(450f, 750f))
    )
    PillFlowTheme {
        PillFlowPillJarSandbox(
            pillsState = dummyPills,
            tiltX = 0f,
            tiltY = 0.5f,
            onLogMedication = {},
            modifier = Modifier.padding(15.dp)
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPillJarSandboxDarkPreview() {
    val dummyPills = listOf(
        PillEntity(id = "1", name = "Aspirin", color = Color.Red, shape = PillShape.CIRCLE, radius = 35f, position = Vector2D(400f, 600f)),
        PillEntity(id = "2", name = "Vitamin C", color = Color.Magenta, shape = PillShape.OVAL, radius = 35f, position = Vector2D(500f, 650f)),
        PillEntity(id = "3", name = "Antibiotic", color = Color.Blue, shape = PillShape.CAPSULE, radius = 30f, position = Vector2D(450f, 750f))
    )
    PillFlowTheme {
        PillFlowPillJarSandbox(
            pillsState = dummyPills,
            tiltX = 0f,
            tiltY = 0.5f,
            onLogMedication = {},
            modifier = Modifier.padding(15.dp)
        )
    }
}