package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PillFlowPullToRefreshBox(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    refreshThreshold: Dp = 90.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { refreshThreshold.toPx() }
    var currentOffset by remember { mutableFloatStateOf(0f) }
    var animationJob by remember { mutableStateOf<Job?>(null) }
    var isHoldingRefresh by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val effectiveRefreshing = isRefreshing || isHoldingRefresh
    val currentEffectiveRefreshing by rememberUpdatedState(effectiveRefreshing)
    val currentOnRefresh by rememberUpdatedState(onRefresh)

    LaunchedEffect(isHoldingRefresh, isRefreshing) {
        if (isRefreshing) {
            isHoldingRefresh = true
        } else if (isHoldingRefresh) {
            delay(900.milliseconds)
            isHoldingRefresh = false
        }
    }

    LaunchedEffect(effectiveRefreshing) {
        animationJob?.cancel()
        val target = if (effectiveRefreshing) thresholdPx else 0f
        animate(
            initialValue = currentOffset,
            targetValue = target,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = if (effectiveRefreshing) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy
            )
        ) { value, _ ->
            currentOffset = value
        }
    }

    val nestedScrollConnection = remember(thresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y < 0f && currentOffset > 0f && !currentEffectiveRefreshing) {
                    animationJob?.cancel()
                    val consumed = available.y.coerceAtLeast(-currentOffset)
                    currentOffset += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0f && !currentEffectiveRefreshing) {
                    animationJob?.cancel()
                    // Progressive rubber-band resistance so it never hits a sudden wall mid-pull
                    val overPull = (currentOffset / thresholdPx).coerceAtLeast(0f)
                    val dynamicResistance = 0.55f / (1f + overPull * 0.45f)
                    currentOffset = (currentOffset + available.y * dynamicResistance)
                        .coerceAtMost(thresholdPx * 3.0f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (currentOffset >= thresholdPx && !currentEffectiveRefreshing) {
                    isHoldingRefresh = true
                    currentOnRefresh()
                } else if (!currentEffectiveRefreshing && currentOffset > 0f) {
                    animationJob?.cancel()
                    animationJob = coroutineScope.launch {
                        animate(
                            initialValue = currentOffset,
                            targetValue = 0f,
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                dampingRatio = Spring.DampingRatioNoBouncy
                            )
                        ) { value, _ ->
                            currentOffset = value
                        }
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier.nestedScroll(nestedScrollConnection),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = currentOffset * 0.35f
                }
        ) {
            content()
        }

        if (currentOffset > 0.5f || effectiveRefreshing) {
            val progress = (currentOffset / thresholdPx).coerceIn(0f, 2.5f)
            val indicatorTopOffset = with(density) { 54.dp.toPx() }

            PillFlowCutePillRefreshIndicator(
                isRefreshing = effectiveRefreshing,
                pullProgress = progress,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = currentOffset - indicatorTopOffset
                        alpha = (currentOffset / (thresholdPx * 0.6f)).coerceIn(0f, 1f)
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