package com.riramzy.pillfllow.ui.components.onboarding.together

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.riramzy.pillfllow.ui.components.custom.PillFlowHoveringCard
import com.riramzy.pillfllow.ui.components.custom.PillFlowStatusCard
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import com.riramzy.pillfllow.utils.medication.ComplianceStatus
import com.riramzy.pillfllow.utils.medication.IndicatorColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.arrow
import pillfllow.shared.generated.resources.avatar2
import pillfllow.shared.generated.resources.key
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PillFlowOnboardingTogetherHero(
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val start = if (isPreview) 1f else 0f

    val animAlex = remember { Animatable(start) }
    val animArrow = remember { Animatable(start) }
    val animMary = remember { Animatable(start) }
    val animLinked = remember { Animatable(start) }

    val animNudge = remember { Animatable(start) }
    val animReply = remember { Animatable(start) }
    val animSchedule = remember { Animatable(start) }
    val animDelivered = remember { Animatable(start) }

    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect

        val springSpec = spring<Float>(
            dampingRatio = 0.70f,
            stiffness = Spring.StiffnessLow
        )

        launch { animAlex.animateTo(1f, springSpec) }
        delay(400.milliseconds)

        launch { animArrow.animateTo(1f, springSpec) }
        delay(350.milliseconds)

        launch { animMary.animateTo(1f, springSpec) }
        delay(400.milliseconds)

        launch { animLinked.animateTo(1f, springSpec) }
        delay(600.milliseconds)

        launch { animNudge.animateTo(1f, springSpec) }
        delay(750.milliseconds)

        launch { animReply.animateTo(1f, springSpec) }
        delay(600.milliseconds)

        launch { animSchedule.animateTo(1f, springSpec) }
        delay(400.milliseconds)

        launch { animDelivered.animateTo(1f, springSpec) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            PillFlowPatientIcon(
                name = "Mary",
                relationship = "Mom",
                avatar = Res.drawable.avatar2,
                indicatorColor = IndicatorColor.GREEN.color,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 8.dp)
                    .graphicsLayer {
                        rotationZ = -11f
                        scaleX = animMary.value
                        scaleY = animMary.value
                        alpha = animMary.value
                    }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 45.dp)
            ) {
                Image(
                    imageVector = vectorResource(Res.drawable.arrow),
                    contentDescription = "Link Arrow",
                    modifier = Modifier
                        .width(155.dp)
                        .graphicsLayer {
                            scaleX = animArrow.value
                            scaleY = animArrow.value
                            alpha = animArrow.value
                        }
                )

                PillFlowHoveringCard(
                    customTitle = "Linked",
                    customIcon = Res.drawable.key,
                    modifier = Modifier
                        .width(100.dp)
                        .graphicsLayer {
                            scaleX = animLinked.value
                            scaleY = animLinked.value
                            alpha = animLinked.value
                        }
                )
            }


            PillFlowPatientIcon(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 40.dp)
                    .graphicsLayer {
                        rotationZ = 11f
                        scaleX = animAlex.value
                        scaleY = animAlex.value
                        alpha = animAlex.value
                    }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            PillFlowActivityCard(
                patientName = "",
                actionDescription = "Delivered in real-time to dish",
                timestampText = "",
                status = ComplianceStatus.ON_TIME,
                modifier = Modifier
                    .width(200.dp)
                    .offset(x = 40.dp, y = 235.dp)
                    .graphicsLayer {
                        rotationZ = 8f
                        scaleX = animDelivered.value
                        scaleY = animDelivered.value
                        alpha = animDelivered.value
                    }
                    .dropShadow(
                        shape = RoundedCornerShape(25.dp),
                        shadow = Shadow(
                            radius = 10.dp,
                            spread = 4.dp,
                            offset = DpOffset(x = 0.dp, y = 4.dp),
                            color = IndicatorColor.GREEN.color.copy(0.3f),
                        )
                    ),
            )

            PillFlowStatusCard(
                customText = "Metformin 500mg scheduled for 8:00 PM",
                customBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                customTextColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .height(35.dp)
                    .width(225.dp)
                    .offset(x = (-40).dp, y = 200.dp)
                    .graphicsLayer {
                        rotationZ = -5f
                        scaleX = animSchedule.value
                        scaleY = animSchedule.value
                        alpha = animSchedule.value
                    }
                    .dropShadow(
                        shape = RoundedCornerShape(25.dp),
                        shadow = Shadow(
                            radius = 10.dp,
                            spread = 4.dp,
                            offset = DpOffset(x = 0.dp, y = 4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(0.3f),
                        )
                    ),
            )

            PillFlowMessageCard(
                modifier = Modifier
                    .offset(x = 40.dp, y = 150.dp)
                    .graphicsLayer {
                        rotationZ = 9f
                        scaleX = animReply.value
                        scaleY = animReply.value
                        alpha = animReply.value
                    }
            )

            PillFlowNudgeCard(
                modifier = Modifier
                    .offset(x = (-30).dp, y = 60.dp)
                    .graphicsLayer {
                        rotationZ = -6f
                        scaleX = animNudge.value
                        scaleY = animNudge.value
                        alpha = animNudge.value
                    }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowOnboardingTogetherHeroPreview() {
    PillFlowTheme {
        PillFlowOnboardingTogetherHero(modifier = Modifier.padding(20.dp))
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, backgroundColor = 0xFF000000)
@Composable
fun PillFlowOnboardingTogetherHeroPreviewDark() {
    PillFlowTheme {
        PillFlowOnboardingTogetherHero(modifier = Modifier.padding(20.dp))
    }
}