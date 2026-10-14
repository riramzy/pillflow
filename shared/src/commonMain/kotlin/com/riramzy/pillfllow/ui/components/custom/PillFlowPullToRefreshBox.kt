package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import kotlinx.coroutines.launch

@Composable
fun PillFlowPullToRefreshBox(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    refreshThreshold: Dp = 80.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { refreshThreshold.toPx() }
    val pullOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullOffset.animateTo(
                targetValue = thresholdPx,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        } else {
            pullOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        }
    }

    val nestedScrollConnection = remember(thresholdPx, isRefreshing) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y < 0 && pullOffset.value > 0) {
                    val consumed = available.y.coerceAtLeast(-pullOffset.value)

                    coroutineScope.launch {
                        pullOffset.snapTo(pullOffset.value + consumed)
                    }

                    return Offset(0f, consumed)
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0 && !isRefreshing) {
                    val dragResistance = 0.5f
                    val newOffset = (pullOffset.value + available.y * dragResistance)
                        .coerceAtMost(thresholdPx * 1.6f)

                    coroutineScope.launch {
                        pullOffset.snapTo(newOffset)
                    }

                    return Offset(0f, available.y)
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pullOffset.value >= thresholdPx && !isRefreshing) {
                    onRefresh()
                } else if (!isRefreshing) {
                    pullOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        )
                    )
                }

                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier.nestedScroll(nestedScrollConnection),
        contentAlignment = Alignment.TopCenter
    ) {
        content()

        if (pullOffset.value > 0f || isRefreshing) {
            val progress = (pullOffset.value / thresholdPx).coerceIn(0f, 1.5f)
            val indicatorTopOffset = with(density) { 54.dp.toPx() }

            PillFlowCutePillRefreshIndicator(
                isRefreshing = isRefreshing,
                pullProgress = progress,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = pullOffset.value - indicatorTopOffset
                        alpha = progress.coerceIn(0f, 1f)
                    }
                    .padding(top = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPullToRefreshBoxPreview() {
    PillFlowTheme {
        PillFlowPullToRefreshBox(
            isRefreshing = true,
            onRefresh = {},
            content = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPullToRefreshBoxPreviewDark() {
    PillFlowTheme {
        PillFlowPullToRefreshBox(
            isRefreshing = true,
            onRefresh = {},
            content = {}
        )
    }
}