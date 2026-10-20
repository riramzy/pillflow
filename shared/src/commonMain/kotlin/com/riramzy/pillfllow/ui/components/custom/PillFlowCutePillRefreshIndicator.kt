package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import kotlin.math.sin

@Composable
fun PillFlowCutePillRefreshIndicator(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    pullProgress: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PillRefreshTransition")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PillFlip"
    )

    val hopOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PillHop"
    )

    val isAnimating = isRefreshing || pullProgress >= 1f

    Surface(
        modifier = modifier.wrapContentSize(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 22.dp)
                    .graphicsLayer {
                        rotationZ = if (isAnimating) rotation else (sin(pullProgress * 3.14f * 2.5f) * 25f)
                        translationY = if (isAnimating) hopOffset else 0f
                        scaleX = if (isAnimating) 1f else (1f + (pullProgress * 0.08f).coerceAtMost(0.15f))
                        scaleY = if (isAnimating) 1f else (1f - (pullProgress * 0.05f).coerceAtMost(0.12f))
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(11.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(22.dp)
                        .align(Alignment.CenterEnd)
                        .background(
                            color = MaterialTheme.colorScheme.inversePrimary,
                            shape = RoundedCornerShape(topEnd = 11.dp, bottomEnd = 11.dp)
                        )
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(width = 28.dp, height = 3.dp)
                    .graphicsLayer {
                        val shadowScale = if (isAnimating) {
                            (1f - (-hopOffset / 15f)).coerceIn(0.4f, 1.1f)
                        } else {
                            pullProgress.coerceIn(0.3f, 1f)
                        }
                        scaleX = shadowScale
                        scaleY = shadowScale
                    }
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowCutePillRefreshIndicatorPreview() {
    PillFlowTheme {
        PillFlowCutePillRefreshIndicator(
            isRefreshing = true,
            pullProgress = 0.5f,
            modifier = Modifier.padding(15.dp)
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowCutePillRefreshIndicatorPreviewDark() {
    PillFlowTheme {
        PillFlowCutePillRefreshIndicator(
            isRefreshing = true,
            pullProgress = 0.5f,
            modifier = Modifier.padding(15.dp)
        )
    }
}

