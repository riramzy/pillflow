package com.riramzy.pillfllow.ui.components.onboarding.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.components.custom.PillFlowActivityCard
import com.riramzy.pillfllow.ui.components.history.PillFlowMonthlyHeatmapCard
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.streak
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PillFlowOnboardingResultHero(
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val isPreview = LocalInspectionMode.current
    val start = if (isPreview) 1f else 0f
    var hasAnimated by remember { mutableStateOf(false) }

    val animHeatmap = remember { Animatable(start) }
    val animScore = remember { Animatable(start) }
    val animStreak = remember { Animatable(start) }

    val animOnTime = remember { Animatable(start) }
    val animLate = remember { Animatable(start) }
    val animMissed = remember { Animatable(start) }

    LaunchedEffect(isActive) {
        if (!isActive || hasAnimated || isPreview) return@LaunchedEffect
        hasAnimated = true

        val dropSpring = spring<Float>(
            dampingRatio = 0.70f,
            stiffness = Spring.StiffnessLow
        )
        val slideSpring = spring<Float>(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        )

        launch { animHeatmap.animateTo(1f, dropSpring) }
        delay(500.milliseconds)

        launch { animScore.animateTo(1f, dropSpring) }
        delay(450.milliseconds)

        launch { animStreak.animateTo(1f, dropSpring) }
        delay(550.milliseconds)

        launch { animOnTime.animateTo(1f, slideSpring) }
        delay(350.milliseconds)

        launch { animLate.animateTo(1f, slideSpring) }
        delay(350.milliseconds)

        launch { animMissed.animateTo(1f, slideSpring) }
    }

    Column(
        modifier = modifier
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(top = 10.dp)

        ) {
            Card(
                modifier = Modifier
                    .width(312.dp)
                    .height(395.dp)
                    .dropShadow(
                        shape = RoundedCornerShape(32.dp),
                        shadow = Shadow(
                            radius = 20.dp,
                            spread = 6.dp,
                            offset = DpOffset(x = 0.dp, y = 8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(0.8f),
                        )
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {}

            PillFlowMonthlyHeatmapCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 0.dp, y = 25.dp)
                    .graphicsLayer {
                        translationY = (1f - animHeatmap.value) * -320.dp.toPx()
                        alpha = animHeatmap.value
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(32.dp)
                    )
            )

            PillFlowAccomplishmentCard(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        rotationZ = -11f
                        translationY = (1f - animScore.value) * -200.dp.toPx()
                        scaleX = animScore.value
                        scaleY = animScore.value
                        alpha = animScore.value
                    }
                    .offset(x = (-12).dp, y = (-25).dp)
            )

            PillFlowAccomplishmentCard(
                title = "21-Day Streak",
                subtitle = "Personal Best!",
                icon = Res.drawable.streak,
                iconColor = IndicatorColor.RED.color,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        rotationZ = 11f
                        translationY = (1f - animStreak.value) * -200.dp.toPx()
                        scaleX = animStreak.value
                        scaleY = animStreak.value
                        alpha = animStreak.value
                    }
                    .offset(x = 12.dp, y = (-25).dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            PillFlowActivityCard(
                patientName = "",
                timestampText = "",
                actionDescription = "29 On-Time",
                status = ComplianceStatus.ON_TIME,
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .offset(x = (-80).dp)
                    .graphicsLayer {
                        translationX = (1f - animOnTime.value) * 160.dp.toPx()
                        alpha = animOnTime.value
                    }
            )

            PillFlowActivityCard(
                patientName = "",
                timestampText = "",
                actionDescription = "1 Late",
                status = ComplianceStatus.LATE,
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .offset(x = 0.dp)
                    .graphicsLayer {
                        translationX = (1f - animLate.value) * 160.dp.toPx()
                        alpha = animLate.value
                    }
            )

            PillFlowActivityCard(
                patientName = "",
                timestampText = "",
                actionDescription = "1 Missed",
                status = ComplianceStatus.MISSED,
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .offset(x = 80.dp)
                    .graphicsLayer {
                        translationX = (1f - animMissed.value) * 160.dp.toPx()
                        alpha = animMissed.value
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowOnboardingResultHeroPreview() {
    PillFlowTheme {
        PillFlowOnboardingResultHero(modifier = Modifier.padding(25.dp))
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowOnboardingResultHeroPreviewDark() {
    PillFlowTheme {
        PillFlowOnboardingResultHero(modifier = Modifier.padding(25.dp))
    }
}