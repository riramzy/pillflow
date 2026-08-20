package com.riramzy.pillfllow.ui.components.dashboard.patient

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos

@Composable
fun PillFlowPillsDish(
    center: Offset,
    radius: Float,
    chuteWidth: Float = 200f,
    handleWidth: Float = 110f,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val shadowColor = if (isDark) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.inversePrimary
    }

    // Calculate seamless connection geometry
    val halfChute = (chuteWidth / 2f).coerceAtMost(radius * 0.95f)
    val angleOffsetRad = asin(halfChute / radius)
    val angleOffsetDeg = angleOffsetRad * (180f / PI.toFloat())
    val contactX = center.x - radius * cos(angleOffsetRad)
    val chuteLeftX = center.x - radius - handleWidth * 0.45f
    val cornerRadius = 36f

    // 1. BUILD UNIFIED CONTINUOUS CONTOUR PATH (Rim + Chute in one seamless piece)
    val unifiedDishPath = remember(center, radius, chuteWidth, handleWidth) {
        Path().apply {
            // Start at top of chute
            moveTo(chuteLeftX + cornerRadius, center.y - halfChute)
            // Straight line to top of circular rim
            lineTo(contactX, center.y - halfChute)
            // Arc clockwise around the entire circle to the bottom of the chute
            arcTo(
                rect = Rect(center = center, radius = radius),
                startAngleDegrees = 180f + angleOffsetDeg,
                sweepAngleDegrees = 360f - (2f * angleOffsetDeg),
                forceMoveTo = false
            )
            // Straight line to bottom of chute
            lineTo(chuteLeftX + cornerRadius, center.y + halfChute)
            // Rounded bottom-left chute corner
            arcTo(
                rect = Rect(
                    left = chuteLeftX,
                    top = center.y + halfChute - 2f * cornerRadius,
                    right = chuteLeftX + 2f * cornerRadius,
                    bottom = center.y + halfChute
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Left vertical edge of chute
            lineTo(chuteLeftX, center.y - halfChute + cornerRadius)
            // Rounded top-left chute corner
            arcTo(
                rect = Rect(
                    left = chuteLeftX,
                    top = center.y - halfChute,
                    right = chuteLeftX + 2f * cornerRadius,
                    bottom = center.y - halfChute + 2f * cornerRadius
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
    }

    // Directional Lighting Vector: Top-Left -> Bottom-Right
    val lightStart = Offset(center.x - radius * 0.9f, center.y - radius * 0.9f)
    val lightEnd = Offset(center.x + radius * 0.9f, center.y + radius * 0.9f)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // A. SUNKEN CONCAVE BOWL (Top-Left inner shadow -> Bright center glow)
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to (if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.90f)),
                        0.45f to (if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.55f)),
                        0.80f to (if (isDark) shadowColor.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.15f)),
                        1.00f to shadowColor.copy(alpha = if (isDark) 0.35f else 0.30f)
                    ),
                    center = Offset(center.x - 10f, center.y - 10f),
                    radius = radius * 0.95f
                ),
                radius = radius * 0.95f,
                center = center
            )

            // B. SEAMLESS CHUTE FROSTED INTERIOR
            drawPath(
                path = unifiedDishPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        (if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFF7E9DC).copy(alpha = 0.55f)),
                        Color.Transparent
                    ),
                    startX = chuteLeftX,
                    endX = contactX
                )
            )

            // C. 3D EMBOSSED GLASS RIM WITH GENTLY BLENDED DIRECTIONAL LIGHTING
            // 1. Ambient Drop Shadow (Soft depth underneath the unified piece)
            drawPath(
                path = unifiedDishPath,
                color = shadowColor.copy(alpha = if (isDark) 0.40f else 0.35f),
                style = Stroke(width = 34f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 2. Directional Beveled Glass Body (Soft warm transition)
            drawPath(
                path = unifiedDishPath,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to (if (isDark) Color.White.copy(alpha = 0.35f) else Color(0xFFFFF9F2).copy(alpha = 0.90f)),
                        0.35f to (if (isDark) Color.White.copy(alpha = 0.18f) else Color(0xFFF7EBDD).copy(alpha = 0.75f)),
                        0.70f to (if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFEED7C4).copy(alpha = 0.40f)),
                        1.00f to shadowColor.copy(alpha = if (isDark) 0.30f else 0.45f)
                    ),
                    start = lightStart,
                    end = lightEnd
                ),
                style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 3. Gently Blended Crest Sheen (Diffused highlight that melts smoothly into the glass body)
            drawPath(
                path = unifiedDishPath,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to (if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.55f)),
                        0.40f to (if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.25f)),
                        0.70f to Color.Transparent,
                        1.00f to Color.Transparent
                    ),
                    start = lightStart,
                    end = lightEnd
                ),
                style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 460)
@Composable
fun PillFlowPillsDishPreview() {
    PillFlowTheme {
        Surface {
            PillFlowPillsDish(
                center = Offset(360f / 2f * 2.75f + 25f, 460f / 2f * 2.75f),
                radius = 330f,
                chuteWidth = 200f,
                handleWidth = 110f
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, widthDp = 360, heightDp = 460)
@Composable
fun PillFlowPillsDishPreviewDark() {
    PillFlowTheme {
        Surface {
            PillFlowPillsDish(
                center = Offset(360f / 2f * 2.75f + 25f, 460f / 2f * 2.75f),
                radius = 330f,
                chuteWidth = 200f,
                handleWidth = 110f
            )
        }
    }
}