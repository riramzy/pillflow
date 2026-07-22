package com.riramzy.pillfllow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.data.local.entity.PillEntity
import com.riramzy.pillfllow.domain.physics.PhysicsEngine
import com.riramzy.pillfllow.domain.physics.Vector2D
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.PillShape
import kotlinx.coroutines.isActive

@Composable
fun PillJarSandbox(
    pillsState: List<PillEntity>,
    tiltX: Float,
    tiltY: Float,
    onLogMedication: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    //val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    //val background = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    //val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val isDark = isSystemInDarkTheme()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(surface)
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val center = remember(width, height) { Vector2D(width / 2f + 40f, height / 2.5f) }
        val radius = remember(width) { width * 0.38f }
        val chuteWidth = 160f
        val activePills = remember { mutableStateListOf<PillEntity>().apply { addAll(pillsState) } }

        var triggerRedraw by remember { mutableStateOf(0) }

        val currentTick = triggerRedraw
        val jarCenterOffset = Offset(center.x, center.y)
        val handleWidth = 140f
        val handleTopLeft = Offset(center.x - radius - handleWidth + 20f, center.y - chuteWidth / 2f)

        LaunchedEffect(pillsState) {
            activePills.clear()
            activePills.addAll(pillsState)
        }

        val engine = remember(center, radius) {
            PhysicsEngine(
                jarRadius = radius,
                jarCenter = center,
                chuteWidth = chuteWidth,
                onPillLogged = { pillId ->
                    activePills.removeAll { it.id == pillId }
                    onLogMedication(pillId)
                }
            )
        }

        LaunchedEffect(Unit) {
            var lastNanos = withFrameNanos { it }
            while (isActive) {
                withFrameNanos { currentNanos ->
                    val elapsedSeconds = ((currentNanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.033f)
                    lastNanos = currentNanos
                    engine.update(activePills, tiltX, tiltY, elapsedSeconds)
                    triggerRedraw++
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "TILT TO GUIDE PILLS → DISH OPENING",
                color = onSurface.copy(alpha = 0.4f),
                letterSpacing = 2.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. GLASS BASE
            // Dish circle backing
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color.White.copy(alpha = 0.11f) else Color.White.copy(alpha = 0.4f),
                        primary.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = jarCenterOffset,
                    radius = radius
                ),
                radius = radius,
                center = jarCenterOffset
            )

            // Glass opening/chute backing
            drawRoundRect(
                color = primary.copy(alpha = 0.04f),
                topLeft = handleTopLeft,
                size = Size(handleWidth, chuteWidth),
                cornerRadius = CornerRadius(30f, 30f)
            )

            // 2. GLASS RIM
            // Glass opening rim
            drawRoundRect(
                color = primary.copy(alpha = 0.2f),
                topLeft = handleTopLeft,
                size = Size(handleWidth, chuteWidth),
                cornerRadius = CornerRadius(30f, 30f),
                style = Stroke(width = 6f)
            )

            // Main glass dish rim
            drawArc(
                color = primary.copy(alpha = 0.15f),
                startAngle = 195f,
                sweepAngle = 330f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 38f, cap = StrokeCap.Round)
            )

            // 3. PILLS RENDERING
            activePills.forEach { pill ->
                val baseColor = pill.color
                val secondaryColor = pill.color.copy(0.7f)

                when (pill.shape) {
                    PillShape.CIRCLE -> {
                        // 1. CIRCLE (3D Sphere)
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
                            center = Offset(pill.position.x, pill.position.y),

                        )
                        // Micro specular highlight dot
                        drawCircle(
                            color = Color.White.copy(alpha = 0.4f),
                            radius = pill.radius * 0.2f,
                            center = Offset(pill.position.x - pill.radius * 0.4f, pill.position.y - pill.radius * 0.4f)
                        )
                    }

                    PillShape.OVAL -> {
                        // 2. OVAL (Smooth caplet)
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

                        // Specular highlight line along top edge
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(pill.position.x - ovalWidth * 0.25f, pill.position.y - ovalHeight * 0.2f),
                            end = Offset(pill.position.x + ovalWidth * 0.25f, pill.position.y - ovalHeight * 0.2f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    PillShape.CAPSULE -> {
                        // 3. TWO-COLORED CAPSULE (Exact 50/50 split)
                        val capWidth = pill.radius * 1.3f * 1.2f
                        val capHeight = pill.radius * 2.4f * 1.2f
                        val topLeft = Offset(pill.position.x - capWidth / 2f, pill.position.y - capHeight / 2f)
                        // Sharp 50/50 color stop split
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
                        // Specular highlight line along left side of capsule
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

        // BLURRED HIGHLIGHT LAYER
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 4.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            // Inner bright highlight edge
            drawArc(
                color = if (isDark) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.4f),
                startAngle = 195f,
                sweepAngle = 330f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )

            // Specular light reflection on top-right
            val curvedHighlightBrush = Brush.sweepGradient(
                0.70f to Color.Transparent,
                0.75f to (if (isDark) Color.White.copy(alpha = 0.35f) else Color.White),
                0.88f to (if (isDark) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.6f)),
                1.00f to Color.Transparent,
                center = jarCenterOffset
            )

            drawArc(
                brush = curvedHighlightBrush,
                startAngle = 250f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )
        }
    }
}

@Preview
@Composable
fun PillJarSandboxPreview() {
    val dummyPills = listOf(
        PillEntity(id = "1", name = "Aspirin", color = Color.Red, shape = PillShape.CIRCLE, radius = 35f, position = Vector2D(400f, 600f)),
        PillEntity(id = "2", name = "Vitamin C", color = Color.Magenta, shape = PillShape.OVAL, radius = 35f, position = Vector2D(500f, 650f)),
        PillEntity(id = "3", name = "Antibiotic", color = Color.Blue, shape = PillShape.CAPSULE, radius = 30f, position = Vector2D(450f, 750f))
    )
    PillFlowTheme {
        PillJarSandbox(
            pillsState = dummyPills,
            tiltX = 0f,
            tiltY = 0.5f,
            onLogMedication = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillJarSandboxDarkPreview() {
    val dummyPills = listOf(
        PillEntity(id = "1", name = "Aspirin", color = Color.Red, shape = PillShape.CIRCLE, radius = 35f, position = Vector2D(400f, 600f)),
        PillEntity(id = "2", name = "Vitamin C", color = Color.Magenta, shape = PillShape.OVAL, radius = 35f, position = Vector2D(500f, 650f)),
        PillEntity(id = "3", name = "Antibiotic", color = Color.Blue, shape = PillShape.CAPSULE, radius = 30f, position = Vector2D(450f, 750f))
    )
    PillFlowTheme {
        PillJarSandbox(
            pillsState = dummyPills,
            tiltX = 0f,
            tiltY = 0.5f,
            onLogMedication = {}
        )
    }
}